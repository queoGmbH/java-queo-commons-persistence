package com.queomedia.persistence.domain;

import java.io.Serializable;
import java.util.Comparator;

import com.queomedia.persistence.BusinessIdOwner;

/**
 * Comparator to compare titled BusinessIdOwners by title and, if the title is equal, by businessIds.
 *
 * The ARGUMENTS must also implement the {@link BusinessIdOwner} Interface!
 * @param <T> the generic type
 */
public final class TitleComparator<T extends Titled> implements Comparator<T>, Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 5940088827747107766L;
    
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
        int titleComparison = o1.getTitle().compareTo(o2.getTitle());
        if (titleComparison != 0) {
            return titleComparison;
        } else {
            if ((o1 instanceof BusinessIdOwner) && (o2 instanceof BusinessIdOwner)) {
                @SuppressWarnings("rawtypes")
                BusinessIdOwner businessIdOwner1 = (BusinessIdOwner) o1;
                @SuppressWarnings("rawtypes")
                BusinessIdOwner businessIdOwner2 = (BusinessIdOwner) o2;
                @SuppressWarnings("unchecked")
                int bidComparison = businessIdOwner1.getBusinessId().compareTo(businessIdOwner2.getBusinessId());
                return bidComparison;
            }
            return 0;
        }
    }

}
