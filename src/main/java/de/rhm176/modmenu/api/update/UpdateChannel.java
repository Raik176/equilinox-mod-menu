package de.rhm176.modmenu.api.update;

import de.rhm176.modmenu.config.Config;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the stability channel for a mod update.
 * <p>
 * This enum is used to categorize different types of releases, allowing users to
 * choose how bleeding-edge they want their updates to be. It also allows an
 * {@link UpdateChecker} to filter releases based on the user's preference.
 *
 * @since 1.0.0
 */
@ApiStatus.AvailableSince("1.0.0")
public enum UpdateChannel {
    /**
     * Represents an alpha release. These are typically unstable, work-in-progress builds
     * intended for testing and development purposes.
     */
    ALPHA,
    /**
     * Represents a beta release. These are often feature-complete but may still contain
     * bugs. They are more stable than alpha builds but less stable than final releases.
     */
    BETA,
    /**
     * Represents a stable, public release. This is the most stable channel and is
     * recommended for most users.
     */
    RELEASE;

    /**
     * Retrieves the update channel preferred by the user.
     * <p>
     * An {@link UpdateChecker} should use this value to determine
     * which updates to show. For example, if the user preference is {@code RELEASE},
     * the checker should ignore alpha and beta builds.
     *
     * @return The {@code UpdateChannel} the user has configured as their preference.
     */
    @NotNull
    public static UpdateChannel getUserPreference() {
        return Config.instance().updateChannel;
    }
}
