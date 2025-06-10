package de.rhm176.modmenu.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.rhm176.modmenu.api.update.UpdateChannel;
import de.rhm176.modmenu.api.update.UpdateCheckException;
import de.rhm176.modmenu.api.update.UpdateChecker;
import de.rhm176.modmenu.api.update.UpdateInfo;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Optional;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public class FabricLoaderUpdateChecker implements UpdateChecker {
    private static final URI LOADER_VERSIONS = URI.create("https://meta.fabricmc.net/v2/versions/loader");
    public static final URI UPDATE_LINK = URI.create("https://github.com/SilkLoader/silk-installer/releases/latest");

    @Override
    public @NotNull Optional<UpdateInfo> checkForUpdates() {
        HttpResponse<JsonElement> response;
        try {
            response = HttpUtil.httpGet(LOADER_VERSIONS);

            if (response.statusCode() != 200) {
                throw new UpdateCheckException(
                        "Failed to fetch loader versions, server responded with status code: " + response.statusCode());
            }

            if (!response.body().isJsonArray()) {
                throw new UpdateCheckException("Expected a JSON array from Fabric Meta API, but got something else.");
            }

            boolean latestIsBeta = false;
            SemanticVersion latestVersion = null;
            for (JsonElement version : response.body().getAsJsonArray()) {
                if (!version.isJsonObject()) continue;
                JsonObject versionObj = version.getAsJsonObject();
                SemanticVersion parsedVersion =
                        SemanticVersion.parse(versionObj.get("version").getAsString());
                boolean beta =
                        !versionObj.has("stable") || !versionObj.get("stable").getAsBoolean();

                if (UpdateChannel.getUserPreference() == UpdateChannel.RELEASE && beta) continue;

                if (latestVersion == null || parsedVersion.compareTo((Version) latestVersion) > 0) {
                    latestVersion = parsedVersion;
                    latestIsBeta = beta;
                }
            }

            //noinspection OptionalGetWithoutIsPresent
            if (latestVersion == null
                    || latestVersion.compareTo(FabricLoader.getInstance()
                                    .getModContainer("fabricloader")
                                    .get()
                                    .getMetadata()
                                    .getVersion())
                            < 1) {
                return Optional.empty();
            }

            return Optional.of(new UpdateInfo(
                    UPDATE_LINK,
                    latestVersion.getFriendlyString(),
                    latestIsBeta ? UpdateChannel.BETA : UpdateChannel.RELEASE));
        } catch (Exception e) {
            LogUtil.err("Failed to check for Fabric Loader updates!", e);
        }
        return Optional.empty();
    }
}
