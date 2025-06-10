package de.rhm176.modmenu.mixin;

import de.rhm176.modmenu.api.ModConfigPanelUi;
import de.rhm176.modmenu.api.ModMenuApi;
import de.rhm176.modmenu.duck.SecondPanelUiDuck;
import gameMenu.GameMenuGui;
import gameMenu.SecondPanelUi;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import userInterfaces.ClickListener;

@Mixin(SecondPanelUi.class)
public class SecondPanelUiMixin implements SecondPanelUiDuck {
    @Shadow
    @Final
    private GameMenuGui gameMenu;

    @Unique
    private boolean shouldCloseToModmenu = false;

    // TODO: make this less messy
    @ModifyArg(
            method = "createBackOption",
            at =
                    @At(
                            value = "INVOKE",
                            target = "LuserInterfaces/IconButtonUi;addListener(LuserInterfaces/ClickListener;)V"))
    private ClickListener redirectBackButtonToModMenu(ClickListener par1) {
        return shouldCloseToModmenu
                ? guiClickEvent -> {
                    if (guiClickEvent.isLeftClick()) {
                        if ((Object) this instanceof ModConfigPanelUi modConfigPanelUi) modConfigPanelUi.close();
                        gameMenu.setNewSecondaryScreen(ModMenuApi.getModMenuPanel(gameMenu));
                    }
                }
                : par1;
    }

    @Override
    public void modmenu$setShouldCloseToModmenu() {
        shouldCloseToModmenu = true;
    }
}
