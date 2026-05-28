package meteordevelopment.meteorclient.gui.themes.revive.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.revive.ReviveGuiTheme;
import meteordevelopment.meteorclient.gui.themes.revive.ReviveWidget;
import meteordevelopment.meteorclient.gui.utils.AlignmentX;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.util.math.MathHelper;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public class WReviveModule extends WPressable implements ReviveWidget {
    private final Module module;
    private double titleWidth;
    private double animProgress1; // hover/active fill
    private double animProgress2; // active left-bar

    public WReviveModule(Module module) {
        this.module = module;
        this.tooltip = module.description;
        animProgress1 = module.isActive() ? 1 : 0;
        animProgress2 = module.isActive() ? 1 : 0;
    }

    /** Larger padding = taller, more spacious module cells */
    @Override
    public double pad() {
        return theme.scale(7);
    }

    @Override
    protected void onCalculateSize() {
        double pad = pad();
        if (titleWidth == 0) titleWidth = theme.textWidth(module.title);
        width  = pad + titleWidth + pad;
        height = pad + theme.textHeight() + pad;
    }

    @Override
    protected void onPressed(int button) {
        if (button == GLFW_MOUSE_BUTTON_LEFT)  module.toggle();
        else if (button == GLFW_MOUSE_BUTTON_RIGHT) mc.setScreen(theme.moduleScreen(module));
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        ReviveGuiTheme theme = theme();
        double pad = pad();

        // Animate hover/active background fill
        animProgress1 += delta * 5 * ((module.isActive() || mouseOver) ? 1 : -1);
        animProgress1 = MathHelper.clamp(animProgress1, 0, 1);

        // Animate active indicator bar
        animProgress2 += delta * 8 * (module.isActive() ? 1 : -1);
        animProgress2 = MathHelper.clamp(animProgress2, 0, 1);

        // Subtle background fill when hovered or active
        if (animProgress1 > 0) {
            renderer.quad(x, y, width * animProgress1, height, theme.moduleBackground.get());
        }

        // Left-side accent bar (3px wide) that slides in from top when active
        if (animProgress2 > 0) {
            double barW = theme.scale(3);
            renderer.quad(
                x, y + height * (1 - animProgress2),
                barW, height * animProgress2,
                theme.accentColor.get()
            );
        }

        // Bottom separator line — always visible, very subtle
        renderer.quad(x, y + height - theme.scale(1), width, theme.scale(1),
            theme.outlineColor.get());

        // Text
        double tx = this.x + pad;
        double w  = width - pad * 2;

        if (theme.moduleAlignment.get() == AlignmentX.Center) {
            tx += w / 2 - titleWidth / 2;
        } else if (theme.moduleAlignment.get() == AlignmentX.Right) {
            tx += w - titleWidth;
        }

        renderer.text(module.title, tx, y + pad, theme.textColor.get(), false);
    }
}
