/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.hud.elements;

import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import org.lwjgl.glfw.GLFW;

public class KeystrokesHud extends HudElement {
    public static final HudElementInfo<KeystrokesHud> INFO = new HudElementInfo<>(Hud.GROUP, "keystrokes", "Displays your key presses.", KeystrokesHud::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("Scale of the keystrokes display.")
        .defaultValue(1)
        .min(0.5)
        .sliderRange(0.5, 3)
        .build()
    );

    private final Setting<SettingColor> pressedColor = sgGeneral.add(new ColorSetting.Builder()
        .name("pressed-color")
        .description("Color when key is pressed.")
        .defaultValue(new SettingColor(255, 255, 255, 200))
        .build()
    );

    private final Setting<SettingColor> unpressedColor = sgGeneral.add(new ColorSetting.Builder()
        .name("unpressed-color")
        .description("Color when key is not pressed.")
        .defaultValue(new SettingColor(100, 100, 100, 100))
        .build()
    );

    private final Setting<SettingColor> textColor = sgGeneral.add(new ColorSetting.Builder()
        .name("text-color")
        .description("Text color.")
        .defaultValue(new SettingColor())
        .build()
    );

    public KeystrokesHud() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        double scale = this.scale.get();
        double keySize = 30 * scale;
        double gap = 2 * scale;

        renderer.post(() -> {
            double x = this.x;
            double y = this.y;

            // W key
            boolean wPressed = Input.isKeyPressed(GLFW.GLFW_KEY_W);
            drawKey(renderer, x + keySize + gap, y, keySize, "W", wPressed);

            // A key
            boolean aPressed = Input.isKeyPressed(GLFW.GLFW_KEY_A);
            drawKey(renderer, x, y + keySize + gap, keySize, "A", aPressed);

            // S key
            boolean sPressed = Input.isKeyPressed(GLFW.GLFW_KEY_S);
            drawKey(renderer, x + keySize + gap, y + keySize + gap, keySize, "S", sPressed);

            // D key
            boolean dPressed = Input.isKeyPressed(GLFW.GLFW_KEY_D);
            drawKey(renderer, x + (keySize + gap) * 2, y + keySize + gap, keySize, "D", dPressed);

            // Space key
            boolean spacePressed = Input.isKeyPressed(GLFW.GLFW_KEY_SPACE);
            drawKey(renderer, x, y + (keySize + gap) * 2, keySize * 3 + gap * 2, "Space", spacePressed);

            // LMB
            boolean lmbPressed = Input.isButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT);
            drawKey(renderer, x, y + (keySize + gap) * 3, (keySize * 3 + gap * 2) / 2 - gap / 2, "LMB", lmbPressed);

            // RMB
            boolean rmbPressed = Input.isButtonPressed(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
            drawKey(renderer, x + (keySize * 3 + gap * 2) / 2 + gap / 2, y + (keySize + gap) * 3, (keySize * 3 + gap * 2) / 2 - gap / 2, "RMB", rmbPressed);
        });

        setSize((keySize + gap) * 3, (keySize + gap) * 4);
    }

    private void drawKey(HudRenderer renderer, double x, double y, double width, String text, boolean pressed) {
        double keyHeight = 30 * scale.get();
        
        // Draw background
        Renderer2D.COLOR.begin();
        Renderer2D.COLOR.quad(x, y, width, keyHeight, pressed ? pressedColor.get() : unpressedColor.get());
        Renderer2D.COLOR.render();

        // Draw text
        TextRenderer textRenderer = TextRenderer.get();
        textRenderer.begin(0.4 * scale.get(), false, true);
        double textWidth = textRenderer.getWidth(text);
        double textHeight = textRenderer.getHeight();
        double textX = x + (width - textWidth) / 2;
        double textY = y + (keyHeight - textHeight) / 2;
        textRenderer.render(text, textX, textY, textColor.get());
        textRenderer.end();
    }
}
