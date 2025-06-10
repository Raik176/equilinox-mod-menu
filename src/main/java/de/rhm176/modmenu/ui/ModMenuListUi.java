package de.rhm176.modmenu.ui;

import gameMenu.GameMenuGui;
import mainGuis.ColourPalette;
import org.jetbrains.annotations.ApiStatus;
import userInterfaces.GuiScrollPanel;

@ApiStatus.Internal
public class ModMenuListUi extends GuiScrollPanel {
    private final ModMenuList listUi;

    public ModMenuListUi(ModMenuModInfoUi infoUi, GameMenuGui gameMenu) {
        super(ColourPalette.DARK_GREY, 0.7F);

        this.listUi = new ModMenuList(this, infoUi, gameMenu);
    }

    @Override
    protected void init() {
        super.init();
        realInit();
    }

    void realInit() {
        setContents(this.listUi, this.listUi.getHeightInPixels() / this.getPixelHeight());
    }
}
