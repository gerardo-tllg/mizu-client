package meteordevelopment.meteorclient.systems.hud.screens;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.newgui.screens.NewAddHudElementScreen;
import meteordevelopment.meteorclient.gui.newgui.screens.NewHudEditorScreen;
import meteordevelopment.meteorclient.gui.newgui.screens.NewHudElementPresetsScreen;
import meteordevelopment.meteorclient.gui.newgui.screens.NewHudElementScreen;
import meteordevelopment.meteorclient.gui.tabs.builtin.HudTab;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.other.Snapper;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_437;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/screens/HudEditorScreen.class */
public class HudEditorScreen extends WidgetScreen implements Snapper.Container {
    private static final Color SPLIT_LINES_COLOR = new Color(255, 255, 255, 75);
    private static final Color INACTIVE_BG_COLOR = new Color(200, 25, 25, 50);
    private static final Color INACTIVE_OL_COLOR = new Color(200, 25, 25, 200);
    private static final Color HOVER_BG_COLOR = new Color(200, 200, 200, 50);
    private static final Color HOVER_OL_COLOR = new Color(200, 200, 200, 200);
    private static final Color SELECTION_BG_COLOR = new Color(225, 225, 225, 25);
    private static final Color SELECTION_OL_COLOR = new Color(225, 225, 225, 100);
    private final Hud hud;
    private final Snapper snapper;
    private Snapper.Element selectionSnapBox;
    private int lastMouseX;
    private int lastMouseY;
    private boolean pressed;
    private int clickX;
    private int clickY;
    private final List<HudElement> selection;
    private boolean moved;
    private boolean dragging;
    private HudElement addedHoveredToSelectionWhenClickedElement;
    private double splitLinesAnimation;

    public HudEditorScreen(GuiTheme theme) {
        super(theme, "Hud Editor");
        this.selection = new ArrayList();
        this.hud = Hud.get();
        this.snapper = new Snapper(this);
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public void initWidgets() {
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public boolean method_25402(double mouseX, double mouseY, int button) {
        double s = MeteorClient.mc.method_22683().method_4495();
        double mouseX2 = mouseX * s;
        double mouseY2 = mouseY * s;
        if (button == 0) {
            this.pressed = true;
            this.selectionSnapBox = null;
            HudElement hovered = getHovered((int) mouseX2, (int) mouseY2);
            this.dragging = hovered != null;
            if (this.dragging) {
                if (!this.selection.contains(hovered)) {
                    this.selection.clear();
                    this.selection.add(hovered);
                    this.addedHoveredToSelectionWhenClickedElement = hovered;
                }
            } else {
                this.selection.clear();
            }
            this.clickX = (int) mouseX2;
            this.clickY = (int) mouseY2;
            return false;
        }
        return false;
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public void method_16014(double mouseX, double mouseY) {
        double s = MeteorClient.mc.method_22683().method_4495();
        double mouseX2 = mouseX * s;
        double mouseY2 = mouseY * s;
        if (this.dragging && !this.selection.isEmpty()) {
            if (this.selectionSnapBox == null) {
                this.selectionSnapBox = new SelectionBox();
            }
            this.snapper.move(this.selectionSnapBox, ((int) mouseX2) - this.lastMouseX, ((int) mouseY2) - this.lastMouseY);
        }
        if (this.pressed) {
            this.moved = true;
        }
        this.lastMouseX = (int) mouseX2;
        this.lastMouseY = (int) mouseY2;
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public boolean method_25406(double mouseX, double mouseY, int button) {
        double s = MeteorClient.mc.method_22683().method_4495();
        double mouseX2 = mouseX * s;
        double mouseY2 = mouseY * s;
        if (button == 0) {
            this.pressed = false;
        }
        if (this.addedHoveredToSelectionWhenClickedElement != null) {
            this.selection.remove(this.addedHoveredToSelectionWhenClickedElement);
            this.addedHoveredToSelectionWhenClickedElement = null;
        }
        if (this.moved) {
            if (button == 0 && !this.dragging) {
                fillSelection((int) mouseX2, (int) mouseY2);
            }
        } else if (button == 0) {
            HudElement hovered = getHovered((int) mouseX2, (int) mouseY2);
            if (hovered != null) {
                hovered.toggle();
            }
        } else if (button == 1) {
            HudElement hovered2 = getHovered((int) mouseX2, (int) mouseY2);
            if (hovered2 != null) {
                MeteorClient.mc.method_1507(new HudElementScreen(this.theme, hovered2));
            } else {
                MeteorClient.mc.method_1507(new AddHudElementScreen(this.theme, this.lastMouseX, this.lastMouseY));
            }
        }
        if (button == 0) {
            this.snapper.unsnap();
            this.dragging = false;
            this.moved = false;
            return false;
        }
        return false;
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public boolean method_25404(int keyCode, int scanCode, int modifiers) {
        if (!this.pressed) {
            if (keyCode == 257 || keyCode == 335) {
                HudElement hovered = getHovered(this.lastMouseX, this.lastMouseY);
                if (hovered != null) {
                    hovered.toggle();
                }
            } else if (keyCode == 261) {
                HudElement hovered2 = getHovered(this.lastMouseX, this.lastMouseY);
                if (hovered2 != null) {
                    hovered2.remove();
                } else {
                    for (HudElement element : this.selection) {
                        element.remove();
                    }
                    this.selection.clear();
                }
            }
        }
        return super.method_25404(keyCode, scanCode, modifiers);
    }

    private void fillSelection(int mouseX, int mouseY) {
        int x1 = Math.min(this.clickX, mouseX);
        int x2 = Math.max(this.clickX, mouseX);
        int y1 = Math.min(this.clickY, mouseY);
        int y2 = Math.max(this.clickY, mouseY);
        for (HudElement e : this.hud) {
            if (e.getX() <= x2 && e.getX2() >= x1 && e.getY() <= y2 && e.getY2() >= y1) {
                this.selection.add(e);
            }
        }
    }

    @Override // meteordevelopment.meteorclient.utils.other.Snapper.Container
    public Iterable<Snapper.Element> getElements() {
        return () -> {
            return new Iterator<Snapper.Element>() { // from class: meteordevelopment.meteorclient.systems.hud.screens.HudEditorScreen.1
                private final Iterator<HudElement> it;

                {
                    this.it = HudEditorScreen.this.hud.iterator();
                }

                @Override // java.util.Iterator
                public boolean hasNext() {
                    return this.it.hasNext();
                }

                /* JADX WARN: Can't rename method to resolve collision */
                @Override // java.util.Iterator
                public Snapper.Element next() {
                    return this.it.next();
                }
            };
        };
    }

    @Override // meteordevelopment.meteorclient.utils.other.Snapper.Container
    public boolean shouldNotSnapTo(Snapper.Element element) {
        return this.selection.contains((HudElement) element);
    }

    @Override // meteordevelopment.meteorclient.utils.other.Snapper.Container
    public int getSnappingRange() {
        return this.hud.snappingRange.get().intValue();
    }

    private void onRender(int mouseX, int mouseY) {
        HudElement hovered;
        for (HudElement element : this.hud) {
            if (!element.isActive()) {
                renderElement(element, INACTIVE_BG_COLOR, INACTIVE_OL_COLOR);
            }
        }
        if (this.pressed && !this.dragging) {
            fillSelection(mouseX, mouseY);
        }
        Iterator<HudElement> it = this.selection.iterator();
        while (it.hasNext()) {
            renderElement(it.next(), HOVER_BG_COLOR, HOVER_OL_COLOR);
        }
        if (this.pressed && !this.dragging) {
            this.selection.clear();
        }
        if (this.pressed && !this.dragging) {
            int x1 = Math.min(this.clickX, mouseX);
            int x2 = Math.max(this.clickX, mouseX);
            int y1 = Math.min(this.clickY, mouseY);
            int y2 = Math.max(this.clickY, mouseY);
            renderQuad(x1, y1, x2 - x1, y2 - y1, SELECTION_BG_COLOR, SELECTION_OL_COLOR);
        }
        if (this.pressed || (hovered = getHovered(mouseX, mouseY)) == null) {
            return;
        }
        renderElement(hovered, HOVER_BG_COLOR, HOVER_OL_COLOR);
    }

    private HudElement getHovered(int mouseX, int mouseY) {
        for (HudElement element : this.hud) {
            if (mouseX >= element.x && mouseX <= element.x + element.getWidth() && mouseY >= element.y && mouseY <= element.y + element.getHeight()) {
                return element;
            }
        }
        return null;
    }

    private void renderQuad(double x, double y, double w, double h, Color bgColor, Color olColor) {
        Renderer2D.COLOR.quad(x + 1.0d, y + 1.0d, w - 2.0d, h - 2.0d, bgColor);
        Renderer2D.COLOR.quad(x, y, w, 1.0d, olColor);
        Renderer2D.COLOR.quad(x, (y + h) - 1.0d, w, 1.0d, olColor);
        Renderer2D.COLOR.quad(x, y + 1.0d, 1.0d, h - 2.0d, olColor);
        Renderer2D.COLOR.quad((x + w) - 1.0d, y + 1.0d, 1.0d, h - 2.0d, olColor);
    }

    private void renderElement(HudElement element, Color bgColor, Color olColor) {
        renderQuad(element.x, element.y, element.getWidth(), element.getHeight(), bgColor, olColor);
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        if (!Utils.canUpdate()) {
            method_25420(context, mouseX, mouseY, delta);
        }
        double s = MeteorClient.mc.method_22683().method_4495();
        int mouseX2 = (int) (((double) mouseX) * s);
        int mouseY2 = (int) (((double) mouseY) * s);
        Utils.unscaledProjection();
        boolean renderSplitLines = this.pressed && !this.selection.isEmpty() && this.moved;
        if (renderSplitLines || this.splitLinesAnimation > 0.0d) {
            renderSplitLines(renderSplitLines, delta / 20.0f);
        }
        renderElements(context);
        Renderer2D.COLOR.begin();
        onRender(mouseX2, mouseY2);
        Renderer2D.COLOR.render();
        Utils.scaledProjection();
        runAfterRenderTasks();
    }

    public static void renderElements(class_332 drawContext) {
        Hud hud = Hud.get();
        boolean inactiveOnly = Utils.canUpdate() && hud.active;
        HudRenderer.INSTANCE.begin(drawContext);
        for (HudElement element : hud) {
            element.updatePos();
            if (inactiveOnly) {
                if (!element.isActive()) {
                    element.render(HudRenderer.INSTANCE);
                }
            } else {
                element.render(HudRenderer.INSTANCE);
            }
        }
        HudRenderer.INSTANCE.end();
    }

    private void renderSplitLines(boolean increment, double delta) {
        if (increment) {
            this.splitLinesAnimation += delta * 6.0d;
        } else {
            this.splitLinesAnimation -= delta * 6.0d;
        }
        this.splitLinesAnimation = class_3532.method_15350(this.splitLinesAnimation, 0.0d, 1.0d);
        Renderer2D renderer = Renderer2D.COLOR;
        renderer.begin();
        double w = Utils.getWindowWidth();
        double h = Utils.getWindowHeight();
        double w3 = w / 3.0d;
        double h3 = h / 3.0d;
        int prevA = SPLIT_LINES_COLOR.a;
        Color color = SPLIT_LINES_COLOR;
        color.a = (int) (((double) color.a) * this.splitLinesAnimation);
        renderSplitLine(renderer, w3, 0.0d, w3, h);
        renderSplitLine(renderer, w3 * 2.0d, 0.0d, w3 * 2.0d, h);
        renderSplitLine(renderer, 0.0d, h3, w, h3);
        renderSplitLine(renderer, 0.0d, h3 * 2.0d, w, h3 * 2.0d);
        SPLIT_LINES_COLOR.a = prevA;
        renderer.render();
    }

    private void renderSplitLine(Renderer2D renderer, double x, double y, double destX, double destY) {
        double incX = 0.0d;
        double incY = 0.0d;
        if (x == destX) {
            incY = ((double) Utils.getWindowWidth()) / 25.0d;
        } else {
            incX = ((double) Utils.getWindowWidth()) / 25.0d;
        }
        while (true) {
            renderer.line(x, y, x + incX, y + incY, SPLIT_LINES_COLOR);
            x += incX * 2.0d;
            y += incY * 2.0d;
            if (x >= destX && y >= destY) {
                return;
            }
        }
    }

    public static boolean isOpen() {
        class_437 s = MeteorClient.mc.field_1755;
        return (s instanceof HudEditorScreen) || (s instanceof AddHudElementScreen) || (s instanceof HudElementPresetsScreen) || (s instanceof HudElementScreen) || (s instanceof HudTab.HudScreen) || (s instanceof NewHudEditorScreen) || (s instanceof NewHudElementScreen) || (s instanceof NewAddHudElementScreen) || (s instanceof NewHudElementPresetsScreen);
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/screens/HudEditorScreen$SelectionBox.class */
    private class SelectionBox implements Snapper.Element {
        private int x;
        private int y;
        private final int width;
        private final int height;

        public SelectionBox() {
            int x1 = Integer.MAX_VALUE;
            int y1 = Integer.MAX_VALUE;
            int x2 = 0;
            int y2 = 0;
            for (HudElement element : HudEditorScreen.this.selection) {
                if (element.getX() < x1) {
                    x1 = element.getX();
                } else if (element.getX() > x2) {
                    x2 = element.getX();
                }
                if (element.getX2() < x1) {
                    x1 = element.getX2();
                } else if (element.getX2() > x2) {
                    x2 = element.getX2();
                }
                if (element.getY() < y1) {
                    y1 = element.getY();
                } else if (element.getY() > y2) {
                    y2 = element.getY();
                }
                if (element.getY2() < y1) {
                    y1 = element.getY2();
                } else if (element.getY2() > y2) {
                    y2 = element.getY2();
                }
            }
            this.x = x1;
            this.y = y1;
            this.width = x2 - x1;
            this.height = y2 - y1;
        }

        @Override // meteordevelopment.meteorclient.utils.other.Snapper.Element
        public int getX() {
            return this.x;
        }

        @Override // meteordevelopment.meteorclient.utils.other.Snapper.Element
        public int getY() {
            return this.y;
        }

        @Override // meteordevelopment.meteorclient.utils.other.Snapper.Element
        public int getWidth() {
            return this.width;
        }

        @Override // meteordevelopment.meteorclient.utils.other.Snapper.Element
        public int getHeight() {
            return this.height;
        }

        @Override // meteordevelopment.meteorclient.utils.other.Snapper.Element
        public void setPos(int x, int y) {
            for (HudElement element : HudEditorScreen.this.selection) {
                element.setPos(x + (element.x - this.x), y + (element.y - this.y));
            }
            this.x = x;
            this.y = y;
        }

        @Override // meteordevelopment.meteorclient.utils.other.Snapper.Element
        public void move(int deltaX, int deltaY) {
            int prevX = this.x;
            int prevY = this.y;
            int border = Hud.get().border.get().intValue();
            this.x = class_3532.method_15340(this.x + deltaX, border, (Utils.getWindowWidth() - this.width) - border);
            this.y = class_3532.method_15340(this.y + deltaY, border, (Utils.getWindowHeight() - this.height) - border);
            for (HudElement element : HudEditorScreen.this.selection) {
                element.move(this.x - prevX, this.y - prevY);
            }
        }
    }
}
