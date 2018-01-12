package com.queomedia.persistence.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.hamcrest.Matchers;
import org.junit.Test;

public class VersionTest {

    @Test
    public void testVersion() throws Exception {
        Version version = new Version("1.2.3");
        assertEquals(1, version.getMajor());
        assertEquals(2, version.getMinor());
        assertEquals(3, version.getBugfix());  
    }
    
    @Test
    public void testVersion_FinalPrefix() throws Exception {
        Version version = new Version("5.2.12.Final");
        assertEquals(5, version.getMajor());
        assertEquals(2, version.getMinor());
        assertEquals(12, version.getBugfix());     
    }
    
    @Test
    public void testVersion_4levels() throws Exception {
        Version version = new Version("1.2.3.4");
        assertEquals(1, version.getMajor());
        assertEquals(2, version.getMinor());
        assertEquals(3, version.getBugfix());     
    }
    
    @Test
    public void testVersion_Snapshot() throws Exception {
        Version version = new Version("1.2.3-SNAPSHOT");
        assertEquals(1, version.getMajor());
        assertEquals(2, version.getMinor());
        assertEquals(3, version.getBugfix());     
    }
    
    @Test
    public void testVersion_Comment() throws Exception {
        Version version = new Version("1.2.3 someWhiteSpaceSeparetedCommet");
        assertEquals(1, version.getMajor());
        assertEquals(2, version.getMinor());
        assertEquals(3, version.getBugfix());     
    }
    
    @Test
    public void testVersion_ExpliciteConstructor() throws Exception {
        assertEquals(new Version("1.2.3"), new Version(1, 2, 3));
    }

    @Test
    public void testIsGreatherOrEqualsThanIntInt() throws Exception {
        Version version = new Version("5.2.12.Final");
        
        assertEquals(true, version.isGreatherOrEqualsThan(5));
        assertEquals(true, version.isGreatherOrEqualsThan(5, 0));
        assertEquals(true, version.isGreatherOrEqualsThan(5, 1));
        assertEquals(true, version.isGreatherOrEqualsThan(5, 1, 13));
        assertEquals(true, version.isGreatherOrEqualsThan(5, 2));        
        assertEquals(true, version.isGreatherOrEqualsThan(5, 2, 11));
        assertEquals(true, version.isGreatherOrEqualsThan(5, 2, 12));
        
        assertEquals(false, version.isGreatherOrEqualsThan(5, 2, 13));
        assertEquals(false, version.isGreatherOrEqualsThan(5, 3));
        assertEquals(false, version.isGreatherOrEqualsThan(5, 3, 1));
        assertEquals(false, version.isGreatherOrEqualsThan(6));
        assertEquals(false, version.isGreatherOrEqualsThan(6, 1));
        assertEquals(false, version.isGreatherOrEqualsThan(6, 1, 1));
    }

    @Test
    public void testCompareToVersion() throws Exception {
        Version version = new Version("5.2.12.Final");
               
        assertEquals(1, version.compareTo(new Version("5.0.0")));
        assertEquals(1, version.compareTo(new Version("5.1.0")));
        assertEquals(1, version.compareTo(new Version("5.1.13")));
        assertEquals(1, version.compareTo(new Version("5.2.11")));
                
        assertEquals(0, version.compareTo(new Version("5.2.12")));        
        
        assertEquals(-1, version.compareTo(new Version("5.2.13")));
        assertEquals(-1, version.compareTo(new Version("5.3.0")));
        assertEquals(-1, version.compareTo(new Version("6.0.0")));
    }

    /** This test check almost that there is no exception. */
    @Test
    public void testHibernateVersion() throws Exception {
        Version hibernateVersion = Version.hibernateVersion();
                
        assertEquals(new Version(org.hibernate.Version.getVersionString()), hibernateVersion);        
        assertThat(hibernateVersion, Matchers.greaterThan(new Version("5.0.0")));
    }

    @Test
    public void testIsGreatherOrEqualsThanVersion() throws Exception {
        Version version = new Version("5.2.12.Final");
        
        assertTrue(version.isGreatherOrEqualsThan(new Version("5.0.0")));
        
    }

}
