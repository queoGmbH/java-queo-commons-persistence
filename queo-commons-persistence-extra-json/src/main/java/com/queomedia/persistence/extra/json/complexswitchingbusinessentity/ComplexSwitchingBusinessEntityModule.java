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
}
