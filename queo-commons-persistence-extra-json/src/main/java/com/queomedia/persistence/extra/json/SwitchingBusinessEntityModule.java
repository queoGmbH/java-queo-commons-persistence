package com.queomedia.persistence.extra.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.queomedia.commons.checks.Check;
import com.queomedia.commons.exceptions.NotImplementedCaseException;
import com.queomedia.persistence.BusinessEntity;
import com.queomedia.persistence.GeneralLoaderDao;
import com.queomedia.persistence.extra.json.BusinessEntityModule.BusinessEntityJsonSerializer;
import com.queomedia.persistence.extra.json.BusinessEntityModule.TypedBusinessEntityJsonDeserializer;
import com.queomedia.persistence.extra.json.BusinessIdModule.BusinessIdJsonDeserializer;
import com.queomedia.persistence.extra.json.BusinessIdModule.BusinessIdJsonSerializer;

/**
 * Jackson Module that activate the Switching Business Enitity Functionality.
 *
 * <p>
 * This Jackson Module registere a serializer and deserializer as well as KeySerializer/Deserializer for
 * {@link com.queomedia.persistence.BusinessEntity}s that can either serialized as plain entity or just with there
 * BusinessID (like the {@link BusinessEntityModule}). The switch between this two serialization modes can be controlled by
 * {@link com.queomedia.persistence.extra.json.BusinessEntityJsonSerialization}. A annotation that can be placed by
 * any parent class in the json-serialization path.
 * </p>
 *
 * <p>
 * This module activate, the switching serializer and deserializer as well as a KeySerializer/Deserializer that
 * always serialize just the Business Id.
 * </p>
 *
 * <p>
 * The {@link com.queomedia.persistence.extra.json.BusinessEntityJsonSerialization} defined
 * how the business entities are serialized. The annotation is picked up by
 * {@link com.queomedia.persistence.extra.json.SwitchingAnnotationScanner} which is invoked by
 * the serializers and deserializers when they needs to serialize/deserialize an
 * {@link com.queomedia.persistence.BusinessEntity}.
 * </p>
 * 
 * <p>
 * This Jackson Module should be used together with two other modules: {@link BusinessIdModule} and 
 * {@link BusinessEntityOmitIdModule}. To make it more easy, this module can register them automatic.
 * Either by setting  {@code enableBusinessIdModule} and {@code omitBusinessEnitityIds} in the constructor
 * {@link #SwitchingBusinessEntityModule(GeneralLoaderDao, SwitchingAnnotationScanner, boolean, boolean)}
 * to true, or use the simple constructor {@link #SwitchingBusinessEntityModule(GeneralLoaderDao)}
 * that enable them both.
 * </p>
 */
public class SwitchingBusinessEntityModule extends Module {

    /** The general loader dao. */
    private final GeneralLoaderDao generalLoaderDao;

    /** The switching annotation scanner. */
    private final SwitchingAnnotationScanner switchingAnnotationScanner;

    /**
     * If set to true, the {@link BusinessIdJsonSerializer} and {@link BusinessIdJsonDeserializer} from
     * {@link BusinessIdModule} become registered too.
     *
     * <p>
     * This extra functionality is added to this module, because it is required to have the same json format
     * of Business Ids from {@link BusinessEntity} that are serialized in mode
     * {@link BusinessEntitySerializationMode#ENTITY} (which need {@link BusinessIdModule}) and
     * {@link BusinessEntitySerializationMode#BUSINESS_ID} (that serialize just the businessId value, even if
     * {@link BusinessIdModule} is not activated).
     * </p>
     */
    private final boolean enableBusinessIdModule;

    /**
     * If set to true, then the {@link BusinessEntityOmitIdModule.BusinessEntityJacksonMixin} is registered
     * to exclude the fields {@code BusinessEntity.id}, {@code BusinessEntity.hibernateId},
     * {@code BusinessEntity.businessIdValue} and {@code BusinessEntity.isNew} from ALL {@link BusinessEntity}s.
     */
    private final boolean omitBusinessEnitityIds;

    /**
     * Instantiates a new business id module and register the mapping.
     *
     * @param generalLoaderDao the general loader dao
     * @param switchingAnnotationScanner the switching annotation scanner
     * @param enableBusinessIdModule if true, then the {@link BusinessIdModule} become enabled too
     * @param omitBusinessEnitityIds if true, then the fields id, hibernateId, businessIdValue and isNew are omitted
     *        from ALL {@link BusinessEntity}s.
     */
    public SwitchingBusinessEntityModule(final GeneralLoaderDao generalLoaderDao,
            final SwitchingAnnotationScanner switchingAnnotationScanner, final boolean enableBusinessIdModule,
            final boolean omitBusinessEnitityIds) {
        Check.notNullArgument(generalLoaderDao, "generalLoaderDao");
        Check.notNullArgument(switchingAnnotationScanner, "switchingAnnotationScanner");

        this.generalLoaderDao = generalLoaderDao;
        this.switchingAnnotationScanner = switchingAnnotationScanner;
        this.enableBusinessIdModule = enableBusinessIdModule;
        this.omitBusinessEnitityIds = omitBusinessEnitityIds;
    }

    /**
     * Instantiates a new switching business entity module.
     *
     * @param generalLoaderDao the general loader dao
     * @param defaultMode the default serialization mode used if no explicit mode is defined.
     * @param enableBusinessIdModule if true, then the {@link BusinessIdModule} become enabled too
     * @param omitBusinessEnitityIds if true, then the fields id, hibernateId, businessIdValue and isNew are omitted
     *        from ALL {@link BusinessEntity}s.
     */
    public SwitchingBusinessEntityModule(final GeneralLoaderDao generalLoaderDao,
            final BusinessEntitySerializationMode defaultMode, final boolean enableBusinessIdModule,
            final boolean omitBusinessEnitityIds) {
        this(generalLoaderDao,
                new SwitchingAnnotationScanner(defaultMode),
                enableBusinessIdModule,
                omitBusinessEnitityIds);
    }

    /**
     * Instantiates a new switching business entity module with default mode {@link BusinessEntitySerializationMode#ENTITY}
     * enabled {@link BusinessIdModule}  and omit add  fields id, hibernateId, businessIdValue and isNew from ALL
     * {@link BusinessEntity}s.
     *
     * @param generalLoaderDao the general loader dao
     */
    public SwitchingBusinessEntityModule(final GeneralLoaderDao generalLoaderDao) {
        this(generalLoaderDao, BusinessEntitySerializationMode.ENTITY, true, true);
    }

    @Override
    public String getModuleName() {
        return "SwitchingBusinessEntityModule";
    }

    @Override
    public Version version() {
        return Version.unknownVersion();
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

        context.addBeanSerializerModifier(new SwitchingBusinessEntitySerializerModifier(this.switchingAnnotationScanner,
                new BusinessEntityModule.BusinessEntityJsonSerializer()));
        context.addBeanDeserializerModifier(
                new SwitchingBusinessEntityDeserializerModfier(this.switchingAnnotationScanner, this.generalLoaderDao));

        context.addKeySerializers(new BusinessEntityModule.BusinessEntityKeySerializers());
        context.addKeyDeserializers(new BusinessEntityModule.BusinessEntityKeyDeserializers(this.generalLoaderDao));

        if (this.enableBusinessIdModule) {
            new BusinessIdModule().setupModule(context);
        }

        if (this.omitBusinessEnitityIds) {
            context.setMixInAnnotations(BusinessEntity.class,
                    BusinessEntityOmitIdModule.BusinessEntityJacksonMixin.class);
        }

    }

    /**
     * Enable the {@link SwitchingBusinessEntityJsonSerializer} when a {@link BusinessEntity} is serialized.
     *
     * <p>
     * Note: A {@link BeanSerializerModifier} is needed because the {@link SwitchingBusinessEntityJsonSerializer}
     * need an explicit reference to the default {@link JsonSerializer}. And this reference could be obtained
     * in the {@link BeanSerializerModifier}.
     * </p>
     *
     * @see <a href="https://www.baeldung.com/jackson-call-default-serializer-from-custom-serializer">
     *      Calling Default Serializer from Custom Serializer in Jackson</a>
     */
    public class SwitchingBusinessEntitySerializerModifier extends BeanSerializerModifier {

        private final SwitchingAnnotationScanner switchingAnnotationScanner;

        private final BusinessEntityModule.BusinessEntityJsonSerializer businessEntityJsonSerializer;

        public SwitchingBusinessEntitySerializerModifier(final SwitchingAnnotationScanner switchingAnnotationScanner,
                final BusinessEntityJsonSerializer businessEntityJsonSerializer) {
            Check.notNullArgument(switchingAnnotationScanner, "switchingAnnotationScanner");
            Check.notNullArgument(businessEntityJsonSerializer, "businessEntityJsonSerializer");

            this.switchingAnnotationScanner = switchingAnnotationScanner;
            this.businessEntityJsonSerializer = businessEntityJsonSerializer;
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public JsonSerializer<?> modifySerializer(final SerializationConfig config, final BeanDescription beanDesc,
                final JsonSerializer<?> serializer) {
            Check.notNullArgument(beanDesc, "beanDesc");
            Check.notNullArgument(serializer, "serializer");

            if (BusinessEntity.class.isAssignableFrom(beanDesc.getBeanClass())) {
                return new SwitchingBusinessEntityJsonSerializer(beanDesc.getBeanClass(),
                        this.switchingAnnotationScanner,
                        this.businessEntityJsonSerializer,
                        serializer);
            } else {
                return serializer;
            }
        }
    }

    /**
     * Jackson Serializer that switch between BusinessEntityJsonSerializer to serialize just the business ids,
     * and the normal {@code defaultSerializer} that serialize the complete entity.
     */
    static class SwitchingBusinessEntityJsonSerializer<T extends BusinessEntity<T>> extends StdSerializer<T> {

        private static final long serialVersionUID = -6539439448441519263L;

        /** Detect the mode how to serialize. */
        private final SwitchingAnnotationScanner switchingAnnotationScanner;

        /** Serializer used for {@link BusinessEntitySerializationMode#BUSINESS_ID} to serialize just the BusinessID. */
        private final BusinessEntityModule.BusinessEntityJsonSerializer businessEntityJsonSerializer;

        /** Serializer used for {@link BusinessEntitySerializationMode#ENTITY} to serialize the complete entity. */
        private final JsonSerializer<Object> defaultSerializer;

        /**
         * Instantiates a new switching business entity json serializer.
         *
         * @param beanClazz   Nominal type supported, usually declared type of property for which serializer is used.
         * @param switchingAnnotationScanner the switching annotation scanner
         * @param businessEntityJsonSerializer the business entity json serializer
         * @param defaultSerializer the default serializer
         */
        public SwitchingBusinessEntityJsonSerializer(final Class<T> beanClazz,
                final SwitchingAnnotationScanner switchingAnnotationScanner,
                final BusinessEntityJsonSerializer businessEntityJsonSerializer,
                final JsonSerializer<Object> defaultSerializer) {
            super(beanClazz);
            Check.notNullArgument(switchingAnnotationScanner, "switchingAnnotationScanner");
            Check.notNullArgument(businessEntityJsonSerializer, "businessEntityJsonSerializer");
            Check.notNullArgument(defaultSerializer, "defaultSerializer");

            this.switchingAnnotationScanner = switchingAnnotationScanner;
            this.businessEntityJsonSerializer = businessEntityJsonSerializer;
            this.defaultSerializer = defaultSerializer;
        }

        /**
         * Serialize either with {@link #businessEntityJsonSerializer} or {@link #defaultSerializer}.
         *
         * @param value Value to serialize; can <b>not</b> be null.
         * @param jgen Generator used to output resulting Json content
         * @param serializers Provider that can be used to get serializers for serializing Objects value contains, if any.
         */
        @Override
        public void serialize(final T businessEntity, final JsonGenerator jgen, final SerializerProvider provider)
                throws IOException {

            BusinessEntitySerializationMode mode = this.switchingAnnotationScanner
                    .getSwitchDefinition(jgen.getOutputContext());
            switch (mode) {
            case BUSINESS_ID:
                this.businessEntityJsonSerializer.serialize(businessEntity, jgen, provider);
                return;
            case ENTITY:
                this.defaultSerializer.serialize(businessEntity, jgen, provider);
                return;
            default:
                throw new NotImplementedCaseException(mode);
            }
        }

        /**
         * Return serializer instance that produces "unwrapped" serialization.
         *
         * The returned serializer will write a field {@code businessId} if the mode is
         * {@link BusinessEntitySerializationMode#BUSINESS_ID}. For mode {@link BusinessEntitySerializationMode#ENTITY}
         * it will just reuse the {@link #defaultSerializer}s
         * {@link JsonSerializer#unwrappingSerializer(NameTransformer)} implementation.
         *
         * @param unwrapper Name transformation to use to convert between names of unwrapper properties
         */
        @Override
        public JsonSerializer<T> unwrappingSerializer(final NameTransformer unwrapper) {
            Check.notNullArgument(unwrapper, "unwrapper");

            return new JsonSerializer<T>() {
                @Override
                public boolean isUnwrappingSerializer() {
                    return true;
                }

                @Override
                public void serialize(final T businessEntity, final JsonGenerator jgen,
                        final SerializerProvider providers)
                        throws IOException {
                    BusinessEntitySerializationMode mode = SwitchingBusinessEntityJsonSerializer.this.switchingAnnotationScanner
                            .getSwitchDefinition(jgen.getOutputContext());
                    switch (mode) {
                    case BUSINESS_ID:
                        jgen.writeFieldName("businessId");
                        SwitchingBusinessEntityJsonSerializer.this.businessEntityJsonSerializer
                                .serialize(businessEntity, jgen, providers);
                        return;
                    case ENTITY:
                        SwitchingBusinessEntityJsonSerializer.this.defaultSerializer.unwrappingSerializer(unwrapper)
                                .serialize(businessEntity, jgen, providers);
                        return;
                    default:
                        throw new NotImplementedCaseException(mode);
                    }
                }
            };
        }
    }

    /**
     * Enable the {@link SwitchingBusinessEntityDeserializer} when a {@link BusinessEntity} is deserialized.
     *
     * <p>
     * Note: A {@link BeanDeserializerModifier} is needed because the {@link SwitchingBusinessEntityDeserializer}
     * need an explicit reference to the default {@link JsonDeserializer}. And this reference could be obtained
     * in the {@link BeanDeserializerModifier}.
     * </p>
     */
    class SwitchingBusinessEntityDeserializerModfier extends BeanDeserializerModifier {

        private final SwitchingAnnotationScanner switchingAnnotationScanner;

        private final GeneralLoaderDao generalLoaderDao;

        public SwitchingBusinessEntityDeserializerModfier(final SwitchingAnnotationScanner switchingAnnotationScanner,
                final GeneralLoaderDao generalLoaderDao) {
            Check.notNullArgument(generalLoaderDao, "generalLoaderDao");
            Check.notNullArgument(switchingAnnotationScanner, "switchingAnnotationScanner");

            this.switchingAnnotationScanner = switchingAnnotationScanner;
            this.generalLoaderDao = generalLoaderDao;
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public JsonDeserializer<?> modifyDeserializer(final DeserializationConfig config,
                final BeanDescription beanDesc, final JsonDeserializer<?> deserializer) {
            if (BusinessEntity.class.isAssignableFrom(beanDesc.getBeanClass())) {
                return new SwitchingBusinessEntityDeserializer(beanDesc.getBeanClass(),
                        this.switchingAnnotationScanner,
                        new BusinessEntityModule.TypedBusinessEntityJsonDeserializer(beanDesc.getBeanClass(),
                                this.generalLoaderDao),
                        deserializer);
            } else {
                return deserializer;
            }
        }
    }

    /**
     * Jackson Deserializer that switch between TypedBusinessEntityJsonDeserializer to deserialize from just the business ids,
     * and the normal {@code defaultSerializer} that deserialize the complete entity.
     */
    public static class SwitchingBusinessEntityDeserializer<T extends BusinessEntity<T>> extends StdDeserializer<T>
            implements ResolvableDeserializer {

        private static final long serialVersionUID = 2217533520312959119L;

        /** Detect the mode how to serialize. */
        private final SwitchingAnnotationScanner switchingAnnotationScanner;

        /** Deerializer used for {@link BusinessEntitySerializationMode#BUSINESS_ID} to deserialize form the BusinessID. */
        private final TypedBusinessEntityJsonDeserializer<T> typedBusinessEntityJsonDeserializer;

        /** Deerializer used for {@link BusinessEntitySerializationMode#ENTITY} to deserialize the complete entity. */
        private final JsonDeserializer<T> defaultDeserializer;

        public SwitchingBusinessEntityDeserializer(final Class<T> beanClazz,
                final SwitchingAnnotationScanner switchingAnnotationScanner,
                final BusinessEntityModule.TypedBusinessEntityJsonDeserializer<T> typedBusinessEntityJsonDeserializer,
                final JsonDeserializer<T> defaultDeserializer) {
            super(beanClazz);
            Check.notNullArgument(switchingAnnotationScanner, "switchingAnnotationScanner");
            Check.notNullArgument(typedBusinessEntityJsonDeserializer, "typedBusinessEntityJsonDeserializer");
            Check.notNullArgument(defaultDeserializer, "defaultDeserializer");

            this.switchingAnnotationScanner = switchingAnnotationScanner;
            this.typedBusinessEntityJsonDeserializer = typedBusinessEntityJsonDeserializer;
            this.defaultDeserializer = defaultDeserializer;
        }

        /**
         * Deserialize either with {@link #typedBusinessEntityJsonDeserializer} or {@link #defaultDeserializer}.
         *
         * @param jp Parsed used for reading JSON content
         * @param ctxt Context that can be used to access information about this deserialization activity.
         *
         * @return Deserialized {@link BusinessEntity}
         */
        @Override
        public T deserialize(final JsonParser jp, final DeserializationContext ctxt)
                throws IOException, JsonProcessingException {

            BusinessEntitySerializationMode mode = this.switchingAnnotationScanner
                    .getSwitchDefinition(jp.getParsingContext());
            switch (mode) {
            case BUSINESS_ID: {
                return this.typedBusinessEntityJsonDeserializer.deserialize(jp, ctxt);
            }
            case ENTITY: {
                return this.defaultDeserializer.deserialize(jp, ctxt);
            }
            default:
                throw new NotImplementedCaseException(mode);
            }
        }

        /**
         * Return a deserializer instance that is able to handle "unwrapped" value instances.
         *
         * If mode {@link BusinessEntitySerializationMode#BUSINESS_ID}, then the returned deserializer will read a
         * field {@code businessId} with {@link #typedBusinessEntityJsonDeserializer}.
         * For mode {@link BusinessEntitySerializationMode#ENTITY}
         * it will just reuse the {@link #defaultDeserializer}s
         * {@link JsonSerializer#unwrappingSerializer(NameTransformer)} implementation.
         *
         * @param unwrapper Name transformation to use to convert between names of unwrapper properties
         */
        @Override
        public JsonDeserializer<T> unwrappingDeserializer(final NameTransformer unwrapper) {
            Check.notNullArgument(unwrapper, "unwrapper");

            return new JsonDeserializer<T>() {
                @Override
                public T deserialize(final JsonParser jp, final DeserializationContext ctxt)
                        throws IOException, JsonProcessingException {

                    BusinessEntitySerializationMode mode = SwitchingBusinessEntityDeserializer.this.switchingAnnotationScanner
                            .getSwitchDefinition(jp.getParsingContext());
                    switch (mode) {
                    case BUSINESS_ID:
                        if (!jp.hasToken(JsonToken.START_OBJECT)) {
                            throw new JsonParseException(jp, "START_OBJECT expected, but found: " + jp.currentToken());
                        }
                        for (String fieldName = jp.nextFieldName(); fieldName != null; fieldName = jp.nextFieldName()) {
                            if (fieldName.equals("businessId")) {
                                jp.nextToken();
                                return SwitchingBusinessEntityDeserializer.this.typedBusinessEntityJsonDeserializer
                                        .deserialize(jp, ctxt);
                            }
                        }
                        return null;
                    case ENTITY:
                        return SwitchingBusinessEntityDeserializer.this.defaultDeserializer
                                .unwrappingDeserializer(unwrapper).deserialize(jp, ctxt);
                    default:
                        throw new NotImplementedCaseException(mode);
                    }
                }
            };
        }

        @Override
        public void resolve(final DeserializationContext ctxt) throws JsonMappingException {
            ((ResolvableDeserializer) this.defaultDeserializer).resolve(ctxt);
        }

    }

}
