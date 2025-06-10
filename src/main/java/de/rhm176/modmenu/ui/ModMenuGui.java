package de.rhm176.modmenu.ui;

import de.rhm176.api.lang.I18n;
import de.rhm176.modmenu.ModMenu;
import de.rhm176.modmenu.util.LogUtil;
import fontRendering.Text;
import gameMenu.GameMenuGui;
import gameMenu.SecondPanelUi;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import mainGuis.ColourPalette;
import mainGuis.EquilinoxGuis;
import mainGuis.GuiRepository;
import mainGuis.UiSettings;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.ApiStatus;
import userInterfaces.GuiPanel;
import userInterfaces.GuiTextButton;
import userInterfaces.Listener;

@ApiStatus.Internal
public class ModMenuGui extends SecondPanelUi {
    private static final float BUTTON_PADDING = 0.01f;

    private final ModMenuModInfoUi infoUi;
    private final ModMenuListUi listUi;

    public ModMenuGui(GameMenuGui gameMenu) {
        super(gameMenu);

        infoUi = new ModMenuModInfoUi();
        listUi = new ModMenuListUi(infoUi, gameMenu);
    }

    @Override
    protected void init() {
        super.init();

        addComponent(infoUi, 0.58F, 0.05F, 0.4f, 0.8F);

        addButton(
                I18n.translate("modmenu.modsFolder"),
                (on) -> tryOpenFolder(FabricLoader.getInstance().getGameDir().resolve("mods")),
                0.58F);

        addButton(
                I18n.translate("modmenu.configFolder"),
                (on) -> tryOpenFolder(FabricLoader.getInstance().getGameDir().resolve("config")),
                0.78F + (BUTTON_PADDING / 2.0f));

        addComponent(listUi, 0.075f, 0.05F, 0.5F, 0.9F);
    }

    private static void tryOpenFolder(Path path) {
        File asFile = path.toFile();
        if (asFile.isFile()) return;

        try {
            Files.createDirectories(path);

            Desktop.getDesktop().open(asFile);
        } catch (IOException e) {
            EquilinoxGuis.notify(
                    "[" + ModMenu.MOD_MENU_CONTAINER.getMetadata().getName() + "]",
                    I18n.translate("modmenu.error.directory", asFile.getName()),
                    GuiRepository.INFO,
                    null);
            LogUtil.err("Could not open the " + asFile.getName() + " folder:", e);
        }
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
