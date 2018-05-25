package com.queomedia.persistence.util;

import java.util.Optional;

import com.queomedia.commons.checks.Check;
import com.queomedia.persistence.domain.Titled;

/**
 * Util class for {@link Titled} entities.
 */
public final class TitledUtil {

    /**
     * Util class does not need public constructor.
     */
    private TitledUtil() {
    }

    /**
     * Try to cast the entity to {@link Titled}, or return empty if it is no instance of {@link Titled}.
     * @param entity the entity
     * @return the casted entity or empty.
     */
    public static Optional<Titled> tryCastToTitled(final Object entity) {
        Check.notNullArgument(entity, "entity");

        // the same in fluent lamda style
        // return Optional.of(entity).filter(Titled.class::isInstance).map(Titled.class::cast);

        if (entity instanceof Titled) {
            return Optional.of((Titled) entity);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Extract the title of the given entity if it implements {@link Titled} else return empty.
     * @param entity the entity
     * @return the entity title or empty.
     */
    public static Optional<String> extractEntityTitle(final Object entity) {
        Check.notNullArgument(entity, "entity");

        return tryCastToTitled(entity).map(Titled::getTitle);
    }

}
