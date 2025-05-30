package de.rhm176;

import java.nio.ByteBuffer;
import java.util.*;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;

public final class Mod {
    private final ModContainer container;
    private final ModMetadata metadata;

    private final Map<String, String> links = new HashMap<>();

    private final List<ModBadgeType> badges = new ArrayList<>();

    public Mod(ModContainer modContainer, Set<String> modpackMods) {
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
                        System.err.println("Error loading parent data from mod: " + id);
                        t.printStackTrace(System.err);
                    }
                }
            }
            links.putAll(ModMenuUtil.getStringMap("links", modMenuObject).orElse(new HashMap<>()));
            if (getSource() != null && !links.containsKey("modmenu.sources")) {
                links.put("modmenu.sources", getSource());
            }
        }

        boolean isGenerated =
                ModMenuUtil.getBoolean("fabric-loom:generated", metadata).orElse(false);
        if (isGenerated && parentId.isEmpty() && container.getContainingMod().isPresent()) {
            ModContainer inside = container.getContainingMod().get();
            parentId = Optional.of(inside.getMetadata().getId());
        }

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
                                    .forEach(badges::add));
                }
            }
        }
    }

    public String getId() {
        return metadata.getId();
    }

    public String getName() {
        return Objects.equals(getId(), "java") ? "Java" : metadata.getName();
    }

    public String getDescription() {
        return Objects.equals(getId(), "java")
                ? "The Java runtime environment.\nRunning: " + metadata.getName()
                : metadata.getDescription();
    }

    public String getVersion() {
        return Objects.equals(getId(), "java")
                ? System.getProperty("java.version")
                : metadata.getVersion().getFriendlyString();
    }

    public List<String> getAuthors() {
        return switch (getId()) {
            case "equilinox" -> List.of("ThinMatrix");
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

    public String getSource() {
        return metadata.getContact().get("sources").orElse(null);
    }

    public Map<String, String> getLinks() {
        return links;
    }

    public List<ModBadgeType> getBadges() {
        return badges;
    }

    public ModContainer getContainer() {
        return container;
    }

    public ByteBuffer getIconBuffer(int size) {
        return switch (getId()) {
            case "equilinox" ->
                ModMenuUtil.loadPng(
                        ModMenu.MODS.get(ModMenu.MOD_ID).getContainer(),
                        "assets/" + ModMenu.MOD_ID + "/equilinox_icon.png",
                        size);
            case "java" ->
                ModMenuUtil.loadPng(
                        ModMenu.MODS.get(ModMenu.MOD_ID).getContainer(),
                        "assets/" + ModMenu.MOD_ID + "/java_icon.png",
                        size);
            default ->
                metadata.getIconPath(size)
                        .map(s -> ModMenuUtil.loadPng(container, s, size))
                        .orElse(null);
        };
    }
}
