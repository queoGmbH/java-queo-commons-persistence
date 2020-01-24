package com.queomedia.persistence.schema.prettyprint;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import com.queomedia.commons.checks.Check;

/** The Pretty Printer does NOT modify the statements (add no separators ...) */
public class SqlPrettyPrinter {

    private final String delimiter;

    public SqlPrettyPrinter(final String delimiter) {    
        this.delimiter = delimiter;
    }

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
        if (lowerCase.startsWith("begin execute immediate '") && lowerCase.endsWith("end if; end;" + delimiter)) {
            return formatCreateIfExistsTableStatment(formatted);
        }
        if (lowerCase.startsWith("create table")) {
            return formatCreateTableStatment(formatted, 0, 4, 0);
        } else {
            return formatted;
        }
    }

    private String formatCreateIfExistsTableStatment(final String statement) {
        String pre = StringUtils.substringBefore(statement, "'");
        String post = StringUtils.substringAfterLast(statement, "';");
        if (!pre.isEmpty() && !post.isEmpty()) {
            String main = StringUtils.substringBetween(statement, pre + "'", "';" + post);

            String formattedMain;
            if (main.toLowerCase(Locale.ENGLISH).startsWith("create table")) {
                formattedMain = formatCreateTableStatment(StringUtils.strip(main), 0, 4, 2);
            } else {
                formattedMain = main;
            }

            boolean multiLineMain = formattedMain.contains("\n");
            if (multiLineMain) {
                return pre.trim() + "\n  '" + formattedMain + "';\n" + post.trim();
            } else {
                return pre.trim() + " '" + formattedMain + "'; " + post.trim();
            }

        } else {
            return formatCreateTableStatment(statement, 0, 4, 0);
        }
    }

    private String formatCreateTableStatment(final String statment, int indentLevelCommand, int indentLevelFields,
            int indentLevelOptions) {
        String command = StringUtils.substringBefore(statment, "(");
        String fields = StringUtils.substringAfter(StringUtils.substringBeforeLast(statment, ")"), "(");
        String options = StringUtils.substringAfterLast(statment, ")");

        StringBuilder formattedFields = new StringBuilder();
        int bracketLevel = 0;
        //used to omit whitespaces between fields
        boolean waitForFirstNoneWhitspace = true;

        if (!fields.isEmpty()) {
            formattedFields.append(indent(indentLevelFields));
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
                    formattedFields.append(indent(indentLevelFields));
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

        return indent(indentLevelCommand) + command + "(\n" + formattedFields.toString() + "\n"
                + indent(indentLevelOptions) + ")" + options;
    }

    private static String indent(int indentLevel) {
        //in java11 use " ".repeat(indentLevel);
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < indentLevel; i++) {
            b.append(" ");
        }
        return b.toString();
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
            final String[] parts;
            if (statment.toLowerCase(Locale.ENGLISH).startsWith("begin execute immediate '")) {
                parts = StringUtils.split(StringUtils.substringAfter(statment, "begin execute immediate '"));
            } else {
                parts = StringUtils.split(statment);
            }

            if ((parts.length > 3) && (parts[0].equalsIgnoreCase("create") || parts[0].equalsIgnoreCase("update")
                    || parts[0].equalsIgnoreCase("alter")) && parts[1].equalsIgnoreCase("table")) {
                return new Group(parts[0], parts[2]);
            } else {
                return new Group("unknown", "unknown");
            }
        }
    }
}
