package com.queomedia.persistence.schema.demo;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class DemoEntiyWithRelation {

    @Id
    private long id;

    @ManyToOne
    private DemoEntity demoEntity;
}
