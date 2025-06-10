package de.rhm176.modmenu.mixin;

import guis.GuiTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import userInterfaces.GuiPanel;

@Mixin(GuiPanel.class)
public interface GuiPanelAccessor {
    @Accessor
    GuiTexture getInner();

    @Accessor
    GuiTexture getOuter();
}
