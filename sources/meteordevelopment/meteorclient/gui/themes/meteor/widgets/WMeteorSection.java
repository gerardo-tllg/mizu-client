package meteordevelopment.meteorclient.gui.themes.meteor.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorWidget;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.pressable.WTriangle;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/meteor/widgets/WMeteorSection.class */
public class WMeteorSection extends WSection {
    public WMeteorSection(String title, boolean expanded, WWidget headerWidget) {
        super(title, expanded, headerWidget);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.containers.WSection
    protected WSection.WHeader createHeader() {
        return new WMeteorHeader(this.title);
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/meteor/widgets/WMeteorSection$WMeteorHeader.class */
    protected class WMeteorHeader extends WSection.WHeader {
        private WTriangle triangle;

        public WMeteorHeader(String title) {
            super(title);
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
        public void init() {
            add(this.theme.horizontalSeparator(this.title)).expandX();
            if (WMeteorSection.this.headerWidget != null) {
                add(WMeteorSection.this.headerWidget);
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
            this.triangle.rotation = (1.0d - WMeteorSection.this.animProgress) * (-90.0d);
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/meteor/widgets/WMeteorSection$WHeaderTriangle.class */
    protected static class WHeaderTriangle extends WTriangle implements MeteorWidget {
        protected WHeaderTriangle() {
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            renderer.rotatedQuad(this.x, this.y, this.width, this.height, this.rotation, GuiRenderer.TRIANGLE, theme().textColor.get());
        }
    }
}
