package de.rhm176.modmenu.compat;

import de.rhm176.api.lang.I18n;
import de.rhm176.modmenu.api.ModConfigPanelUi;
import de.rhm176.modmenu.config.Config;
import de.rhm176.modmenu.config.SortingOrder;
import dropDownBoxUi.ComboBoxUi;
import fontRendering.Text;
import gameMenu.GameMenuGui;
import java.util.Arrays;
import java.util.Locale;
import mainGuis.ColourPalette;
import optionsMenu.CheckOptionUi;
import org.jetbrains.annotations.NotNull;
import toolbox.Colour;
import userInterfaces.GuiPanel;

public class ModMenuConfigPanel extends ModConfigPanelUi {
    private static final float GUI_WIDTH = 0.6f;
    private static final float GUI_HEIGHT = 0.8f;
    private static final Colour TEXT_COLOR = ColourPalette.WHITE;

    public ModMenuConfigPanel(@NotNull GameMenuGui gameMenu) {
        super(gameMenu);
    }

    @Override
    protected void init() {
        super.init();
        addComponent(new ModMenuConfigGui(), (1 - GUI_WIDTH) / 2, (1 - GUI_HEIGHT) / 2, GUI_WIDTH, GUI_HEIGHT);
    }

    @Override
    public void close() {
        Config.instance().save();
    }

    static class ModMenuConfigGui extends GuiPanel {
        public ModMenuConfigGui() {
            super(ColourPalette.DARK_GREY, 0.7F);
        }

        @Override
        protected void init() {
            super.init();

            ComboBoxUi sortingOrder = this.addMenuComponent(
                    0.072500005F,
                    0,
                    I18n.translate("modmenu.config.sortingOrder"),
                    Arrays.stream(SortingOrder.values())
                            .map(s -> I18n.translate(
                                    "modmenu.config.sortingOrder." + s.name().toLowerCase(Locale.ROOT)))
                            .toArray(String[]::new),
                    Config.instance().sortingOrder.ordinal());
            sortingOrder.addSelectionListener(
                    on -> Config.instance().sortingOrder = SortingOrder.values()[sortingOrder.getSelectedIndex()]);

            CheckOptionUi updateCheckerEnabled = new CheckOptionUi(
                            Config.instance().enableUpdateChecking,
                            I18n.translate("modmenu.config.updateCheckerEnabled"),
                            1.11111F)
                    .addListener(on -> Config.instance().enableUpdateChecking = on);
            updateCheckerEnabled.setTextColour(TEXT_COLOR);
            addComponent(updateCheckerEnabled, 0.072500005F, this.getPositionY(2), 0.25f, 0.07F);
        }

        private ComboBoxUi addMenuComponent(float xPos, int row, String name, Object[] options, int selected) {
            this.addText(name, xPos, row, 1.11111F);
            return this.addDropMenu(xPos, row + 1, options, selected);
        }

        private ComboBoxUi addDropMenu(float xPos, int row, Object[] options, int selected) {
            ComboBoxUi menu = new ComboBoxUi(options, selected);
            menu.setFontSize(1.11111F);
            super.addComponent(menu, xPos, this.getPositionY(row) - 0.04F, 0.25f, 0.08F);
            return menu;
        }

        private Text addText(String name, float xPos, int row, float font) {
            Text text = Text.newText(name).setFontSize(font).create();
            text.setColour(TEXT_COLOR);
            super.addText(text, xPos, this.getPositionY(row) - 0.02F, 0.18F);
            return text;
        }

        private float getPositionY(int row) {
            return 0.1F + (float) row * 0.09375F;
        }
    }
}
