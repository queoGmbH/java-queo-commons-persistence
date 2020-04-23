package com.queomedia.persistence.schema.demo;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Entity
public class DemoEntityWithMinConstraint {

    @Id
    private int id;
    
    @Min(1)
    private int minValue;
    
    @Max(3)
    private int maxValue;
    
}
