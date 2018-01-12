package com.queomedia.persistence.schema;

import com.queomedia.commons.checks.Check;

/**
 * Value object to describe an version consist of major.minor.bugfix.
 * @author engelmann
 */
public class Version implements Comparable<Version> {

    /**
     * The major version.
     */
    private final int major;

    /**
     * The minor part of the version.
     */
    private final int minor;

    /**
     * The bufix level part of the version.
     */
    private final int bugfix;

    /**
     * Build a version from a version string of the format "major.minor.bugfix".
     *
     * If the given version string contains more inforamtion after the bugfix level,
     * for example "-SNAPSHOT", ".final" or even more detailed version numbersn then this is ignored.
     *
     * @param versionString the version that is pared and stored in this object as major, minor and bugfix
     */
    public Version(final String versionString) {
        Check.notNullArgument(versionString, "versionString");

        try {
            String[] parts = versionString.split("\\.");
            this.major = Integer.parseInt(parts[0]);
            this.minor = Integer.parseInt(parts[1]);
            this.bugfix = Integer.parseInt(substringBefore(substringBefore(parts[2], " "), "-"));
        } catch (RuntimeException e) {
            throw new RuntimeException("error while parsing version string `" + versionString + "`");
        }
    }
   
    /**
     * Instantiates a new version.
     *
     * @param major the major
     * @param minor the minor
     * @param bugfix the bugfix
     */
    public Version(int major, int minor, int bugfix) {    
        this.major = major;
        this.minor = minor;
        this.bugfix = bugfix;
    }


    private static String substringBefore(final String s, final String separator) {
        int separatorStartIndex = s.indexOf(separator);
        if (separatorStartIndex == -1) {
            //no occurence
            return s;
        } else {
            return s.substring(0, separatorStartIndex);
        }
    }

    public int getMajor() {
        return this.major;
    }

    public int getMinor() {
        return this.minor;
    }

    public int getBugfix() {
        return this.bugfix;
    }

    /**
     * Compare this version with the given major version.
     *
     * @param major the major
     * @return the value {@code 0} if {@code this == major};
     *      a value less than {@code 0} if {@code this < major}; and
     *      a value greater than {@code 0} if {@code this > major}
     */
    public int compareTo(final int major) {
        return Integer.compare(this.major, major);
    }

    public boolean isGreatherOrEqualsThan(final int major) {
        return compareTo(major) >= 0;
    }

    /**
     * Compare this version with the given major and minor version.
     *
     * @param major the major
     * @param minor the minor
     * @return the value {@code 0} if {@code this == major.minor};
     *      a value less than {@code 0} if {@code this < major.minor}; and
     *      a value greater than {@code 0} if {@code this > major.minor}
     */
    public int compareTo(final int major, final int minor) {
        int majorCompareResult = compareTo(major);
        if (majorCompareResult < 0) {
            return -1;
        } else if (majorCompareResult == 0) {
            return Integer.compare(this.minor, minor);
        } else {
            return 1;
        }
    }

    public boolean isGreatherOrEqualsThan(final int major, final int minor) {
        return compareTo(major, minor) >= 0;
    }

    /**
     * Compare this version with the given major, minor and bugfix version.
     *
     * @param major the major
     * @param minor the minor
     * @param bugfix the bugfix
     * @return the value {@code 0} if {@code this == major.minor.bugfix};
     *      a value less than {@code 0} if {@code this < major.minor.bugfix}; and
     *      a value greater than {@code 0} if {@code this > major.minor.bugfix}
     */
    public int compareTo(final int major, final int minor, final int bugfix) {
        int majorMinorCompareResult = compareTo(major, minor);
        if (majorMinorCompareResult < 0) {
            return -1;
        } else if (majorMinorCompareResult == 0) {
            return Integer.compare(this.bugfix, bugfix);
        } else {
            return 1;
        }
    }

    public boolean isGreatherOrEqualsThan(final int major, final int minor, final int bugfix) {
        return compareTo(major, minor, bugfix) >= 0;
    }
    
    public boolean isGreatherOrEqualsThan(Version otherVersion) {
        return this.compareTo(otherVersion) >= 0;
    }

    /**
     * Compare this version with the other version.
     *
     * @param other the other
     * @return the value {@code 0} if {@code this == other};
     *      a value less than {@code 0} if {@code this < other}; and
     *      a value greater than {@code 0} if {@code this > other}
     */
    @Override
    public int compareTo(final Version other) {
        Check.notNullArgument(other, "other");

        return this.compareTo(other.major, other.minor, other.bugfix);

    }

    @Override
    public String toString() {
        return "Version [major=" + this.major + ", minor=" + this.minor + ", bugfix=" + this.bugfix + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + this.bugfix;
        result = (prime * result) + this.major;
        result = (prime * result) + this.minor;
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
        Version other = (Version) obj;
        if (this.bugfix != other.bugfix) {
            return false;
        }
        if (this.major != other.major) {
            return false;
        }
        if (this.minor != other.minor) {
            return false;
        }
        return true;
    }
    
    public static final Version hibernateVersion() {
        return new Version(org.hibernate.Version.getVersionString());
    }
}
