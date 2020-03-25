package com.queomedia.persistence.extra.json;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.impl.BeanAsArrayDeserializer;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.impl.BeanAsArraySerializer;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.impl.UnwrappingBeanSerializer;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.queomedia.commons.checks.Check;
import com.queomedia.commons.exceptions.NotImplementedCaseException;
import com.queomedia.persistence.BusinessEntity;
import com.queomedia.persistence.BusinessId;
import com.queomedia.persistence.GeneralLoaderDao;
import com.queomedia.persistence.extra.json.BusinessEntityModule.BusinessEntityJsonSerializer;
import com.queomedia.persistence.extra.json.BusinessEntityModule.TypedBusinessEntityJsonDeserializer;
import com.queomedia.persistence.extra.json.SwitchingBusinessEntityAnnotation.BusinessEntitySerialization;

public class SwitchingBusinessEntityModule extends Module {

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
    public SwitchingBusinessEntityModule(final GeneralLoaderDao generalLoaderDao,
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
     * @param defaultMode the default serialization mode used if no explicite mode is defined.
     */
    public SwitchingBusinessEntityModule(final GeneralLoaderDao generalLoaderDao,
            final BusinessEntitySerialization defaultMode) {
        this(generalLoaderDao, new SwitchingAnnotationScanner(defaultMode));
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

        context.addBeanSerializerModifier(new SwitchingBusinessEntitSerializerModifier(switchingAnnotationScanner,
                new BusinessEntityModule.BusinessEntityJsonSerializer()));
        context.addBeanDeserializerModifier(
                new SwitchingBusinessEntityDeserializerModfier(switchingAnnotationScanner, this.generalLoaderDao));

        context.addKeySerializers(new BusinessEntityModule.BusinessEntityKeySerializers());
        context.addKeyDeserializers(new BusinessEntityModule.BusinessEntityKeyDeserializers(this.generalLoaderDao));

    }

    public class SwitchingBusinessEntitSerializerModifier extends BeanSerializerModifier {

        private final SwitchingAnnotationScanner switchingAnnotationScanner;

        private final BusinessEntityModule.BusinessEntityJsonSerializer businessEntityJsonSerializer;

        public SwitchingBusinessEntitSerializerModifier(final SwitchingAnnotationScanner switchingAnnotationScanner,
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

    /**
     * Jackson Serializer that switch between BusinessEntityJsonSerializer to serialize just the business ids, and the normal
     * {@code defaultSerializer} that serialize the complete entity.
     *
     * @author engelmann
     */
    public static class SwitchingBusinessEntityJsonSerializer extends BeanSerializerBase {

        private static final long serialVersionUID = 369005311052139994L;

        /** Detect the mode how to serialize */
        private final SwitchingAnnotationScanner switchingAnnotationScanner;

        /** Serializer used for {@link BusinessEntitySerialization#BUSINESS_ID} to serialize just the BusinessID. */
        private final BusinessEntityModule.BusinessEntityJsonSerializer businessEntityJsonSerializer;

        /** Serializer used for {@link BusinessEntitySerialization#ENTITY} to serialize the complete entity. */
        private final BeanSerializerBase defaultSerializer;

        /**
         * Standard constructor.
         *
         * @param switchingAnnotationScanner the switching annotation scanner
         * @param businessEntityJsonSerializer the business entity json serializer
         * @param defaultSerializer the default serializer
         */
        public SwitchingBusinessEntityJsonSerializer(final SwitchingAnnotationScanner switchingAnnotationScanner,
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

        protected SwitchingBusinessEntityJsonSerializer(final SwitchingBusinessEntityJsonSerializer src,
                final ObjectIdWriter objectIdWriter) {
            super(src, objectIdWriter);

            this.switchingAnnotationScanner = src.switchingAnnotationScanner;
            this.businessEntityJsonSerializer = src.businessEntityJsonSerializer;
            this.defaultSerializer = src.defaultSerializer;
        }

        protected SwitchingBusinessEntityJsonSerializer(final SwitchingBusinessEntityJsonSerializer src,
                final Set<String> toIgnore) {
            super(src, toIgnore);

            this.switchingAnnotationScanner = src.switchingAnnotationScanner;
            this.businessEntityJsonSerializer = src.businessEntityJsonSerializer;
            this.defaultSerializer = src.defaultSerializer;
        }

        protected SwitchingBusinessEntityJsonSerializer(final SwitchingBusinessEntityJsonSerializer src,
                final ObjectIdWriter objectIdWriter, final Object filterId) {
            super(src, objectIdWriter, filterId);

            this.switchingAnnotationScanner = src.switchingAnnotationScanner;
            this.businessEntityJsonSerializer = src.businessEntityJsonSerializer;
            this.defaultSerializer = src.defaultSerializer;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public void serialize(final Object businessEntity, final JsonGenerator jgen, final SerializerProvider provider)
                throws IOException {

            BusinessEntitySerialization mode = this.switchingAnnotationScanner
                    .getSwitchDefinition(jgen.getOutputContext());
            switch (mode) {
            case BUSINESS_ID:
                this.businessEntityJsonSerializer.serialize((BusinessEntity) businessEntity, jgen, provider);
                return;
            case ENTITY:
                this.defaultSerializer.serialize(businessEntity, jgen, provider);
                return;
            default:
                throw new NotImplementedCaseException(mode);
            }
        }

        @Override
        public JsonSerializer<Object> unwrappingSerializer(final NameTransformer transformer) {
            return new UnwrappingBeanSerializer(this, transformer) {
                private static final long serialVersionUID = 1L;

                @Override
                protected void serializeFields(final Object bean, final JsonGenerator jgen,
                        final SerializerProvider provider)
                        throws IOException {
                    BusinessEntitySerialization mode = switchingAnnotationScanner
                            .getSwitchDefinition(jgen.getOutputContext());
                    switch (mode) {
                    case BUSINESS_ID:
                        jgen.writeFieldName("businessId");
                        SwitchingBusinessEntityJsonSerializer.this.businessEntityJsonSerializer
                                .serialize(((BusinessEntity) bean), jgen, provider);
                        return;
                    case ENTITY:
                        super.serializeFields(bean, jgen, provider);
                        return;
                    default:
                        throw new NotImplementedCaseException(mode);
                    }
                }
            };
        }

        @Override
        public BeanSerializerBase withObjectIdWriter(final ObjectIdWriter objectIdWriter) {
            return new SwitchingBusinessEntityJsonSerializer(this, objectIdWriter);
        }

        @Override
        protected BeanSerializerBase withIgnorals(final Set<String> toIgnore) {
            return new SwitchingBusinessEntityJsonSerializer(this, toIgnore);
        }

        @Override
        protected BeanSerializerBase asArraySerializer() {
            /*
             * Cannot:
             *
             * - have Object Id (may be allowed in future) - have "any getter" - have
             * per-property filters
             */
            if ((this._objectIdWriter == null) && (this._anyGetterWriter == null) && (this._propertyFilterId == null)) {
                return new BeanAsArraySerializer(this);
            }
            // already is one, so:
            return this;
        }

        @Override
        public BeanSerializerBase withFilterId(final Object filterId) {
            return new SwitchingBusinessEntityJsonSerializer(this, this._objectIdWriter, filterId);
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

        private final SwitchingAnnotationScanner switchingAnnotationScanner;

        private final GeneralLoaderDao generalLoaderDao;

        public SwitchingBusinessEntityDeserializerModfier(final SwitchingAnnotationScanner switchingAnnotationScanner,
                final GeneralLoaderDao generalLoaderDao) {
            Check.notNullArgument(generalLoaderDao, "generalLoaderDao");
            Check.notNullArgument(switchingAnnotationScanner, "switchingAnnotationScanner");

            this.switchingAnnotationScanner = switchingAnnotationScanner;
            this.generalLoaderDao = generalLoaderDao;
        }

        @Override
        public JsonDeserializer<?> modifyDeserializer(final DeserializationConfig config,
                final BeanDescription beanDesc, final JsonDeserializer<?> deserializer) {
            if (BusinessEntity.class.isAssignableFrom(beanDesc.getBeanClass())) {
                return new SwitchingBusinessEntityDeserializer(this.switchingAnnotationScanner,
                        new BusinessEntityModule.TypedBusinessEntityJsonDeserializer(beanDesc.getBeanClass(),
                                this.generalLoaderDao),
                        (BeanDeserializerBase) deserializer);
            } else {
                return deserializer;
            }
        }
    }

    //    // https://stackoverflow.com/questions/18313323/how-do-i-call-the-default-deserializer-from-a-custom-deserializer-in-jackson
    //    public static class SwitchingBusinessEntityDeserializer extends StdDeserializer<BusinessEntity>
    //            implements ResolvableDeserializer {
    //
    //        private final JsonDeserializer<?> defaultDeserializer;
    //
    //        private BusinessEntityModule.TypedBusinessEntityJsonDeserializer typedBusinessEntityJsonDeserializer;
    //        private Class beanClazz;
    //
    //        private GeneralLoaderDao generalLoaderDao;
    //
    //        public SwitchingBusinessEntityDeserializer(
    //                BusinessEntityModule.TypedBusinessEntityJsonDeserializer typedBusinessEntityJsonDeserializer,
    //                final Class beanClazz, JsonDeserializer<?> defaultDeserializer, GeneralLoaderDao generalLoaderDao) {
    //            super(BusinessEntity.class);
    //
    //            this.typedBusinessEntityJsonDeserializer = typedBusinessEntityJsonDeserializer;
    //            this.beanClazz = beanClazz;
    //            this.defaultDeserializer = defaultDeserializer;
    //            this.generalLoaderDao = generalLoaderDao;
    //        }
    //
    //        @Override
    //        public BusinessEntity deserialize(JsonParser jp, DeserializationContext ctxt)
    //                throws IOException, JsonProcessingException {
    //
    //            JsonToken currentToken = jp.currentToken();
    //            if (currentToken == JsonToken.VALUE_STRING) {
    //                String bidString = jp.readValueAs(String.class);
    //                System.out.println("bidString: " + bidString);
    //                System.out.println("beanClazz: " + beanClazz);
    //                return this.generalLoaderDao.getByBusinessId(BusinessId.parse(bidString), beanClazz);
    //            } else {
    //                return (BusinessEntity) defaultDeserializer.deserialize(jp, ctxt);
    //            }
    //        }
    //
    //        @Override
    //        public JsonDeserializer<BusinessEntity> unwrappingDeserializer(NameTransformer unwrapper) {
    //            JsonDeserializer<?> unwrappingDeserializer = defaultDeserializer.unwrappingDeserializer(unwrapper);
    //            return (JsonDeserializer<BusinessEntity>) unwrappingDeserializer;
    //        }
    //
    //        public void resolve(DeserializationContext ctxt) throws JsonMappingException {
    //            ((ResolvableDeserializer) defaultDeserializer).resolve(ctxt);
    //        }
    //
    //    }

    // https://stackoverflow.com/questions/18313323/how-do-i-call-the-default-deserializer-from-a-custom-deserializer-in-jackson
    public static class SwitchingBusinessEntityDeserializer<T extends BusinessEntity<T>> extends BeanDeserializerBase
            implements ResolvableDeserializer {

        private static final long serialVersionUID = 6686258398018608306L;

        private final SwitchingAnnotationScanner switchingAnnotationScanner;

        private TypedBusinessEntityJsonDeserializer<T> typedBusinessEntityJsonDeserializer;

        private final BeanDeserializerBase defaultDeserializer;

        private final boolean unwarppedMode;

        public SwitchingBusinessEntityDeserializer(final SwitchingAnnotationScanner switchingAnnotationScanner,
                final TypedBusinessEntityJsonDeserializer<T> typedBusinessEntityJsonDeserializer,
                final BeanDeserializerBase defaultDeserializer) {
            super(defaultDeserializer);
            Check.notNullArgument(switchingAnnotationScanner, "switchingAnnotationScanner");

            this.switchingAnnotationScanner = switchingAnnotationScanner;
            this.typedBusinessEntityJsonDeserializer = typedBusinessEntityJsonDeserializer;
            this.defaultDeserializer = defaultDeserializer;

            this.unwarppedMode = false;
        }

        protected SwitchingBusinessEntityDeserializer(final SwitchingBusinessEntityDeserializer<T> src,
                final NameTransformer unwrapper) {
            super(src, unwrapper);

            this.switchingAnnotationScanner = src.switchingAnnotationScanner;
            this.typedBusinessEntityJsonDeserializer = src.typedBusinessEntityJsonDeserializer;
            this.defaultDeserializer = src.defaultDeserializer;

            this.unwarppedMode = true;
        }

        protected SwitchingBusinessEntityDeserializer(final SwitchingBusinessEntityDeserializer<T> src,
                final ObjectIdReader oir) {
            super(src, oir);

            this.switchingAnnotationScanner = src.switchingAnnotationScanner;
            this.typedBusinessEntityJsonDeserializer = src.typedBusinessEntityJsonDeserializer;
            this.defaultDeserializer = src.defaultDeserializer;

            this.unwarppedMode = false;
        }

        protected SwitchingBusinessEntityDeserializer(final SwitchingBusinessEntityDeserializer<T> src,
                final Set<String> ignorableProps) {
            super(src, ignorableProps);

            this.switchingAnnotationScanner = src.switchingAnnotationScanner;
            this.typedBusinessEntityJsonDeserializer = src.typedBusinessEntityJsonDeserializer;
            this.defaultDeserializer = src.defaultDeserializer;

            this.unwarppedMode = false;
        }

        //for what is this method used?
        @Override
        public Object deserializeFromObject(final JsonParser jp, final DeserializationContext ctxt) throws IOException {

            BusinessEntitySerialization mode = this.switchingAnnotationScanner
                    .getSwitchDefinition(jp.getParsingContext());
            switch (mode) {
            case BUSINESS_ID: {
                if (this.unwarppedMode) {
                    if (!jp.hasToken(JsonToken.START_OBJECT)) {
                        throw new JsonParseException(jp, "START_OBJECT expected, but found:" + jp.currentToken());
                    }
                    for (String fieldName = jp.nextFieldName(); fieldName != null; fieldName = jp.nextFieldName()) {
                        if (fieldName.equals("businessId")) {
                            jp.nextToken();
                            return typedBusinessEntityJsonDeserializer.deserialize(jp, ctxt);
                        }
                    }
                    return null;
                } else {
                    return typedBusinessEntityJsonDeserializer.deserialize(jp, ctxt);
                }
            }
            case ENTITY: {
                return this.defaultDeserializer.deserializeFromObject(jp, ctxt);
            }
            default:
                throw new NotImplementedCaseException(mode);
            }
        }

        @Override
        protected Object _deserializeUsingPropertyBased(final JsonParser jp, final DeserializationContext ctxt)
                throws IOException {
            //for load by business id, it does not matter if the ctor must be used,
            //and for the default, there is no public _deserializeUsingPropertyBased method, but
            //BeanDeserializer.deserializeFromObject will forward it
            //and so one can reuse this.deserializeFromObject            
            return deserializeFromObject(jp, ctxt);
        }

        @Override
        public Object deserialize(final JsonParser jp, final DeserializationContext ctxt)
                throws IOException, JsonProcessingException {

            BusinessEntitySerialization mode = this.switchingAnnotationScanner
                    .getSwitchDefinition(jp.getParsingContext());
            switch (mode) {
            case BUSINESS_ID: {
                if (this.unwarppedMode) {
                    if (!jp.hasToken(JsonToken.START_OBJECT)) {
                        throw new JsonParseException(jp, "START_OBJECT expected, but found:" + jp.currentToken());
                    }
                    for (String fieldName = jp.nextFieldName(); fieldName != null; fieldName = jp.nextFieldName()) {
                        if (fieldName.equals("businessId")) {
                            jp.nextToken();
                            return typedBusinessEntityJsonDeserializer.deserialize(jp, ctxt);
                        }
                    }
                    return null;
                } else {
                    return typedBusinessEntityJsonDeserializer.deserialize(jp, ctxt);
                }
            }
            case ENTITY: {
                return this.defaultDeserializer.deserialize(jp, ctxt);
            }
            default:
                throw new NotImplementedCaseException(mode);
            }

        }

        /**
         * State marker we need in order to avoid infinite recursion for some cases
         * (not very clean, alas, but has to do for now)
         *
         * @since 2.9
         */
        private volatile transient NameTransformer _currentlyTransforming;

        @Override
        public JsonDeserializer<Object> unwrappingDeserializer(final NameTransformer transformer) {
            // code from BeanDeserializer.unwrappingDeserializer

            // bit kludgy but we don't want to accidentally change type; sub-classes
            // MUST override this method to support unwrapped properties...
            if (getClass() != SwitchingBusinessEntityDeserializer.class) {
                return this;
            }
            // 25-Mar-2017, tatu: Not clean at all, but for [databind#383] we do need
            //   to keep track of accidental recursion...
            if (this._currentlyTransforming == transformer) {
                return this;
            }
            this._currentlyTransforming = transformer;
            try {
                return new SwitchingBusinessEntityDeserializer<T>(this, transformer);
            } finally {
                this._currentlyTransforming = null;
            }
        }

        @Override
        public SwitchingBusinessEntityDeserializer<T> withObjectIdReader(final ObjectIdReader oir) {
            return new SwitchingBusinessEntityDeserializer<T>(this, oir);
        }

        @Override
        public SwitchingBusinessEntityDeserializer<T> withIgnorableProperties(final Set<String> ignorableProps) {
            return new SwitchingBusinessEntityDeserializer<T>(this, ignorableProps);
        }

        @Override
        protected BeanDeserializerBase asArrayDeserializer() {
            SettableBeanProperty[] props = this._beanProperties.getPropertiesInInsertionOrder();
            return new BeanAsArrayDeserializer(this, props);
        }

    }

    static class SwitchingAnnotationScanner {

        /** The serialization mode returned if no annotation is found. */
        private final BusinessEntitySerialization defaultMode;

        public SwitchingAnnotationScanner(final BusinessEntitySerialization defaultMode) {
            Check.notNullArgument(defaultMode, "defaultMode");

            this.defaultMode = defaultMode;
        }

        BusinessEntitySerialization getSwitchDefinition(final JsonStreamContext context) {
            return findSwitchDefinition(context).orElse(this.defaultMode);
        }

        Optional<BusinessEntitySerialization> findSwitchDefinition(final JsonStreamContext context) {
            return findSwitchingBusinessEntityAnnotation(context).map(SwitchingBusinessEntityAnnotation::value);
        }

        Optional<SwitchingBusinessEntityAnnotation> findSwitchingBusinessEntityAnnotation(
                final JsonStreamContext context) {
            Object currentValue = context.getCurrentValue();
            System.out.println(
                    "currentValue:" + currentValue + " " + (currentValue != null ? currentValue.getClass() : ""));

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
