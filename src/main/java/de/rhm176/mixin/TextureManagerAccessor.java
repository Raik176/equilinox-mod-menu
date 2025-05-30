package de.rhm176.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import textures.TextureBuilder;
import textures.TextureData;
import textures.TextureManager;

@Mixin(TextureManager.class)
public interface TextureManagerAccessor {
    @Invoker
    static int callLoadTextureToOpenGL(TextureData data, TextureBuilder builder) {
        throw new AssertionError();
    }
}
