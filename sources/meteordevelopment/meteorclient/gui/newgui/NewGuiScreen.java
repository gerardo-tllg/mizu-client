package meteordevelopment.meteorclient.gui.newgui;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.gui.newgui.components.CategoryPanel;
import meteordevelopment.meteorclient.gui.newgui.components.ModuleButton;
import meteordevelopment.meteorclient.gui.newgui.components.NewGuiBindCapture;
import meteordevelopment.meteorclient.gui.newgui.components.SettingGroupRenderer;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.gui.Gui;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/newgui/NewGuiScreen.class */
public class NewGuiScreen extends class_437 {
    private final List<CategoryPanel> panels;
    private boolean initialized;
    private long openTime;

    public NewGuiScreen() {
        super(class_2561.method_43470("Mizu"));
        this.panels = new ArrayList();
        this.initialized = false;
    }

    protected void method_25426() {
        super.method_25426();
        NewGuiBindCapture.ensureSubscribed();
        this.openTime = System.currentTimeMillis();
        try {
            Gui guiMod = (Gui) Modules.get().get(Gui.class);
            if (guiMod != null) {
                guiMod.applyToFontManager();
            }
        } catch (Throwable th) {
        }
        if (!this.initialized) {
            int gap = CategoryPanel.getNativeGap();
            for (Category category : Modules.loopCategories()) {
                this.panels.add(new CategoryPanel(category, 0, 10));
            }
            int maxWidth = this.field_22789 - 4;
            int currentX = 4;
            int currentY = 10;
            int rowTallest = 0;
            for (CategoryPanel panel : this.panels) {
                int pw = panel.getWidth();
                if (currentX != 4 && currentX + pw > maxWidth) {
                    currentX = 4;
                    currentY += rowTallest + 4;
                    rowTallest = 0;
                }
                panel.setX(currentX);
                panel.setY(currentY);
                currentX += pw + gap;
                int ph = panel.getTotalHeight();
                if (ph > rowTallest) {
                    rowTallest = ph;
                }
            }
            this.initialized = true;
        }
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        super.method_25394(context, mouseX, mouseY, delta);
        float animProgress = getAnimProgress();
        for (int i = this.panels.size() - 1; i >= 0; i--) {
            this.panels.get(i).render(context, mouseX, mouseY, animProgress);
        }
    }

    private float getAnimProgress() {
        long elapsed = System.currentTimeMillis() - this.openTime;
        return Math.min(1.0f, elapsed / 1500.0f);
    }

    public boolean method_25402(double mouseX, double mouseY, int button) {
        for (CategoryPanel panel : this.panels) {
            if (panel.mouseClicked((int) mouseX, (int) mouseY, button)) {
                return true;
            }
        }
        return super.method_25402(mouseX, mouseY, button);
    }

    public boolean method_25406(double mouseX, double mouseY, int button) {
        for (CategoryPanel panel : this.panels) {
            panel.mouseReleased((int) mouseX, (int) mouseY, button);
        }
        return super.method_25406(mouseX, mouseY, button);
    }

    public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (CategoryPanel panel : this.panels) {
            if (panel.mouseScrolled((int) mouseX, (int) mouseY, verticalAmount)) {
                return true;
            }
        }
        return super.method_25401(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean method_25404(int keyCode, int scanCode, int modifiers) {
        if (ModuleButton.onKeyPressed(keyCode)) {
            return true;
        }
        return super.method_25404(keyCode, scanCode, modifiers);
    }

    public boolean method_25400(char chr, int modifiers) {
        if (ModuleButton.onCharTyped(chr)) {
            return true;
        }
        return super.method_25400(chr, modifiers);
    }

    public boolean method_25421() {
        return false;
    }

    public void method_25419() {
        Modules modules = Modules.get();
        if (modules != null && modules.isBinding()) {
            modules.setModuleToBind(null);
        }
        NewGuiBindCapture.get().cancelSettingListen();
        SettingGroupRenderer.commitStringEdit();
        super.method_25419();
    }
}
