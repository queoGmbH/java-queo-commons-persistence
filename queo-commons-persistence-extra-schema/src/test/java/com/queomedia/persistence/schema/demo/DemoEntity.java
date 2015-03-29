package com.queomedia.persistence.schema.demo;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.Valid;
import javax.validation.constraints.Size;

@Entity
public class DemoEntity {

    @Id
    private int id;

    @Size(max = 100)
    private String normalString;

    /**
     * To get the JSR 303 Annotations working on embeddeds for Schema generation,
     * @Valid must beend added: https://hibernate.atlassian.net/browse/ANN-652
     */
    @Embedded
    @Valid
    private DemoEmbeddable demoEmbeddable;
}
