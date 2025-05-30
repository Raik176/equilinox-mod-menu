package de.rhm176.mixin;

import de.rhm176.ui.ModMenuGui;
import gameMenu.DnaButtonGui;
import gameMenu.GameMenuGui;
import gameMenu.MenuPanelGui;
import guis.GuiComponent;
import guis.GuiTexture;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import textures.Texture;
import userInterfaces.ClickListener;

@Debug(export = true)
@Mixin(MenuPanelGui.class)
public abstract class MenuPanelGuiMixin extends GuiComponent {
    @Shadow
    private GameMenuGui gameMenu;

    @Shadow
    protected abstract DnaButtonGui addButton(int index, GuiTexture line, String text, ClickListener listener);

    @Inject(method = "init", at = @At("TAIL"))
    private void addModsButton(CallbackInfo ci) {
        addButton(8, new GuiTexture(Texture.getEmptyTexture()), "Mods", event -> {
            if (event.isLeftClick()) {
                this.gameMenu.setNewSecondaryScreen(new ModMenuGui(gameMenu));
            }
        });
    }
}
