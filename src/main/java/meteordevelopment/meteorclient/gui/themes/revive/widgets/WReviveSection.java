package meteordevelopment.meteorclient.gui.themes.revive.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.revive.ReviveWidget;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.pressable.WTriangle;

public class WReviveSection extends WSection {
    public WReviveSection(String title, boolean expanded, WWidget headerWidget) {
        super(title, expanded, headerWidget);
    }

    @Override
    protected WHeader createHeader() {
        return new WReviveHeader(title);
    }

    protected class WReviveHeader extends WHeader {
        private WTriangle triangle;

        public WReviveHeader(String title) { super(title); }

        @Override
        public void init() {
            add(theme.horizontalSeparator(title)).expandX();
            if (headerWidget != null) add(headerWidget);
            triangle = new WHeaderTriangle();
            triangle.theme = theme;
            triangle.action = this::onClick;
            add(triangle);
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            triangle.rotation = (1 - animProgress) * -90;
        }
    }

    protected static class WHeaderTriangle extends WTriangle implements ReviveWidget {
        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            renderer.rotatedQuad(x, y, width, height, rotation, GuiRenderer.TRIANGLE, theme().accentColor.get());
        }
    }
}
