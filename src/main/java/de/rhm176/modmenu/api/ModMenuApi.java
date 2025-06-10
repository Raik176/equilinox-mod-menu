package de.rhm176.modmenu.api;

import de.rhm176.modmenu.api.update.UpdateChecker;
import de.rhm176.modmenu.ui.ModMenuGui;
import gameMenu.GameMenuGui;
import gameMenu.SecondPanelUi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The primary entry point for mods to integrate with the Mod Menu.
 * <p>
 * Implement this interface on a new entrypoint to provide custom functionality,
 * such as a configuration screen or an update checker.
 *
 * @since 1.0.0
 */
public interface ModMenuApi {
    /**
     * Creates the main panel that will be displayed when a user selects "Mods" from the main menu.
     *
     * @param gameMenu The game menu.
     * @return The UI panel for the mod menu.
     */
    @NotNull
    static SecondPanelUi getModMenuPanel(@NotNull GameMenuGui gameMenu) {
        return new ModMenuGui(gameMenu);
    }

    /**
     * Returns a factory for creating a custom configuration screen for this mod.
     * <p>
     * If this method returns a non-null factory, Mod Menu will display a clickable configure icon
     * on your mod's icon. Clicking this button will invoke
     * the factory's {@link ModConfigPanelFactory#create(GameMenuGui)} method to open your
     * mod's settings panel.
     *
     * @return A factory for creating a configuration panel, or {@code null} if the mod
     * has no configuration screen.
     * @see ModConfigPanelFactory
     */
    @Nullable
    default ModConfigPanelFactory getModConfigPanelFactory() {
        return null;
    }

    /**
     * Returns a custom update checker for this mod.
     * <p>
     * The Mod Menu will use this checker to determine if a new version of the mod is
     * available and will display an indicator in the UI if an update is found.
     *
     * @return An {@link UpdateChecker} implementation, or {@code null} if update
     * checking should be disabled for this mod.
     * @see UpdateChecker
     */
    @Nullable
    default UpdateChecker getUpdateChecker() {
        return null;
    }
}
