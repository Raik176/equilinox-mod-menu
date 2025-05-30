package de.rhm176.ui;

import mainGuis.ColourPalette;
import userInterfaces.GuiScrollPanel;

public class ModMenuListUi extends GuiScrollPanel {
    private final ModMenuList listUi;
    private final ModMenuModInfoUi infoUi;

    public ModMenuListUi(ModMenuModInfoUi infoUi) {
        super(ColourPalette.DARK_GREY, 0.7F);

        this.listUi = new ModMenuList(infoUi);
        this.infoUi = infoUi;
    }

    @Override
    protected void init() {
        super.init();

        setContents(this.listUi, this.listUi.getHeightInPixels() / this.getPixelHeight());
    }
}
