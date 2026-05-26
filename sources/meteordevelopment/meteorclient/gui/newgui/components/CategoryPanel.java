package meteordevelopment.meteorclient.gui.newgui.components;

import java.util.ArrayList;
import java.util.List;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.gui.newgui.FontManager;
import meteordevelopment.meteorclient.gui.newgui.util.RenderUtils;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.FontFaceSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.ProvidedStringSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4587;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/newgui/components/CategoryPanel.class */
public class CategoryPanel {
    private final Category category;
    private int x;
    private int y;
    private int width;
    private static final int PADDING = 2;
    private int dragOffsetX;
    private int dragOffsetY;
    private final List<ModuleButton> buttons = new ArrayList();
    private boolean dragging = false;
    private int scrollOffset = 0;
    private int heightCap = 0;

    public CategoryPanel(Category category, int x, int y) {
        this.category = category;
        this.x = x;
        this.y = y;
        List<Module> modules = Modules.get().getGroup(category);
        for (Module module : modules) {
            this.buttons.add(new ModuleButton(module, category));
        }
        recalcWidth();
    }

    public void recalcWidth() {
        FontManager fm = FontManager.get();
        int max = fm.getGuiTextWidth(this.category.name);
        for (ModuleButton btn : this.buttons) {
            Module mod = btn.getModule();
            int w = fm.getGuiTextWidth(mod.title);
            if (w > max) {
                max = w;
            }
        }
        int sw = class_310.method_1551().method_22683().method_4486();
        int hardCap = Math.max(80, sw / 4);
        int minWidth = Math.max(60, fm.getGuiTextWidth(this.category.name) + 8);
        int computed = max + 4 + 20;
        this.width = Math.max(minWidth, Math.min(computed, hardCap));
    }

    private int getSettingTextWidth(FontManager fm, Setting<?> setting) {
        if (setting instanceof ColorSetting) {
            ColorSetting cs = (ColorSetting) setting;
            return fm.getGuiTextWidth(cs.name) + 16;
        }
        if (setting instanceof EnumSetting) {
            EnumSetting<?> es = (EnumSetting) setting;
            return fm.getGuiTextWidth(es.name + ": " + ((Enum) es.get()).toString()) + 6;
        }
        if (setting instanceof BoolSetting) {
            BoolSetting bs = (BoolSetting) setting;
            return fm.getGuiTextWidth(bs.name) + 14;
        }
        if (setting instanceof IntSetting) {
            IntSetting is = (IntSetting) setting;
            int displayMax = Math.min(is.sliderMax, 99999);
            return fm.getGuiTextWidth(is.name + ": " + displayMax) + 6;
        }
        if (setting instanceof DoubleSetting) {
            DoubleSetting ds = (DoubleSetting) setting;
            double displayMax2 = Math.min(ds.sliderMax, 99999.0d);
            String longest = ds.name + ": " + String.format("%." + Math.max(1, Math.min(2, ds.decimalPlaces)) + "f", Double.valueOf(displayMax2));
            return fm.getGuiTextWidth(longest) + 6;
        }
        if (setting instanceof KeybindSetting) {
            KeybindSetting ks = (KeybindSetting) setting;
            return fm.getGuiTextWidth(ks.name + ": " + ks.get().toString()) + 6;
        }
        if (setting instanceof ProvidedStringSetting) {
            ProvidedStringSetting pss = (ProvidedStringSetting) setting;
            return fm.getGuiTextWidth(pss.name + ": " + (pss.get() == null ? "" : pss.get())) + 6;
        }
        if (setting instanceof FontFaceSetting) {
            FontFaceSetting fs = (FontFaceSetting) setting;
            return fm.getGuiTextWidth(fs.name + ": " + (fs.get() == null ? "(none)" : fs.get().toString())) + 6;
        }
        if (setting instanceof StringSetting) {
            StringSetting ss = (StringSetting) setting;
            return fm.getGuiTextWidth(ss.name + ": ") + 40;
        }
        return fm.getGuiTextWidth(setting.name) + 40;
    }

    public int getWidth() {
        return this.width;
    }

    public void render(class_332 context, int mouseX, int mouseY, float animProgress) {
        if (this.dragging) {
            this.x = mouseX - this.dragOffsetX;
            this.y = mouseY - this.dragOffsetY;
            int sw = class_310.method_1551().method_22683().method_4486();
            int sh = class_310.method_1551().method_22683().method_4502();
            int headerH = FontManager.get().getHeaderHeight();
            if (this.x < 0) {
                this.x = 0;
            }
            if (this.x > sw - this.width) {
                this.x = sw - this.width;
            }
            if (this.y < 0) {
                this.y = 0;
            }
            if (this.y > sh - headerH) {
                this.y = sh - headerH;
            }
        }
        recalcWidth();
        FontManager fm = FontManager.get();
        int headerH2 = fm.getHeaderHeight();
        int contentHeight = getVisibleContentHeight();
        int totalHeight = headerH2 + contentHeight;
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, getMaxScroll()));
        int outlineColor = fm.primaryAlpha(Opcode.TABLESWITCH);
        int headerColor = fm.primaryAlpha(220);
        RenderUtils.drawThickOutline(context, this.x, this.y, this.width, totalHeight, 3, outlineColor);
        RenderUtils.fillNative(context, this.x, this.y, this.width, headerH2, headerColor);
        context.method_44379(this.x, this.y, this.x + this.width, this.y + headerH2);
        int headerTextY = this.y + ((headerH2 - fm.getTextHeight()) / 2);
        fm.drawText(context, this.category.name, this.x + 2 + 2, headerTextY, fm.getTextColor());
        context.method_44380();
        int contentTop = this.y + headerH2;
        int contentBottom = this.y + totalHeight;
        context.method_44379(this.x, contentTop, this.x + this.width, contentBottom);
        int moduleY = contentTop - this.scrollOffset;
        for (ModuleButton button : this.buttons) {
            button.render(context, this.x, moduleY, this.width, mouseX, mouseY);
            moduleY += button.getHeight();
        }
        context.method_44380();
        int maxScroll = getMaxScroll();
        if (maxScroll > 0) {
            int totalContent = getContentHeight();
            int trackH = Math.max(1, contentHeight - 2);
            int thumbH = Math.max(8, (int) ((((long) contentHeight) * ((long) trackH)) / ((long) totalContent)));
            int thumbY = contentTop + 1 + ((int) ((((long) (trackH - thumbH)) * ((long) this.scrollOffset)) / ((long) maxScroll)));
            int sbX = (this.x + this.width) - 2;
            fillNative(context, sbX, thumbY, sbX + 1, thumbY + thumbH, fm.primaryAlpha(220));
        }
        if (fm.isAnimation() && animProgress < 1.0f) {
            drawBorderTrace(context, this.x, this.y, this.width, totalHeight, animProgress, fm);
        }
    }

    private void drawBorderTrace(class_332 context, int bx, int by, int bw, int bh, float progress, FontManager fm) {
        int px;
        int i;
        int animColor = fm.animationAlpha(220);
        float scale = (float) class_310.method_1551().method_22683().method_4495();
        int sx = (int) (bx * scale);
        int sy = (int) (by * scale);
        int sw = (int) (bw * scale);
        int sh = (int) (bh * scale);
        int perimeter = (2 * sh) + sw;
        int segLen = Math.max(8, perimeter / 8);
        int headPos = (int) (progress * (perimeter + segLen));
        int tailPos = headPos - segLen;
        class_4587 matrices = context.method_51448();
        matrices.method_22903();
        matrices.method_22905(1.0f / scale, 1.0f / scale, 1.0f);
        boolean ltr = fm.isAnimationLTR();
        for (int p = Math.max(0, tailPos); p < Math.min(perimeter, headPos); p++) {
            if (ltr) {
                if (p < sh) {
                    px = sx;
                    i = sy + p;
                } else if (p < sh + sw) {
                    px = sx + (p - sh);
                    i = (sy + sh) - 1;
                } else {
                    px = (sx + sw) - 1;
                    i = ((sy + sh) - 1) - ((p - sh) - sw);
                }
            } else if (p < sh) {
                px = (sx + sw) - 1;
                i = sy + p;
            } else if (p < sh + sw) {
                px = ((sx + sw) - 1) - (p - sh);
                i = (sy + sh) - 1;
            } else {
                px = sx;
                i = ((sy + sh) - 1) - ((p - sh) - sw);
            }
            int py = i;
            context.method_25294(px - 1, py - 1, px + 2, py + 2, animColor);
        }
        matrices.method_22909();
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        FontManager fm = FontManager.get();
        int headerH = fm.getHeaderHeight();
        int totalHeight = headerH + getVisibleContentHeight();
        if (mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + headerH && button == 0) {
            this.dragging = true;
            this.dragOffsetX = mouseX - this.x;
            this.dragOffsetY = mouseY - this.y;
            return true;
        }
        if (mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y + headerH && mouseY < this.y + totalHeight) {
            int moduleY = (this.y + headerH) - this.scrollOffset;
            for (ModuleButton moduleButton : this.buttons) {
                int btnHeight = moduleButton.getHeight();
                if (mouseY >= moduleY && mouseY < moduleY + btnHeight && moduleButton.mouseClicked(this.x, moduleY, this.width, mouseX, mouseY, button)) {
                    return true;
                }
                moduleY += btnHeight;
            }
            return false;
        }
        return false;
    }

    public void mouseReleased(int mouseX, int mouseY, int button) {
        this.dragging = false;
        for (ModuleButton moduleButton : this.buttons) {
            moduleButton.mouseReleased(mouseX, mouseY, button);
        }
    }

    public boolean mouseScrolled(int mouseX, int mouseY, double amount) {
        int totalHeight = FontManager.get().getHeaderHeight() + getVisibleContentHeight();
        if (mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + totalHeight) {
            this.scrollOffset -= (int) (amount * 14.0d);
            this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, getMaxScroll()));
            return true;
        }
        return false;
    }

    private int getContentHeight() {
        int h = 2;
        for (ModuleButton button : this.buttons) {
            h += button.getHeight();
        }
        return h;
    }

    private int getVisibleContentHeight() {
        int screenH = class_310.method_1551().method_22683().method_4502();
        int headerH = FontManager.get().getHeaderHeight();
        int maxAvail = Math.max(FontManager.get().getRowHeight() * 2, ((screenH - this.y) - headerH) - 2);
        return Math.min(getContentHeight(), maxAvail);
    }

    public void setHeightCap(int cap) {
        this.heightCap = cap;
    }

    private int getMaxScroll() {
        return Math.max(0, getContentHeight() - getVisibleContentHeight());
    }

    private void fillNative(class_332 context, int x1, int y1, int x2, int y2, int color) {
        float scale = (float) class_310.method_1551().method_22683().method_4495();
        class_4587 matrices = context.method_51448();
        matrices.method_22903();
        matrices.method_22905(1.0f / scale, 1.0f / scale, 1.0f);
        context.method_25294((int) (x1 * scale), (int) (y1 * scale), (int) (x2 * scale), (int) (y2 * scale), color);
        matrices.method_22909();
    }

    public static int getNativeGap() {
        float scale = (float) class_310.method_1551().method_22683().method_4495();
        return Math.max(1, (int) Math.ceil(1.0d / ((double) scale)));
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getTotalHeight() {
        return FontManager.get().getHeaderHeight() + getVisibleContentHeight();
    }

    public Category getCategory() {
        return this.category;
    }
}
