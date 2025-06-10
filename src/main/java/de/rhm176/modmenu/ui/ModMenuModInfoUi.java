package de.rhm176.modmenu.ui;

import de.rhm176.api.lang.I18n;
import de.rhm176.modmenu.util.LogUtil;
import fontRendering.Text;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Consumer;
import mainGuis.ColourPalette;
import mainGuis.EquilinoxGuis;
import mainGuis.GuiRepository;
import mainGuis.UiSettings;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.util.vector.Vector2f;
import userInterfaces.GuiImage;
import userInterfaces.GuiPanel;
import userInterfaces.Listener;

@ApiStatus.Internal
public class ModMenuModInfoUi extends GuiPanel {
    private static final float BUTTON_PADDING = 0.01f;
    private static final float BUTTON_HEIGHT = 0.05f;
    public static final float PANEL_EDGE_PADDING = 0.01f;

    private ModMenuList.ModMenuListElement currentMod;

    public ModMenuModInfoUi() {
        super(ColourPalette.DARK_GREY, 0.7F);
    }

    public void showModInfo(ModMenuList.ModMenuListElement elem) {
        clear();

        Vector2f iconScale = elem.getIcon().getScale();
        float relScaleY = iconScale.y / getScale().y;
        float relScaleX = iconScale.x / getScale().x;

        addComponent(new GuiPanel(ColourPalette.DARK_GREY, 0.8F), 0, 0, 1, relScaleY);
        addComponent(new GuiImage(elem.getIcon().getTexture()), 0.0f, 0.0f, relScaleX, relScaleY);

        float textPosX = relScaleX + PANEL_EDGE_PADDING;

        Text name = Text.newText(elem.getMod().getName())
                .setFontSize(UiSettings.NORM_FONT)
                .create();
        name.setColour(ColourPalette.WHITE);
        addText(name, textPosX, 0, 1.0F);

        String versionStr = elem.getMod().getVersion();
        Text version = Text.newText(versionStr.startsWith("v") ? versionStr : ("v" + versionStr))
                .setFontSize(UiSettings.NORM_FONT)
                .create();
        version.setColour(ColourPalette.LIGHT_GREY);
        addText(version, textPosX, name.getRelativeY() + getRelativeHeightCoords(name.getHeight()), 1.0F);

        List<String> names = elem.getMod().getAuthors();
        float currentY = version.getRelativeY() + getRelativeHeightCoords(version.getHeight());
        if (!names.isEmpty()) {
            String authorString;
            if (names.size() == 1) {
                authorString = names.get(0);
            } else {
                authorString = String.join(", ", names);
            }

            Text authors = Text.newText(I18n.translate("modmenu.author", authorString))
                    .setFontSize(UiSettings.NORM_FONT)
                    .create();
            authors.setColour(ColourPalette.LIGHT_GREY);
            addText(authors, textPosX, currentY, 1.0F);
            currentY = version.getRelativeY() + getRelativeHeightCoords(version.getHeight());
        }

        float buttonWidth = ((1.0f - PANEL_EDGE_PADDING - textPosX) - BUTTON_PADDING) / 2.0f;
        float buttonY = currentY + (BUTTON_HEIGHT * 1.25f);
        addButton(
                I18n.translate("modmenu.website"),
                (button) -> {
                    if (elem.getMod().getWebsite() == null) button.block();
                },
                (on) -> {
                    try {
                        Desktop.getDesktop().browse(new URI(elem.getMod().getWebsite()));
                    } catch (IOException | URISyntaxException e) {
                        EquilinoxGuis.notify("[Mod Menu]", "Failed to open website.", GuiRepository.INFO, null);
                        LogUtil.err("Could not open website:", e);
                    }
                },
                textPosX,
                buttonY,
                buttonWidth);
        addButton(
                I18n.translate("modmenu.issues"),
                (button) -> {
                    if (elem.getMod().getId().equals("java")
                            || elem.getMod().getId().equals("equilinox")
                            || elem.getMod().getIssueTracker() == null) button.block();
                },
                (on) -> {
                    try {
                        Desktop.getDesktop().browse(new URI(elem.getMod().getIssueTracker()));
                    } catch (IOException | URISyntaxException e) {
                        EquilinoxGuis.notify("[Mod Menu]", "Failed to open issue tracker.", GuiRepository.INFO, null);
                        LogUtil.err("Could not open issue tracker:", e);
                    }
                },
                textPosX + buttonWidth + BUTTON_PADDING,
                buttonY,
                buttonWidth);

        addComponent(new ModMenuModInfoLowerUi(elem.getMod()), 0, relScaleY + 0.02f, 1, 1f - (relScaleY + 0.02f));

        currentMod = elem;
    }

    public ModMenuList.ModMenuListElement getCurrentMod() {
        return currentMod;
    }

    private void addButton(
            String text,
            Consumer<ModMenuGui.ModMenuButton> setupButtonConsumer,
            Listener listener,
            float relX,
            float relY,
            float relScaleX) {
        Text guiText =
                Text.newText(text).setFontSize(UiSettings.NORM_FONT).center().create();
        guiText.setColour(ColourPalette.WHITE);
        ModMenuGui.ModMenuButton button = new ModMenuGui.ModMenuButton(guiText, 1);
        setupButtonConsumer.accept(button);

        button.addListener(listener);
        addComponent(button, relX, relY, relScaleX, BUTTON_HEIGHT);
    }
}
