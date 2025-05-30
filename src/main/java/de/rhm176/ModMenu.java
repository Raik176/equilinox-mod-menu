package de.rhm176;

import de.rhm176.api.ModMenuApi;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModMenu implements ModInitializer {
    public static final String MOD_ID = "mod-menu";
    public static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu");
    public static final Map<String, Mod> MODS = new ConcurrentHashMap<>();

    @Override
    public void onInitialize() {
        Set<String> modpackMods = new HashSet<>();

        FabricLoader.getInstance()
                .getEntrypointContainers("modmenu", ModMenuApi.class)
                .forEach(entrypoint -> {
                    ModMetadata metadata = entrypoint.getProvider().getMetadata();
                    String modId = metadata.getId();
                    try {
                        ModMenuApi api = entrypoint.getEntrypoint();

                    } catch (Throwable e) {
                        LOGGER.error(
                                "Mod {} provides a broken implementation of {}",
                                modId,
                                ModMenuApi.class.getSimpleName(),
                                e);
                    }
                });

        for (ModContainer modContainer : FabricLoader.getInstance().getAllMods()) {
            Mod mod = new Mod(modContainer, modpackMods);

            MODS.put(mod.getId(), mod);
        }
    }
}
