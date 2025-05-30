package de.rhm176;

import toolbox.Colour;

public enum ModBadgeType {
    LIBRARY("library", "Library", new Colour(100, 149, 237, true)),
    DEPRECATED("deprecated", "Deprecated", new Colour(220, 75, 75, true)),
    EQUILINOX(null, "Equilinox", new Colour(90, 180, 90, true));

    private final String id;
    private final String name;
    private final Colour color;

    ModBadgeType(String id, String name, Colour color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public Colour getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public static ModBadgeType getById(String id) {
        for (ModBadgeType badgeType : values()) {
            if (badgeType.getId() == null) continue;

            if (badgeType.getId().equals(id)) {
                return badgeType;
            }
        }

        return null;
    }
}
