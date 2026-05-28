/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.newgui.screens;

import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.meteorclient.gui.newgui.FontManager;
import meteordevelopment.meteorclient.gui.newgui.GuiColors;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.screens.HudEditorScreen;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.other.Snapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.Iterator;

/**
 * New-theme HUD editor. Renders live HUD elements, overlays a themed
 * inactive/hover box on each, supports drag (with snapping), left-click
 * toggle, right-click → open {@link NewHudElementScreen} for that element
 * (or the legacy add-element list for empty space), and Delete to remove.
 * Coordinates are in native pixels to match {@link HudElement#x}/y.
 */
public class NewHudEditorScreen extends Screen implements Snapper.Container {
    private static final int INACTIVE_BG = 0x32C81919; // red, low alpha
    private static final int INACTIVE_OL = 0xC8C81919;
    private static final int HOVER_BG    = 0x32000000 | (GuiColors.ACCENT & 0x00FFFFFF);
    private static final int HOVER_OL    = 0xC8000000 | (GuiColors.ACCENT & 0x00FFFFFF);
    private static final int HINT_BG     = 0xB0000000 | (GuiColors.BG_PANEL & 0x00FFFFFF);
    private static final int HINT_BORDER = 0xB0000000 | (GuiColors.BORDER_LIGHT & 0x00FFFFFF);

    private final Hud hud;
    private final Snapper snapper;

    private HudElement dragging;
    private boolean moved;
    private int lastMouseX, lastMouseY;
    private int clickX, clickY;
    private double dragRemainderX, dragRemainderY; // sub-pixel accumulator

    public NewHudEditorScreen() {
        super(Text.literal("Hud Editor"));
        this.hud = Hud.get();
        this.snapper = new Snapper(this);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double s = MinecraftClient.getInstance().getWindow().getScaleFactor();
        int mx = (int) (mouseX * s);
        int my = (int) (mouseY * s);
        clickX = mx;
        clickY = my;
        lastMouseX = mx;
        lastMouseY = my;
        dragRemainderX = 0;
        dragRemainderY = 0;
        moved = false;

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            dragging = getHovered(mx, my);
            // Mark Minecraft's internal drag state so it keeps dispatching drag
            // events to this screen (some MouseHandler paths gate on this).
            setDragging(true);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (dragging != null && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            double s = MinecraftClient.getInstance().getWindow().getScaleFactor();
            // Use the vanilla deltas scaled to native pixels; accumulate the
            // sub-pixel fraction so slow drags still register over time.
            double nativeDx = dx * s + dragRemainderX;
            double nativeDy = dy * s + dragRemainderY;
            int ddx = (int) nativeDx;
            int ddy = (int) nativeDy;
            dragRemainderX = nativeDx - ddx;
            dragRemainderY = nativeDy - ddy;
            if (ddx != 0 || ddy != 0) {
                snapper.move(dragging, ddx, ddy);
                moved = true;
            }
            lastMouseX += ddx;
            lastMouseY += ddy;
            return true;
        }
        double s = MinecraftClient.getInstance().getWindow().getScaleFactor();
        lastMouseX = (int) (mouseX * s);
        lastMouseY = (int) (mouseY * s);
        return false;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        double s = MinecraftClient.getInstance().getWindow().getScaleFactor();
        lastMouseX = (int) (mouseX * s);
        lastMouseY = (int) (mouseY * s);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        double s = MinecraftClient.getInstance().getWindow().getScaleFactor();
        int mx = (int) (mouseX * s);
        int my = (int) (mouseY * s);

        if (!moved) {
            HudElement hovered = getHovered(mx, my);
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                if (hovered != null) hovered.toggle();
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                if (hovered != null) {
                    MinecraftClient.getInstance().setScreen(new NewHudElementScreen(hovered, this));
                } else {
                    MinecraftClient.getInstance().setScreen(new NewAddHudElementScreen(mx, my, this));
                }
            }
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            snapper.unsnap();
            dragging = null;
            moved = false;
            dragRemainderX = 0;
            dragRemainderY = 0;
            setDragging(false);
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_DELETE) {
            HudElement hovered = getHovered(lastMouseX, lastMouseY);
            if (hovered != null) {
                hovered.remove();
                return true;
            }
        } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            HudElement hovered = getHovered(lastMouseX, lastMouseY);
            if (hovered != null) {
                hovered.toggle();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!Utils.canUpdate()) renderBackground(context, mouseX, mouseY, delta);

        // HudElement coords are native pixels — render them under the unscaled
        // projection, then restore the ORTHOGRAPHIC GUI projection. We don't
        // use Utils.scaledProjection because it sets ProjectionType.PERSPECTIVE
        // which breaks subsequent GUI fills.
        Matrix4f savedProj = new Matrix4f(RenderSystem.getProjectionMatrix());
        Utils.unscaledProjection();
        HudEditorScreen.renderElements(context);
        RenderSystem.setProjectionMatrix(savedProj, ProjectionType.ORTHOGRAPHIC);

        double s = MinecraftClient.getInstance().getWindow().getScaleFactor();
        int mx = (int) (mouseX * s);
        int my = (int) (mouseY * s);

        HudElement hovered = dragging != null ? dragging : getHovered(mx, my);
        for (HudElement e : hud) {
            if (!e.isActive()) drawNativeBox(context, e.x, e.y, e.getWidth(), e.getHeight(), INACTIVE_BG, INACTIVE_OL);
        }
        if (hovered != null) {
            drawNativeBox(context, hovered.x, hovered.y, hovered.getWidth(), hovered.getHeight(), HOVER_BG, HOVER_OL);
        }

        drawHint(context);
    }

    private void drawHint(DrawContext context) {
        FontManager fm = FontManager.get();
        String hint = "Left click: toggle / drag   \u2022   Right click: edit (or add)   \u2022   Delete: remove   \u2022   Esc: close";
        int rowH = fm.getRowHeight();
        int w = fm.getTextWidth(hint) + 10;
        int h = rowH + 4;
        int x = (this.width - w) / 2;
        int y = this.height - h - 4;
        context.fill(x, y, x + w, y + h, HINT_BG);
        context.fill(x, y, x + w, y + 1, HINT_BORDER);
        context.fill(x, y + h - 1, x + w, y + h, HINT_BORDER);
        context.fill(x, y, x + 1, y + h, HINT_BORDER);
        context.fill(x + w - 1, y, x + w, y + h, HINT_BORDER);
        fm.drawText(context, hint, x + 5, y + (h - fm.getTextHeight()) / 2, fm.getTextColor());
    }

    private HudElement getHovered(int mx, int my) {
        HudElement result = null;
        for (HudElement e : hud) {
            if (mx >= e.x && mx <= e.x + e.getWidth() && my >= e.y && my <= e.y + e.getHeight()) {
                result = e; // later elements draw on top — prefer them as hover target
            }
        }
        return result;
    }

    /** Fill + 1-pixel outline at native resolution (element coords are already native). */
    private void drawNativeBox(DrawContext ctx, int x, int y, int w, int h, int bg, int ol) {
        float scale = (float) MinecraftClient.getInstance().getWindow().getScaleFactor();
        MatrixStack ms = ctx.getMatrices();
        ms.push();
        ms.scale(1f / scale, 1f / scale, 1f);
        ctx.fill(x + 1, y + 1, x + w - 1, y + h - 1, bg);
        ctx.fill(x, y, x + w, y + 1, ol);
        ctx.fill(x, y + h - 1, x + w, y + h, ol);
        ctx.fill(x, y + 1, x + 1, y + h - 1, ol);
        ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, ol);
        ms.pop();
    }

    // --- Snapper.Container ---

    @Override
    public Iterable<Snapper.Element> getElements() {
        return () -> new Iterator<>() {
            private final Iterator<HudElement> it = hud.iterator();
            @Override public boolean hasNext() { return it.hasNext(); }
            @Override public Snapper.Element next() { return it.next(); }
        };
    }

    @Override
    public boolean shouldNotSnapTo(Snapper.Element element) {
        return element == dragging;
    }

    @Override
    public int getSnappingRange() {
        return hud.snappingRange.get();
    }

    /** Used by {@link HudEditorScreen#isOpen()} via the patched check in its companion. */
    public static boolean matches(Screen s) {
        return s instanceof NewHudEditorScreen || s instanceof NewHudElementScreen;
    }

    @SuppressWarnings("unused")
    private static int clamp(int v, int lo, int hi) { return MathHelper.clamp(v, lo, hi); }
}
