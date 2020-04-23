package com.queomedia.persistence.domain;

import java.io.Serializable;
import java.util.Comparator;

import com.queomedia.persistence.BusinessIdOwner;

/**
 * This class is deprecated use {@link TitleComparatorStrict} instead.
 * 
 * <p>
 * This comparator compares titles in strict mode, in order to make this explicit use {@link TitleComparatorStrict} instead.
 * If a case insensitive compare is needed, then use {@link TitleComparatorIgnoreCase}.
 * </p>
 * 
 * Comparator to compare titled BusinessIdOwners by title and, if the title is equal, by businessIds.
 *
 * The ARGUMENTS must also implement the {@link BusinessIdOwner} Interface!
 * @param <T> the generic type
 * @see TitleComparatorStrict
 * @see TitleComparatorIgnoreCase
 */
@Deprecated
public final class TitleComparator<T extends Titled> implements Comparator<T>, Serializable {

    /**  The Constant serialVersionUID. */
    private static final long serialVersionUID = 5724551288058433643L;
    
    /** The only one instance. */
    @SuppressWarnings("rawtypes")
    private static final TitleComparator INSTANCE = new TitleComparator();
    
    /**
     * Return the instance.
     *
     * @param <T> the generic type
     * @return single instance of Title Comparator
     */
    @SuppressWarnings("unchecked")
    public static <T extends Titled> TitleComparator<T> getInstance() {        
        return (TitleComparator<T>) INSTANCE;
    }
    
    /** Use {@link #getInstance()} instead. */
    private TitleComparator() {
        super();
    }

    @Override
    public int compare(final T o1, final T o2) {
        return TitleComparatorStrict.getInstance().compare(o1, o2);
    }

}
