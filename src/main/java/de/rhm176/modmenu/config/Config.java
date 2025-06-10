package de.rhm176.modmenu.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.rhm176.modmenu.ModMenu;
import de.rhm176.modmenu.api.update.UpdateChannel;
import de.rhm176.modmenu.util.LogUtil;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class Config {
    private static final Path CONFIG_FILE =
            FabricLoader.getInstance().getConfigDir().resolve(ModMenu.MOD_ID + ".json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Config config;

    public SortingOrder sortingOrder = SortingOrder.A_Z;

    public UpdateChannel updateChannel = UpdateChannel.RELEASE;
    public boolean enableUpdateChecking = true;

    private Config() {}

    public static Config instance() {
        return config == null ? config = load() : config;
    }

    private static Config load() {
        if (Files.exists(CONFIG_FILE)) {
            try (var reader = Files.newBufferedReader(CONFIG_FILE)) {
                config = GSON.fromJson(reader, Config.class);
            } catch (Exception e) {
                LogUtil.err("Could not load config. Using default config.", e);
                config = new Config();
            }
        } else {
            config = new Config();
            config.save();
        }

        return config;
    }

    public void save() {
        try (var writer = Files.newBufferedWriter(CONFIG_FILE)) {
            GSON.toJson(config, writer);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
