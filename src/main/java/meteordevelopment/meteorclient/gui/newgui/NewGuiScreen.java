/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.newgui;

import meteordevelopment.meteorclient.gui.newgui.components.ModuleButton;
import meteordevelopment.meteorclient.gui.newgui.components.NewGuiBindCapture;
import meteordevelopment.meteorclient.gui.newgui.components.SettingGroupRenderer;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.gui.Gui;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NewGuiScreen extends Screen {
    // Layout constants
    private static final int SIDEBAR_W = 140;
    private static final int SEARCH_H  = 20;
    private static final int CAT_ROW_H = 18;

    // Search query (static so other code can read it if needed)
    public static String searchQuery = "";

    private final List<Category>                    categories  = new ArrayList<>();
    private final Map<Category, List<ModuleButton>> buttonMap   = new LinkedHashMap<>();
    private final List<ModuleButton>                allButtons  = new ArrayList<>();

    private Category selectedCategory = null;
    private int      moduleScrollOffset = 0;
    private boolean  initialized = false;
    private long     openTime;

    public NewGuiScreen() {
        super(Text.literal("Mizu"));
    }

    // ---- Screen lifecycle ----

    @Override
    protected void init() {
        super.init();
        NewGuiBindCapture.ensureSubscribed();
        openTime = System.currentTimeMillis();
        searchQuery = "";
        moduleScrollOffset = 0;

        try {
            Gui guiMod = Modules.get().get(Gui.class);
            if (guiMod != null) guiMod.applyToFontManager();
        } catch (Throwable ignored) {}

        if (!initialized) {
            for (Category cat : Modules.loopCategories()) {
                categories.add(cat);
                List<ModuleButton> btns = new ArrayList<>();
                for (Module mod : Modules.get().getGroup(cat)) {
                    ModuleButton btn = new ModuleButton(mod, cat);
                    btns.add(btn);
                    allButtons.add(btn);
                }
                buttonMap.put(cat, btns);
            }
            // Default selection: Combat
            selectedCategory = categories.isEmpty() ? null : Categories.Combat;
            if (selectedCategory == null && !categories.isEmpty()) selectedCategory = categories.get(0);
            initialized = true;
        }
    }

    @Override
    public void close() {
        Modules modules = Modules.get();
        if (modules != null && modules.isBinding()) modules.setModuleToBind(null);
        NewGuiBindCapture.get().cancelSettingListen();
        SettingGroupRenderer.commitStringEdit();
        searchQuery = "";
        // Reset so button states are fresh on next open
        initialized = false;
        categories.clear();
        buttonMap.clear();
        allButtons.clear();
        super.close();
    }

    // ---- Rendering ----

    @Override
    public void renderBackground(DrawContext ctx, int mx, int my, float delta) {
        // Solid dark navy — no world showing through
        ctx.fill(0, 0, width, height, 0xFF050e1a);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        drawSearchBar(ctx, mx, my);
        drawSidebar(ctx, mx, my);
        drawModulePanel(ctx, mx, my);
    }

    private int panelTop() { return SEARCH_H + 4; }

    // ---- Search bar ----

    private void drawSearchBar(DrawContext ctx, int mx, int my) {
        int bw = 200;
        int bx = (width - bw) / 2;
        int by = 2;
        int bh = SEARCH_H - 2;

        ctx.fill(bx, by, bx + bw, by + bh, 0xFF060d18);
        boolean active = !searchQuery.isEmpty();
        ctx.drawBorder(bx, by, bw, bh, active ? 0xFF1D9E75 : 0xFF0D3A5C);

        String display = searchQuery.isEmpty() ? "search modules..." : searchQuery;
        int tc = searchQuery.isEmpty() ? 0xFF185FA5 : 0xFFF0F0FA;
        ctx.drawText(client.textRenderer, display, bx + 5, by + 4, tc, false);

        if (active && (System.currentTimeMillis() / 500) % 2 == 0) {
            int cx = bx + 5 + client.textRenderer.getWidth(searchQuery);
            ctx.fill(cx, by + 3, cx + 1, by + bh - 3, 0xFF1D9E75);
        }
    }

    // ---- Sidebar ----

    private void drawSidebar(DrawContext ctx, int mx, int my) {
        int top = panelTop();

        // Background + right border
        ctx.fill(0, top, SIDEBAR_W, height, 0xFF060d18);
        ctx.fill(SIDEBAR_W, top, SIDEBAR_W + 1, height, 0xFF0D3A5C);

        int y = top + 4;
        for (Category cat : categories) {
            boolean sel = cat == selectedCategory && searchQuery.isEmpty();
            boolean hov = mx >= 0 && mx < SIDEBAR_W && my >= y && my < y + CAT_ROW_H;

            // Row background
            if (sel || hov) ctx.fill(0, y, SIDEBAR_W, y + CAT_ROW_H, 0xFF0a1e30);
            // Selected accent bar
            if (sel) ctx.fill(0, y, 2, y + CAT_ROW_H, 0xFF1D9E75);

            int tc = sel ? 0xFF1D9E75 : (hov ? 0xFF378ADD : 0xFF185FA5);
            ctx.drawText(client.textRenderer, cat.name, 10, y + (CAT_ROW_H - 8) / 2, tc, false);
            y += CAT_ROW_H;
        }
    }

    // ---- Module panel ----

    private void drawModulePanel(DrawContext ctx, int mx, int my) {
        int px   = SIDEBAR_W + 4;
        int top  = panelTop();
        int pw   = width - SIDEBAR_W - 8;
        int ph   = height - top;

        List<ModuleButton> buttons = getVisibleButtons();

        // Clamp scroll
        int total = totalHeight(buttons);
        int maxScroll = Math.max(0, total - ph);
        if (moduleScrollOffset > maxScroll) moduleScrollOffset = maxScroll;
        if (moduleScrollOffset < 0) moduleScrollOffset = 0;

        // Scissor to panel area
        ctx.enableScissor(SIDEBAR_W + 1, top, width, height);

        int by = top - moduleScrollOffset + 2;
        for (ModuleButton btn : buttons) {
            btn.render(ctx, px, by, pw, mx, my);
            by += btn.getHeight();
        }

        ctx.disableScissor();

        // Scrollbar
        if (total > ph) {
            int sbx = width - 4;
            int trackH = ph - 4;
            int thumbH = Math.max(16, (int)((long) ph * trackH / total));
            int thumbY = top + 2 + (maxScroll > 0
                ? (int)((long)(trackH - thumbH) * moduleScrollOffset / maxScroll) : 0);
            ctx.fill(sbx, thumbY, sbx + 2, thumbY + thumbH, 0xFF0D3A5C);
        }
    }

    private List<ModuleButton> getVisibleButtons() {
        if (!searchQuery.isEmpty()) {
            String q = searchQuery.toLowerCase();
            List<ModuleButton> res = new ArrayList<>();
            for (ModuleButton b : allButtons) {
                if (b.getModule().title.toLowerCase().contains(q)) res.add(b);
            }
            return res;
        }
        if (selectedCategory == null) return List.of();
        return buttonMap.getOrDefault(selectedCategory, List.of());
    }

    private int totalHeight(List<ModuleButton> buttons) {
        int h = 4;
        for (ModuleButton b : buttons) h += b.getHeight();
        return h;
    }

    // ---- Input ----

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int top = panelTop();

        // Sidebar — category selection
        if ((int) mx < SIDEBAR_W) {
            int y = top + 4;
            for (Category cat : categories) {
                if ((int) my >= y && (int) my < y + CAT_ROW_H) {
                    selectedCategory = cat;
                    moduleScrollOffset = 0;
                    searchQuery = "";
                    return true;
                }
                y += CAT_ROW_H;
            }
            return true; // absorb clicks in sidebar even if not on a category
        }

        // Module panel — delegate to buttons
        int px = SIDEBAR_W + 4;
        int pw = width - SIDEBAR_W - 8;
        List<ModuleButton> buttons = getVisibleButtons();
        int by = top - moduleScrollOffset + 2;
        for (ModuleButton btn : buttons) {
            int bh = btn.getHeight();
            if ((int) my >= by && (int) my < by + bh) {
                if (btn.mouseClicked(px, by, pw, (int) mx, (int) my, button)) return true;
            }
            by += bh;
        }

        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        for (ModuleButton btn : allButtons) btn.mouseReleased((int) mx, (int) my, button);
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double ha, double va) {
        if ((int) mx > SIDEBAR_W) {
            moduleScrollOffset -= (int)(va * 14);
            return true;
        }
        return super.mouseScrolled(mx, my, ha, va);
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (ModuleButton.onKeyPressed(key)) return true;

        if (key == GLFW.GLFW_KEY_BACKSPACE && !searchQuery.isEmpty()) {
            searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
            moduleScrollOffset = 0;
            return true;
        }
        if (key == GLFW.GLFW_KEY_ESCAPE && !searchQuery.isEmpty()) {
            searchQuery = "";
            moduleScrollOffset = 0;
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public boolean charTyped(char chr, int mods) {
        if (ModuleButton.onCharTyped(chr)) return true;
        if (chr >= 32 && chr != 127) {
            searchQuery += chr;
            moduleScrollOffset = 0;
            return true;
        }
        return super.charTyped(chr, mods);
    }

    @Override
    public boolean shouldPause() { return false; }
}
