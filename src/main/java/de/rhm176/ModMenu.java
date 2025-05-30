package de.rhm176;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public class ModMenu implements ModInitializer {
    public static final String MOD_ID = "mod-menu";
    public static final Map<String, Mod> MODS = new ConcurrentHashMap<>();

    @Override
    public void onInitialize() {
        Set<String> modpackMods = new HashSet<>();
        for (ModContainer modContainer : FabricLoader.getInstance().getAllMods()) {
            Mod mod = new Mod(modContainer, modpackMods);

            MODS.put(mod.getId(), mod);
        }
    }
}
