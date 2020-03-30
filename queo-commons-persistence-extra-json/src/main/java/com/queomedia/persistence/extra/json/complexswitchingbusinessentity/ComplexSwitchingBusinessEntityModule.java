package com.queomedia.persistence.extra.json.complexswitchingbusinessentity;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.queomedia.commons.checks.Check;
import com.queomedia.persistence.GeneralLoaderDao;
import com.queomedia.persistence.extra.json.BusinessEntityModule;
import com.queomedia.persistence.extra.json.BusinessEntitySerializationMode;
import com.queomedia.persistence.extra.json.SwitchingAnnotationScanner;

/**
 * Jackson Module that activate the Switching Business Enitity Functionality (see
 * {@link com.queomedia.persistence.extra.json.complexswitchingbusinessentity packagedoc}).
 * <p>
 * This module register 4 serializer/deserializer.
 * ...
 * </p>
 *
 * @see com.queomedia.persistence.extra.json.complexswitchingbusinessentity
 */
public class ComplexSwitchingBusinessEntityModule extends Module {

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
    public ComplexSwitchingBusinessEntityModule(final GeneralLoaderDao generalLoaderDao,
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
    public ComplexSwitchingBusinessEntityModule(final GeneralLoaderDao generalLoaderDao,
            final BusinessEntitySerializationMode defaultMode) {
        this(generalLoaderDao, new SwitchingAnnotationScanner(defaultMode));
    }

    /**
     * Instantiates a new switching business entity module with default mode {@link BusinessEntitySerializationMode#ENTITY}.
     *
     * @param generalLoaderDao the general loader dao
     */
    public ComplexSwitchingBusinessEntityModule(final GeneralLoaderDao generalLoaderDao) {
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

        context.addBeanSerializerModifier(new ComplexSwitchingBusinessEntitySerializerModifier(this.switchingAnnotationScanner,
                new BusinessEntityModule.BusinessEntityJsonSerializer()));
        context.addBeanDeserializerModifier(
                new ComplexSwitchingBusinessEntityDeserializerModfier(this.switchingAnnotationScanner, this.generalLoaderDao));

        context.addKeySerializers(new BusinessEntityModule.BusinessEntityKeySerializers());
        context.addKeyDeserializers(new BusinessEntityModule.BusinessEntityKeyDeserializers(this.generalLoaderDao));

    }

//        /*
//         * https://www.baeldung.com/jackson-call-default-serializer-from-custom-
//         * serializer
//         */
//        static class SwitchingBusinessEntityJsonSerializer extends StdSerializer<BusinessEntity> {
//            private final SwitchingAnnotationScanner switchingAnnotationScanner;
//            private final BusinessEntityModule.BusinessEntityJsonSerializer businessEntityJsonSerializer;
//    
//            private final JsonSerializer<Object> defaultSerializer;
//    
//            public SwitchingBusinessEntityJsonSerializer(final SwitchingAnnotationScanner switchingAnnotationScanner,
//                    final BusinessEntityJsonSerializer businessEntityJsonSerializer,
//                    final JsonSerializer<Object> defaultSerializer) {
//                super(BusinessEntity.class);
//    
//                Check.notNullArgument(switchingAnnotationScanner, "switchingAnnotationScanner");
//                Check.notNullArgument(businessEntityJsonSerializer, "businessEntityJsonSerializer");
//                Check.notNullArgument(defaultSerializer, "defaultSerializer");
//    
//                this.switchingAnnotationScanner = switchingAnnotationScanner;
//                this.businessEntityJsonSerializer = businessEntityJsonSerializer;
//    
//                this.defaultSerializer = defaultSerializer;
//            }
//    
//            @Override
//            public JsonSerializer<BusinessEntity> unwrappingSerializer(NameTransformer transformer) {
//                
//                JsonSerializer unwrappingSerializer = defaultSerializer.unwrappingSerializer(transformer);
//                return (JsonSerializer<BusinessEntity>) unwrappingSerializer;
//            }
//    
//            @Override
//            public void serialize(final BusinessEntity businessEntity, final JsonGenerator jgen,
//                    final SerializerProvider provider) throws IOException {
//    
//                BusinessEntitySerialization mode = switchingAnnotationScanner
//                        .detectSwitchDefinition(jgen.getOutputContext()).orElse(BusinessEntitySerialization.ENTITY);
//                switch (mode) {
//                case BUSINESS_ID:
//                    businessEntityJsonSerializer.serialize(businessEntity, jgen, provider);
//                    return;
//                case ENTITY:
//                    defaultSerializer.serialize(businessEntity, jgen, provider);
//                    return;
//                default:
//                    throw new NotImplementedCaseException(mode);
//                }
//            }
//        }

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
