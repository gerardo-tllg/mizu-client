package meteordevelopment.meteorclient.gui.screens.settings;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WQuad;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WIntEdit;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.class_3532;
import net.minecraft.class_437;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/screens/settings/ColorSettingScreen.class */
public class ColorSettingScreen extends WindowScreen {
    private static final Color[] HUE_COLORS = {new Color(255, 0, 0), new Color(255, 255, 0), new Color(0, 255, 0), new Color(0, 255, 255), new Color(0, 0, 255), new Color(255, 0, 255), new Color(255, 0, 0)};
    private static final Color WHITE = new Color(255, 255, 255);
    private static final Color BLACK = new Color(0, 0, 0);
    public Runnable action;
    private final Setting<SettingColor> setting;
    private WQuad displayQuad;
    private WBrightnessQuad brightnessQuad;
    private WHueQuad hueQuad;
    private WIntEdit rItb;
    private WIntEdit gItb;
    private WIntEdit bItb;
    private WIntEdit aItb;
    private WCheckbox rainbow;

    public ColorSettingScreen(GuiTheme theme, Setting<SettingColor> setting) {
        super(theme, "Select Color");
        this.setting = setting;
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public void initWidgets() {
        this.displayQuad = (WQuad) add(this.theme.quad(this.setting.get())).expandX().widget();
        this.brightnessQuad = (WBrightnessQuad) add(new WBrightnessQuad()).expandX().widget();
        this.hueQuad = (WHueQuad) add(new WHueQuad()).expandX().widget();
        WTable rgbaTable = (WTable) add(this.theme.table()).expandX().widget();
        rgbaTable.add(this.theme.label("R:"));
        this.rItb = (WIntEdit) rgbaTable.add(this.theme.intEdit(this.setting.get().r, 0, 255, 0, 255, false)).expandX().widget();
        this.rItb.action = this::rgbaChanged;
        rgbaTable.row();
        rgbaTable.add(this.theme.label("G:"));
        this.gItb = (WIntEdit) rgbaTable.add(this.theme.intEdit(this.setting.get().g, 0, 255, 0, 255, false)).expandX().widget();
        this.gItb.action = this::rgbaChanged;
        rgbaTable.row();
        rgbaTable.add(this.theme.label("B:"));
        this.bItb = (WIntEdit) rgbaTable.add(this.theme.intEdit(this.setting.get().b, 0, 255, 0, 255, false)).expandX().widget();
        this.bItb.action = this::rgbaChanged;
        rgbaTable.row();
        rgbaTable.add(this.theme.label("A:"));
        this.aItb = (WIntEdit) rgbaTable.add(this.theme.intEdit(this.setting.get().a, 0, 255, 0, 255, false)).expandX().widget();
        this.aItb.action = this::rgbaChanged;
        WHorizontalList rainbowList = (WHorizontalList) add(this.theme.horizontalList()).expandX().widget();
        rainbowList.add(this.theme.label("Rainbow: "));
        this.rainbow = this.theme.checkbox(this.setting.get().rainbow);
        this.rainbow.action = () -> {
            this.setting.get().rainbow = this.rainbow.checked;
            this.setting.onChanged();
        };
        rainbowList.add(this.rainbow).expandCellX().right();
        WHorizontalList bottomList = (WHorizontalList) add(this.theme.horizontalList()).expandX().widget();
        WButton backButton = (WButton) bottomList.add(this.theme.button("Back")).expandX().widget();
        backButton.action = this::method_25419;
        WButton copyButton = (WButton) bottomList.add(this.theme.button(GuiRenderer.COPY)).widget();
        copyButton.action = this::toClipboard;
        copyButton.tooltip = "Copy config";
        WButton pasteButton = (WButton) bottomList.add(this.theme.button(GuiRenderer.PASTE)).widget();
        pasteButton.action = this::fromClipboard;
        pasteButton.tooltip = "Paste config";
        WButton resetButton = (WButton) bottomList.add(this.theme.button(GuiRenderer.RESET)).widget();
        resetButton.action = () -> {
            this.setting.reset();
            setFromSetting();
            callAction();
        };
        this.hueQuad.calculateFromSetting(false);
        this.brightnessQuad.calculateFromColor(this.setting.get(), false);
    }

    private void setFromSetting() {
        SettingColor c = this.setting.get();
        if (c.r != this.rItb.get()) {
            this.rItb.set(c.r);
        }
        if (c.g != this.gItb.get()) {
            this.gItb.set(c.g);
        }
        if (c.b != this.bItb.get()) {
            this.bItb.set(c.b);
        }
        if (c.a != this.aItb.get()) {
            this.aItb.set(c.a);
        }
        this.rainbow.checked = c.rainbow;
        this.displayQuad.color.set((Color) this.setting.get());
        this.hueQuad.calculateFromSetting(true);
        this.brightnessQuad.calculateFromColor(this.setting.get(), true);
    }

    private void callAction() {
        if (this.action != null) {
            this.action.run();
        }
    }

    public void method_25393() {
        super.method_25393();
        if (this.setting.get().rainbow) {
            setFromSetting();
        }
    }

    private void rgbaChanged() {
        Color c = this.setting.get();
        c.r = this.rItb.get();
        c.g = this.gItb.get();
        c.b = this.bItb.get();
        c.a = this.aItb.get();
        c.validate();
        if (c.r != this.rItb.get()) {
            this.rItb.set(c.r);
        }
        if (c.g != this.gItb.get()) {
            this.gItb.set(c.g);
        }
        if (c.b != this.bItb.get()) {
            this.bItb.set(c.b);
        }
        if (c.a != this.aItb.get()) {
            this.aItb.set(c.a);
        }
        this.displayQuad.color.set(c);
        this.hueQuad.calculateFromSetting(true);
        this.brightnessQuad.calculateFromColor(this.setting.get(), true);
        this.setting.onChanged();
        callAction();
    }

    private void hsvChanged() {
        double r = 0.0d;
        double g = 0.0d;
        double b = 0.0d;
        boolean calculated = false;
        if (this.brightnessQuad.saturation <= 0.0d) {
            r = this.brightnessQuad.value;
            g = this.brightnessQuad.value;
            b = this.brightnessQuad.value;
            calculated = true;
        }
        if (!calculated) {
            double hh = this.hueQuad.hueAngle;
            if (hh >= 360.0d) {
                hh = 0.0d;
            }
            double hh2 = hh / 60.0d;
            int i = (int) hh2;
            double ff = hh2 - ((double) i);
            double p = this.brightnessQuad.value * (1.0d - this.brightnessQuad.saturation);
            double q = this.brightnessQuad.value * (1.0d - (this.brightnessQuad.saturation * ff));
            double t = this.brightnessQuad.value * (1.0d - (this.brightnessQuad.saturation * (1.0d - ff)));
            switch (i) {
                case 0:
                    r = this.brightnessQuad.value;
                    g = t;
                    b = p;
                    break;
                case 1:
                    r = q;
                    g = this.brightnessQuad.value;
                    b = p;
                    break;
                case 2:
                    r = p;
                    g = this.brightnessQuad.value;
                    b = t;
                    break;
                case 3:
                    r = p;
                    g = q;
                    b = this.brightnessQuad.value;
                    break;
                case 4:
                    r = t;
                    g = p;
                    b = this.brightnessQuad.value;
                    break;
                default:
                    r = this.brightnessQuad.value;
                    g = p;
                    b = q;
                    break;
            }
        }
        Color c = this.setting.get();
        c.r = (int) (r * 255.0d);
        c.g = (int) (g * 255.0d);
        c.b = (int) (b * 255.0d);
        c.validate();
        this.rItb.set(c.r);
        this.gItb.set(c.g);
        this.bItb.set(c.b);
        this.displayQuad.color.set(c);
        this.setting.onChanged();
        callAction();
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/screens/settings/ColorSettingScreen$WBrightnessQuad.class */
    private class WBrightnessQuad extends WWidget {
        double saturation;
        double value;
        double handleX;
        double handleY;
        boolean dragging;
        double lastMouseX;
        double lastMouseY;
        double fixedHeight = -1.0d;

        private WBrightnessQuad() {
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
        protected void onCalculateSize() {
            double s = this.theme.scale(75.0d);
            this.width = s;
            this.height = s;
            if (this.fixedHeight != -1.0d) {
                this.height = this.fixedHeight;
                this.fixedHeight = -1.0d;
            }
        }

        void calculateFromColor(Color c, boolean calculateNow) {
            double min = Math.min(Math.min(c.r, c.g), c.b);
            double max = Math.max(Math.max(c.r, c.g), c.b);
            double delta = max - min;
            this.value = max / 255.0d;
            if (delta == 0.0d) {
                this.saturation = 0.0d;
            } else {
                this.saturation = delta / max;
            }
            if (calculateNow) {
                this.handleX = this.saturation * this.width;
                this.handleY = (1.0d - this.value) * this.height;
            }
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
        public boolean onMouseClicked(double mouseX, double mouseY, int button, boolean used) {
            if (!used && this.mouseOver) {
                this.dragging = true;
                this.handleX = this.lastMouseX - this.x;
                this.handleY = this.lastMouseY - this.y;
                handleMoved();
                return true;
            }
            return false;
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
        public boolean onMouseReleased(double mouseX, double mouseY, int button) {
            if (this.dragging) {
                this.dragging = false;
                return false;
            }
            return false;
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
        public void onMouseMoved(double mouseX, double mouseY, double lastMouseX, double lastMouseY) {
            if (this.dragging) {
                if (mouseX >= this.x && mouseX <= this.x + this.width) {
                    this.handleX += mouseX - lastMouseX;
                } else if (this.handleX > 0.0d && mouseX < this.x) {
                    this.handleX = 0.0d;
                } else if (this.handleX < this.width && mouseX > this.x + this.width) {
                    this.handleX = this.width;
                }
                if (mouseY >= this.y && mouseY <= this.y + this.height) {
                    this.handleY += mouseY - lastMouseY;
                } else if (this.handleY > 0.0d && mouseY < this.y) {
                    this.handleY = 0.0d;
                } else if (this.handleY < this.height && mouseY > this.y + this.height) {
                    this.handleY = this.height;
                }
                handleMoved();
            }
            this.lastMouseX = mouseX;
            this.lastMouseY = mouseY;
        }

        void handleMoved() {
            double handleXPercentage = this.handleX / this.width;
            double handleYPercentage = this.handleY / this.height;
            this.saturation = handleXPercentage;
            this.value = 1.0d - handleYPercentage;
            ColorSettingScreen.this.hsvChanged();
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            if (this.height != this.width) {
                this.fixedHeight = this.width;
                invalidate();
                this.handleX = this.saturation * this.width;
                this.handleY = (1.0d - this.value) * this.fixedHeight;
            }
            ColorSettingScreen.this.hueQuad.calculateColor();
            renderer.quad(this.x, this.y, this.width, this.height, ColorSettingScreen.WHITE, ColorSettingScreen.this.hueQuad.color, ColorSettingScreen.BLACK, ColorSettingScreen.BLACK);
            double s = this.theme.scale(2.0d);
            renderer.quad((this.x + this.handleX) - (s / 2.0d), (this.y + this.handleY) - (s / 2.0d), s, s, ColorSettingScreen.WHITE);
        }
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public boolean toClipboard() {
        return NbtUtils.toClipboard(this.setting.get());
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public boolean fromClipboard() {
        if (!NbtUtils.fromClipboard(this.setting.get())) {
            String clipboard = MeteorClient.mc.field_1774.method_1460().trim();
            SettingColor parsed = parseRGBA(clipboard);
            if (parsed == null) {
                parsed = parseHex(clipboard);
                if (parsed == null) {
                    return false;
                }
            }
            this.setting.set(parsed);
        }
        this.setting.get().validate();
        class_437 class_437Var = this.parent;
        if (class_437Var instanceof WidgetScreen) {
            WidgetScreen p = (WidgetScreen) class_437Var;
            p.reload();
        }
        reload();
        return true;
    }

    private SettingColor parseRGBA(String string) {
        String[] rgba = string.replaceAll("[^0-9|,]", "").split(",");
        if (rgba.length < 3 || rgba.length > 4) {
            return null;
        }
        try {
            SettingColor color = new SettingColor(Integer.parseInt(rgba[0]), Integer.parseInt(rgba[1]), Integer.parseInt(rgba[2]));
            if (rgba.length == 4) {
                color.a = Integer.parseInt(rgba[3]);
            }
            return color;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private SettingColor parseHex(String string) {
        if (!string.startsWith("#")) {
            return null;
        }
        String hex = string.toLowerCase().replaceAll("[^0-9a-f]", "");
        if (hex.length() != 6 && hex.length() != 8) {
            return null;
        }
        try {
            SettingColor color = new SettingColor(Integer.parseInt(hex.substring(0, 2), 16), Integer.parseInt(hex.substring(2, 4), 16), Integer.parseInt(hex.substring(4, 6), 16));
            if (hex.length() == 8) {
                color.a = Integer.parseInt(hex.substring(6, 8), 16);
            }
            return color;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/screens/settings/ColorSettingScreen$WHueQuad.class */
    private class WHueQuad extends WWidget {
        private double hueAngle;
        private double handleX;
        private final Color color = new Color();
        private boolean dragging;
        private double lastMouseX;
        private boolean calculateHandleXOnLayout;

        private WHueQuad() {
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
        protected void onCalculateSize() {
            this.width = this.theme.scale(75.0d);
            this.height = this.theme.scale(10.0d);
        }

        void calculateFromSetting(boolean calculateNow) {
            Color c = ColorSettingScreen.this.setting.get();
            boolean calculated = false;
            double min = Math.min(c.r, c.g);
            double min2 = min < ((double) c.b) ? min : c.b;
            double max = Math.max(c.r, c.g);
            double max2 = max > ((double) c.b) ? max : c.b;
            double delta = max2 - min2;
            if (delta < 1.0E-5d) {
                this.hueAngle = 0.0d;
                calculated = true;
            }
            if (!calculated) {
                if (max2 <= 0.0d) {
                    this.hueAngle = 0.0d;
                    calculated = true;
                }
                if (!calculated) {
                    if (c.r >= max2) {
                        this.hueAngle = ((double) (c.g - c.b)) / delta;
                    } else if (c.g >= max2) {
                        this.hueAngle = 2.0d + (((double) (c.b - c.r)) / delta);
                    } else {
                        this.hueAngle = 4.0d + (((double) (c.r - c.g)) / delta);
                    }
                    this.hueAngle *= 60.0d;
                    if (this.hueAngle < 0.0d) {
                        this.hueAngle += 360.0d;
                    }
                }
            }
            if (calculateNow) {
                double huePercentage = this.hueAngle / 360.0d;
                this.handleX = huePercentage * this.width;
            } else {
                this.calculateHandleXOnLayout = true;
            }
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
        protected void onCalculateWidgetPositions() {
            if (this.calculateHandleXOnLayout) {
                double huePercentage = this.hueAngle / 360.0d;
                this.handleX = huePercentage * this.width;
                this.calculateHandleXOnLayout = false;
            }
            super.onCalculateWidgetPositions();
        }

        void calculateColor() {
            double r;
            double g;
            double b;
            double hh = this.hueAngle;
            if (hh >= 360.0d) {
                hh = 0.0d;
            }
            double hh2 = hh / 60.0d;
            int i = (int) hh2;
            double ff = hh2 - ((double) i);
            double q = 1.0d * (1.0d - (1.0d * ff));
            double t = 1.0d * (1.0d - (1.0d * (1.0d - ff)));
            switch (i) {
                case 0:
                    r = 1.0d;
                    g = t;
                    b = 0.0d;
                    break;
                case 1:
                    r = q;
                    g = 1.0d;
                    b = 0.0d;
                    break;
                case 2:
                    r = 0.0d;
                    g = 1.0d;
                    b = t;
                    break;
                case 3:
                    r = 0.0d;
                    g = q;
                    b = 1.0d;
                    break;
                case 4:
                    r = t;
                    g = 0.0d;
                    b = 1.0d;
                    break;
                default:
                    r = 1.0d;
                    g = 0.0d;
                    b = q;
                    break;
            }
            this.color.r = (int) (r * 255.0d);
            this.color.g = (int) (g * 255.0d);
            this.color.b = (int) (b * 255.0d);
            this.color.validate();
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
        public boolean onMouseClicked(double mouseX, double mouseY, int button, boolean used) {
            if (!used && this.mouseOver) {
                this.dragging = true;
                this.handleX = this.lastMouseX - this.x;
                calculateHueAngleFromHandleX();
                ColorSettingScreen.this.hsvChanged();
                return true;
            }
            return false;
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
        public boolean onMouseReleased(double mouseX, double mouseY, int button) {
            if (this.dragging) {
                this.dragging = false;
            }
            return this.mouseOver;
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
        public void onMouseMoved(double mouseX, double mouseY, double lastMouseX, double lastMouseY) {
            if (this.dragging) {
                if (mouseX >= this.x && mouseX <= this.x + this.width) {
                    this.handleX += mouseX - lastMouseX;
                    this.handleX = class_3532.method_15350(this.handleX, 0.0d, this.width);
                } else if (this.handleX > 0.0d && mouseX < this.x) {
                    this.handleX = 0.0d;
                } else if (this.handleX < this.width && mouseX > this.x + this.width) {
                    this.handleX = this.width;
                }
                calculateHueAngleFromHandleX();
                ColorSettingScreen.this.hsvChanged();
            }
            this.lastMouseX = mouseX;
        }

        void calculateHueAngleFromHandleX() {
            double handleXPercentage = this.handleX / (this.width - 4.0d);
            this.hueAngle = handleXPercentage * 360.0d;
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            double sectionWidth = this.width / ((double) (ColorSettingScreen.HUE_COLORS.length - 1));
            double sectionX = this.x;
            for (int i = 0; i < ColorSettingScreen.HUE_COLORS.length - 1; i++) {
                renderer.quad(sectionX, this.y, sectionWidth, this.height, ColorSettingScreen.HUE_COLORS[i], ColorSettingScreen.HUE_COLORS[i + 1], ColorSettingScreen.HUE_COLORS[i + 1], ColorSettingScreen.HUE_COLORS[i]);
                sectionX += sectionWidth;
            }
            double s = this.theme.scale(2.0d);
            renderer.quad((this.x + this.handleX) - (s / 2.0d), this.y, s, this.height, ColorSettingScreen.WHITE);
        }
    }
}
