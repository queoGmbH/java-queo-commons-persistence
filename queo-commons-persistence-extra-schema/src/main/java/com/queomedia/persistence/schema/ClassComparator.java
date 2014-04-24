package com.queomedia.persistence.schema;

import java.util.Comparator;

/** Compare classes by there class name. */
public final class ClassComparator implements Comparator<Class<?>> {

    /** The only one instance. */
    public static final ClassComparator INSTANCE = new ClassComparator();
    
    /** Use {@link #INSTANCE} */
    private ClassComparator() {
        super();
    }
    
    @Override
    public int compare(Class<?> o1, Class<?> o2) {
        return o1.getName().compareTo(o2.getName());
    }

}
