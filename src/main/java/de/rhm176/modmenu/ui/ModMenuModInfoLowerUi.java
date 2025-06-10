package de.rhm176.modmenu.ui;

import de.rhm176.api.lang.I18n;
import de.rhm176.modmenu.Mod;
import de.rhm176.modmenu.ModMenu;
import de.rhm176.modmenu.api.update.UpdateInfo;
import de.rhm176.modmenu.util.LogUtil;
import fontRendering.Text;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import mainGuis.ColourPalette;
import mainGuis.EquilinoxGuis;
import mainGuis.GuiRepository;
import mainGuis.UiSettings;
import org.jetbrains.annotations.ApiStatus;
import toolbox.Colour;
import userInterfaces.GuiScrollPanel;
import userInterfaces.GuiTextButton;

@ApiStatus.Internal
public class ModMenuModInfoLowerUi extends GuiScrollPanel {
    private final float ELEMENT_PADDING = 0.05f;
    private final Mod mod;

    public ModMenuModInfoLowerUi(Mod mod) {
        super(ColourPalette.DARK_GREY, 0.8F);

        this.mod = mod;
    }

    @Override
    protected void init() {
        super.init();

        float currentY = 0;
        float lastHeight;
        for (String s : mod.getDescription().split("\n")) {
            Text description = newText(s, ColourPalette.BRIGHT_GREY);
            addText(description, 0.01f, currentY, 0.99f);
            lastHeight = getRelativeHeightCoords(description.getHeight());
            if (description.getNumberOfLines() > 1) {
                lastHeight += getRelativeHeightCoords(description.getHeight()) / 4;
            }

            currentY += lastHeight;
        }
        currentY += ELEMENT_PADDING;

        if (mod.getUpdateInfo() != null) {
            UpdateInfo updateInfo = mod.getUpdateInfo();

            Text updateTitle = newText(I18n.translate("modmenu.update.available"), ColourPalette.LIGHT_GREY);
            addText(updateTitle, 0.01f, currentY, 0.99f);
            currentY += getRelativeHeightCoords(updateTitle.getHeight());

            String versionString = updateInfo.updateMessage();
            Text versionText = newText(versionString, ColourPalette.BRIGHT_GREY);
            addText(versionText, 0.03f, currentY, 0.98f);
            currentY += getRelativeHeightCoords(versionText.getHeight());

            currentY += ELEMENT_PADDING / 2;

            Text downloadButtonText = newText(I18n.translate("modmenu.update.download"), ColourPalette.BLUE_TEXT);
            GuiTextButton downloadButton =
                    createLinkButton(updateInfo.updateUrl().toString(), downloadButtonText);

            addComponent(downloadButton, 0.03f, currentY, 0.25f, 0.075f);
            currentY += 0.075f;

            currentY += ELEMENT_PADDING;
        }

        Map<String, String> links = mod.getLinks();
        if (!links.isEmpty()) {
            Text linksText = newText(I18n.translate("modmenu.links"), ColourPalette.LIGHT_GREY);
            addText(linksText, 0.01f, currentY, 0.99f);
            currentY += getRelativeHeightCoords(linksText.getHeight());

            for (String s : links.keySet()) {
                Text linkText = newText(I18n.translate(s), ColourPalette.BLUE_TEXT);
                GuiTextButton linkButton = createLinkButton(links.get(s), linkText);

                addComponent(linkButton, 0.03f, currentY, 0.2f, 0.075f);

                currentY += 0.075f * 0.975f; // DO NOT TOUCH SECOND NUMBER, IT IS FINE-TUNED!!
            }

            currentY += ELEMENT_PADDING;
        }

        Set<String> licenses = mod.getLicenses();
        if (!licenses.isEmpty()) {
            Text licensesText = newText(I18n.translate("modmenu.licenses"), ColourPalette.LIGHT_GREY);
            addText(licensesText, 0.01f, currentY, 0.99f);
            currentY += getRelativeHeightCoords(licensesText.getHeight());

            for (String s : licenses) {
                Text licenseText = newText(s, ColourPalette.BRIGHT_GREY);
                addText(licenseText, 0.03f, currentY, 0.98f);
                currentY += getRelativeHeightCoords(licenseText.getHeight());
            }

            currentY += ELEMENT_PADDING;
        }

        SortedMap<String, Set<String>> credits = mod.getCredits();
        if (!credits.isEmpty()) {
            Text creditsText = newText(I18n.translate("modmenu.credits"), ColourPalette.LIGHT_GREY);
            addText(creditsText, 0.01f, currentY, 0.99f);
            currentY += getRelativeHeightCoords(creditsText.getHeight());

            for (String s : credits.keySet()) {
                // Author -> Authors. should work in a lot of cases.
                String text = I18n.translateWithFallback(
                        "modmenu.credits.role" + s.replaceAll("[ -]", "_").toLowerCase(),
                        s.endsWith("r") ? (s + "s") : s);
                Text typeText = newText(text + ":", ColourPalette.LIGHT_GREY);
                addText(typeText, 0.03f, currentY, 0.98f);
                currentY += getRelativeHeightCoords(typeText.getHeight());

                for (String s2 : credits.get(s)) {
                    Text personText = newText(s2, ColourPalette.BRIGHT_GREY);
                    addText(personText, 0.05f, currentY, 0.98f);
                    currentY += getRelativeHeightCoords(personText.getHeight());
                }

                currentY += ELEMENT_PADDING;
            }

            currentY += ELEMENT_PADDING;
        }
    }

    private static GuiTextButton createLinkButton(String link, Text linkText) {
        GuiTextButton linkButton = new GuiTextButton(linkText);
        linkButton.addListener((on) -> {
            try {
                Desktop.getDesktop().browse(new URI(link));
            } catch (IOException | URISyntaxException e) {
                EquilinoxGuis.notify(
                        "[" + ModMenu.MOD_MENU_CONTAINER.getMetadata().getName() + "]",
                        I18n.translate("modmenu.error.link"),
                        GuiRepository.INFO,
                        null);
                LogUtil.err("Could not open website:", e);
            }
        });
        return linkButton;
    }

    private static Text newText(String content, Colour color) {
        Text text = Text.newText(content).setFontSize(UiSettings.NORM_FONT).create();

        text.setColour(color);

        return text;
    }
}
