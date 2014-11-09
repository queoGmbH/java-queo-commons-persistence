package generator;

import com.queomedia.persistence.schema.Dialect;
import com.queomedia.persistence.schema.SchemaGeneratorJpa;

/**
 * Start the {@link SchemaGenerator} to create an DDL Schema for this Project.
 * It will be stored in file {@link #DDL_FILENAME}.
 */
public final class SchemaGeneratorRunner {

    /**
     * The file name of the generated ddl.
     */
    public static final String DDL_FILENAME = "ddl_mysql.sql";


    /** Use only the {@link #main(String[])} method. */
    private SchemaGeneratorRunner() {
        super();
    }

    /**
     * The main method.
     *
     * @param args the args
     *
     * @throws Exception the exception
     */
    public static void main(final String[] args) throws Exception {
        new SchemaGeneratorJpa("ddl_mysql.sql", Dialect.MYSQL).generate("persistenceUnit");
    }

}
