package com.queomedia.persistence.schema.demo;

import javax.persistence.Embeddable;
import javax.validation.constraints.Size;

@Embeddable
public class DemoEmbeddable {

    @Size(max=100)
    private String embeddedString;
}
