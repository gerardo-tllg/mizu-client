package meteordevelopment.meteorclient.gui.newgui.components;

import javassist.bytecode.Opcode;
import javassist.compiler.TokenId;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.newgui.FontManager;
import meteordevelopment.meteorclient.gui.newgui.GuiColors;
import meteordevelopment.meteorclient.gui.newgui.util.RenderUtils;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.class_1291;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import org.lwjgl.glfw.GLFW;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/newgui/components/ModuleButton.class */
public class ModuleButton {
    private final Module module;
    private final Category category;
    private static final int ACCENT_X = 3;
    private static ModuleButton listeningForKey = null;
    private boolean expanded = false;
    private float expandAnimation = 0.0f;

    public ModuleButton(Module module, Category category) {
        this.module = module;
        this.category = category;
    }

    private int getRowHeight() {
        return FontManager.get().getRowHeight();
    }

    private int nativePixelGap() {
        float scale = (float) class_310.method_1551().method_22683().method_4495();
        return Math.max(1, (int) Math.ceil(1.0d / ((double) scale)));
    }

    private int textY(int y, int rowH) {
        return y + ((rowH - FontManager.get().getTextHeight()) / 2);
    }

    private void fillNative(class_332 context, int x1, int y1, int x2, int y2, int color) {
        float scale = (float) class_310.method_1551().method_22683().method_4495();
        class_4587 matrices = context.method_51448();
        matrices.method_22903();
        matrices.method_22905(1.0f / scale, 1.0f / scale, 1.0f);
        context.method_25294((int) (x1 * scale), (int) (y1 * scale), (int) (x2 * scale), (int) (y2 * scale), color);
        matrices.method_22909();
    }

    public void render(class_332 context, int x, int y, int width, int mouseX, int mouseY) {
        FontManager fm = FontManager.get();
        int rowH = getRowHeight();
        float target = this.expanded ? 1.0f : 0.0f;
        this.expandAnimation += (target - this.expandAnimation) * 0.35f;
        if (Math.abs(this.expandAnimation - target) < 0.01f) {
            this.expandAnimation = target;
        }
        if (this.module.isActive()) {
            RenderUtils.fillNativeHInset(context, x, y, width, rowH, 4, fm.secondaryAlpha(Opcode.IF_ICMPNE));
        }
        int textColor = this.module.isActive() ? fm.getTextColor() : fm.getTextSecondary();
        boolean titleHovered = mouseY >= y && mouseY < y + rowH && mouseX >= x && mouseX < x + width;
        context.method_44379(x + 3, y, (x + width) - 3, y + rowH);
        fm.drawTextMarquee(context, this.module, this.module.title, x + 5, textY(y, rowH), (x + width) - 5, textColor, titleHovered);
        context.method_44380();
        if (this.expandAnimation > 0.01f && hasAnyVisibleSetting()) {
            renderSettings(context, x, y + rowH + nativePixelGap(), width, mouseX, mouseY);
        }
    }

    private boolean hasAnyVisibleSetting() {
        for (SettingGroup group : this.module.settings) {
            for (Setting<?> setting : group) {
                if (setting.isVisible()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void renderSettings(class_332 context, int x, int y, int width, int mouseX, int mouseY) {
        FontManager fm = FontManager.get();
        int settingsHeight = getSettingsHeight();
        int visibleHeight = (int) (settingsHeight * this.expandAnimation);
        context.method_44379(x, y, x + width, y + visibleHeight);
        RenderUtils.drawThinVLine(context, x + 3, y, settingsHeight, fm.secondaryAlpha(Opcode.F2L));
        int currentY = SettingGroupRenderer.renderGroups(context, this.module.settings, x, y, width, mouseX, mouseY);
        renderModuleKeybind(context, fm, x, currentY, width);
        context.method_44380();
    }

    private void renderModuleKeybind(class_332 context, FontManager fm, int x, int y, int width) {
        String text;
        int color;
        int rowH = getRowHeight();
        context.method_44379(x, y, x + width, y + rowH);
        if (listeningForKey == this) {
            text = "Key ...";
            color = fm.getTextColor();
        } else {
            text = "Key " + getKeyName(this.module.keybind.getValue());
            color = GuiColors.TEXT_DISABLED;
        }
        fm.drawText(context, text, x + 6, textY(y, rowH), color);
        context.method_44380();
    }

    private static String getKeyName(int key) {
        if (key == -1) {
            return "NONE";
        }
        String name = GLFW.glfwGetKeyName(key, 0);
        if (name != null) {
            return name.toUpperCase();
        }
        switch (key) {
            case 32:
                return "Space";
            case 256:
                return "Esc";
            case 257:
                return "Enter";
            case 258:
                return "Tab";
            case 259:
                return "Back";
            case 260:
                return "Ins";
            case 261:
                return "Del";
            case 262:
                return "Right";
            case 263:
                return "Left";
            case 264:
                return "Down";
            case 265:
                return "Up";
            case 266:
                return "PgUp";
            case 267:
                return "PgDn";
            case 268:
                return "Home";
            case 269:
                return "End";
            case 280:
                return "Caps";
            case 290:
                return "F1";
            case 291:
                return "F2";
            case 292:
                return "F3";
            case 293:
                return "F4";
            case 294:
                return "F5";
            case 295:
                return "F6";
            case 296:
                return "F7";
            case 297:
                return "F8";
            case 298:
                return "F9";
            case 299:
                return "F10";
            case TokenId.ABSTRACT /* 300 */:
                return "F11";
            case TokenId.BOOLEAN /* 301 */:
                return "F12";
            case TokenId.THROW /* 340 */:
                return "LShift";
            case TokenId.THROWS /* 341 */:
                return "LCtrl";
            case TokenId.TRANSIENT /* 342 */:
                return "LAlt";
            case TokenId.VOID /* 344 */:
                return "RShift";
            case TokenId.VOLATILE /* 345 */:
                return "RCtrl";
            case TokenId.WHILE /* 346 */:
                return "RAlt";
            default:
                return "KEY " + key;
        }
    }

    public static boolean onKeyPressed(int key) {
        return SettingGroupRenderer.onKeyPressed(key);
    }

    public static boolean onCharTyped(char chr) {
        return SettingGroupRenderer.onCharTyped(chr);
    }

    public static boolean isListening() {
        return listeningForKey != null || SettingGroupRenderer.isListening();
    }

    public static class_1799 statusEffectIcon(class_1291 effect) {
        return SettingGroupRenderer.statusEffectIcon(effect);
    }

    public boolean mouseClicked(int x, int y, int width, int mouseX, int mouseY, int button) {
        int rowH = getRowHeight();
        if (mouseY >= y && mouseY < y + rowH) {
            if (button == 0) {
                this.module.toggle();
                this.module.sendToggledMsg();
                return true;
            }
            if (button == 1 && hasAnyVisibleSetting()) {
                this.expanded = !this.expanded;
                return true;
            }
        }
        if (this.expanded && this.expandAnimation > 0.5f && hasAnyVisibleSetting()) {
            int settingsY = y + rowH + nativePixelGap();
            if (SettingGroupRenderer.mouseClickedGroups(this.module.settings, x, settingsY, width, mouseX, mouseY, button, () -> {
                return GuiThemes.get().moduleScreen(this.module);
            })) {
                return true;
            }
            int settingsHeight = SettingGroupRenderer.getGroupsHeight(this.module.settings);
            int keybindY = settingsY + settingsHeight;
            if (mouseY >= keybindY && mouseY < keybindY + rowH && button == 0) {
                listeningForKey = this;
                Modules.get().setModuleToBind(this.module);
                return true;
            }
            return false;
        }
        return false;
    }

    public static void clearKeyListeningIfMatches(Module module) {
        if (listeningForKey != null && listeningForKey.module == module) {
            listeningForKey = null;
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int button) {
        SettingGroupRenderer.mouseReleasedGroups();
    }

    public Module getModule() {
        return this.module;
    }

    public int getHeight() {
        int rowH = getRowHeight();
        if (this.expandAnimation < 0.01f) {
            return rowH;
        }
        int full = rowH + getSettingsHeight() + nativePixelGap();
        return (int) (rowH + ((full - rowH) * this.expandAnimation));
    }

    private int getSettingsHeight() {
        return SettingGroupRenderer.getGroupsHeight(this.module.settings) + getRowHeight();
    }

    public boolean isExpanded() {
        return this.expanded;
    }
}
