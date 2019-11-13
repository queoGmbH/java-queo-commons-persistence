package com.queomedia.persistence.extra.json;

import java.io.IOException;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * A Jackon Module for an Spring Date {@link Page} serializer that only serialize content and pageing information.
 * @author engelmann
 *
 */
public class PageModule extends SimpleModule {

    private static final long serialVersionUID = -2147712840723920450L;

    /**
     * C-tor that register the {@link PageJsonSerializer}.
     *
     * @param includeDeprecatedFields include the deprecated fields "number" and "size"
     *        ("currentPage" and "pageSize" are always included) 
     */
    public PageModule(boolean includeDeprecatedFields) {
        super("pageModule", new Version(1, 0, 0, null, "com.queomedia", null));

        this.addSerializer(Page.class, new PageJsonSerializer(includeDeprecatedFields));
    }
    
    /**
     * C-tor that register the {@link PageJsonSerializer} without deprecated fields.
     */
    public PageModule() {
        this(false);
    }

    /**
     * Serialize a Spring Data Page.
     * 
     * Serialize the following attributes:
     * <ul>
     *  <li>content: {@code page.getContent()}</li>
     *  <li>totalElements: {@code page.getTotalElements()}</li>
     *  <li>totalPages: {@code page.getTotalPages()}</li>
     *  <li>offset: {@code page.getSize() * page.getNumber()} (first offset is 0)</li>
     *  <li>currentPage: {@code number()}  the current page (first page is 0)</li>
     *  <li>number: only included when {@code includeDeprecatedFields==true} alias for "totalPages"</li>
     *  <li>size: only included when {@code includeDeprecatedFields==true} alias for "size"</li>
     * </ul>
     */
    @SuppressWarnings("rawtypes")
    static class PageJsonSerializer extends JsonSerializer<Page> {

        /**
         * Enable this flag to include the fields "number" and "size" that are deprecated because,
         * "currentPage" and "pageSize" are much better names.
         */
        private final boolean includeDeprecatedFields;

        /**
         * Instantiates a new page json serializer.
         *
         * @param includeDeprecatedFields include deprecated fields
         */
        public PageJsonSerializer(boolean includeDeprecatedFields) {        
            this.includeDeprecatedFields = includeDeprecatedFields;
        }

        /*
         * (non-Javadoc)
         *
         * @see com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang.Object,
         * com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
         */
        @Override
        public void serialize(final Page page, final JsonGenerator jsonGenerator, final SerializerProvider provider)
                throws IOException, JsonProcessingException {

            jsonGenerator.writeStartObject();
            jsonGenerator.writeObjectField("content", page.getContent());
            jsonGenerator.writeNumberField("totalElements", page.getTotalElements());
            jsonGenerator.writeNumberField("totalPages", page.getTotalPages());
            jsonGenerator.writeNumberField("pageSize", page.getSize());
            jsonGenerator.writeNumberField("offset", page.getSize() * page.getNumber());
            jsonGenerator.writeNumberField("currentPage", page.getNumber());
            if (includeDeprecatedFields) {
                jsonGenerator.writeNumberField("number", page.getNumber()); //number is deprecated, use currentPage instead
                jsonGenerator.writeNumberField("size", page.getSize()); //size is deprecated, use pageSize instead
            }
            jsonGenerator.writeEndObject();
        }

    }
}
