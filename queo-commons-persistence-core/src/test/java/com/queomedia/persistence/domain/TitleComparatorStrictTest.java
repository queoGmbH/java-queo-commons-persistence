package com.queomedia.persistence.domain;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TitleComparatorStrictTest {

    @Test
    public void testTitleComparatorStrict_sameBigAndSmallLetter() {
        
        TiltedClass tiltedClass1 = new TiltedClass("A");
        TiltedClass tiltedClass2 = new TiltedClass("a");
        
        TitleComparatorStrict<TiltedClass> comparator = TitleComparatorStrict
                .getInstance();
        
        int result = comparator.compare(tiltedClass1, tiltedClass2);
        /** by using String.compareTo 'A' is lower 'a' */
        assertTrue(result < 0);
    }
    
    @Test
    public void testTitleComparatorStrict_differentBigAndSmallLetters() {
        
        TiltedClass tiltedClass1 = new TiltedClass("a");
        TiltedClass tiltedClass2 = new TiltedClass("Z");
        
        TitleComparatorStrict<TiltedClass> comparator = TitleComparatorStrict
                .getInstance();
        
        int result = comparator.compare(tiltedClass2, tiltedClass1);
        /** by using String.compareTo 'Z' is lower as 'a' */
        assertTrue(result < 0);
    }
    
    @Test
    public void testTitleComparatorStrict_specialLettersAreAfertZ() {
        
        TiltedClass tiltedClass1 = new TiltedClass("ä");
        TiltedClass tiltedClass2 = new TiltedClass("z");
        
        TitleComparatorStrict<TiltedClass> comparator = TitleComparatorStrict
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
