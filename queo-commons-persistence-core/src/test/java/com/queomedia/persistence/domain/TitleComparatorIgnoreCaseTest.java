package com.queomedia.persistence.domain;

import static org.junit.Assert.*;

import org.junit.Test;

public class TitleComparatorIgnoreCaseTest {

    @Test
    public void testTitleComparatorIgnoreCase_sameBigAndSmallLetter() {
        
        TiltedClass tiltedClass1 = new TiltedClass("A");
        TiltedClass tiltedClass2 = new TiltedClass("a");
        
        TitleComparatorIgnoreCase<TiltedClass> comparator = TitleComparatorIgnoreCase
                .getInstance();
        
        int result = comparator.compare(tiltedClass1, tiltedClass2);
        /** 'A' and 'a' are equals */
        assertEquals(0, result);
    }
    
    @Test
    public void testTitleComparatorIgnoreCase_differentBigAndSmallLetters() {
        
        TiltedClass tiltedClass1 = new TiltedClass("a");
        TiltedClass tiltedClass2 = new TiltedClass("Z");
        
        TitleComparatorIgnoreCase<TiltedClass> comparator = TitleComparatorIgnoreCase
                .getInstance();
        
        int result = comparator.compare(tiltedClass1, tiltedClass2);
        /** 'a' is lower as 'Z' */
        assertTrue(result < 0);
    }
    
    @Test
    public void testTitleComparatorIgnoreCase_specialLettersAreAfertZ() {
        
        TiltedClass tiltedClass1 = new TiltedClass("ä");
        TiltedClass tiltedClass2 = new TiltedClass("z");
        
        TitleComparatorIgnoreCase<TiltedClass> comparator = TitleComparatorIgnoreCase
                .getInstance();
        
        int result = comparator.compare(tiltedClass1, tiltedClass2);
        /** 'ä' is after 'z' */
        assertTrue(result > 0);
    }

    public static class TiltedClass implements Titled {

        private String title;

        public TiltedClass(final String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

    }

}
