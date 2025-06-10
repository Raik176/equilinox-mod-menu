package de.rhm176.modmenu;

import de.rhm176.api.lang.I18n;
import de.rhm176.modmenu.api.ModConfigPanelFactory;
import de.rhm176.modmenu.api.ModMenuApi;
import de.rhm176.modmenu.api.update.GithubUpdateChecker;
import de.rhm176.modmenu.api.update.UpdateChecker;
import de.rhm176.modmenu.api.update.UpdateInfo;
import de.rhm176.modmenu.duck.SecondPanelUiDuck;
import de.rhm176.modmenu.util.FabricLoaderUpdateChecker;
import de.rhm176.modmenu.util.LogUtil;
import java.nio.ByteBuffer;
import java.util.*;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import optionsMenu.OptionsPanelUi;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class Mod {
    private final ModContainer container;
    private final ModMetadata metadata;

    private final Map<String, String> links = new HashMap<>();
    private final List<ModBadgeType> badges = new ArrayList<>();

    private final Optional<String> parent;

    private UpdateInfo updateInfo;

    public Mod(ModContainer modContainer) {
        this.container = modContainer;
        this.metadata = modContainer.getMetadata();
        String id = metadata.getId();

        Optional<String> parentId = Optional.empty();
        CustomValue modMenuValue = metadata.getCustomValue(ModMenu.MOD_ID);
        if (modMenuValue != null && modMenuValue.getType() == CustomValue.CvType.OBJECT) {
            CustomValue.CvObject modMenuObject = modMenuValue.getAsObject();
            CustomValue parentCv = modMenuObject.get("parent");
            if (parentCv != null) {
                if (parentCv.getType() == CustomValue.CvType.STRING) {
                    parentId = Optional.of(parentCv.getAsString());
                } else if (parentCv.getType() == CustomValue.CvType.OBJECT) {
                    try {
                        CustomValue.CvObject parentObj = parentCv.getAsObject();
                        parentId = ModMenuUtil.getString("id", parentObj);
                        if (parentId.orElse("").equals(id)) {
                            parentId = Optional.empty();
                            throw new RuntimeException("Mod declared itself as its own parent");
                        }
                    } catch (Throwable t) {
                        LogUtil.err("Error loading parent data from mod: " + id, t);
                    }
                }
            }
            links.putAll(ModMenuUtil.getStringMap("links", modMenuObject).orElse(new HashMap<>()));
            if (!links.containsKey("modmenu.sources") && !links.containsKey("sources")) {
                metadata.getContact().get("sources").ifPresent(s -> links.put("modmenu.sources", s));
            }
            links.forEach((k, v) -> links.put(k, I18n.translate(v)));
        }
        parent = parentId;

        switch (getId()) {
            case "fabricloader", "mixinextras", "java": {
                badges.add(ModBadgeType.LIBRARY);
                break;
            }
            case "equilinox": {
                badges.add(ModBadgeType.EQUILINOX);
                break;
            }
            default: {
                if (modMenuValue != null) {
                    ModMenuUtil.getStringSet("badges", modMenuValue.getAsObject())
                            .ifPresent(stringSet -> stringSet.stream()
                                    .map(ModBadgeType::getById)
                                    .filter(Objects::nonNull)
                                    .filter(ModBadgeType::canAddBadge)
                                    .forEach(badges::add));
                }
            }
        }
    }

    public UpdateInfo getUpdateInfo() {
        return updateInfo;
    }

    public void setUpdateInfo(UpdateInfo updateInfo) {
        this.updateInfo = updateInfo;
    }

    public UpdateChecker getUpdateChecker() {
        return switch (getId()) {
            case "fabricloader" -> new FabricLoaderUpdateChecker();
            case "silkloader" ->
                new GithubUpdateChecker("silkloader", "SilkLoader/silk-loader")
                        .releaseUrlFunction(s -> FabricLoaderUpdateChecker.UPDATE_LINK);
            default -> {
                ModMenuApi api = ModMenu.MOD_APIS.get(getId());
                yield api != null ? api.getUpdateChecker() : null;
            }
        };
    }

    @SuppressWarnings("DataFlowIssue")
    public ModConfigPanelFactory getConfigFactory() {
        if ("equilinox".equals(getId())) {
            return (gameMenu) -> {
                OptionsPanelUi optionsPanelUi = new OptionsPanelUi(gameMenu);
                ((SecondPanelUiDuck) optionsPanelUi).modmenu$setShouldCloseToModmenu();

                return optionsPanelUi;
            };
        } else {
            ModMenuApi api = ModMenu.MOD_APIS.get(getId());
            if (api == null) return null;

            return api.getModConfigPanelFactory();
        }
    }

    public Optional<String> getParent() {
        return parent;
    }

    public String getId() {
        return metadata.getId();
    }

    public String getName() {
        return Objects.equals(getId(), "java")
                ? "Java"
                : I18n.translateWithFallback("modmenu.nameTranslation." + getId(), metadata.getName());
    }

    public String getDescription() {
        return Objects.equals(getId(), "java")
                ? "The Java runtime environment.\nRunning: " + metadata.getName()
                : I18n.translateWithFallback("modmenu.descriptionTranslation." + getId(), metadata.getDescription());
    }

    public String getVersion() {
        return Objects.equals(getId(), "java")
                ? System.getProperty("java.version")
                : metadata.getVersion().getFriendlyString();
    }

    public List<String> getAuthors() {
        return switch (getId()) {
            case "equilinox" -> List.of("ThinMatrix", "Jamal Green Music", "Dannek Studio");
            case "java" -> List.of(System.getProperty("java.vendor"));
            default -> metadata.getAuthors().stream().map(Person::getName).toList();
        };
    }

    public Map<String, Collection<String>> getContributors() {
        var contributors = new LinkedHashMap<String, Collection<String>>();
        for (var contributor : this.metadata.getContributors()) {
            contributors.put(contributor.getName(), List.of("Contributor"));
        }

        return contributors;
    }

    public SortedMap<String, Set<String>> getCredits() {
        SortedMap<String, Set<String>> credits = new TreeMap<>();

        var authors = this.getAuthors();
        var contributors = this.getContributors();
        for (var author : authors) {
            contributors.put(author, List.of("Author"));
        }

        for (var contributor : contributors.entrySet()) {
            for (var role : contributor.getValue()) {
                credits.computeIfAbsent(role, key -> new LinkedHashSet<>());
                credits.get(role).add(contributor.getKey());
            }
        }

        return credits;
    }

    public Set<String> getLicenses() {
        return new HashSet<>(metadata.getLicense());
    }

    public String getWebsite() {
        return switch (getId()) {
            case "equilinox" -> "https://www.equilinox.com/";
            case "java" -> System.getProperty("java.vendor.url", null);
            default -> metadata.getContact().get("homepage").orElse(null);
        };
    }

    public String getIssueTracker() {
        return metadata.getContact().get("issues").orElse(null);
    }

    public Map<String, String> getLinks() {
        return links;
    }

    public List<ModBadgeType> getBadges() {
        List<ModBadgeType> newList = new ArrayList<>(badges);
        if (getUpdateInfo() != null) {
            newList.add(ModBadgeType.UPDATE);
        }

        return newList;
    }

    public ModContainer getContainer() {
        return container;
    }

    public ByteBuffer getIconBuffer(int size) {
        return switch (getId()) {
            case "equilinox" ->
                ModMenuUtil.loadPng(
                        ModMenu.MOD_MENU_CONTAINER, "assets/" + ModMenu.MOD_ID + "/equilinox_icon.png", size);
            case "java" ->
                ModMenuUtil.loadPng(ModMenu.MOD_MENU_CONTAINER, "assets/" + ModMenu.MOD_ID + "/java_icon.png", size);
            default ->
                metadata.getIconPath(size)
                        .map(s -> ModMenuUtil.loadPng(container, s, size))
                        .orElse(null);
        };
    }
}
