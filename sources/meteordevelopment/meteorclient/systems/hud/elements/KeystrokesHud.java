package meteordevelopment.meteorclient.systems.hud.elements;

import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/KeystrokesHud.class */
public class KeystrokesHud extends HudElement {
    public static final HudElementInfo<KeystrokesHud> INFO = new HudElementInfo<>(Hud.GROUP, "keystrokes", "Displays your key presses.", KeystrokesHud::new);
    private final SettingGroup sgGeneral;
    private final Setting<Double> scale;
    private final Setting<SettingColor> pressedColor;
    private final Setting<SettingColor> unpressedColor;
    private final Setting<SettingColor> textColor;

    public KeystrokesHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.scale = this.sgGeneral.add(new DoubleSetting.Builder().name("scale").description("Scale of the keystrokes display.").defaultValue(1.0d).min(0.5d).sliderRange(0.5d, 3.0d).build());
        this.pressedColor = this.sgGeneral.add(new ColorSetting.Builder().name("pressed-color").description("Color when key is pressed.").defaultValue(new SettingColor(255, 255, 255, 200)).build());
        this.unpressedColor = this.sgGeneral.add(new ColorSetting.Builder().name("unpressed-color").description("Color when key is not pressed.").defaultValue(new SettingColor(100, 100, 100, 100)).build());
        this.textColor = this.sgGeneral.add(new ColorSetting.Builder().name("text-color").description("Text color.").defaultValue(new SettingColor()).build());
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void render(HudRenderer renderer) {
        double scale = this.scale.get().doubleValue();
        double keySize = 30.0d * scale;
        double gap = 2.0d * scale;
        renderer.post(() -> {
            double x = this.x;
            double y = this.y;
            boolean wPressed = Input.isKeyPressed(87);
            drawKey(renderer, x + keySize + gap, y, keySize, "W", wPressed);
            boolean aPressed = Input.isKeyPressed(65);
            drawKey(renderer, x, y + keySize + gap, keySize, "A", aPressed);
            boolean sPressed = Input.isKeyPressed(83);
            drawKey(renderer, x + keySize + gap, y + keySize + gap, keySize, "S", sPressed);
            boolean dPressed = Input.isKeyPressed(68);
            drawKey(renderer, x + ((keySize + gap) * 2.0d), y + keySize + gap, keySize, "D", dPressed);
            boolean spacePressed = Input.isKeyPressed(32);
            drawKey(renderer, x, y + ((keySize + gap) * 2.0d), (keySize * 3.0d) + (gap * 2.0d), "Space", spacePressed);
            boolean lmbPressed = Input.isButtonPressed(0);
            drawKey(renderer, x, y + ((keySize + gap) * 3.0d), (((keySize * 3.0d) + (gap * 2.0d)) / 2.0d) - (gap / 2.0d), "LMB", lmbPressed);
            boolean rmbPressed = Input.isButtonPressed(1);
            drawKey(renderer, x + (((keySize * 3.0d) + (gap * 2.0d)) / 2.0d) + (gap / 2.0d), y + ((keySize + gap) * 3.0d), (((keySize * 3.0d) + (gap * 2.0d)) / 2.0d) - (gap / 2.0d), "RMB", rmbPressed);
        });
        setSize((keySize + gap) * 3.0d, (keySize + gap) * 4.0d);
    }

    private void drawKey(HudRenderer renderer, double x, double y, double width, String text, boolean pressed) {
        double keyHeight = 30.0d * this.scale.get().doubleValue();
        Renderer2D.COLOR.begin();
        Renderer2D.COLOR.quad(x, y, width, keyHeight, pressed ? this.pressedColor.get() : this.unpressedColor.get());
        Renderer2D.COLOR.render();
        TextRenderer textRenderer = TextRenderer.get();
        textRenderer.begin(0.4d * this.scale.get().doubleValue(), false, true);
        double textWidth = textRenderer.getWidth(text);
        double textHeight = textRenderer.getHeight();
        double textX = x + ((width - textWidth) / 2.0d);
        double textY = y + ((keyHeight - textHeight) / 2.0d);
        textRenderer.render(text, textX, textY, this.textColor.get());
        textRenderer.end();
    }
}
