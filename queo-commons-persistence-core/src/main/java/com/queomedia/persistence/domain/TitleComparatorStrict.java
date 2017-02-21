package com.queomedia.persistence.domain;

import java.io.Serializable;
import java.util.Comparator;

import com.queomedia.persistence.BusinessIdOwner;
import com.queomedia.persistence.util.BusinessIdOwnerUtil;

/**
 * Comparator to compare titled BusinessIdOwners by title and, if the title is equal, by businessIds.
 * Tiles are compared using {@link String#compareTo(String)}.
 *
 * The ARGUMENTS must also implement the {@link BusinessIdOwner} Interface!
 * @param <T> the generic type
 * 
 * @see TitleComparatorIgnoreCase
 */
public final class TitleComparatorStrict<T extends Titled> implements Comparator<T>, Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 5740088327747107766L;

    /** The only one instance. */
    @SuppressWarnings("rawtypes")
    private static final TitleComparatorStrict INSTANCE = new TitleComparatorStrict();

    /**
     * Return the instance.
     *
     * @param <T> the generic type
     * @return single instance of Title Comparator
     */
    @SuppressWarnings("unchecked")
    public static <T extends Titled> TitleComparatorStrict<T> getInstance() {
        return (TitleComparatorStrict<T>) INSTANCE;
    }

    /** Use {@link #getInstance()} instead. */
    private TitleComparatorStrict() {
        super();
    }

    @Override
    public int compare(final T o1, final T o2) {
        int titleComparison = o1.getTitle().compareTo(o2.getTitle());
        if (titleComparison != 0) {
            return titleComparison;
        } else {
            return BusinessIdOwnerUtil.compareByBidWhenPossible(o1, o2);
        }
    }

}
