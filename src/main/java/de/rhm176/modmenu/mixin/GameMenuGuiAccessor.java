package de.rhm176.modmenu.mixin;

import gameMenu.GameMenuGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameMenuGui.class)
public interface GameMenuGuiAccessor {
    @Invoker("removeTertiaryScreen")
    void modmenu$removeTertiaryScreen();
}
