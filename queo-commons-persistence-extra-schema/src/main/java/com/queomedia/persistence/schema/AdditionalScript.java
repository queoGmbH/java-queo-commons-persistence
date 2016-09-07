package com.queomedia.persistence.schema;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

public class AdditionalScript {

    /** The split maker in the ddl addtional file. */
    private static final String SPLIT_MARKER = "/* <<add at the beginning hibernate, add at the end hibernate>> */";

    private final String prePart;

    private final String postPart;

    public AdditionalScript(String prePart, String postPart) {
        this.prePart = prePart;
        this.postPart = postPart;
    }

    public String getPrePart() {
        return prePart;
    }

    public String getPostPart() {
        return postPart;
    }

    /**
     * Load the additional script for the given {@link Dialect}.
     * 
     * The additional script must match this file name/location:
     * {@code src/main/resources/ddlAdditional_<lower case dialect name>.sql}
     * When there is no such file, than the loaded {@link AdditionalScript} will be empty.
     *
     * @param dialect the dialect
     * @return the loaded additional script
     */
    public static AdditionalScript load(Dialect dialect) {
        String fileName = "src/main/resources/ddlAdditional_" + dialect.name().toLowerCase() + ".sql";
        File additionalFile = new File(fileName);
        

        if (additionalFile.exists()) {
            System.out.println("extending additional script from file: " + additionalFile.getAbsolutePath());
            try {
                String ddlAdditional = FileUtils.readFileToString(additionalFile, "utf-8");
                String[] parts = ddlAdditional.split(Pattern.quote(SPLIT_MARKER));
                if (parts.length != 2) {
                    throw new RuntimeException("not exct 1 split marker not found `" + SPLIT_MARKER + "`");
                }

                return new AdditionalScript(parts[0], parts[1]);
            } catch (IOException e) {
                throw new RuntimeException("Error while loading additional script `" + additionalFile.getAbsolutePath()
                        + "`", e);
            }
        } else {
            System.out.println("no additional script file found: " + additionalFile.getAbsolutePath());
            return new AdditionalScript("", "");
        }
    }
}
