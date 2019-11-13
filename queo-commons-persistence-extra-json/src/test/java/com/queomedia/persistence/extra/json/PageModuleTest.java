package com.queomedia.persistence.extra.json;

import java.io.IOException;
import java.util.Arrays;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PageModuleTest {

    @Test
    public void testSerialize() throws JsonGenerationException, JsonMappingException, IOException, JSONException {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new PageModule());

        String result = objectMapper
                .writeValueAsString(new PageImpl<>(Arrays.asList(1, 2), PageRequest.of(0, 10, Sort.by("test")), 2));

        String expected = ("{              "
                + "  'content' :[1,2],     "
                + "  'totalElements' : 2,  "                
                + "  'totalPages' : 1,     "
                + "  'pageSize' : 10,      "
                + "  'offset' : 0,         "
                + "  'currentPage' : 0    "                
                + "}                       ").replaceAll("'", "\"");
        JSONAssert.assertEquals(expected, result, JSONCompareMode.NON_EXTENSIBLE);
    }
    
    @Test
    public void testSerialize_includeDeprecatedFields() throws JsonGenerationException, JsonMappingException, IOException, JSONException {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new PageModule(true));

        String result = objectMapper
                .writeValueAsString(new PageImpl<>(Arrays.asList(1, 2), PageRequest.of(0, 10, Sort.by("test")), 2));

        String expected = ("{              "
                + "  'content' :[1,2],     "
                + "  'totalElements' : 2,  "                
                + "  'totalPages' : 1,     "
                + "  'pageSize' : 10,      "
                + "  'offset' : 0,         "
                + "  'currentPage' : 0,    "                
                + "  'number' : 0,         "
                + "  'size' : 10           "
                + "}                       ").replaceAll("'", "\"");
        JSONAssert.assertEquals(expected, result, JSONCompareMode.NON_EXTENSIBLE);
    }

}
