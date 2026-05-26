package meteordevelopment.meteorclient.systems.modules.render;

import it.unimi.dsi.fastutil.ints.IntDoubleImmutablePair;
import javassist.compiler.TokenId;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.ResolutionChangedEvent;
import meteordevelopment.meteorclient.events.render.RenderAfterWorldEvent;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.renderer.Framebuffer;
import meteordevelopment.meteorclient.renderer.Shader;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.listeners.ConsumerListener;
import meteordevelopment.orbit.listeners.IListener;
import net.minecraft.class_408;
import net.minecraft.class_437;
import net.minecraft.class_465;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/Blur.class */
public class Blur extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgScreens;
    private final IntDoubleImmutablePair[] strengths;
    private final Setting<Integer> strength;
    private final Setting<Integer> fadeTime;
    private final Setting<Boolean> meteor;
    private final Setting<Boolean> inventories;
    private final Setting<Boolean> chat;
    private final Setting<Boolean> other;
    private Shader shaderDown;
    private Shader shaderUp;
    private Shader shaderPassthrough;
    private final Framebuffer[] fbos;
    private boolean enabled;
    private long fadeEndAt;

    public Blur() {
        super(Categories.Render, "blur", "Blurs background when in GUI screens.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgScreens = this.settings.createGroup("Screens");
        this.strengths = new IntDoubleImmutablePair[]{IntDoubleImmutablePair.of(1, 1.25d), IntDoubleImmutablePair.of(1, 2.25d), IntDoubleImmutablePair.of(2, 2.0d), IntDoubleImmutablePair.of(2, 3.0d), IntDoubleImmutablePair.of(2, 4.25d), IntDoubleImmutablePair.of(3, 2.5d), IntDoubleImmutablePair.of(3, 3.25d), IntDoubleImmutablePair.of(3, 4.25d), IntDoubleImmutablePair.of(3, 5.5d), IntDoubleImmutablePair.of(4, 3.25d), IntDoubleImmutablePair.of(4, 4.0d), IntDoubleImmutablePair.of(4, 5.0d), IntDoubleImmutablePair.of(4, 6.0d), IntDoubleImmutablePair.of(4, 7.25d), IntDoubleImmutablePair.of(4, 8.25d), IntDoubleImmutablePair.of(5, 4.5d), IntDoubleImmutablePair.of(5, 5.25d), IntDoubleImmutablePair.of(5, 6.25d), IntDoubleImmutablePair.of(5, 7.25d), IntDoubleImmutablePair.of(5, 8.5d)};
        this.strength = this.sgGeneral.add(new IntSetting.Builder().name("strength").description("How strong the blur should be.").defaultValue(5).min(1).max(20).sliderRange(1, 20).build());
        this.fadeTime = this.sgGeneral.add(new IntSetting.Builder().name("fade-time").description("How long the fade will last in milliseconds.").defaultValue(100).min(0).sliderMax(TokenId.BadToken).build());
        this.meteor = this.sgScreens.add(new BoolSetting.Builder().name("meteor").description("Applies blur to Meteor screens.").defaultValue(true).build());
        this.inventories = this.sgScreens.add(new BoolSetting.Builder().name("inventories").description("Applies blur to inventory screens.").defaultValue(true).build());
        this.chat = this.sgScreens.add(new BoolSetting.Builder().name("chat").description("Applies blur when in chat.").defaultValue(false).build());
        this.other = this.sgScreens.add(new BoolSetting.Builder().name("other").description("Applies blur to all other screen types.").defaultValue(true).build());
        this.fbos = new Framebuffer[6];
        MeteorClient.EVENT_BUS.subscribe((IListener) new ConsumerListener(ResolutionChangedEvent.class, event -> {
            for (int i = 0; i < this.fbos.length; i++) {
                if (this.fbos[i] != null) {
                    this.fbos[i].resize();
                } else {
                    this.fbos[i] = new Framebuffer(1.0d / Math.pow(2.0d, i));
                }
            }
        }));
        MeteorClient.EVENT_BUS.subscribe((IListener) new ConsumerListener(RenderAfterWorldEvent.class, event2 -> {
            onRenderAfterWorld();
        }));
    }

    private void onRenderAfterWorld() {
        boolean shouldRender = shouldRender();
        long time = System.currentTimeMillis();
        if (this.enabled) {
            if (!shouldRender) {
                if (this.fadeEndAt == -1) {
                    this.fadeEndAt = System.currentTimeMillis() + ((long) this.fadeTime.get().intValue());
                }
                if (time >= this.fadeEndAt) {
                    this.enabled = false;
                    this.fadeEndAt = -1L;
                }
            }
        } else if (shouldRender) {
            this.enabled = true;
            this.fadeEndAt = System.currentTimeMillis() + ((long) this.fadeTime.get().intValue());
        }
        if (this.enabled) {
            if (this.shaderDown == null) {
                this.shaderDown = new Shader("blur.vert", "blur_down.frag");
                this.shaderUp = new Shader("blur.vert", "blur_up.frag");
                this.shaderPassthrough = new Shader("passthrough.vert", "passthrough.frag");
                for (int i = 0; i < this.fbos.length; i++) {
                    if (this.fbos[i] == null) {
                        this.fbos[i] = new Framebuffer(1.0d / Math.pow(2.0d, i));
                    }
                }
            }
        }
    }

    private boolean shouldRender() {
        if (!isActive()) {
            return false;
        }
        class_437 screen = this.mc.field_1755;
        if (screen instanceof WidgetScreen) {
            return this.meteor.get().booleanValue();
        }
        if (screen instanceof class_465) {
            return this.inventories.get().booleanValue();
        }
        if (screen instanceof class_408) {
            return this.chat.get().booleanValue();
        }
        if (screen != null) {
            return this.other.get().booleanValue();
        }
        return false;
    }
}
