package de.rhm176.ui;

import de.rhm176.Mod;
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
import toolbox.Colour;
import userInterfaces.GuiScrollPanel;
import userInterfaces.GuiTextButton;

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
        float lastHeight = 0;
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

        Map<String, String> links = mod.getLinks();
        if (!links.isEmpty()) {
            Text linksText = newText("Links:", ColourPalette.LIGHT_GREY);
            addText(linksText, 0.01f, currentY, 0.99f);
            currentY += getRelativeHeightCoords(linksText.getHeight());

            for (String s : links.keySet()) {
                Text linkText = newText(s, ColourPalette.BLUE_TEXT);
                GuiTextButton linkButton = new GuiTextButton(linkText);
                linkButton.addListener((on) -> {
                    try {
                        Desktop.getDesktop().browse(new URI(links.get(s)));
                    } catch (IOException | URISyntaxException e) {
                        EquilinoxGuis.notify("[Mod Menu]", "Failed to open website.", GuiRepository.INFO, null);
                        System.out.println("Could not open website:");
                        e.printStackTrace(System.out);
                    }
                });

                addComponent(linkButton, 0.03f, currentY, 0.2f, 0.075f);

                currentY += getRelativeHeightCoords(linkText.getHeight());
            }

            currentY += ELEMENT_PADDING;
        }

        Set<String> licenses = mod.getLicenses();
        if (!licenses.isEmpty()) {
            Text licensesText = newText("Licenses:", ColourPalette.LIGHT_GREY);
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
            Text creditsText = newText("Credits:", ColourPalette.LIGHT_GREY);
            addText(creditsText, 0.01f, currentY, 0.99f);
            currentY += getRelativeHeightCoords(creditsText.getHeight());

            for (String s : credits.keySet()) {
                // Author -> Authors. should work in a lot of cases.
                Text typeText = newText((s.endsWith("r") ? (s + "s") : s) + ":", ColourPalette.LIGHT_GREY);
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

    private static Text newText(String content, Colour color) {
        Text text = Text.newText(content).setFontSize(UiSettings.NORM_FONT).create();

        text.setColour(color);

        return text;
    }
}
