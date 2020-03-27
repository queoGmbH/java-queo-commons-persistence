package com.queomedia.persistence.extra.json.switchingbusinessentity;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.queomedia.commons.checks.Check;
import com.queomedia.commons.exceptions.NotImplementedCaseException;
import com.queomedia.persistence.BusinessEntity;
import com.queomedia.persistence.GeneralLoaderDao;
import com.queomedia.persistence.extra.json.BusinessEntityModule;
import com.queomedia.persistence.extra.json.BusinessEntityModule.BusinessEntityJsonSerializer;

/**
 * Jackson Module that activate the Switching Business Enitity Functionality (see
 * {@link com.queomedia.persistence.extra.json.switchingbusinessentity packagedoc}).
 * <p>
 * This module register 4 serializer/deserializer.
 * ...
 * </p>
 *
 * @see com.queomedia.persistence.extra.json.switchingbusinessentity
 */
public class SimpleSwitchingBusinessEntityModule extends Module {

    /** The general loader dao. */
    private final GeneralLoaderDao generalLoaderDao;

    /** The switching annotation scanner. */
    private final SwitchingAnnotationScanner switchingAnnotationScanner;

    /** The version. */
    private final Version version = new Version(1,
            0,
            0,
            null,
            "com.queomedia.persistence.extra.json.SwitchingBusinessEntityModule",
            null);

    /**
     * Instantiates a new business id module and register the mapping.
     *
     * @param generalLoaderDao the general loader dao
     * @param switchingAnnotationScanner the switching annotation scanner
     */
    public SimpleSwitchingBusinessEntityModule(final GeneralLoaderDao generalLoaderDao,
            final SwitchingAnnotationScanner switchingAnnotationScanner) {
        Check.notNullArgument(generalLoaderDao, "generalLoaderDao");
        Check.notNullArgument(switchingAnnotationScanner, "switchingAnnotationScanner");

        this.generalLoaderDao = generalLoaderDao;
        this.switchingAnnotationScanner = switchingAnnotationScanner;
    }

    /**
     * Instantiates a new switching business entity module.
     *
     * @param generalLoaderDao the general loader dao
     * @param defaultMode the default serialization mode used if no explicit mode is defined.
     */
    public SimpleSwitchingBusinessEntityModule(final GeneralLoaderDao generalLoaderDao,
            final BusinessEntitySerializationMode defaultMode) {
        this(generalLoaderDao, new SwitchingAnnotationScanner(defaultMode));
    }

    /**
     * Instantiates a new switching business entity module with default mode {@link BusinessEntitySerializationMode#ENTITY}.
     *
     * @param generalLoaderDao the general loader dao
     */
    public SimpleSwitchingBusinessEntityModule(final GeneralLoaderDao generalLoaderDao) {
        this(generalLoaderDao, BusinessEntitySerializationMode.ENTITY);
    }

    @Override
    public String getModuleName() {
        return "SwitchingBusinessEntityModule";
    }

    @Override
    public Version version() {
        return this.version;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.fasterxml.jackson.databind.Module#setupModule(com.fasterxml.jackson.
     * databind.Module.SetupContext)
     */
    @Override
    public void setupModule(final SetupContext context) {
        Check.notNullArgument(context, "context");

        context.addBeanSerializerModifier(new SimpleSwitchingBusinessEntitySerializerModifier(this.switchingAnnotationScanner,
                new BusinessEntityModule.BusinessEntityJsonSerializer()));
        context.addBeanDeserializerModifier(
                new SwitchingBusinessEntityDeserializerModfier(this.switchingAnnotationScanner, this.generalLoaderDao));

        context.addKeySerializers(new BusinessEntityModule.BusinessEntityKeySerializers());
        context.addKeyDeserializers(new BusinessEntityModule.BusinessEntityKeyDeserializers(this.generalLoaderDao));

    }
    
    public class SimpleSwitchingBusinessEntitySerializerModifier extends BeanSerializerModifier {

        private final SwitchingAnnotationScanner switchingAnnotationScanner;

        private final BusinessEntityModule.BusinessEntityJsonSerializer businessEntityJsonSerializer;

        public SimpleSwitchingBusinessEntitySerializerModifier(final SwitchingAnnotationScanner switchingAnnotationScanner,
                final BusinessEntityJsonSerializer businessEntityJsonSerializer) {
            this.switchingAnnotationScanner = switchingAnnotationScanner;
            this.businessEntityJsonSerializer = businessEntityJsonSerializer;
        }

        @Override
        public JsonSerializer<?> modifySerializer(final SerializationConfig config, final BeanDescription beanDesc,
                final JsonSerializer<?> serializer) {

            if (BusinessEntity.class.isAssignableFrom(beanDesc.getBeanClass())) {
                return new SwitchingBusinessEntityJsonSerializer(this.switchingAnnotationScanner,
                        this.businessEntityJsonSerializer,
                        (BeanSerializerBase) serializer);
            } else {
                return serializer;
            }
        }
    }

    /*
     * https://www.baeldung.com/jackson-call-default-serializer-from-custom-
     * serializer
     */
    static class SwitchingBusinessEntityJsonSerializer extends StdSerializer<BusinessEntity> {
        private final SwitchingAnnotationScanner switchingAnnotationScanner;

        private final BusinessEntityModule.BusinessEntityJsonSerializer businessEntityJsonSerializer;

        private final JsonSerializer<Object> defaultSerializer;

        public SwitchingBusinessEntityJsonSerializer(final SwitchingAnnotationScanner switchingAnnotationScanner,
                final BusinessEntityJsonSerializer businessEntityJsonSerializer,
                final JsonSerializer<Object> defaultSerializer) {
            super(BusinessEntity.class);
            Check.notNullArgument(switchingAnnotationScanner, "switchingAnnotationScanner");
            Check.notNullArgument(businessEntityJsonSerializer, "businessEntityJsonSerializer");
            Check.notNullArgument(defaultSerializer, "defaultSerializer");

            this.switchingAnnotationScanner = switchingAnnotationScanner;
            this.businessEntityJsonSerializer = businessEntityJsonSerializer;
            this.defaultSerializer = defaultSerializer;
        }

        @Override
        public JsonSerializer<BusinessEntity> unwrappingSerializer(NameTransformer transformer) {
            return new SimpleSerializationUnwrapper(switchingAnnotationScanner,
                    businessEntityJsonSerializer,
                    defaultSerializer,
                    transformer);
        }

        @Override
        public void serialize(final BusinessEntity businessEntity, final JsonGenerator jgen,
                final SerializerProvider provider)
                throws IOException {

            BusinessEntitySerializationMode mode = switchingAnnotationScanner
                    .getSwitchDefinition(jgen.getOutputContext());
            switch (mode) {
            case BUSINESS_ID:
                businessEntityJsonSerializer.serialize(businessEntity, jgen, provider);
                return;
            case ENTITY:
                defaultSerializer.serialize(businessEntity, jgen, provider);
                return;
            default:
                throw new NotImplementedCaseException(mode);
            }
        }
    }

    //        // https://stackoverflow.com/questions/18313323/how-do-i-call-the-default-deserializer-from-a-custom-deserializer-in-jackson
    //        public static class SwitchingBusinessEntityDeserializer extends StdDeserializer<BusinessEntity>
    //                implements ResolvableDeserializer {
    //    
    //            private final JsonDeserializer<?> defaultDeserializer;
    //    
    //            private BusinessEntityModule.TypedBusinessEntityJsonDeserializer typedBusinessEntityJsonDeserializer;
    //            private Class beanClazz;
    //    
    //            private GeneralLoaderDao generalLoaderDao;
    //    
    //            public SwitchingBusinessEntityDeserializer(
    //                    BusinessEntityModule.TypedBusinessEntityJsonDeserializer typedBusinessEntityJsonDeserializer,
    //                    final Class beanClazz, JsonDeserializer<?> defaultDeserializer, GeneralLoaderDao generalLoaderDao) {
    //                super(BusinessEntity.class);
    //    
    //                this.typedBusinessEntityJsonDeserializer = typedBusinessEntityJsonDeserializer;
    //                this.beanClazz = beanClazz;
    //                this.defaultDeserializer = defaultDeserializer;
    //                this.generalLoaderDao = generalLoaderDao;
    //            }
    //    
    //            @Override
    //            public BusinessEntity deserialize(JsonParser jp, DeserializationContext ctxt)
    //                    throws IOException, JsonProcessingException {
    //    
    //                JsonToken currentToken = jp.currentToken();
    //                if (currentToken == JsonToken.VALUE_STRING) {
    //                    String bidString = jp.readValueAs(String.class);
    //                    System.out.println("bidString: " + bidString);
    //                    System.out.println("beanClazz: " + beanClazz);
    //                    return this.generalLoaderDao.getByBusinessId(BusinessId.parse(bidString), beanClazz);
    //                } else {
    //                    return (BusinessEntity) defaultDeserializer.deserialize(jp, ctxt);
    //                }
    //            }
    //    
    //            @Override
    //            public JsonDeserializer<BusinessEntity> unwrappingDeserializer(NameTransformer unwrapper) {
    //                JsonDeserializer<?> unwrappingDeserializer = defaultDeserializer.unwrappingDeserializer(unwrapper);
    //                return (JsonDeserializer<BusinessEntity>) unwrappingDeserializer;
    //            }
    //    
    //            public void resolve(DeserializationContext ctxt) throws JsonMappingException {
    //                ((ResolvableDeserializer) defaultDeserializer).resolve(ctxt);
    //            }
    //    
    //        }

}
