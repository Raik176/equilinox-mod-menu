package de.rhm176.modmenu.api.update;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.rhm176.modmenu.util.HttpUtil;
import de.rhm176.modmenu.util.LogUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.fabricmc.loader.api.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An implementation of {@link UpdateChecker} that checks for updates from a GitHub repository.
 * <p>
 * This class works by fetching release information from the GitHub Releases API. It parses
 * the JSON response to find the latest release, compares it against the currently installed
 * version, and reports if a newer version is found.
 *
 * @since 1.0.0
 */
@ApiStatus.AvailableSince("1.0.0")
public class GithubUpdateChecker implements UpdateChecker {
    private final URI apiUrl;
    private final String modId;

    private Function<String, Version> tagParseFunction;
    private Function<String, URI> releaseUrlFunction;
    private BiFunction<Version, Boolean, UpdateChannel> updateChannelFunction;

    /**
     * Constructs a new GitHub update checker.
     *
     * @param modId          The ID of the mod to check for updates.
     * @param repoIdentifier The repository identifier, either in "owner/repo" format or as a full GitHub URL.
     * @throws IllegalArgumentException if the repoIdentifier is not in a valid format.
     */
    public GithubUpdateChecker(String modId, String repoIdentifier) {
        this(modId, extractOwnerFromIdentifier(repoIdentifier), extractRepoNameFromIdentifier(repoIdentifier));
    }

    /**
     * Constructs a new GitHub update checker with an explicit owner and repository name.
     *
     * @param modId     The ID of the mod to check for updates.
     * @param repoOwner The owner of the GitHub repository.
     * @param repoName  The name of the GitHub repository.
     * @throws IllegalArgumentException if any of the arguments are null or blank.
     */
    @SuppressWarnings("ConstantValue") // so it doesn't warn for the null check
    public GithubUpdateChecker(@NotNull String modId, @NotNull String repoOwner, @NotNull String repoName) {
        if (modId == null || modId.isBlank()) {
            throw new IllegalArgumentException("modId cannot be null or blank.");
        }
        if (repoOwner == null || repoOwner.isBlank()) {
            throw new IllegalArgumentException("repoOwner cannot be null or blank.");
        }
        if (repoName == null || repoName.isBlank()) {
            throw new IllegalArgumentException("repoName cannot be null or blank.");
        }

        this.apiUrl = URI.create("https://api.github.com/repos/%s/%s/releases".formatted(repoOwner, repoName));
        this.modId = modId;

        this.releaseUrlFunction =
                (tag) -> URI.create("https://github.com/%s/%s/releases/%s".formatted(repoOwner, repoName, tag));
        this.updateChannelFunction =
                (version, isPrerelease) -> isPrerelease ? UpdateChannel.BETA : UpdateChannel.RELEASE;
        this.tagParseFunction = (tag) -> {
            try {
                return SemanticVersion.parse(tag.startsWith("v") ? tag.substring(1) : tag);
            } catch (VersionParsingException e) {
                return null;
            }
        };
    }

    /**
     * Overrides the default function used to generate the download/release URL for an update.
     * <p>
     * The default implementation points to the release tag URL on GitHub.
     *
     * @param function A function that takes a tag name string and returns a {@link URI}.
     * @return This {@link GithubUpdateChecker} instance for chaining.
     */
    public GithubUpdateChecker releaseUrlFunction(@NotNull Function<@NotNull String, @NotNull URI> function) {
        this.releaseUrlFunction = function;
        return this;
    }

    /**
     * Overrides the default function used to determine the {@link UpdateChannel} for a version.
     * <p>
     * The default implementation considers any release marked as "prerelease" on GitHub as
     * {@link UpdateChannel#BETA} and all others as {@link UpdateChannel#RELEASE}.
     *
     * @param function A bi-function that takes a {@link Version} and a boolean indicating
     * if it's a prerelease, and returns an {@link UpdateChannel}.
     * @return This {@link GithubUpdateChecker} instance for chaining.
     */
    public GithubUpdateChecker updateChannelFunction(
            @NotNull BiFunction<@NotNull Version, @NotNull Boolean, @NotNull UpdateChannel> function) {
        this.updateChannelFunction = function;
        return this;
    }

    /**
     * Overrides the default function used to parse version strings from the release tags.
     * <p>
     * The default implementation uses {@link SemanticVersion#parse(String)} and automatically
     * strips a leading "v" if present.
     *
     * @param function A function that takes a tag name string and returns a {@link Version}.
     * It should return {@code null} if parsing fails.
     * @return This {@link GithubUpdateChecker} instance for chaining.
     */
    public GithubUpdateChecker tagParseFunction(@NotNull Function<@NotNull String, @Nullable Version> function) {
        this.tagParseFunction = function;
        return this;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation fetches and parses the JSON release data from the GitHub API
     * to find the latest available version.
     *
     * @throws UpdateCheckException if there is a network error, a problem parsing the JSON,
     * or any other issue during the check.
     */
    @Override
    public @NotNull Optional<UpdateInfo> checkForUpdates() {
        Optional<ModContainer> modContainerOptional = FabricLoader.getInstance().getModContainer(modId);
        if (modContainerOptional.isEmpty()) {
            LogUtil.log("Failed to check for updates for mod with id '%s' because it's not loaded.".formatted(modId));
            return Optional.empty();
        }

        ModContainer modContainer = modContainerOptional.get();
        try {
            Version currentVersion = modContainer.getMetadata().getVersion();

            HttpResponse<JsonElement> response = HttpUtil.httpGet(apiUrl);
            if (response.statusCode() != 200) {
                throw new UpdateCheckException("Failed to fetch releases, server responded with status code: %s"
                        .formatted(response.statusCode()));
            }

            if (!response.body().isJsonArray()) {
                throw new UpdateCheckException(
                        "Expected a JSON array from GitHub Releases API, but got something else.");
            }

            String latestTagName = null;
            Version latestVersion = null;
            boolean latestIsPrerelease = false;

            for (JsonElement releaseElement : response.body().getAsJsonArray()) {
                if (!releaseElement.isJsonObject()) continue;
                JsonObject releaseObj = releaseElement.getAsJsonObject();

                boolean isPrerelease = releaseObj.get("prerelease").getAsBoolean();
                if (UpdateChannel.getUserPreference() == UpdateChannel.RELEASE && isPrerelease) {
                    continue;
                }

                String tagName = releaseObj.get("tag_name").getAsString();
                Version parsedVersion = tagParseFunction.apply(tagName);
                if (parsedVersion == null) {
                    LogUtil.err("Failed to compare current version with new version (%s).".formatted(tagName));
                    continue;
                }

                if (latestVersion == null || parsedVersion.compareTo(latestVersion) > 0) {
                    latestTagName = tagName;
                    latestVersion = parsedVersion;
                    latestIsPrerelease = isPrerelease;
                }
            }

            if (latestVersion == null || latestVersion.compareTo(currentVersion) <= 0) {
                return Optional.empty();
            }

            return Optional.of(new UpdateInfo(
                    releaseUrlFunction.apply(latestTagName),
                    latestVersion.getFriendlyString(),
                    updateChannelFunction.apply(latestVersion, latestIsPrerelease)));

        } catch (Exception e) {
            throw new UpdateCheckException("Unknown exception occurred during update check.", e);
        }
    }

    /**
     * Parses a repository identifier string to extract the owner and repository name.
     * Supports both "owner/repo" format and full "https://github.com/owner/repo" URLs.
     *
     * @param repoIdentifier The identifier string.
     * @return An array of strings containing the owner and repository name.
     * @throws IllegalArgumentException if the identifier is malformed.
     */
    private static String[] getRepoParts(@NotNull String repoIdentifier) {
        if (Objects.requireNonNull(repoIdentifier, "Repository Identifier cannot be null.")
                .contains("://")) {
            try {
                URI uri = new URI(repoIdentifier);
                String path = uri.getPath();

                if (path == null || path.isEmpty() || path.equals("/")) {
                    throw new IllegalArgumentException("Invalid repository path in URL: " + repoIdentifier);
                }

                String[] parts = path.substring(1).split("/");

                if (parts.length < 2) {
                    throw new IllegalArgumentException(
                            "Could not extract owner and repository name from URL: " + repoIdentifier);
                }

                return parts;
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid URL format: " + repoIdentifier, e);
            }
        } else {
            String[] parts = repoIdentifier.split("/");
            if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
                throw new IllegalArgumentException("Invalid 'owner/name' format: '" + repoIdentifier
                        + "'. It must contain two non-empty parts separated by a slash.");
            }
            return parts;
        }
    }

    /**
     * Extracts the repository owner from an identifier string.
     *
     * @param repoIdentifier The identifier string.
     * @return The repository owner.
     */
    private static String extractOwnerFromIdentifier(@NotNull String repoIdentifier) {
        return getRepoParts(repoIdentifier)[0];
    }

    /**
     * Extracts the repository name from an identifier string.
     *
     * @param repoIdentifier The identifier string.
     * @return The repository name.
     */
    private static String extractRepoNameFromIdentifier(@NotNull String repoIdentifier) {
        return getRepoParts(repoIdentifier)[1];
    }
}
