package de.rhm176.modmenu.api;

import gameMenu.GameMenuGui;
import gameMenu.SecondPanelUi;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A factory for creating a mod's configuration user interface panel.
 * <p>
 * This is a {@link FunctionalInterface} whose single method, {@link #create(GameMenuGui)},
 * is responsible for instantiating and returning a configuration screen.
 * <p>
 * An implementation of this interface should be returned from {@link ModMenuApi#getModConfigPanelFactory()}
 * to signal that a mod has a configuration screen.
 *
 * @see ModMenuApi#getModConfigPanelFactory()
 * @see ModConfigPanelUi
 * @since 1.0.0
 */
@ApiStatus.AvailableSince("1.0.0")
@FunctionalInterface
public interface ModConfigPanelFactory {
    /**
     * Creates and returns a new configuration panel instance.
     * <p>
     * It is recommended that the returned panel be an instance of or a subclass
     * of {@link ModConfigPanelUi} to ensure correct navigation behavior.
     *
     * @param gameMenu The game menu.
     * @return A new, non-null instance of the configuration UI panel.
     */
    @NotNull
    SecondPanelUi create(@NotNull GameMenuGui gameMenu);
}
