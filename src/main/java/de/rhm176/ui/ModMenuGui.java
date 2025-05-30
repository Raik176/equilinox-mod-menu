package de.rhm176.ui;

import fontRendering.Text;
import gameMenu.GameMenuGui;
import gameMenu.SecondPanelUi;
import java.awt.*;
import java.io.IOException;
import mainGuis.ColourPalette;
import mainGuis.EquilinoxGuis;
import mainGuis.GuiRepository;
import mainGuis.UiSettings;
import net.fabricmc.loader.api.FabricLoader;
import userInterfaces.GuiPanel;
import userInterfaces.GuiTextButton;
import userInterfaces.Listener;

public class ModMenuGui extends SecondPanelUi {
    private static final float BUTTON_PADDING = 0.01f;

    private final ModMenuModInfoUi infoUi;
    private final ModMenuListUi listUi;

    public ModMenuGui(GameMenuGui gameMenu) {
        super(gameMenu);

        infoUi = new ModMenuModInfoUi();
        listUi = new ModMenuListUi(infoUi);
    }

    @Override
    protected void init() {
        super.init();

        addComponent(infoUi, 0.58F, 0.05F, 0.4f, 0.8F);

        addButton(
                "Open Mods Folder",
                (on) -> {
                    try {
                        Desktop.getDesktop()
                                .open(FabricLoader.getInstance()
                                        .getGameDir()
                                        .resolve("mods")
                                        .toFile());
                    } catch (IOException e) {
                        EquilinoxGuis.notify(
                                "[Mod Menu]", "Failed to open the mods directory.", GuiRepository.INFO, null);
                        System.out.println("Could not open the mods folder:");
                        e.printStackTrace(System.out);
                    }
                },
                0.58F);

        addButton(
                "Placeholder",
                (on) -> {
                    EquilinoxGuis.notify("[Mod Menu]", "Failed to open the mods directory.", GuiRepository.INFO, null);
                },
                0.78F + (BUTTON_PADDING / 2.0f));

        addComponent(listUi, 0.075f, 0.05F, 0.5F, 0.9F);
    }

    private void addButton(String text, Listener listener, float relX) {
        Text guiText =
                Text.newText(text).setFontSize(UiSettings.NORM_FONT).center().create();
        guiText.setColour(ColourPalette.WHITE);
        ModMenuButton button = new ModMenuButton(guiText);

        button.addListener(listener);
        addComponent(button, relX, 0.875f, 0.195f, 0.05f);
    }

    public static class ModMenuButton extends GuiTextButton {
        public ModMenuButton(Text text) {
            this(text, 0.7F);
        }

        public ModMenuButton(Text text, float alpha) {
            super(text);

            addComponent(new GuiPanel(ColourPalette.DARK_GREY, alpha), 0, 0, 1, 1);
        }
    }
}
