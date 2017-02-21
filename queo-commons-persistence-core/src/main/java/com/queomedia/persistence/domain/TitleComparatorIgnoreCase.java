package com.queomedia.persistence.domain;

import java.io.Serializable;
import java.util.Comparator;

import com.queomedia.persistence.BusinessIdOwner;
import com.queomedia.persistence.util.BusinessIdOwnerUtil;

/**
 * Comparator to compare titled BusinessIdOwners by title (case insensitive) and, if the title is equal, by businessIds.
 * Tiles are compared using {@link String#compareToIgnoreCase(String)} 
 * 
 * The ARGUMENTS must also implement the {@link BusinessIdOwner} Interface!
 * @param <T> the generic type
 * 
 * @see TitleComparatorStrict
 */
public class TitleComparatorIgnoreCase<T extends Titled> implements Comparator<T>, Serializable {

    /**  The Constant serialVersionUID. */
    private static final long serialVersionUID = 4365432368702588594L;

    /** The only one instance. */
    @SuppressWarnings("rawtypes")
    private static final TitleComparatorIgnoreCase INSTANCE = new TitleComparatorIgnoreCase();

    /**
     * Return the instance.
     *
     * @param <T> the generic type
     * @return single instance of Title Comparator Ignore Case Locale Independent
     */
    @SuppressWarnings("unchecked")
    public static <T extends Titled> TitleComparatorIgnoreCase<T> getInstance() {
        return (TitleComparatorIgnoreCase<T>) INSTANCE;
    }

    @Override
    public int compare(final T o1, final T o2) {
        int titleComparison = o1.getTitle().compareToIgnoreCase(o2.getTitle());
        if (titleComparison != 0) {
            return titleComparison;
        } else {
            return BusinessIdOwnerUtil.compareByBidWhenPossible(o1, o2);
        }
    }

}
