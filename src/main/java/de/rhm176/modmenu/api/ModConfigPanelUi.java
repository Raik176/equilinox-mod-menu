package de.rhm176.modmenu.api;

import de.rhm176.modmenu.duck.SecondPanelUiDuck;
import gameMenu.GameMenuGui;
import gameMenu.SecondPanelUi;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * An abstract base class for creating a mod configuration screen.
 * <p>
 * Developers should extend this class to create their mod's settings panel.
 * It ensures that when this panel is closed, it correctly returns to the main
 * Mod Menu screen rather than the main game menu.
 * <p>
 * An instance of this panel should be returned by a {@link ModConfigPanelFactory}.
 *
 * @see ModConfigPanelFactory
 * @since 1.0.0
 */
@ApiStatus.AvailableSince("1.0.0")
public abstract class ModConfigPanelUi extends SecondPanelUi {
    /**
     * Constructs a new mod configuration panel.
     *
     * @param gameMenu The game menu.
     */
    public ModConfigPanelUi(@NotNull GameMenuGui gameMenu) {
        super(gameMenu);

        ((SecondPanelUiDuck) this).modmenu$setShouldCloseToModmenu();
    }

    /**
     * A hook that is called when the configuration panel is closed.
     * <p>
     * Subclasses can override this method to perform cleanup operations, such as
     * saving configuration settings. The default implementation does nothing.
     */
    public void close() {}
}
