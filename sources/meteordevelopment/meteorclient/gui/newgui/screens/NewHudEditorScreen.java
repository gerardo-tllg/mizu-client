package meteordevelopment.meteorclient.gui.newgui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Iterator;
import meteordevelopment.meteorclient.gui.newgui.FontManager;
import meteordevelopment.meteorclient.gui.newgui.GuiColors;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.screens.HudEditorScreen;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.other.Snapper;
import net.minecraft.class_10366;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_437;
import net.minecraft.class_4587;
import org.joml.Matrix4f;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/newgui/screens/NewHudEditorScreen.class */
public class NewHudEditorScreen extends class_437 implements Snapper.Container {
    private static final int INACTIVE_BG = 851974425;
    private static final int INACTIVE_OL = -926410471;
    private static final int HOVER_BG = 838860800 | (GuiColors.ACCENT & 16777215);
    private static final int HOVER_OL = (-939524096) | (GuiColors.ACCENT & 16777215);
    private static final int HINT_BG = (-1342177280) | (GuiColors.BG_PANEL & 16777215);
    private static final int HINT_BORDER = (-1342177280) | (GuiColors.BORDER_LIGHT & 16777215);
    private final Hud hud;
    private final Snapper snapper;
    private HudElement dragging;
    private boolean moved;
    private int lastMouseX;
    private int lastMouseY;
    private int clickX;
    private int clickY;
    private double dragRemainderX;
    private double dragRemainderY;

    public NewHudEditorScreen() {
        super(class_2561.method_43470("Hud Editor"));
        this.hud = Hud.get();
        this.snapper = new Snapper(this);
    }

    public boolean method_25421() {
        return false;
    }

    public boolean method_25402(double mouseX, double mouseY, int button) {
        double s = class_310.method_1551().method_22683().method_4495();
        int mx = (int) (mouseX * s);
        int my = (int) (mouseY * s);
        this.clickX = mx;
        this.clickY = my;
        this.lastMouseX = mx;
        this.lastMouseY = my;
        this.dragRemainderX = 0.0d;
        this.dragRemainderY = 0.0d;
        this.moved = false;
        if (button == 0) {
            this.dragging = getHovered(mx, my);
            method_25398(true);
            return true;
        }
        return false;
    }

    public boolean method_25403(double mouseX, double mouseY, int button, double dx, double dy) {
        if (this.dragging != null && button == 0) {
            double s = class_310.method_1551().method_22683().method_4495();
            double nativeDx = (dx * s) + this.dragRemainderX;
            double nativeDy = (dy * s) + this.dragRemainderY;
            int ddx = (int) nativeDx;
            int ddy = (int) nativeDy;
            this.dragRemainderX = nativeDx - ((double) ddx);
            this.dragRemainderY = nativeDy - ((double) ddy);
            if (ddx != 0 || ddy != 0) {
                this.snapper.move(this.dragging, ddx, ddy);
                this.moved = true;
            }
            this.lastMouseX += ddx;
            this.lastMouseY += ddy;
            return true;
        }
        double s2 = class_310.method_1551().method_22683().method_4495();
        this.lastMouseX = (int) (mouseX * s2);
        this.lastMouseY = (int) (mouseY * s2);
        return false;
    }

    public void method_16014(double mouseX, double mouseY) {
        double s = class_310.method_1551().method_22683().method_4495();
        this.lastMouseX = (int) (mouseX * s);
        this.lastMouseY = (int) (mouseY * s);
    }

    public boolean method_25406(double mouseX, double mouseY, int button) {
        double s = class_310.method_1551().method_22683().method_4495();
        int mx = (int) (mouseX * s);
        int my = (int) (mouseY * s);
        boolean mutated = false;
        if (!this.moved) {
            HudElement hovered = getHovered(mx, my);
            if (button == 0) {
                if (hovered != null) {
                    hovered.toggle();
                    mutated = true;
                }
            } else if (button == 1) {
                if (hovered != null) {
                    class_310.method_1551().method_1507(new NewHudElementScreen(hovered, this));
                } else {
                    class_310.method_1551().method_1507(new NewAddHudElementScreen(mx, my, this));
                }
            }
        } else if (button == 0) {
            mutated = true;
        }
        if (button == 0) {
            this.snapper.unsnap();
            this.dragging = null;
            this.moved = false;
            this.dragRemainderX = 0.0d;
            this.dragRemainderY = 0.0d;
            method_25398(false);
        }
        if (mutated) {
            this.hud.save();
            return false;
        }
        return false;
    }

    public boolean method_25404(int keyCode, int scanCode, int modifiers) {
        HudElement hovered;
        if (keyCode == 261) {
            HudElement hovered2 = getHovered(this.lastMouseX, this.lastMouseY);
            if (hovered2 != null) {
                hovered2.remove();
                this.hud.save();
                return true;
            }
        } else if ((keyCode == 257 || keyCode == 335) && (hovered = getHovered(this.lastMouseX, this.lastMouseY)) != null) {
            hovered.toggle();
            this.hud.save();
            return true;
        }
        return super.method_25404(keyCode, scanCode, modifiers);
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        if (!Utils.canUpdate()) {
            method_25420(context, mouseX, mouseY, delta);
        }
        Matrix4f savedProj = new Matrix4f(RenderSystem.getProjectionMatrix());
        Utils.unscaledProjection();
        HudEditorScreen.renderElements(context);
        RenderSystem.setProjectionMatrix(savedProj, class_10366.field_54954);
        double s = class_310.method_1551().method_22683().method_4495();
        int mx = (int) (((double) mouseX) * s);
        int my = (int) (((double) mouseY) * s);
        HudElement hovered = this.dragging != null ? this.dragging : getHovered(mx, my);
        for (HudElement e : this.hud) {
            if (!e.isActive()) {
                drawNativeBox(context, e.x, e.y, e.getWidth(), e.getHeight(), INACTIVE_BG, INACTIVE_OL);
            }
        }
        if (hovered != null) {
            drawNativeBox(context, hovered.x, hovered.y, hovered.getWidth(), hovered.getHeight(), HOVER_BG, HOVER_OL);
        }
        drawHint(context);
    }

    private void drawHint(class_332 context) {
        FontManager fm = FontManager.get();
        int rowH = fm.getRowHeight();
        int w = fm.getTextWidth("Left click: toggle / drag   •   Right click: edit (or add)   •   Delete: remove   •   Esc: close") + 10;
        int h = rowH + 4;
        int x = (this.field_22789 - w) / 2;
        int y = (this.field_22790 - h) - 4;
        context.method_25294(x, y, x + w, y + h, HINT_BG);
        context.method_25294(x, y, x + w, y + 1, HINT_BORDER);
        context.method_25294(x, (y + h) - 1, x + w, y + h, HINT_BORDER);
        context.method_25294(x, y, x + 1, y + h, HINT_BORDER);
        context.method_25294((x + w) - 1, y, x + w, y + h, HINT_BORDER);
        fm.drawText(context, "Left click: toggle / drag   •   Right click: edit (or add)   •   Delete: remove   •   Esc: close", x + 5, y + ((h - fm.getTextHeight()) / 2), fm.getTextColor());
    }

    private HudElement getHovered(int mx, int my) {
        HudElement result = null;
        for (HudElement e : this.hud) {
            if (mx >= e.x && mx <= e.x + e.getWidth() && my >= e.y && my <= e.y + e.getHeight()) {
                result = e;
            }
        }
        return result;
    }

    private void drawNativeBox(class_332 ctx, int x, int y, int w, int h, int bg, int ol) {
        float scale = (float) class_310.method_1551().method_22683().method_4495();
        class_4587 ms = ctx.method_51448();
        ms.method_22903();
        ms.method_22905(1.0f / scale, 1.0f / scale, 1.0f);
        ctx.method_25294(x + 1, y + 1, (x + w) - 1, (y + h) - 1, bg);
        ctx.method_25294(x, y, x + w, y + 1, ol);
        ctx.method_25294(x, (y + h) - 1, x + w, y + h, ol);
        ctx.method_25294(x, y + 1, x + 1, (y + h) - 1, ol);
        ctx.method_25294((x + w) - 1, y + 1, x + w, (y + h) - 1, ol);
        ms.method_22909();
    }

    @Override // meteordevelopment.meteorclient.utils.other.Snapper.Container
    public Iterable<Snapper.Element> getElements() {
        return () -> {
            return new Iterator<Snapper.Element>() { // from class: meteordevelopment.meteorclient.gui.newgui.screens.NewHudEditorScreen.1
                private final Iterator<HudElement> it;

                {
                    this.it = NewHudEditorScreen.this.hud.iterator();
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
        return element == this.dragging;
    }

    @Override // meteordevelopment.meteorclient.utils.other.Snapper.Container
    public int getSnappingRange() {
        return this.hud.snappingRange.get().intValue();
    }

    public static boolean matches(class_437 s) {
        return (s instanceof NewHudEditorScreen) || (s instanceof NewHudElementScreen);
    }

    private static int clamp(int v, int lo, int hi) {
        return class_3532.method_15340(v, lo, hi);
    }
}
