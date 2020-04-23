package com.queomedia.persistence.schema.statmentpostprocessor;

import java.util.List;

public interface StatementPostProcessor {

    List<String> postProcess(String sqlStatment);
}
