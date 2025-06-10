package de.rhm176.modmenu.config;

import de.rhm176.modmenu.Mod;
import de.rhm176.modmenu.ModMenu;
import java.util.*;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public enum SortingOrder {
    A_Z {
        @Override
        public Comparator<Mod> getBaseComparator() {
            return Comparator.comparing(Mod::getId, String.CASE_INSENSITIVE_ORDER);
        }
    },

    Z_A {
        @Override
        public Comparator<Mod> getBaseComparator() {
            return Comparator.comparing(Mod::getId, String.CASE_INSENSITIVE_ORDER)
                    .reversed();
        }
    },

    UPDATE_AVAILABLE {
        @Override
        public Comparator<Mod> getBaseComparator() {
            return Comparator.<Mod, Boolean>comparing(mod -> mod.getUpdateInfo() == null)
                    .thenComparing(mod -> mod.getUpdateChecker() == null);
        }
    };

    public abstract Comparator<Mod> getBaseComparator();

    public Comparator<Mod> getComparator() {
        Map<String, String> childToParentMap = new HashMap<>();
        Map<String, Mod> rootCache = new HashMap<>();
        Comparator<Mod> baseComparator = getBaseComparator();

        ModMenu.MOD_CHILDREN.forEach(
                (parent, children) -> children.forEach(child -> childToParentMap.put(child, parent)));

        return (mod1, mod2) -> {
            Mod root1 = getRoot(mod1, childToParentMap, rootCache);
            Mod root2 = getRoot(mod2, childToParentMap, rootCache);

            if (!root1.getId().equals(root2.getId())) {
                return baseComparator.compare(root1, root2);
            }

            if (mod1.getId().equals(root1.getId())) {
                return -1;
            } else if (mod2.getId().equals(root2.getId())) {
                return 1;
            } else {
                return baseComparator.compare(mod1, mod2);
            }
        };
    }

    private static Mod getRoot(Mod mod, Map<String, String> childToParentMap, Map<String, Mod> cache) {
        return cache.computeIfAbsent(mod.getId(), id -> {
            Set<String> visited = new HashSet<>();
            Mod current = mod;
            String parentId;
            while ((parentId = childToParentMap.get(current.getId())) != null) {
                if (!visited.add(current.getId())) {
                    return mod;
                }
                Mod parentMod = ModMenu.MODS.get(parentId);
                if (parentMod == null) {
                    break;
                }
                current = parentMod;
            }
            return current;
        });
    }
}
