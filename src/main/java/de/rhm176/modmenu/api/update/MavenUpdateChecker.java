package de.rhm176.modmenu.api.update;

import de.rhm176.modmenu.util.HttpUtil;
import de.rhm176.modmenu.util.LogUtil;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.fabricmc.loader.api.*;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * An implementation of {@link UpdateChecker} that checks for updates from a Maven repository.
 * <p>
 * This class works by fetching and parsing the {@code maven-metadata.xml} file for a given
 * artifact. It extracts all available versions, compares them against the currently installed
 * version, and reports if a newer version is found.
 *
 * @since 1.0.0
 */
@ApiStatus.AvailableSince("1.0.0")
public class MavenUpdateChecker implements UpdateChecker {
    private final URI metadataUrl;
    private final String modId;
    private final String friendlyRepoName;

    private Function<String, URI> releaseUrlFunction;
    private BiFunction<Version, Boolean, UpdateChannel> updateChannelFunction;
    private Function<String, Version> versionParseFunction;

    /**
     * Constructs a new Maven update checker.
     *
     * @param modId      The ID of the mod to check for updates.
     * @param repoUrl    The base URL of the Maven repository (e.g., "https://maven.rhm176.de/releases").
     * @param groupId    The artifact's group ID (e.g., "de.rhm176.silk").
     * @param artifactId The artifact's ID (e.g., "silk-loader").
     * @throws NullPointerException if any of the arguments are null or blank.
     */
    @SuppressWarnings("ConstantValue") // so it doesn't warn for the null check
    public MavenUpdateChecker(
            @NotNull String modId, @NotNull String repoUrl, @NotNull String groupId, @NotNull String artifactId) {
        if (modId == null || modId.isBlank()) {
            throw new NullPointerException("modId cannot be null or blank.");
        }
        if (repoUrl == null || repoUrl.isBlank()) {
            throw new NullPointerException("repoUrl cannot be null or blank.");
        }
        if (groupId == null || groupId.isBlank()) {
            throw new NullPointerException("groupId cannot be null or blank.");
        }
        if (artifactId == null || artifactId.isBlank()) {
            throw new NullPointerException("artifactId cannot be null or blank.");
        }

        this.modId = modId;
        String repoPath = (repoUrl.endsWith("/") ? repoUrl : repoUrl + "/");
        String artifactPath = groupId.replace('.', '/') + "/" + artifactId + "/";
        this.metadataUrl = URI.create(repoPath + artifactPath + "maven-metadata.xml");
        this.friendlyRepoName = URI.create(repoUrl).getHost();

        this.releaseUrlFunction = (version) -> URI.create(repoPath + artifactPath + version);
        this.updateChannelFunction = (version, isSnapshot) -> isSnapshot ? UpdateChannel.ALPHA : UpdateChannel.RELEASE;
        this.versionParseFunction = (versionStr) -> {
            try {
                return SemanticVersion.parse(versionStr);
            } catch (VersionParsingException e) {
                return null;
            }
        };
    }

    /**
     * Overrides the default function used to generate the download/release URL for an update.
     * <p>
     * The default implementation points to the version's folder within the Maven repository.
     *
     * @param function A function that takes a version string and returns a {@link URI}.
     * @return This {@link MavenUpdateChecker} instance for chaining.
     */
    public MavenUpdateChecker releaseUrlFunction(@NotNull Function<@NotNull String, @NotNull URI> function) {
        this.releaseUrlFunction = function;
        return this;
    }

    /**
     * Overrides the default function used to determine the {@link UpdateChannel} for a version.
     * <p>
     * The default implementation considers any version containing "SNAPSHOT" as {@link UpdateChannel#ALPHA}
     * and all others as {@link UpdateChannel#RELEASE}.
     *
     * @param function A bi-function that takes a {@link Version} and a boolean indicating
     * if it's a snapshot, and returns an {@link UpdateChannel}.
     * @return This {@link MavenUpdateChecker} instance for chaining.
     */
    public MavenUpdateChecker updateChannelFunction(
            @NotNull BiFunction<@NotNull Version, @NotNull Boolean, @NotNull UpdateChannel> function) {
        this.updateChannelFunction = function;
        return this;
    }

    /**
     * Overrides the default function used to parse version strings from the metadata.
     * <p>
     * The default implementation uses {@link SemanticVersion#parse(String)}.
     *
     * @param function A function that takes a version string and returns a {@link Version}.
     * It should return {@code null} if parsing fails.
     * @return This {@link MavenUpdateChecker} instance for chaining.
     */
    public MavenUpdateChecker versionParseFunction(@NotNull Function<@NotNull String, @Nullable Version> function) {
        this.versionParseFunction = function;
        return this;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation fetches and parses the {@code maven-metadata.xml} from the configured
     * repository to find the latest available version.
     *
     * @throws UpdateCheckException if there is a network error, a problem parsing the XML,
     * or any other issue during the check.
     */
    @Override
    public @NotNull Optional<UpdateInfo> checkForUpdates() {
        Optional<ModContainer> modContainerOptional = FabricLoader.getInstance().getModContainer(modId);
        if (modContainerOptional.isEmpty()) {
            LogUtil.log("Failed to check for updates for mod '%s' as it is not loaded.".formatted(modId));
            return Optional.empty();
        }

        ModMetadata modMetadata = modContainerOptional.get().getMetadata();
        Version currentVersion = modMetadata.getVersion();

        try {
            HttpResponse<InputStream> response =
                    HttpUtil.httpGet(metadataUrl, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                throw new UpdateCheckException(
                        "Failed to fetch maven-metadata.xml from %s. Server responded with status code: %s"
                                .formatted(friendlyRepoName, response.statusCode()));
            }

            try (InputStream responseBody = response.body()) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(responseBody);
                doc.getDocumentElement().normalize();

                NodeList versionNodes = doc.getElementsByTagName("version");
                if (versionNodes.getLength() == 0) {
                    throw new UpdateCheckException("No versions found in maven-metadata.xml.");
                }

                Version latestVersion = null;
                String latestVersionStr = null;

                for (int i = 0; i < versionNodes.getLength(); i++) {
                    String versionStr = versionNodes.item(i).getTextContent();
                    boolean isSnapshot = versionStr.toUpperCase().contains("SNAPSHOT");

                    if (UpdateChannel.getUserPreference() == UpdateChannel.RELEASE && isSnapshot) {
                        continue;
                    }

                    Version parsedVersion = versionParseFunction.apply(versionStr);
                    if (parsedVersion == null) {
                        LogUtil.err("Failed to parse version '%s' from %s.".formatted(versionStr, friendlyRepoName));
                        continue;
                    }

                    if (latestVersion == null || parsedVersion.compareTo(latestVersion) > 0) {
                        latestVersion = parsedVersion;
                        latestVersionStr = versionStr;
                    }
                }

                if (latestVersion == null || latestVersion.compareTo(currentVersion) <= 0) {
                    return Optional.empty();
                }

                boolean isLatestSnapshot = latestVersionStr.toUpperCase().contains("SNAPSHOT");

                return Optional.of(new UpdateInfo(
                        releaseUrlFunction.apply(latestVersionStr),
                        latestVersion.getFriendlyString(),
                        updateChannelFunction.apply(latestVersion, isLatestSnapshot)));
            }

        } catch (Exception e) {
            throw new UpdateCheckException("Unknown exception occurred during update check.", e);
        }
    }
}
