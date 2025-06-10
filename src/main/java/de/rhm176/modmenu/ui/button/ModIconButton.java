//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package de.rhm176.modmenu.ui.button;

import guiRendering.GuiRenderData;
import guis.GuiTexture;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.util.vector.Vector2f;
import textures.Texture;
import userInterfaces.GuiClickable;
import visualFxDrivers.ConstantDriver;

@ApiStatus.Internal
public class ModIconButton extends GuiClickable {
    private static final ConstantDriver INVISIBLE_DRIVER = new ConstantDriver(0);
    private static final ConstantDriver VISIBLE_DRIVER = new ConstantDriver(1);

    private final GuiTexture normalTexture;
    private final GuiTexture highlightTexture;

    public ModIconButton(Texture normal, Texture highlight) {
        super(1.0F);
        this.normalTexture = new GuiTexture(normal);
        this.highlightTexture = new GuiTexture(highlight);

        this.highlightTexture.setAlphaDriver(INVISIBLE_DRIVER);

        addListener();
    }

    public void setInvisible() {
        this.normalTexture.setAlphaDriver(INVISIBLE_DRIVER);
        this.highlightTexture.setAlphaDriver(INVISIBLE_DRIVER);
    }

    public void setVisible() {
        this.normalTexture.setAlphaDriver(VISIBLE_DRIVER);
    }

    protected void updateSelf() {
        super.updateSelf();
        this.normalTexture.update();
        this.highlightTexture.update();
    }

    protected void updateGuiTexturePositions(Vector2f position, Vector2f scale) {
        super.updateGuiTexturePositions(position, scale);
        this.normalTexture.setPosition(position.x, position.y, scale.x, scale.y);
        this.highlightTexture.setPosition(position.x, position.y, scale.x, scale.y);
    }

    protected void getGuiTextures(GuiRenderData data) {
        data.addTexture(this.getLevel(), this.normalTexture);
        data.addTexture(this.getLevel(), this.highlightTexture);
    }

    protected void setTextureClippingBounds(int[] bounds) {
        this.normalTexture.setClippingBounds(bounds);
        this.highlightTexture.setClippingBounds(bounds);
    }

    private void addListener() {
        super.addListener(event -> {
            if (event.isMouseOver()) {
                this.normalTexture.setAlphaDriver(INVISIBLE_DRIVER);
                this.highlightTexture.setAlphaDriver(VISIBLE_DRIVER);
            } else if (event.isMouseOff()) {
                this.normalTexture.setAlphaDriver(VISIBLE_DRIVER);
                this.highlightTexture.setAlphaDriver(INVISIBLE_DRIVER);
            }
        });
    }
}
