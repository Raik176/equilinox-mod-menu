package de.rhm176.modmenu;

import de.rhm176.modmenu.api.ModMenuApi;
import de.rhm176.modmenu.api.update.UpdateCheckException;
import de.rhm176.modmenu.api.update.UpdateChecker;
import de.rhm176.modmenu.config.Config;
import de.rhm176.modmenu.util.LogUtil;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ModMenu implements ModInitializer {
    public static final String MOD_ID = "modmenu";
    public static ModContainer MOD_MENU_CONTAINER;
    public static final Map<String, Mod> MODS = new ConcurrentHashMap<>();
    public static final Map<String, List<String>> MOD_CHILDREN = new ConcurrentHashMap<>();
    public static final Map<String, ModMenuApi> MOD_APIS = new ConcurrentHashMap<>();

    @Override
    public void onInitialize() {
        FabricLoader.getInstance().getAllMods().forEach(modContainer -> {
            Mod mod = new Mod(modContainer);
            if (MOD_ID.equals(mod.getId())) {
                MOD_MENU_CONTAINER = modContainer;
            }
            MODS.put(mod.getId(), mod);

            for (ModContainer containedMod : modContainer.getContainedMods()) {
                MOD_CHILDREN
                        .computeIfAbsent(mod.getId(), k -> new ArrayList<>())
                        .add(containedMod.getMetadata().getId());
            }

            mod.getParent().ifPresent(parentId -> MOD_CHILDREN
                    .computeIfAbsent(parentId, k -> new ArrayList<>())
                    .add(mod.getId()));
        });

        MOD_CHILDREN.put("silkloader", List.of("fabricloader"));

        MOD_APIS.putAll(FabricLoader.getInstance().getEntrypointContainers(MOD_ID, ModMenuApi.class).stream()
                .collect(Collectors.toMap(
                        entrypoint -> entrypoint.getProvider().getMetadata().getId(),
                        EntrypointContainer::getEntrypoint)));

        if (Config.instance().enableUpdateChecking) {
            ExecutorService executor = Executors.newFixedThreadPool(
                    Math.max(2, Math.min(Runtime.getRuntime().availableProcessors(), 4)));
            for (Mod mod : MODS.values()) {
                UpdateChecker updateChecker = mod.getUpdateChecker();
                if (updateChecker != null) {
                    executor.submit(() -> {
                        Thread.currentThread()
                                .setName(MOD_MENU_CONTAINER.getMetadata().getName() + "/Update Checker/"
                                        + mod.getName());

                        try {
                            updateChecker.checkForUpdates().ifPresent(((info) -> {
                                LogUtil.log("Update available for '%s' (%s -> %s)"
                                        .formatted(mod.getId(), mod.getVersion(), info.version()));
                                mod.setUpdateInfo(info);
                            }));
                        } catch (UpdateCheckException e) {
                            LogUtil.err("Failed to check for update for mod with id '%s'.".formatted(mod.getId()), e);
                        }
                    });
                }
            }

            executor.shutdown();
        }
    }
}
