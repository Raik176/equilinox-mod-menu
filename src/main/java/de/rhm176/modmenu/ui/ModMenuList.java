package de.rhm176.modmenu.ui;

import de.rhm176.api.lang.I18n;
import de.rhm176.modmenu.Mod;
import de.rhm176.modmenu.ModMenu;
import de.rhm176.modmenu.ModMenuUtil;
import de.rhm176.modmenu.api.ModConfigPanelFactory;
import de.rhm176.modmenu.config.Config;
import de.rhm176.modmenu.mixin.GuiPanelAccessor;
import de.rhm176.modmenu.ui.button.ModIconButton;
import evolveStatusOverview.EvolveOverviewUi;
import fontRendering.Text;
import gameMenu.GameMenuGui;
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
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.util.vector.Vector2f;
import textures.Texture;
import toolbox.Colour;
import toolbox.MyMouse;
import userInterfaces.GuiClickable;
import userInterfaces.GuiImage;
import userInterfaces.GuiPanel;

@ApiStatus.Internal
public class ModMenuList extends GuiComponent {
    public static final int ELEMENT_PADDING = 8;

    private final List<ModMenuListElement> modMenuListElements = new ArrayList<>();
    private final ModMenuModInfoUi infoUi;
    final ModMenuListUi listUi;
    private int pixelHeight;
    GameMenuGui gameMenu;

    public ModMenuList(ModMenuListUi listUi, ModMenuModInfoUi infoUi, GameMenuGui gameMenu) {
        this.infoUi = infoUi;
        this.listUi = listUi;
        this.gameMenu = gameMenu;

        for (Mod mod : ModMenu.MODS.values().stream()
                .sorted(Config.instance().sortingOrder.getComparator())
                .toList()) {
            // if (ModMenu.CHILD_MODS.contains(mod.getId())) continue;

            ModMenuListElement elem = new ModMenuListElement(this, mod);
            this.modMenuListElements.add(elem);
        }

        recalculateHeight();
    }

    public void recalculateHeight() {
        this.pixelHeight = 10;

        List<ModMenuListElement> elementsToRender = new ArrayList<>();
        addElementsToList(elementsToRender, this.modMenuListElements, 0);

        for (ModMenuListElement modMenuListElement : elementsToRender) {
            int elementHeight = modMenuListElement.getHeightInPixels();
            this.pixelHeight = this.pixelHeight + elementHeight + ELEMENT_PADDING;
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

    public int getHeightInPixels() {
        return this.pixelHeight;
    }

    @Override
    protected void init() {
        super.init();
        realInit();
    }

    private void addElementsToList(
            List<ModMenuListElement> listToAdd, List<ModMenuListElement> currentLevel, int indent) {
        for (ModMenuListElement elem : currentLevel) {
            elem.setIndentLevel(indent);
            listToAdd.add(elem);
            if (elem.childrenOpen && !elem.children.isEmpty()) {
                addElementsToList(listToAdd, elem.children, indent + 1);
            }
        }
    }

    void realInit() {
        this.clear();

        List<ModMenuListElement> elementsToRender = new ArrayList<>();
        addElementsToList(elementsToRender, this.modMenuListElements, 0);

        float yPos = pixelsToRelativeY(10.0F);

        for (ModMenuListElement elem : elementsToRender) {
            yPos = this.addListElement(elem, yPos);
            yPos += pixelsToRelativeY(ELEMENT_PADDING);
        }
    }

    private float addListElement(ModMenuListElement elem, float yPos) {
        float indentPixels = elem.indentLevel * 20.0f;
        float xPad = pixelsToRelativeX(10.0F + indentPixels);
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

    public static class ModMenuListElement extends GuiClickable {
        private static final float MOD_BADGE_PADDING = 0.02f;
        private static final int ICON_SIZE = 64;

        private static final float ICON_BUTTON_SCALE = 0.95f;
        private static final int ICON_BUTTON_SIZE = (int) (ICON_SIZE * ICON_BUTTON_SCALE);

        private static final Texture CONFIGURE_TEXTURE = ModMenuUtil.createTexture(
                ModMenuUtil.loadPng(
                        ModMenu.MOD_MENU_CONTAINER, "assets/" + ModMenu.MOD_ID + "/configure.png", ICON_BUTTON_SIZE),
                ICON_BUTTON_SIZE,
                ICON_BUTTON_SIZE,
                null);
        private static final Texture CONFIGURE_HIGHLIGHT_TEXTURE = ModMenuUtil.createTexture(
                ModMenuUtil.loadPng(
                        ModMenu.MOD_MENU_CONTAINER,
                        "assets/" + ModMenu.MOD_ID + "/configure_highlight.png",
                        ICON_BUTTON_SIZE),
                ICON_BUTTON_SIZE,
                ICON_BUTTON_SIZE,
                null);

        private static final Texture OPEN_PARENT_TEXTURE = ModMenuUtil.createTexture(
                ModMenuUtil.loadPng(
                        ModMenu.MOD_MENU_CONTAINER, "assets/" + ModMenu.MOD_ID + "/open_parent.png", ICON_BUTTON_SIZE),
                ICON_BUTTON_SIZE,
                ICON_BUTTON_SIZE,
                null);
        private static final Texture OPEN_PARENT_HIGHLIGHT_TEXTURE = ModMenuUtil.createTexture(
                ModMenuUtil.loadPng(
                        ModMenu.MOD_MENU_CONTAINER,
                        "assets/" + ModMenu.MOD_ID + "/open_parent_highlight.png",
                        ICON_BUTTON_SIZE),
                ICON_BUTTON_SIZE,
                ICON_BUTTON_SIZE,
                null);

        private final ModMenuList parent;
        private final GuiTexture background;
        private final Mod mod;
        private final List<ModBadge> badges;
        private final ModIconButton iconButton;

        private final List<ModMenuListElement> children;
        private boolean childrenOpen = false;

        private GuiImage icon;

        private int indentLevel = 0;

        public ModMenuListElement(ModMenuList parent, Mod mod) {
            List<ModMenuListElement> actualChildren = List.of();

            this.mod = mod;
            this.parent = parent;

            this.background = new GuiTexture(GuiRepository.BLOCK);
            this.background.setOverrideColour(
                    ColourPalette.DARK_GREY.duplicate().scale(1.3F));

            this.badges = mod.getBadges().stream()
                    .map(badge -> new ModBadge(badge.getColor(), I18n.translate("modmenu.badge." + badge.getId())))
                    .toList();

            List<String> childrenIds = ModMenu.MOD_CHILDREN.getOrDefault(mod.getId(), List.of());
            /* // Incredibly buggy for seemingly no reason??
            if (!childrenIds.isEmpty()) {
                this.iconButton = new ModIconButton(OPEN_PARENT_TEXTURE, OPEN_PARENT_HIGHLIGHT_TEXTURE);
                this.iconButton.addListener((event) -> {
                    if (event.isLeftClick()) {
                        childrenOpen = !childrenOpen;
                        parent.recalculateHeight();
                        parent.realInit();
                        parent.listUi.realInit();
                    }
                });

                actualChildren = childrenIds.stream().map(id -> new ModMenuListElement(parent, ModMenu.MODS.get(id))).toList();
            } else */ if (mod.getConfigFactory() != null) {
                this.iconButton = new ModIconButton(CONFIGURE_TEXTURE, CONFIGURE_HIGHLIGHT_TEXTURE);
                this.iconButton.addListener((event) -> {
                    if (event.isLeftClick()) {
                        ModConfigPanelFactory configPanelFactory = mod.getConfigFactory();
                        if (configPanelFactory == null) return;

                        parent.gameMenu.setNewTertiaryScreen(configPanelFactory.create(parent.gameMenu));
                    }
                });
            } else {
                iconButton = null;
            }

            this.children = actualChildren;

            loadIcon();
        }

        public void setIndentLevel(int indentLevel) {
            this.indentLevel = indentLevel;
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
            ByteBuffer buf = mod.getIconBuffer(ICON_SIZE);

            if (buf == null) {
                buf = ModMenuUtil.loadPng(
                        ModMenu.MOD_MENU_CONTAINER, "assets/" + ModMenu.MOD_ID + "/unknown_icon.png", ICON_SIZE);
            }

            CustomValue.CvObject customValue = Optional.ofNullable(
                            mod.getContainer().getMetadata().getCustomValue(ModMenu.MOD_ID))
                    .map(CustomValue::getAsObject)
                    .orElse(null);

            if (buf != null) {
                icon = new GuiImage(ModMenuUtil.createTexture(buf, ICON_SIZE, ICON_SIZE, (builder) -> {
                    if (!mod.getId().equals("equilinox")
                            && ModMenuUtil.getBoolean("pixel-perfect-icon", customValue)
                                    .orElse(true))
                        builder.noFiltering().nearestFiltering().clampEdges();
                }));
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
            if (iconButton != null) {
                addComponent(
                        iconButton,
                        2.0F / getPixelWidth() + (getRelativeWidthCoords(yScale) * (1 - ICON_BUTTON_SCALE)) / 2.0f,
                        yStart + (yScale * (1 - ICON_BUTTON_SCALE)) / 2.0f,
                        getRelativeWidthCoords(yScale) * ICON_BUTTON_SCALE,
                        yScale * ICON_BUTTON_SCALE);
            }
            float xStart = getRelativeWidthCoords(1.0F) + pixelsToRelativeX(5.0F);
            float yPos = pixelsToRelativeY(2);
            float gap = pixelsToRelativeY(EvolveOverviewUi.TEXT_HEIGHT);
            this.addName(xStart - pixelsToRelativeX(5), yPos);

            float badgeWidth = 0.15f;
            float badgeXPos = 1 - badgeWidth - MOD_BADGE_PADDING;
            for (ModBadge modBadge : badges) {
                addComponent(modBadge, badgeXPos, yPos, badgeWidth, 0.25f);

                badgeXPos -= badgeWidth + MOD_BADGE_PADDING;
            }

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

            if (iconButton != null) {
                if (this.isMouseOver()) {
                    this.iconButton.setVisible();
                } else {
                    this.iconButton.setInvisible();
                }
            }
        }

        @Override
        protected void getGuiTextures(GuiRenderData data) {
            data.addTexture(this.getLevel(), this.background);
        }

        @Override
        protected void setTextureClippingBounds(int[] bounds) {
            background.setClippingBounds(bounds);

            // why doesn't the game expose literally any way to do this normally?????
            for (ModBadge badge : badges) {
                GuiPanelAccessor accessor = (GuiPanelAccessor) badge;

                accessor.getInner().setClippingBounds(bounds);
                accessor.getOuter().setClippingBounds(bounds);
            }
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

            addText(text, 0, -0.05f, 1);
        }
    }
}
