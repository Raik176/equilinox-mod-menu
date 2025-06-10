package de.rhm176.modmenu.api.update;

import de.rhm176.api.lang.I18n;
import java.net.URI;
import java.util.Locale;
import org.jetbrains.annotations.ApiStatus;

/**
 * A data object that holds information about an available mod update.
 * <p>
 * This record is returned by an {@link UpdateChecker} to inform the Mod Menu
 * that a new version of a mod has been found. The information contained within
 * is then used to display an update notification to the user.
 *
 * @param updateMessage A user-friendly message describing the update. This message
 * is typically constructed automatically by the convenience
 * constructor to include the version and channel.
 * @param updateUrl     The direct URL where the user can download the update or
 * view the release notes (e.g., a GitHub Releases page).
 * @param version       The new version string of the available update (e.g., "1.2.0").
 * @param updateChannel The channel this update is on.
 * @see UpdateChecker
 * @since 1.0.0
 */
@ApiStatus.AvailableSince("1.0.0")
public record UpdateInfo(String updateMessage, URI updateUrl, String version, UpdateChannel updateChannel) {
    /**
     * Constructs a new {@code UpdateInfo} record with a default, translated update message.
     * <p>
     * This convenience constructor automatically generates a formatted and localized
     * update message.
     *
     * @param updateUrl     The direct URL where the user can download the update.
     * @param version       The new version string of the available update.
     * @param updateChannel The channel the update was found on.
     * @since 1.0.0
     */
    public UpdateInfo(URI updateUrl, String version, UpdateChannel updateChannel) {
        this(
                I18n.translate(
                        "modmenu.update.version",
                        version,
                        I18n.translate(
                                "modmenu.update.channel." + updateChannel.name().toLowerCase(Locale.ROOT))),
                updateUrl,
                version,
                updateChannel);
    }
}
