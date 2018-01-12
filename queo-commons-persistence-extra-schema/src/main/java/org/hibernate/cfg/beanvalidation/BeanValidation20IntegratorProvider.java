package org.hibernate.cfg.beanvalidation;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.hibernate.integrator.spi.Integrator;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.hibernate.jpa.boot.spi.IntegratorProvider;

/** A {@link IntegratorProvider} to programmatic integrate the {@link BeanValidationIntegrator}. */
public class BeanValidation20IntegratorProvider implements IntegratorProvider {

    @Override
    public List<Integrator> getIntegrators() {
        return Arrays.asList(new BeanValidation20Integrator());
    }

    public static void addToConfiguration(final Map<String, Object> props) {
        props.put(EntityManagerFactoryBuilderImpl.INTEGRATOR_PROVIDER, new BeanValidation20IntegratorProvider());
    }

}
