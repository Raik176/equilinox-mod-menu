package de.rhm176.modmenu;

import org.jetbrains.annotations.ApiStatus;
import toolbox.Colour;

@ApiStatus.Internal
public enum ModBadgeType {
    LIBRARY("library", new Colour(100, 149, 237, true)),
    DEPRECATED("deprecated", new Colour(220, 75, 75, true)),
    EQUILINOX("equilinox", new Colour(90, 180, 90, true), false),
    UPDATE("update", new Colour(111, 66, 193, true), false);

    private final String id;
    private final Colour color;
    private boolean canAddBadge;

    ModBadgeType(String id, Colour color, boolean canAddBadge) {
        this.id = id;
        this.color = color;
        this.canAddBadge = canAddBadge;
    }

    ModBadgeType(String id, Colour color) {
        this.id = id;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public Colour getColor() {
        return color;
    }

    public boolean canAddBadge() {
        return canAddBadge;
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
