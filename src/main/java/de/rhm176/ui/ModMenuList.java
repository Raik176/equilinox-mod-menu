package de.rhm176.ui;

import de.rhm176.Mod;
import de.rhm176.ModMenu;
import de.rhm176.ModMenuUtil;
import de.rhm176.mixin.TextureManagerAccessor;
import evolveStatusOverview.EvolveOverviewUi;
import fontRendering.Text;
import guiRendering.GuiRenderData;
import guis.GuiComponent;
import guis.GuiTexture;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mainGuis.ColourPalette;
import mainGuis.GuiRepository;
import mainGuis.UiSettings;
import net.fabricmc.loader.api.metadata.CustomValue;
import org.lwjgl.util.vector.Vector2f;
import textures.Texture;
import textures.TextureBuilder;
import textures.TextureData;
import toolbox.Colour;
import toolbox.MyMouse;
import userInterfaces.GuiImage;
import userInterfaces.GuiPanel;

public class ModMenuList extends GuiComponent {
    public static final int ELEMENT_PADDING = 8;

    private final List<ModMenuListElement> modMenuListElements = new ArrayList<>();
    private final ModMenuModInfoUi infoUi;
    private int pixelHeight;

    public ModMenuList(ModMenuModInfoUi infoUi) {
        this.pixelHeight = 10;
        this.infoUi = infoUi;

        for (Mod mod : ModMenu.MODS.values()) {
            ModMenuListElement elem = new ModMenuListElement(this, mod);
            this.pixelHeight = this.pixelHeight + elem.getHeightInPixels();
            this.modMenuListElements.add(elem);

            this.pixelHeight += ELEMENT_PADDING;
        }
    }

    public void setSelectedMod(ModMenuListElement selectedMod) {
        int index = modMenuListElements.indexOf(selectedMod);
        if (index == -1) {
            return;
        }

        for (int i = 0; i < modMenuListElements.size(); i++) {
            if (i == index) {
                modMenuListElements.get(i).select();
            } else {
                modMenuListElements.get(i).unselect();
            }
        }

        infoUi.showModInfo(selectedMod);
    }

    public List<ModMenuListElement> getElements() {
        return List.copyOf(modMenuListElements);
    }

    public int getHeightInPixels() {
        return this.pixelHeight;
    }

    @Override
    protected void init() {
        super.init();
        float yPos = pixelsToRelativeY(10.0F);

        for (ModMenuListElement elem : this.modMenuListElements) {
            yPos = this.addListElement(elem, yPos);
            yPos += pixelsToRelativeY(ELEMENT_PADDING);
        }
    }

    private float addListElement(ModMenuListElement elem, float yPos) {
        float xPad = pixelsToRelativeX(10.0F);
        float rightPad = pixelsToRelativeX(20.0F);
        float yScale = pixelsToRelativeY(elem.getHeightInPixels());
        addComponent(elem, xPad, yPos, 1.0F - (rightPad + xPad), yScale);
        return yPos + yScale;
    }

    @Override
    protected void updateGuiTexturePositions(Vector2f var1, Vector2f var2) {}

    @Override
    protected void updateSelf() {
        if (this.infoUi.getCurrentMod() == null) {
            // if I set the selected mod in the init function, the icon doesn't work, but here it does???
            setSelectedMod(this.modMenuListElements.get(0));
        }
    }

    @Override
    protected void getGuiTextures(GuiRenderData var1) {}

    public static class ModMenuListElement extends GuiComponent {
        private static final float MOD_BADGE_PADDING = 0.02f;

        private final ModMenuList parent;
        private final GuiTexture background;
        private final Mod mod;

        private GuiImage icon;

        public ModMenuListElement(ModMenuList parent, Mod mod) {
            this.mod = mod;
            this.parent = parent;

            this.background = new GuiTexture(GuiRepository.BLOCK);
            this.background.setOverrideColour(
                    ColourPalette.DARK_GREY.duplicate().scale(1.3F));

            loadIcon();
        }

        public Mod getMod() {
            return mod;
        }

        public GuiTexture getIcon() {
            return icon.getTexture();
        }

        private void addName(float xStart, float yPos) {
            Text name = Text.newText(mod.getName())
                    .setFontSize(UiSettings.NORM_FONT)
                    .create();
            name.setColour(ColourPalette.WHITE);
            addText(name, xStart, yPos, 1.0F);
        }

        private void addDescription(float xStart, float yPos) {
            String desc = mod.getDescription();
            for (String elem : desc.split("\n")) {
                Text text = Text.newText(elem).setFontSize(UiSettings.NORM_FONT).create();
                text.setColour(ColourPalette.LIGHT_GREY);

                addText(text, xStart, yPos, 1 - xStart);

                yPos += getRelativeHeightCoords(text.getHeight());
            }
        }

        private void loadIcon() {
            final int ICON_SIZE = 64;

            ByteBuffer buf = mod.getIconBuffer(ICON_SIZE);

            if (buf == null) {
                buf = ModMenuUtil.loadPng(
                        ModMenu.MODS.get(ModMenu.MOD_ID).getContainer(),
                        "assets/" + ModMenu.MOD_ID + "/unknown_icon.png",
                        ICON_SIZE);
            }

            CustomValue.CvObject customValue = Optional.ofNullable(
                            mod.getContainer().getMetadata().getCustomValue(ModMenu.MOD_ID))
                    .map(CustomValue::getAsObject)
                    .orElse(null);

            if (buf != null) {
                TextureBuilder builder = Texture.newTexture(null);
                if (!mod.getId().equals("equilinox")
                        && ModMenuUtil.getBoolean("pixel-perfect-icon", customValue)
                                .orElse(true))
                    builder = builder.noFiltering().nearestFiltering().clampEdges();

                int texID = TextureManagerAccessor.callLoadTextureToOpenGL(
                        new TextureData(buf, ICON_SIZE, ICON_SIZE), builder);
                Texture texture = Texture.getEmptyTexture();
                texture.setTextureID(texID);

                icon = new GuiImage(texture);
            }
        }

        @Override
        public void remove() {
            if (icon != null) {
                icon.getTexture().getTexture().delete();
            }

            super.remove();
        }

        @Override
        protected void init() {
            super.init();

            float yStart = 2.0F / getPixelHeight();
            float yScale = 1.0F - 2.0F * yStart;
            addComponent(icon, 2.0F / getPixelWidth(), yStart, getRelativeWidthCoords(yScale), yScale);

            float xStart = getRelativeWidthCoords(1.0F) + pixelsToRelativeX(5.0F);
            float yPos = pixelsToRelativeY(2);
            float gap = pixelsToRelativeY(EvolveOverviewUi.TEXT_HEIGHT);
            this.addName(xStart - pixelsToRelativeX(5), yPos);

            /*
            // Literally no matter what i do, the clipping for these little shits
            // does not work. I'm starting to really dislike this game's ui system.
            float badgeWidth = 0.15f;
            float badgeXPos = 1 - badgeWidth - MOD_BADGE_PADDING;
            for (ModBadgeType badge : mod.getBadges()) {
                ModBadge modBadge = new ModBadge(badge.getColor(), badge.getName());
                addComponent(
                        modBadge,
                        badgeXPos,
                        yPos,
                        badgeWidth,
                        0.25f
                );

                badgeXPos -= badgeWidth + MOD_BADGE_PADDING;
            }
             */

            yPos += gap;
            this.addDescription(xStart, yPos);
        }

        public int getHeightInPixels() {
            return 70 + 10;
        }

        @Override
        protected void updateGuiTexturePositions(Vector2f position, Vector2f scale) {
            this.background.setPosition(position.x, position.y, scale.x, scale.y);
        }

        // TODO: maybe highlight the entry here
        public void select() {}

        // TODO:
        public void unselect() {}

        @Override
        protected void updateSelf() {
            this.background.update();

            MyMouse mouse = MyMouse.getActiveMouse();
            Vector2f pos = getPosition();
            Vector2f scale = getScale();

            if (mouse.isLeftClick()
                    && mouse.getX() >= pos.x
                    && mouse.getY() >= pos.y
                    && mouse.getX() <= pos.x + scale.x
                    && mouse.getY() <= pos.y + scale.y) {
                parent.setSelectedMod(this);
            }
        }

        @Override
        protected void getGuiTextures(GuiRenderData data) {
            data.addTexture(this.getLevel(), this.background);
        }

        @Override
        protected void setTextureClippingBounds(int[] bounds) {
            background.setClippingBounds(bounds);
        }
    }

    private static class ModBadge extends GuiPanel {
        private static final float DARKENING_FACTOR = 0.6f;

        private final Text text;

        public ModBadge(Colour colour, String text) {
            super(
                    colour,
                    2,
                    new Colour(
                            colour.getR() * DARKENING_FACTOR,
                            colour.getG() * DARKENING_FACTOR,
                            colour.getB() * DARKENING_FACTOR));

            this.text = Text.newText(text)
                    .setFontSize(UiSettings.NORM_FONT)
                    .center()
                    .create();
            this.text.setColour(ColourPalette.WHITE);
        }

        @Override
        protected void init() {
            super.init();

            addText(text, 0, -0.1f, 1);
        }
    }
}
