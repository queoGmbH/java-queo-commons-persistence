package com.queomedia.persistence.extra.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.deser.KeyDeserializers;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.queomedia.commons.checks.Check;
import com.queomedia.persistence.BusinessEntity;
import com.queomedia.persistence.BusinessId;
import com.queomedia.persistence.GeneralLoaderDao;
import com.queomedia.persistence.extra.json.util.Computable;
import com.queomedia.persistence.extra.json.util.Memorizer;

/**
 * Jackson {@link com.fasterxml.jackson.databind.Module} that registers BusinessEntity Json Serializer/Deserializers.
 *
 * They will map a business entity object to a single (string) value which is the businessId and vice versa.
 * It is important to return the bId as String with "" around, since the Long value is bigger than the
 * Json data type Number.
 *
 * Serialization explained:
 * Example 1: A class with field {@code User user}, with a {@code user.businessId=123} will become
 * {@code "user":"123" }
 * Example 2: A single {@code User} object without any container around it and {@code user.businessId=123}
 * will become {@code "123"}, without a variable name.
 * Example 3: a list of {@code User users} with bIds 123, 456 and 789 will become {@code"123","456","789"}.
 *
 * This makes it necessary to return non entity objects, if the field values are needed. DTOs are an option for this.
 * Example 4: a Dto with two field, first of type String and name "title" with value "xyz" and second od type Entity with
 * name "entity"  and an object with bId=123 will become <code>{"name"="xyz", "entity"="123"}</code>
 *
 * It prevents looping in json string creation, which can be result from bi-directional relations or loops in
 * the database relations.
 * Jackson resolves each field of an object recursive. If object of class A has a field with object of class B
 * and these classes have a bi-directional relation, it can create an endless cycle between A-B-A-B... .
 * Using this Module stops the recursion at the first entity object, since its fields are replaces with only its
 * buisinnesId. With this Id, the values of the object could then be reloaded, returning a Dto or command object
 * with the desired fields, to prevent the behavior of example 2 from above.
 *
 * Deserialization explained:
 * It makes it possible during binding to fill entity fields from given businessIds in JsonStrings.
 *
 * Example 5: A controller method expects a command object containing a field named "user" of the type User,
 * which is an entity. An input string like {"user":"123"} will fill this field with the user object with bid=123
 * from the persistence context during binding. This reduces the need for manual reload. The matching part is
 * the name of the field and the name in the Json object. A field of type User but named "person" would need a
 * JsonString like {"person":"123"} as input for the same result.
 *
 * It setups ( {@link Module#setupModule(SetupContext) }) a {@link SetupContext} with a {@link Serializers} and a
 * {@link Deserializers} (do not miss the "s" at the end of the name) which provide {@link JsonSerializer} and
 * {@link JsonDeserializer} for all {@link BusinessEntity} subclasses.
 *
 */
public class BusinessEntityModule extends Module {

    /** The general loader dao. */
    private final GeneralLoaderDao generalLoaderDao;

    /** The version. */
    private final Version version = new Version(1, 0, 0, null, "com.queomedia", null);

    /**
     * Instantiates a new business id module and register the mapping.
     *
     * @param generalLoaderDao the general loader dao
     */
    public BusinessEntityModule(final GeneralLoaderDao generalLoaderDao) {
        Check.notNullArgument(generalLoaderDao, "generalLoaderDao");

        this.generalLoaderDao = generalLoaderDao;
    }

    @Override
    public String getModuleName() {
        return "BusinessEntityModule";
    }

    /* (non-Javadoc)
     * @see com.fasterxml.jackson.databind.Module#version()
     */
    @Override
    public Version version() {
        return this.version;
    }

    /* (non-Javadoc)
     * @see com.fasterxml.jackson.databind.Module#setupModule(com.fasterxml.jackson.databind.Module.SetupContext)
     */
    @Override
    public void setupModule(final SetupContext context) {
        Check.notNullArgument(context, "context");

        context.addSerializers(new BusinessEntitySerializers());
        context.addDeserializers(new BusinessEntityDeserializers(this.generalLoaderDao));

        context.addKeySerializers(new BusinessEntityKeySerializers());
        context.addKeyDeserializers(new BusinessEntityKeyDeserializers(this.generalLoaderDao));
    }

    /**
     * A Jackson {@link Serializers} that provides (the same) {@link BusinessEntityJsonSerializer}
     * for every class that extends {@link BusinessEntity}.
     */
    static class BusinessEntitySerializers extends Serializers.Base {

        /** The SAME {@link JsonSerializer} is used for every business type. */
        private final BusinessEntityJsonSerializer businessEntityJsonSerializer = new BusinessEntityJsonSerializer();

        /* (non-Javadoc)
         * @see com.fasterxml.jackson.databind.ser.Serializers.Base#findSerializer(com.fasterxml.jackson.databind.SerializationConfig, com.fasterxml.jackson.databind.JavaType, com.fasterxml.jackson.databind.BeanDescription)
         */
        @Override
        public JsonSerializer<?> findSerializer(final SerializationConfig config, final JavaType type,
                final BeanDescription beanDesc) {
            Check.notNullArgument(type, "type");

            if (BusinessEntity.class.isAssignableFrom(type.getRawClass())) {
                return this.businessEntityJsonSerializer;
            } else {
                return null;
            }
        }
    }

    /**
     * The Class BusinessIdJsonSerializer.
     */
    @SuppressWarnings("rawtypes")
    static class BusinessEntityJsonSerializer extends JsonSerializer<BusinessEntity> {

        /*
         * (non-Javadoc)
         *
         * @see com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang.Object,
         * com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
         */
        @Override
        public void serialize(final BusinessEntity businessEntity, final JsonGenerator jgen,
                final SerializerProvider provider)
                throws IOException, JsonProcessingException {

            if (businessEntity == null) {
                jgen.writeNull();
            } else {
                jgen.writeString(businessEntity.getBusinessId().getAsString());
            }
        }
    }

    /**
     * A Jackson (Key-){@link Serializers} that provides {@link BusinessEntityKeySerializer}
     * for Map Keys that extends {@link BusinessEntity}.
     */
    static class BusinessEntityKeySerializers extends Serializers.Base {

        /*
         * (non-Javadoc)
         *
         * @see com.fasterxml.jackson.databind.ser.Serializers.Base#findSerializer(com.fasterxml.jackson.databind.
         * SerializationConfig, com.fasterxml.jackson.databind.JavaType, com.fasterxml.jackson.databind.BeanDescription)
         */
        @Override
        public JsonSerializer<?> findSerializer(final SerializationConfig config, final JavaType type,
                final BeanDescription beanDesc) {
            Check.notNullArgument(type, "type");

            if (BusinessEntity.class.isAssignableFrom(type.getRawClass())) {
                return new BusinessEntityKeySerializer();
            } else {
                return null;
            }
        }
    }

    /**
     * The Class DealingCompanyKeySerializer.
     */
    @SuppressWarnings("rawtypes")
    static class BusinessEntityKeySerializer extends JsonSerializer<BusinessEntity> {

        /*
         * (non-Javadoc)
         *
         * @see com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang.Object,
         * com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
         */
        @Override
        public void serialize(final BusinessEntity entity, final JsonGenerator jgen, final SerializerProvider provider)
                throws IOException, JsonProcessingException {

            if (entity == null) {
                jgen.writeNull();
            } else {
                jgen.writeFieldName(entity.getBusinessId().getAsString());
            }
        }
    }

    /**
     * A Jackson {@link Deserializers} that provides {@link TypedBusinessEntityJsonDeserializer}
     * for every class that extends {@link BusinessEntity}.
     *
     * Provide the same {@link JsonDeserializer} for equal classes.
     */
    static class BusinessEntityDeserializers extends Deserializers.Base {

        /**
         * The {@link com.queomedia.persistence.extra.json.util.Memorizer} that hold and create the {@link TypedBusinessEntityJsonDeserializer}
         * for {@link BusinessEntity} classes.
         */
        @SuppressWarnings("rawtypes")
        private final Memorizer<Class, TypedBusinessEntityJsonDeserializer> deserializerMemorizer;

        /**
         * Instantiates a new business entity deserializes.
         *
         * @param generalLoaderDao the general loader dao
         */
        @SuppressWarnings("rawtypes")
        BusinessEntityDeserializers(final GeneralLoaderDao generalLoaderDao) {
            Check.notNullArgument(generalLoaderDao, "generalLoaderDao");

            // CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
            this.deserializerMemorizer = new Memorizer<>(new Computable<Class, TypedBusinessEntityJsonDeserializer>() {

                @Override
                @SuppressWarnings("unchecked")
                public TypedBusinessEntityJsonDeserializer compute(final Class argument) throws InterruptedException {
                    return new TypedBusinessEntityJsonDeserializer(argument, generalLoaderDao);
                }
            });
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * com.fasterxml.jackson.databind.deser.Deserializers.Base#findBeanDeserializer(com.fasterxml.jackson.databind
         * .JavaType, com.fasterxml.jackson.databind.DeserializationConfig,
         * com.fasterxml.jackson.databind.BeanDescription)
         */
        @Override
        public JsonDeserializer<?> findBeanDeserializer(final JavaType type, final DeserializationConfig config,
                final BeanDescription beanDesc)
                throws JsonMappingException {
            Check.notNullArgument(type, "type");

            if (BusinessEntity.class.isAssignableFrom(type.getRawClass())) {
                try {
                    return this.deserializerMemorizer.compute(type.getRawClass());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("interrupted", e);
                }
            } else {
                return null;
            }
        }
    }

    /**
     * Deserialize a Json String that represent a BusinessID, back to the Business entity, by loading it.
     *
     * @param <T> the generic type that implements {@link BusinessEntity}
     */
    static class TypedBusinessEntityJsonDeserializer<T extends BusinessEntity<T>> extends JsonDeserializer<T> {

        /** The entity class. */
        private final Class<T> entityClass;

        /** The general loader dao. */
        private final GeneralLoaderDao generalLoaderDao;

        /**
         * Instantiates a new typed business entity json deserializer.
         *
         * @param entityClass the entity class
         * @param generalLoaderDao the general loader dao
         */
        TypedBusinessEntityJsonDeserializer(final Class<T> entityClass, final GeneralLoaderDao generalLoaderDao) {
            Check.notNullArgument(entityClass, "entityClass");
            Check.notNullArgument(generalLoaderDao, "generalLoaderDao");

            this.entityClass = entityClass;
            this.generalLoaderDao = generalLoaderDao;
        }

        /*
         * (non-Javadoc)
         *
         * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser,
         * com.fasterxml.jackson.databind.DeserializationContext)
         */
        @Override
        public T deserialize(final JsonParser jp, final DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            String bidString = jp.readValueAs(String.class);

            if (bidString == null) {
                return null;
            } else {
                BusinessId<T> businessId = BusinessId.parse(bidString);
                return this.generalLoaderDao.getByBusinessId(businessId, this.entityClass);
            }
        }
    }

    /**
     * A Jackson {@link KeyDeserializers} that provides a {@link BusinessEntityKeyDeserializer}
     * for every class that extends {@link BusinessEntity}.
     */
    static class BusinessEntityKeyDeserializers implements KeyDeserializers {

        /** The general loader dao. */
        private final GeneralLoaderDao generalLoaderDao;

        /**
         * Instantiates a new dealing company key deserializers.
         *
         * @param generalLoaderDao the general loader dao
         */
        BusinessEntityKeyDeserializers(final GeneralLoaderDao generalLoaderDao) {
            Check.notNullArgument(generalLoaderDao, "generalLoaderDao");

            this.generalLoaderDao = generalLoaderDao;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * com.fasterxml.jackson.databind.deser.KeyDeserializers#findKeyDeserializer(com.fasterxml.jackson.databind.
         * JavaType, com.fasterxml.jackson.databind.DeserializationConfig,
         * com.fasterxml.jackson.databind.BeanDescription)
         */
        @Override
        public KeyDeserializer findKeyDeserializer(final JavaType type, final DeserializationConfig config,
                final BeanDescription beanDesc)
                throws JsonMappingException {
            Check.notNullArgument(type, "type");

            if (BusinessEntity.class.isAssignableFrom(type.getRawClass())) {
                return new BusinessEntityKeyDeserializer(type.getRawClass(), this.generalLoaderDao);
            } else {
                return null;
            }
        }
    }
    
    /**
     *  Deserialize a Json String that represent a BusinessID, back to the Business entity, by loading it.
     */
    static class BusinessEntityKeyDeserializer<T extends BusinessEntity<T>> extends KeyDeserializer {

        /** The entity class. */
        private final Class<T> entityClass;
        
        /** The general loader dao. */
        private final GeneralLoaderDao generalLoaderDao;
        
        /**
         * Instantiates a new typed business entity json deserializer.
         *
         * @param generalLoaderDao the general loader dao
         */
        BusinessEntityKeyDeserializer(final Class<T> entityClass, final GeneralLoaderDao generalLoaderDao) {
            Check.notNullArgument(entityClass, "entityClass");
            Check.notNullArgument(generalLoaderDao, "generalLoaderDao");
            
            this.entityClass = entityClass;
            this.generalLoaderDao = generalLoaderDao;
        }

        /*
         * (non-Javadoc)
         *
         * @see com.fasterxml.jackson.databind.KeyDeserializer#deserializeKey(java.lang.String,
         * com.fasterxml.jackson.databind.DeserializationContext)
         */
        @SuppressWarnings("unchecked")
        @Override
        public Object deserializeKey(final String key, final DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            if (key == null) {
                return null;
            } else {
                @SuppressWarnings("rawtypes")
                BusinessId<T> businessId = BusinessId.parse(key);
                return this.generalLoaderDao.getByBusinessId(businessId, this.entityClass);
            }
        }
    }
}
