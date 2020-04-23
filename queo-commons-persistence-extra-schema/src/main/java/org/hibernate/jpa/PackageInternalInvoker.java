package org.hibernate.jpa;

import java.util.Map;

import org.hibernate.jpa.HibernatePersistenceProvider;
import org.hibernate.jpa.boot.spi.EntityManagerFactoryBuilder;

/**
 * A class that is located in package org.hibernate.jpa in order to invoke protected methods.
 * @author engelmann
 *
 */
public class PackageInternalInvoker {

    public static EntityManagerFactoryBuilder getEntityManagerFactoryBuilderOrNull(
            HibernatePersistenceProvider hbpProvider, String persistenceUnitName, Map properties) {
        return hbpProvider.getEntityManagerFactoryBuilderOrNull(persistenceUnitName, properties);
    }
}
