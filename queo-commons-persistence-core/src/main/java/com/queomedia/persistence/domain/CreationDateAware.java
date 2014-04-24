package com.queomedia.persistence.domain;

import java.util.Date;

/**
 * The Interface CreationDateAware for all kind of Objects that have a creation date.
 */
public interface CreationDateAware {

    /**
     * Gets the creation date.
     *
     * @return the creation date
     */
    Date getCreationDate();
}
