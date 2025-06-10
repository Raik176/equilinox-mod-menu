package de.rhm176.modmenu.api.update;

import de.rhm176.modmenu.api.ModMenuApi;
import java.util.Optional;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A functional interface that defines the contract for checking for mod updates.
 * <p>
 * An implementation of this interface is responsible for communicating with a remote
 * source (like a GitHub repository or a custom server) to determine if a newer
 * version of a mod is available.
 * <p>
 * An instance should be returned from {@link ModMenuApi#getUpdateChecker()} to enable
 * the automatic update check feature for a mod.
 *
 * @see ModMenuApi#getUpdateChecker()
 * @since 1.0.0
 */
@ApiStatus.AvailableSince("1.0.0")
@FunctionalInterface
public interface UpdateChecker {
    /**
     * Performs the check for a new update.
     * <p>
     * This method will be called by the Mod Menu on a background Thread, and should be prepared to handle
     * potential I/O errors or other exceptions by either catching and logging them or
     * re-throwing them as {@link UpdateCheckException}s.
     *
     * @return An {@link Optional} containing an {@link UpdateInfo} record if a new
     * update is found; otherwise, returns an empty {@code Optional} if the
     * current version is up-to-date or if no suitable update is available.
     */
    @NotNull
    Optional<UpdateInfo> checkForUpdates();
}
