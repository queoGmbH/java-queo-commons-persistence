package com.queomedia.persistence.extra.json;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Optional;

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
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.queomedia.commons.checks.Check;
import com.queomedia.persistence.BusinessEntity;
import com.queomedia.persistence.BusinessId;
import com.queomedia.persistence.GeneralLoaderDao;
import com.queomedia.persistence.extra.json.BusinessEntityModule.BusinessEntityDeserializers;
import com.queomedia.persistence.extra.json.BusinessEntityModule.BusinessEntityJsonSerializer;
import com.queomedia.persistence.extra.json.BusinessEntityModule.BusinessEntitySerializers;
import com.queomedia.persistence.extra.json.BusinessEntityModule.TypedBusinessEntityJsonDeserializer;
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

//        SwitchingBusinessEntityJsonSerializer switchingBusinessEntityJsonSerializer = new SwitchingBusinessEntityJsonSerializer(
//                switchingAnnotationScanner, new BusinessEntityJsonSerializer());
//        context.addDeserializers(new SwitchingBusinessEntityDeserializers(this.generalLoaderDao));

        context.addBeanSerializerModifier(new SwitchingBusinessEntitSerializerModifier(switchingAnnotationScanner,
                new BusinessEntityJsonSerializer()));
        context.addBeanDeserializerModifier(new SwitchingBusinessEntityDeserializerModfier(this.generalLoaderDao));
    }

//    static class SwitchingBusinessEntitySerializers extends Serializers.Base {
//
//        private final SwitchingBusinessEntityJsonSerializer switchingBusinessEntityJsonSerializer;
//
//        public SwitchingBusinessEntitySerializers(
//                final SwitchingBusinessEntityJsonSerializer switchingBusinessEntityJsonSerializer) {
//            Check.notNullArgument(switchingBusinessEntityJsonSerializer, "switchingBusinessEntityJsonSerializer");
//            this.switchingBusinessEntityJsonSerializer = switchingBusinessEntityJsonSerializer;
//        }
//
//        /*
//         * (non-Javadoc)
//         * 
//         * @see com.fasterxml.jackson.databind.ser.Serializers.Base#findSerializer(com.
//         * fasterxml.jackson.databind.SerializationConfig,
//         * com.fasterxml.jackson.databind.JavaType,
//         * com.fasterxml.jackson.databind.BeanDescription)
//         */
//        @Override
//        public JsonSerializer<?> findSerializer(final SerializationConfig config, final JavaType type,
//                final BeanDescription beanDesc) {
//            Check.notNullArgument(type, "type");
//
//            if (BusinessEntity.class.isAssignableFrom(type.getRawClass())) {
//                return this.switchingBusinessEntityJsonSerializer;
//            } else {
//                return null;
//            }
//        }
//    }

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

            if (beanDesc.getBeanClass().isInstance(BusinessEntity.class)) {
                return new SwitchingBusinessEntityJsonSerializer(switchingAnnotationScanner,
                        businessEntityJsonSerializer, (JsonSerializer<Object>) serializer);
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
        public void serialize(final BusinessEntity businessEntity, final JsonGenerator jgen,
                final SerializerProvider provider) throws IOException {
            if (switchingAnnotationScanner.isSwitchEnabled(jgen.getOutputContext())) {
                businessEntityJsonSerializer.serialize(businessEntity, jgen, provider);
            } else {
                provider.defaultSerializeValue(businessEntity, jgen);
            }
        }
    }

    static class SwitchingBusinessEntityDeserializerModfier extends BeanDeserializerModifier {

        private final GeneralLoaderDao generalLoaderDao;

        public SwitchingBusinessEntityDeserializerModfier(com.queomedia.persistence.GeneralLoaderDao generalLoaderDao) {
            Check.notNullArgument(generalLoaderDao, "generalLoaderDao");
            this.generalLoaderDao = generalLoaderDao;
        }

//        @Override
//        public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc,
//                JsonDeserializer<?> deserializer) {
//            if (BusinessEntity.class.isAssignableFrom(beanDesc.getBeanClass())) {
//                return new SwitchingBusinessEntityJsonDeserializer(
//                        new BusinessEntityModule.TypedBusinessEntityJsonDeserializer(beanDesc.getBeanClass(), generalLoaderDao),
//                        beanDesc.getBeanClass(),
//                        deserializer);
//            } else {
//                return deserializer;
//            }
//        }

        @Override
        public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc,
                JsonDeserializer<?> deserializer) {
            if (BusinessEntity.class.isAssignableFrom(beanDesc.getBeanClass())) {
                return new SwitchingBusinessEntityDeserializer2(
                         new BusinessEntityModule.TypedBusinessEntityJsonDeserializer(beanDesc.getBeanClass(), generalLoaderDao),
                        beanDesc.getBeanClass(), deserializer, generalLoaderDao);
            } else {
                return deserializer;
            }
        }
    }

    public static class SwitchingBusinessEntityDeserializer2 extends StdDeserializer<BusinessEntity> implements ResolvableDeserializer
    //, ContextualDeserializer 
    {

        private final JsonDeserializer<?> defaultDeserializer;
        
        private BusinessEntityModule.TypedBusinessEntityJsonDeserializer typedBusinessEntityJsonDeserializer;
        private Class beanClazz;
        
        private GeneralLoaderDao generalLoaderDao;

        public SwitchingBusinessEntityDeserializer2(BusinessEntityModule.TypedBusinessEntityJsonDeserializer typedBusinessEntityJsonDeserializer,  
                final Class beanClazz,
                JsonDeserializer<?> defaultDeserializer,
                GeneralLoaderDao generalLoaderDao) {
            super(BusinessEntity.class);
            
            this.typedBusinessEntityJsonDeserializer = typedBusinessEntityJsonDeserializer;
            this.beanClazz = beanClazz;
            this.defaultDeserializer = defaultDeserializer;
            this.generalLoaderDao = generalLoaderDao;
        }

        @Override
        public BusinessEntity deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
//            User deserializedUser = (User) defaultDeserializer.deserialize(jp, ctxt);
            
//            TreeNode tree = jp.readValueAsTree();
            
//            ((ResolvableDeserializer) defaultDeserializer).resolve(ctxt);
            
//            TreeNode tree = jp.getCodec().readTree(jp);
//            TreeNode tree2 = jp.getCodec().readTree(jp);
//            System.out.println(tree.toString());
//            
//            if (tree.isObject()) {
                
//                defaultDeserializer.
            JsonToken currentToken = jp.currentToken();
            System.out.println(currentToken);
            if (currentToken == JsonToken.VALUE_STRING) {
                String bidString = jp.readValueAs(String.class);
                System.out.println("bidString: " + bidString);
                System.out.println("beanClazz: " + beanClazz);
                return this.generalLoaderDao.getByBusinessId(BusinessId.parse(bidString), beanClazz);
            } else {
                return (BusinessEntity) defaultDeserializer.deserialize(jp, ctxt);
            }
//                return (BusinessEntity)tree.traverse(jp.getCodec()).readValueAs(beanClazz);
                
//            } else {
//                String readValueAs = jp.readValueAs(String.class);
//                System.out.println("bid = `"+readValueAs+"`");
//                return null;
////                return typedBusinessEntityJsonDeserializer.deserialize(jp, ctxt);
//            }
            
            
//            return (BusinessEntity) defaultDeserializer.deserialize(jp, ctxt);
            
            
//          TreeNode tree = jp.readValueAsTree();
//
//          
//          
//          if (tree.isObject()) {
////              return (BusinessEntity) defaultDeserializer.deserialize(jp, ctxt);
////                      tree.traverse(jp.getCodec()).readValueAs(targetType);
////              System.out.println("is object");
//              
////              tree.
//              
//              return (BusinessEntity) defaultDeserializer.deserialize(tree.traverse(jp.getCodec()), ctxt);
//          } else {              
//              System.out.println("is number");
//              return typedBusinessEntityJsonDeserializer.deserialize(tree.traverse(jp.getCodec()), ctxt);
//          }
//            
////            return (BusinessEntity) defaultDeserializer.deserialize(jp, ctxt);
//            // Special logic
//
////            return deserializedUser;
        }

        // for some reason you have to implement ResolvableDeserializer when modifying
        // BeanDeserializer
        // otherwise deserializing throws JsonMappingException??
        @Override
        public void resolve(DeserializationContext ctxt) throws JsonMappingException {
            ((ResolvableDeserializer) defaultDeserializer).resolve(ctxt);
        }

//        @Override
//        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
//                throws JsonMappingException {
//            System.out.println("property" + property);
//            return ((ContextualDeserializer) defaultDeserializer).createContextual(ctxt, property);
//        }
    }

//    /**
//     * A Jackson {@link Deserializers} that provides
//     * {@link TypedBusinessEntityJsonDeserializer} for every class that extends
//     * {@link BusinessEntity}.
//     *
//     * Provide the same {@link JsonDeserializer} for equal classes.
//     */
//    static class SwitchingBusinessEntityDeserializers extends Deserializers.Base {
//
//        /**
//         * The {@link com.queomedia.persistence.extra.json.util.Memorizer} that hold and
//         * create the {@link TypedBusinessEntityJsonDeserializer} for
//         * {@link BusinessEntity} classes.
//         */
//        @SuppressWarnings("rawtypes")
//        private final Memorizer<Class, SwitchingBusinessEntityJsonDeserializer> deserializerMemorizer;
//
//        /**
//         * Instantiates a new business entity deserializes.
//         *
//         * @param generalLoaderDao the general loader dao
//         */
//        @SuppressWarnings("rawtypes")
//        SwitchingBusinessEntityDeserializers(final GeneralLoaderDao generalLoaderDao) {
//            Check.notNullArgument(generalLoaderDao, "generalLoaderDao");
//
//            // CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
//            this.deserializerMemorizer = new Memorizer<>(
//                    new Computable<Class, SwitchingBusinessEntityJsonDeserializer>() {
//
//                        @Override
//                        @SuppressWarnings("unchecked")
//                        public SwitchingBusinessEntityJsonDeserializer compute(final Class argument)
//                                throws InterruptedException {
//                            return new SwitchingBusinessEntityJsonDeserializer(
//                                    new BusinessEntityModule.TypedBusinessEntityJsonDeserializer(argument,
//                                            generalLoaderDao),
//                                    argument);
//                        }
//                    });
//        }
//
//        /*
//         * (non-Javadoc)
//         *
//         * @see
//         * com.fasterxml.jackson.databind.deser.Deserializers.Base#findBeanDeserializer(
//         * com.fasterxml.jackson.databind .JavaType,
//         * com.fasterxml.jackson.databind.DeserializationConfig,
//         * com.fasterxml.jackson.databind.BeanDescription)
//         */
//        @Override
//        public JsonDeserializer<?> findBeanDeserializer(final JavaType type, final DeserializationConfig config,
//                final BeanDescription beanDesc) throws JsonMappingException {
//            Check.notNullArgument(type, "type");
//
//            if (BusinessEntity.class.isAssignableFrom(type.getRawClass())) {
//                try {
//                    return this.deserializerMemorizer.compute(type.getRawClass());
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                    throw new RuntimeException("interrupted", e);
//                }
//            } else {
//                return null;
//            }
//        }
//    }

    // https://stackoverflow.com/questions/18313323/how-do-i-call-the-default-deserializer-from-a-custom-deserializer-in-jackson
    static class SwitchingBusinessEntityJsonDeserializer<T extends BusinessEntity<T>> extends JsonDeserializer<T> {

        private final BusinessEntityModule.TypedBusinessEntityJsonDeserializer<T> typedBusinessEntityJsonDeserializer;

        private final Class<T> targetType;

        private final JsonDeserializer<?> defaultDeserializer;

        public SwitchingBusinessEntityJsonDeserializer(
                final BusinessEntityModule.TypedBusinessEntityJsonDeserializer typedBusinessEntityJsonDeserializer,
                final Class<T> targetType, final JsonDeserializer<?> defaultDeserializer) {
            Check.notNullArgument(typedBusinessEntityJsonDeserializer, "typedBusinessEntityJsonDeserializer");
            Check.notNullArgument(targetType, "targetType");
            Check.notNullArgument(defaultDeserializer, "defaultDeserializer");

            this.typedBusinessEntityJsonDeserializer = typedBusinessEntityJsonDeserializer;
            this.targetType = targetType;
            this.defaultDeserializer = defaultDeserializer;
        }

        @Override
        public T deserialize(final JsonParser jp, final DeserializationContext ctxt)
                throws IOException, JsonProcessingException {

            return (T) defaultDeserializer.deserialize(jp, ctxt);

//            TreeNode tree = jp.readValueAsTree();
//
//            if (tree.isObject()) {
//                return (T) defaultDeserializer.deserialize(tree.traverse(jp.getCodec()), ctxt);
////                        tree.traverse(jp.getCodec()).readValueAs(targetType);
//            } else {
//                return typedBusinessEntityJsonDeserializer.deserialize(tree.traverse(jp.getCodec()), ctxt);
//            }
        }

    }

    static class SwitchingAnnotationScanner {

        boolean isSwitchEnabled(final JsonStreamContext context) {
            Optional<SwitchingBusinessEntityAnnotation> annotation = findSwitchingBusinessEntityAnnotation(context);
            return annotation.isPresent();
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
                                .of(field.getAnnotation(SwitchingBusinessEntityAnnotation.class));
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
                                .of(method.getAnnotation(SwitchingBusinessEntityAnnotation.class));
                        if (methodAnnotation.isPresent()) {
                            return methodAnnotation;
                        }
                    } catch (NoSuchMethodException e) {
                    }
                }
                Optional<SwitchingBusinessEntityAnnotation> classAnnotation = Optional
                        .of(currentClass.getAnnotation(SwitchingBusinessEntityAnnotation.class));
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
