package de.rhm176.modmenu.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import textures.TextureBuilder;
import textures.TextureData;
import textures.TextureManager;

@Mixin(TextureManager.class)
public interface TextureManagerAccessor {
    @Invoker("loadTextureToOpenGL")
    static int modmenu$loadTextureToOpenGL(TextureData data, TextureBuilder builder) {
        throw new AssertionError();
    }
}
