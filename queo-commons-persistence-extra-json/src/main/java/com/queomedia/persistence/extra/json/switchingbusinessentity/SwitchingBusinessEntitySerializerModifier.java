package com.queomedia.persistence.extra.json.switchingbusinessentity;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.queomedia.persistence.BusinessEntity;
import com.queomedia.persistence.extra.json.BusinessEntityModule;
import com.queomedia.persistence.extra.json.BusinessEntityModule.BusinessEntityJsonSerializer;

public class SwitchingBusinessEntitySerializerModifier extends BeanSerializerModifier {

    private final SwitchingAnnotationScanner switchingAnnotationScanner;

    private final BusinessEntityModule.BusinessEntityJsonSerializer businessEntityJsonSerializer;

    public SwitchingBusinessEntitySerializerModifier(final SwitchingAnnotationScanner switchingAnnotationScanner,
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
