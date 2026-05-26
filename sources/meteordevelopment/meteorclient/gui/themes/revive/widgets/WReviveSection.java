package meteordevelopment.meteorclient.gui.themes.revive.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.revive.ReviveWidget;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.pressable.WTriangle;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/revive/widgets/WReviveSection.class */
public class WReviveSection extends WSection {
    public WReviveSection(String title, boolean expanded, WWidget headerWidget) {
        super(title, expanded, headerWidget);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.containers.WSection
    protected WSection.WHeader createHeader() {
        return new WReviveHeader(this.title);
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/revive/widgets/WReviveSection$WReviveHeader.class */
    protected class WReviveHeader extends WSection.WHeader {
        private WTriangle triangle;

        public WReviveHeader(String title) {
            super(title);
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
        public void init() {
            add(this.theme.horizontalSeparator(this.title)).expandX();
            if (WReviveSection.this.headerWidget != null) {
                add(WReviveSection.this.headerWidget);
            }
            this.triangle = new WHeaderTriangle();
            this.triangle.theme = this.theme;
            this.triangle.action = () -> {
                this.onClick();
            };
            add(this.triangle);
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            this.triangle.rotation = (1.0d - WReviveSection.this.animProgress) * (-90.0d);
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/revive/widgets/WReviveSection$WHeaderTriangle.class */
    protected static class WHeaderTriangle extends WTriangle implements ReviveWidget {
        protected WHeaderTriangle() {
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            renderer.rotatedQuad(this.x, this.y, this.width, this.height, this.rotation, GuiRenderer.TRIANGLE, theme().accentColor.get());
        }
    }
}
