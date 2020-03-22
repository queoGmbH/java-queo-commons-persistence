package com.queomedia.persistence.extra.json;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.web.config.HateoasAwareSpringDataWebConfiguration;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.Module.SetupContext;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.ser.impl.BeanAsArraySerializer;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.impl.UnwrappingBeanSerializer;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.queomedia.commons.checks.Check;
import com.queomedia.commons.exceptions.NotImplementedCaseException;
import com.queomedia.persistence.BusinessEntity;
import com.queomedia.persistence.BusinessId;
import com.queomedia.persistence.GeneralLoaderDao;
import com.queomedia.persistence.extra.json.BusinessEntityModule.BusinessEntityDeserializers;
import com.queomedia.persistence.extra.json.BusinessEntityModule.BusinessEntityJsonSerializer;
import com.queomedia.persistence.extra.json.BusinessEntityModule.BusinessEntitySerializers;
import com.queomedia.persistence.extra.json.BusinessEntityModule.TypedBusinessEntityJsonDeserializer;
import com.queomedia.persistence.extra.json.SwitchingBusinessEntityAnnotation.BusinessEntitySerialization;
import com.queomedia.persistence.extra.json.util.Computable;
import com.queomedia.persistence.extra.json.util.Memorizer;

public class SwitchingBusinessEntityModule extends Module {

    /** The general loader dao. */
    private final GeneralLoaderDao generalLoaderDao;

    /** The version. */
    private final Version version = new Version(1, 0, 0, null, "com.queomedia", null);

    /**
     * Instantiates a new business id module and register the mapping.
     *
     * @param generalLoaderDao the general loader dao
     */
    public SwitchingBusinessEntityModule(final GeneralLoaderDao generalLoaderDao) {
        Check.notNullArgument(generalLoaderDao, "generalLoaderDao");

        this.generalLoaderDao = generalLoaderDao;
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

        SwitchingAnnotationScanner switchingAnnotationScanner = new SwitchingAnnotationScanner();

        context.addBeanSerializerModifier(new SwitchingBusinessEntitSerializerModifier(switchingAnnotationScanner,
                new BusinessEntityJsonSerializer()));
        context.addBeanDeserializerModifier(new SwitchingBusinessEntityDeserializerModfier(this.generalLoaderDao));
    }

    public class SwitchingBusinessEntitSerializerModifier extends BeanSerializerModifier {

        private final SwitchingAnnotationScanner switchingAnnotationScanner;
        private final BusinessEntityModule.BusinessEntityJsonSerializer businessEntityJsonSerializer;

        public SwitchingBusinessEntitSerializerModifier(SwitchingAnnotationScanner switchingAnnotationScanner,
                BusinessEntityJsonSerializer businessEntityJsonSerializer) {
            this.switchingAnnotationScanner = switchingAnnotationScanner;
            this.businessEntityJsonSerializer = businessEntityJsonSerializer;
        }

        @Override
        public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc,
                JsonSerializer<?> serializer) {

            if (BusinessEntity.class.isAssignableFrom(beanDesc.getBeanClass())) {
//                return new SwitchingBusinessEntityJsonSerializer(switchingAnnotationScanner,
//                        businessEntityJsonSerializer, (JsonSerializer<Object>) serializer);
                return new SwitchingBusinessEntityJsonSerializer2(switchingAnnotationScanner,
                        businessEntityJsonSerializer, (BeanSerializerBase) serializer);
            } else {
                return serializer;
            }
        }
    }

    static class SwitchingBusinessEntityJsonSerializer2 extends BeanSerializerBase {
        private final SwitchingAnnotationScanner switchingAnnotationScanner;
        private final BusinessEntityModule.BusinessEntityJsonSerializer businessEntityJsonSerializer;

        private final BeanSerializerBase defaultSerializer;

        public SwitchingBusinessEntityJsonSerializer2(final SwitchingAnnotationScanner switchingAnnotationScanner,
                final BusinessEntityJsonSerializer businessEntityJsonSerializer,
                final BeanSerializerBase defaultSerializer) {
            super(defaultSerializer);

            Check.notNullArgument(switchingAnnotationScanner, "switchingAnnotationScanner");
            Check.notNullArgument(businessEntityJsonSerializer, "businessEntityJsonSerializer");
            Check.notNullArgument(defaultSerializer, "defaultSerializer");

            this.switchingAnnotationScanner = switchingAnnotationScanner;
            this.businessEntityJsonSerializer = businessEntityJsonSerializer;

            this.defaultSerializer = defaultSerializer;
        }

        SwitchingBusinessEntityJsonSerializer2(final SwitchingAnnotationScanner switchingAnnotationScanner,
                final BusinessEntityJsonSerializer businessEntityJsonSerializer,
                SwitchingBusinessEntityJsonSerializer2 defaultSerializer, ObjectIdWriter objectIdWriter) {
            super(defaultSerializer, objectIdWriter);

            Check.notNullArgument(switchingAnnotationScanner, "switchingAnnotationScanner");
            Check.notNullArgument(businessEntityJsonSerializer, "businessEntityJsonSerializer");
            Check.notNullArgument(defaultSerializer, "defaultSerializer");

            this.switchingAnnotationScanner = switchingAnnotationScanner;
            this.businessEntityJsonSerializer = businessEntityJsonSerializer;

            this.defaultSerializer = defaultSerializer;
        }

        @Deprecated
        SwitchingBusinessEntityJsonSerializer2(final SwitchingAnnotationScanner switchingAnnotationScanner,
                final BusinessEntityJsonSerializer businessEntityJsonSerializer,
                SwitchingBusinessEntityJsonSerializer2 defaultSerializer, String[] toIgnore) {
            super(defaultSerializer, toIgnore);

            Check.notNullArgument(switchingAnnotationScanner, "switchingAnnotationScanner");
            Check.notNullArgument(businessEntityJsonSerializer, "businessEntityJsonSerializer");
            Check.notNullArgument(defaultSerializer, "defaultSerializer");

            this.switchingAnnotationScanner = switchingAnnotationScanner;
            this.businessEntityJsonSerializer = businessEntityJsonSerializer;

            this.defaultSerializer = defaultSerializer;
        }

        SwitchingBusinessEntityJsonSerializer2(final SwitchingAnnotationScanner switchingAnnotationScanner,
                final BusinessEntityJsonSerializer businessEntityJsonSerializer,
                SwitchingBusinessEntityJsonSerializer2 defaultSerializer, Set<String> toIgnore) {
            super(defaultSerializer, toIgnore);

            Check.notNullArgument(switchingAnnotationScanner, "switchingAnnotationScanner");
            Check.notNullArgument(businessEntityJsonSerializer, "businessEntityJsonSerializer");
            Check.notNullArgument(defaultSerializer, "defaultSerializer");

            this.switchingAnnotationScanner = switchingAnnotationScanner;
            this.businessEntityJsonSerializer = businessEntityJsonSerializer;

            this.defaultSerializer = defaultSerializer;
        }

        SwitchingBusinessEntityJsonSerializer2(final SwitchingAnnotationScanner switchingAnnotationScanner,
                final BusinessEntityJsonSerializer businessEntityJsonSerializer,
                SwitchingBusinessEntityJsonSerializer2 defaultSerializer, ObjectIdWriter objectIdWriter,
                Object filterId) {
            super(defaultSerializer, objectIdWriter, filterId);

            Check.notNullArgument(switchingAnnotationScanner, "switchingAnnotationScanner");
            Check.notNullArgument(businessEntityJsonSerializer, "businessEntityJsonSerializer");
            Check.notNullArgument(defaultSerializer, "defaultSerializer");

            this.switchingAnnotationScanner = switchingAnnotationScanner;
            this.businessEntityJsonSerializer = businessEntityJsonSerializer;

            this.defaultSerializer = defaultSerializer;
        }

        @Override
        public BeanSerializerBase withObjectIdWriter(ObjectIdWriter objectIdWriter) {
            return new SwitchingBusinessEntityJsonSerializer2(this.switchingAnnotationScanner,
                    this.businessEntityJsonSerializer, this, objectIdWriter);
        }

        @Deprecated
        @Override
        public BeanSerializerBase withIgnorals(String[] toIgnore) {
            return new SwitchingBusinessEntityJsonSerializer2(this.switchingAnnotationScanner,
                    this.businessEntityJsonSerializer, this, toIgnore);
        }

        @Override
        protected BeanSerializerBase withIgnorals(Set<String> toIgnore) {
            return new SwitchingBusinessEntityJsonSerializer2(this.switchingAnnotationScanner,
                    this.businessEntityJsonSerializer, this, toIgnore);
        }

        @Override
        protected BeanSerializerBase asArraySerializer() {
            /*
             * Cannot:
             * 
             * - have Object Id (may be allowed in future) - have "any getter" - have
             * per-property filters
             */
            if ((_objectIdWriter == null) && (_anyGetterWriter == null) && (_propertyFilterId == null)) {
                return new BeanAsArraySerializer(this);
            }
            // already is one, so:
            return this;
        }

        @Override
        public BeanSerializerBase withFilterId(Object filterId) {
            return new SwitchingBusinessEntityJsonSerializer2(this.switchingAnnotationScanner,
                    this.businessEntityJsonSerializer, this, _objectIdWriter, filterId);
        }

        public class SwitchingUnwrappingBeanSerializer extends UnwrappingBeanSerializer {

            public SwitchingUnwrappingBeanSerializer(BeanSerializerBase src, NameTransformer transformer) {
                super(src, transformer);
            }

            protected void serializeFields(Object bean, JsonGenerator jgen, SerializerProvider provider)
                    throws IOException {
                BusinessEntitySerialization mode = switchingAnnotationScanner
                        .detectSwitchDefinition(jgen.getOutputContext()).orElse(BusinessEntitySerialization.ENTITY);
                switch (mode) {
                case BUSINESS_ID:
                    
//                    businessEntityJsonSerializer.serialize((BusinessEntity) bean, jgen, provider);
                    if (bean == null) {
                        jgen.writeNull();
                    } else {
                        jgen.writeFieldName("businessId");
                        jgen.writeString(((BusinessEntity) bean).getBusinessId().getAsString());
                    }
                    return;
                case ENTITY:
                    super.serializeFields(bean, jgen, provider);
                    return;
                default:
                    throw new NotImplementedCaseException(mode);
                }
            }

        }

        @Override
        public JsonSerializer<Object> unwrappingSerializer(NameTransformer transformer) {
            return new SwitchingUnwrappingBeanSerializer(this, transformer);
//            JsonSerializer unwrappingSerializer = defaultSerializer.unwrappingSerializer(transformer);
//            return (JsonSerializer<BusinessEntity>) unwrappingSerializer;
        }

        @Override
        public void serialize(final Object businessEntity, final JsonGenerator jgen, final SerializerProvider provider)
                throws IOException {

            BusinessEntitySerialization mode = switchingAnnotationScanner
                    .detectSwitchDefinition(jgen.getOutputContext()).orElse(BusinessEntitySerialization.ENTITY);
            switch (mode) {
            case BUSINESS_ID:
                businessEntityJsonSerializer.serialize((BusinessEntity) businessEntity, jgen, provider);
                return;
            case ENTITY:
                defaultSerializer.serialize(businessEntity, jgen, provider);
                return;
            default:
                throw new NotImplementedCaseException(mode);
            }
        }

    }

//    /*
//     * https://www.baeldung.com/jackson-call-default-serializer-from-custom-
//     * serializer
//     */
//    static class SwitchingBusinessEntityJsonSerializer extends StdSerializer<BusinessEntity> {
//        private final SwitchingAnnotationScanner switchingAnnotationScanner;
//        private final BusinessEntityModule.BusinessEntityJsonSerializer businessEntityJsonSerializer;
//
//        private final JsonSerializer<Object> defaultSerializer;
//
//        public SwitchingBusinessEntityJsonSerializer(final SwitchingAnnotationScanner switchingAnnotationScanner,
//                final BusinessEntityJsonSerializer businessEntityJsonSerializer,
//                final JsonSerializer<Object> defaultSerializer) {
//            super(BusinessEntity.class);
//
//            Check.notNullArgument(switchingAnnotationScanner, "switchingAnnotationScanner");
//            Check.notNullArgument(businessEntityJsonSerializer, "businessEntityJsonSerializer");
//            Check.notNullArgument(defaultSerializer, "defaultSerializer");
//
//            this.switchingAnnotationScanner = switchingAnnotationScanner;
//            this.businessEntityJsonSerializer = businessEntityJsonSerializer;
//
//            this.defaultSerializer = defaultSerializer;
//        }
//
//        @Override
//        public JsonSerializer<BusinessEntity> unwrappingSerializer(NameTransformer transformer) {
//            JsonSerializer unwrappingSerializer = defaultSerializer.unwrappingSerializer(transformer);
//            return (JsonSerializer<BusinessEntity>) unwrappingSerializer;
//        }
//
//        @Override
//        public void serialize(final BusinessEntity businessEntity, final JsonGenerator jgen,
//                final SerializerProvider provider) throws IOException {
//
//            BusinessEntitySerialization mode = switchingAnnotationScanner
//                    .detectSwitchDefinition(jgen.getOutputContext()).orElse(BusinessEntitySerialization.ENTITY);
//            switch (mode) {
//            case BUSINESS_ID:
//                businessEntityJsonSerializer.serialize(businessEntity, jgen, provider);
//                return;
//            case ENTITY:
//                defaultSerializer.serialize(businessEntity, jgen, provider);
//                return;
//            default:
//                throw new NotImplementedCaseException(mode);
//            }
//        }
//    }

    static class SwitchingBusinessEntityDeserializerModfier extends BeanDeserializerModifier {

        private final GeneralLoaderDao generalLoaderDao;

        public SwitchingBusinessEntityDeserializerModfier(com.queomedia.persistence.GeneralLoaderDao generalLoaderDao) {
            Check.notNullArgument(generalLoaderDao, "generalLoaderDao");
            this.generalLoaderDao = generalLoaderDao;
        }

        @Override
        public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc,
                JsonDeserializer<?> deserializer) {
            if (BusinessEntity.class.isAssignableFrom(beanDesc.getBeanClass())) {
                return new SwitchingBusinessEntityDeserializer(
                        new BusinessEntityModule.TypedBusinessEntityJsonDeserializer(beanDesc.getBeanClass(),
                                generalLoaderDao),
                        beanDesc.getBeanClass(), deserializer, generalLoaderDao);
            } else {
                return deserializer;
            }
        }
    }

    // https://stackoverflow.com/questions/18313323/how-do-i-call-the-default-deserializer-from-a-custom-deserializer-in-jackson
    public static class SwitchingBusinessEntityDeserializer extends StdDeserializer<BusinessEntity>
            implements ResolvableDeserializer {

        private final JsonDeserializer<?> defaultDeserializer;

        private BusinessEntityModule.TypedBusinessEntityJsonDeserializer typedBusinessEntityJsonDeserializer;
        private Class beanClazz;

        private GeneralLoaderDao generalLoaderDao;

        public SwitchingBusinessEntityDeserializer(
                BusinessEntityModule.TypedBusinessEntityJsonDeserializer typedBusinessEntityJsonDeserializer,
                final Class beanClazz, JsonDeserializer<?> defaultDeserializer, GeneralLoaderDao generalLoaderDao) {
            super(BusinessEntity.class);

            this.typedBusinessEntityJsonDeserializer = typedBusinessEntityJsonDeserializer;
            this.beanClazz = beanClazz;
            this.defaultDeserializer = defaultDeserializer;
            this.generalLoaderDao = generalLoaderDao;
        }

        @Override
        public BusinessEntity deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {

            JsonToken currentToken = jp.currentToken();
            if (currentToken == JsonToken.VALUE_STRING) {
                String bidString = jp.readValueAs(String.class);
                System.out.println("bidString: " + bidString);
                System.out.println("beanClazz: " + beanClazz);
                return this.generalLoaderDao.getByBusinessId(BusinessId.parse(bidString), beanClazz);
            } else {
                return (BusinessEntity) defaultDeserializer.deserialize(jp, ctxt);
            }
        }

        @Override
        public JsonDeserializer<BusinessEntity> unwrappingDeserializer(NameTransformer unwrapper) {
            JsonDeserializer<?> unwrappingDeserializer = defaultDeserializer.unwrappingDeserializer(unwrapper);
            return (JsonDeserializer<BusinessEntity>) unwrappingDeserializer;
        }

        public void resolve(DeserializationContext ctxt) throws JsonMappingException {
            ((ResolvableDeserializer) defaultDeserializer).resolve(ctxt);
        }

    }

    static class SwitchingAnnotationScanner {

        Optional<BusinessEntitySerialization> detectSwitchDefinition(final JsonStreamContext context) {
            return findSwitchingBusinessEntityAnnotation(context).map(SwitchingBusinessEntityAnnotation::value);
        }

        Optional<SwitchingBusinessEntityAnnotation> findSwitchingBusinessEntityAnnotation(
                final JsonStreamContext context) {
            Object currentValue = context.getCurrentValue();

            /*
             * check getter and fields for annotation before class, because they have higher
             * priority.
             */
            if (currentValue != null) {
                Class<? extends Object> currentClass = currentValue.getClass();

                if (context.getCurrentName() != null) {
                    // TODO find field instead of use exception
                    try {

                        Field field = currentClass.getDeclaredField(context.getCurrentName());
                        Optional<SwitchingBusinessEntityAnnotation> fieldAnnotation = Optional
                                .ofNullable(field.getAnnotation(SwitchingBusinessEntityAnnotation.class));
                        if (fieldAnnotation.isPresent()) {
                            return fieldAnnotation;
                        }
                    } catch (NoSuchFieldException e) {
                        // TODO scan super classes
                    }

                    // TODO find methos instead of use exception
                    try {
                        /*
                         * Getter are always public, so we can use getMethoth instead of
                         * getDeclaredMethods and not not need a manual scan in super classes.
                         */
                        Method method = currentClass.getMethod(getterName(context.getCurrentName()));
                        Optional<SwitchingBusinessEntityAnnotation> methodAnnotation = Optional
                                .ofNullable(method.getAnnotation(SwitchingBusinessEntityAnnotation.class));
                        if (methodAnnotation.isPresent()) {
                            return methodAnnotation;
                        }
                    } catch (NoSuchMethodException e) {
                    }
                }
                Optional<SwitchingBusinessEntityAnnotation> classAnnotation = Optional
                        .ofNullable(currentClass.getAnnotation(SwitchingBusinessEntityAnnotation.class));
                if (classAnnotation.isPresent()) {
                    return classAnnotation;
                }
            }

            /* recursion to json parent */
            if (context.getParent() != null) {
                return findSwitchingBusinessEntityAnnotation(context.getParent());
            }

            return Optional.empty();
        }

        /**
         * Returns a String which capitalizes the first letter of the string.
         */
        public static String getterName(final String fieldName) {
            Check.notNullArgument(fieldName, "fieldName");

            return "get" + fieldName.substring(0, 1).toUpperCase(Locale.ENGLISH) + fieldName.substring(1);
        }
    }

}
