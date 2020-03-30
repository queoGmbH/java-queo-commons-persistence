package com.queomedia.persistence.extra.json.complexswitchingbusinessentity;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.queomedia.commons.checks.Check;
import com.queomedia.persistence.BusinessEntity;
import com.queomedia.persistence.GeneralLoaderDao;
import com.queomedia.persistence.extra.json.BusinessEntityModule;
import com.queomedia.persistence.extra.json.SwitchingAnnotationScanner;

class ComplexSwitchingBusinessEntityDeserializerModfier extends BeanDeserializerModifier {

    private final SwitchingAnnotationScanner switchingAnnotationScanner;

    private final GeneralLoaderDao generalLoaderDao;

    public ComplexSwitchingBusinessEntityDeserializerModfier(final SwitchingAnnotationScanner switchingAnnotationScanner,
            final GeneralLoaderDao generalLoaderDao) {
        Check.notNullArgument(generalLoaderDao, "generalLoaderDao");
        Check.notNullArgument(switchingAnnotationScanner, "switchingAnnotationScanner");

        this.switchingAnnotationScanner = switchingAnnotationScanner;
        this.generalLoaderDao = generalLoaderDao;
    }

    @Override
    public JsonDeserializer<?> modifyDeserializer(final DeserializationConfig config, final BeanDescription beanDesc,
            final JsonDeserializer<?> deserializer) {
        if (BusinessEntity.class.isAssignableFrom(beanDesc.getBeanClass())) {
            return new ComplexSwitchingBusinessEntityDeserializer(this.switchingAnnotationScanner,
                    new BusinessEntityModule.TypedBusinessEntityJsonDeserializer(beanDesc.getBeanClass(),
                            this.generalLoaderDao),
                    (BeanDeserializerBase) deserializer);
        } else {
            return deserializer;
        }
    }
}
