package com.queomedia.persistence.schema.prettyprint;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import com.queomedia.commons.checks.Check;

/** The Pretty Printer does NOT modify the statements (add no separators ...) */
public class SqlPrettyPrinter {

    /**
     *  Pretty Print the given statement.
     *
     * @param statement the statement - must be contain no line breaks.
     * @return the string
     */
    public String formatLineStatement(final String statement) {
        Check.notNullArgument(statement, "statement");

        String formatted = StringUtils.strip(statement);

        String lowerCase = formatted.toLowerCase(Locale.ENGLISH);
        if (lowerCase.startsWith("create table")) {
            return formatCreateTableStatment(formatted);
        } else {
            return formatted;
        }
    }

    private static final String INDENTION = "    ";

    private String formatCreateTableStatment(final String statment) {
        String command = StringUtils.substringBefore(statment, "(");
        String fields = StringUtils.substringAfter(StringUtils.substringBeforeLast(statment, ")"), "(");
        String options = StringUtils.substringAfterLast(statment, ")");

        StringBuilder formattedFields = new StringBuilder();
        int bracketLevel = 0;
        //used to omit whitespaces between fields
        boolean waitForFirstNoneWhitspace = true;

        if (!fields.isEmpty()) {
            formattedFields.append(SqlPrettyPrinter.INDENTION);
        }
        for (char c : fields.toCharArray()) {
            switch (c) {
            case '(':
                bracketLevel++;
                formattedFields.append(c);
                break;
            case ')':
                bracketLevel--;
                formattedFields.append(c);
                break;
            case ',':
                formattedFields.append(c);
                if (bracketLevel == 0) {
                    waitForFirstNoneWhitspace = true;
                    formattedFields.append('\n');
                    formattedFields.append(SqlPrettyPrinter.INDENTION);
                }
                break;
            case ' ':
                if (!waitForFirstNoneWhitspace) {
                    formattedFields.append(c);
                }
                break;
            default:
                waitForFirstNoneWhitspace = false;
                formattedFields.append(c);
            }
        }

        return command + "(\n" + formattedFields.toString() + "\n)" + options;
    }

    public List<List<String>> groupStatments(final List<String> statements) {
        List<List<String>> result = new ArrayList<List<String>>();

        if (statements.size() > 0) {
            ArrayList<String> currentGroupStatements = new ArrayList<String>();
            Group currentGroup = Group.extractFromStatment(statements.get(0));
            result.add(currentGroupStatements);
            currentGroupStatements.add(statements.get(0));

            for (int i = 1; i < statements.size(); i++) {
                String statement = statements.get(i);
                Group group = Group.extractFromStatment(statement);
                if (group.equals(currentGroup)) {
                    currentGroupStatements.add(statement);
                } else {
                    currentGroupStatements = new ArrayList<String>();
                    currentGroup = group;
                    result.add(currentGroupStatements);
                    currentGroupStatements.add(statement);
                }
            }
        }
        return result;
    }

    public static class Group {
        private String type;

        private String table;

        public Group(final String type, final String table) {
            this.type = type;
            this.table = table;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + ((this.table == null) ? 0 : this.table.hashCode());
            result = (prime * result) + ((this.type == null) ? 0 : this.type.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Group other = (Group) obj;
            if (this.table == null) {
                if (other.table != null) {
                    return false;
                }
            } else if (!this.table.equals(other.table)) {
                return false;
            }
            if (this.type == null) {
                if (other.type != null) {
                    return false;
                }
            } else if (!this.type.equals(other.type)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Group [type=" + this.type + ", table=" + this.table + "]";
        }

        public static Group extractFromStatment(final String statment) {
            String[] parts = StringUtils.split(statment);
            if ((parts.length > 3)
                    && (parts[0].equalsIgnoreCase("create") || parts[0].equalsIgnoreCase("update") || parts[0]
                            .equalsIgnoreCase("alter")) && parts[1].equalsIgnoreCase("table")) {
                return new Group(parts[0], parts[2]);
            } else {
                return new Group("unknown", "unknown");
            }
        }
    }
}
