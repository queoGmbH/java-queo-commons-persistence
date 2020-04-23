package com.queomedia.persistence.extra.springdatajpa;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

public class AllPageableTest {

    @Test
    public void testSortedBy() {
        Sort sortByTestAsc = Sort.by("test");
        Pageable result = AllPageable.sortedBy(sortByTestAsc);
        
        assertEquals(sortByTestAsc, result.getSort());
        assertPageSizeAll(result);
    }
    
    @Test
    public void testSorted() throws Exception {
        Pageable result = AllPageable.sorted(Direction.ASC, "test");
        
        assertEquals(result.getSort(), Sort.by("test"));
        assertPageSizeAll(result);
    }
    
    @Test
    public void testUnsorted() throws Exception {
        Pageable result = AllPageable.unsorted();
        
        assertEquals(result.getSort(), Sort.unsorted());        
        assertPageSizeAll(result);
    }
    
    public static void assertPageSizeAll(Pageable pageable) {
        assertEquals(true, pageable.isPaged());
        assertEquals(0, pageable.getOffset());
        assertEquals(Integer.MAX_VALUE, pageable.getPageSize());
    }
   

}
