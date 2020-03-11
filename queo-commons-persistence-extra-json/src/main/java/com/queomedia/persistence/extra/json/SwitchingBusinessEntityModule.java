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
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.Module.SetupContext;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.queomedia.commons.checks.Check;
import com.queomedia.persistence.BusinessEntity;
import com.queomedia.persistence.GeneralLoaderDao;
import com.queomedia.persistence.extra.json.BusinessEntityModule.BusinessEntityDeserializers;
import com.queomedia.persistence.extra.json.BusinessEntityModule.BusinessEntityJsonSerializer;
import com.queomedia.persistence.extra.json.BusinessEntityModule.BusinessEntitySerializers;

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

        SwitchingBusinessEntityJsonSerializers switchingBusinessEntityJsonSerializers = new SwitchingBusinessEntityJsonSerializers(
                switchingAnnotationScanner, new BusinessEntityJsonSerializer());

        context.addSerializers(new SwitchingBusinessEntitySerializers(switchingBusinessEntityJsonSerializers));
        context.addDeserializers(new BusinessEntityDeserializers(this.generalLoaderDao));
    }

    static class SwitchingBusinessEntitySerializers extends Serializers.Base {

        private final SwitchingBusinessEntityJsonSerializer switchingBusinessEntityJsonSerializer;

        public SwitchingBusinessEntitySerializers(
                final SwitchingBusinessEntityJsonSerializer switchingBusinessEntityJsonSerializer) {
            Check.notNullArgument(switchingBusinessEntityJsonSerializer, "switchingBusinessEntityJsonSerializer");
            this.switchingBusinessEntityJsonSerializer = switchingBusinessEntityJsonSerializer;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.fasterxml.jackson.databind.ser.Serializers.Base#findSerializer(com.
         * fasterxml.jackson.databind.SerializationConfig,
         * com.fasterxml.jackson.databind.JavaType,
         * com.fasterxml.jackson.databind.BeanDescription)
         */
        @Override
        public JsonSerializer<?> findSerializer(final SerializationConfig config, final JavaType type,
                final BeanDescription beanDesc) {
            Check.notNullArgument(type, "type");

            if (BusinessEntity.class.isAssignableFrom(type.getRawClass())) {
                return this.switchingBusinessEntityJsonSerializers;
            } else {
                return null;
            }
        }
    }

    static class SwitchingBusinessEntityJsonSerializer extends JsonSerializer<BusinessEntity> {
        private final SwitchingAnnotationScanner switchingAnnotationScanner;
        private final BusinessEntityModule.BusinessEntityJsonSerializer businessEntityJsonSerializer;

        public SwitchingBusinessEntityJsonSerializer(final SwitchingAnnotationScanner switchingAnnotationScanner,
                final BusinessEntityJsonSerializer businessEntityJsonSerializer) {
            Check.notNullArgument(switchingAnnotationScanner, "switchingAnnotationScanner");
            Check.notNullArgument(businessEntityJsonSerializer, "businessEntityJsonSerializer");

            this.switchingAnnotationScanner = switchingAnnotationScanner;
            this.businessEntityJsonSerializer = businessEntityJsonSerializer;
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
    
    //https://stackoverflow.com/questions/18313323/how-do-i-call-the-default-deserializer-from-a-custom-deserializer-in-jackson
    static class SwitchingBusinessEntityJsonDeserializer<T extends BusinessEntity<T>> extends JsonDeserializer<T> {
        private final SwitchingAnnotationScanner switchingAnnotationScanner;
        private final BusinessEntityModule.TypedBusinessEntityJsonDeserializer<T> typedBusinessEntityJsonDeserializer;

        private final Class<T> targetType;
        
        public SwitchingBusinessEntityJsonDeserializer(final SwitchingAnnotationScanner switchingAnnotationScanner,
                final BusinessEntityModule.TypedBusinessEntityJsonDeserializer typedBusinessEntityJsonDeserializer,
                final Class<T> targetType) {
            Check.notNullArgument(switchingAnnotationScanner, "switchingAnnotationScanner");
            Check.notNullArgument(typedBusinessEntityJsonDeserializer, "typedBusinessEntityJsonDeserializer");

            this.switchingAnnotationScanner = switchingAnnotationScanner;
            this.typedBusinessEntityJsonDeserializer = typedBusinessEntityJsonDeserializer;
            this.targetType = targetType;
        }

        @Override
        public T deserialize(final JsonParser jp, final DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            
            TreeNode tree = jp.readValueAsTree();
            
            if(tree.isObject()) {
                return tree.traverse(jp.getCodec()).readValueAs(targetType);
            } else {
                return typedBusinessEntityJsonDeserializer.deserialize(tree.traverse(), ctxt); 
            }
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
                    //TODO find field instead of use exception
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

                  //TODO find methos instead of use exception
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
