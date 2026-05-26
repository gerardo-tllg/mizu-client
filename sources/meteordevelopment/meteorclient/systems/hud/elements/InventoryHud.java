package meteordevelopment.meteorclient.systems.hud.elements;

import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2960;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/InventoryHud.class */
public class InventoryHud extends HudElement {
    public static final HudElementInfo<InventoryHud> INFO = new HudElementInfo<>(Hud.GROUP, "inventory", "Displays your inventory.", InventoryHud::new);
    private static final class_2960 TEXTURE = MeteorClient.identifier("textures/container.png");
    private static final class_2960 TEXTURE_TRANSPARENT = MeteorClient.identifier("textures/container-transparent.png");
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> containers;
    private final Setting<Double> scale;
    private final Setting<Background> background;
    private final Setting<SettingColor> color;
    private final class_1799[] containerItems;

    private InventoryHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.containers = this.sgGeneral.add(new BoolSetting.Builder().name("containers").description("Shows the contents of a container when holding them.").defaultValue(false).build());
        this.scale = this.sgGeneral.add(new DoubleSetting.Builder().name("scale").description("The scale.").defaultValue(2.0d).min(1.0d).sliderRange(1.0d, 5.0d).onChanged(aDouble -> {
            calculateSize();
        }).build());
        this.background = this.sgGeneral.add(new EnumSetting.Builder().name("background").description("Background of inventory viewer.").defaultValue(Background.Texture).onChanged(bg -> {
            calculateSize();
        }).build());
        this.color = this.sgGeneral.add(new ColorSetting.Builder().name("background-color").description("Color of the background.").defaultValue(new SettingColor(255, 255, 255)).visible(() -> {
            return this.background.get() != Background.None;
        }).build());
        this.containerItems = new class_1799[27];
        calculateSize();
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void render(HudRenderer renderer) {
        double x = this.x;
        double y = this.y;
        class_1799 container = getContainer();
        boolean hasContainer = this.containers.get().booleanValue() && container != null;
        if (hasContainer) {
            Utils.getItemsInContainerItem(container, this.containerItems);
        }
        Color drawColor = hasContainer ? Utils.getShulkerColor(container) : this.color.get();
        if (this.background.get() != Background.None) {
            drawBackground(renderer, (int) x, (int) y, drawColor);
        }
        if (MeteorClient.mc.field_1724 == null) {
            return;
        }
        renderer.post(() -> {
            for (int row = 0; row < 3; row++) {
                for (int i = 0; i < 9; i++) {
                    int index = (row * 9) + i;
                    class_1799 stack = hasContainer ? this.containerItems[index] : MeteorClient.mc.field_1724.method_31548().method_5438(index + 9);
                    if (stack != null) {
                        int itemX = this.background.get() == Background.Texture ? (int) (x + (((double) (8 + (i * 18))) * this.scale.get().doubleValue())) : (int) (x + (((double) (1 + (i * 18))) * this.scale.get().doubleValue()));
                        int itemY = this.background.get() == Background.Texture ? (int) (y + (((double) (7 + (row * 18))) * this.scale.get().doubleValue())) : (int) (y + (((double) (1 + (row * 18))) * this.scale.get().doubleValue()));
                        renderer.item(stack, itemX, itemY, this.scale.get().floatValue(), true);
                    }
                }
            }
        });
    }

    private void calculateSize() {
        setSize(((double) this.background.get().width) * this.scale.get().doubleValue(), ((double) this.background.get().height) * this.scale.get().doubleValue());
    }

    private void drawBackground(HudRenderer renderer, int x, int y, Color color) {
        int w = getWidth();
        int h = getHeight();
        switch (this.background.get().ordinal()) {
            case 1:
            case 2:
                renderer.texture(this.background.get() == Background.Texture ? TEXTURE : TEXTURE_TRANSPARENT, x, y, w, h, color);
                break;
            case 3:
                renderer.quad(x, y, w, h, color);
                break;
        }
    }

    private class_1799 getContainer() {
        if (isInEditor() || MeteorClient.mc.field_1724 == null) {
            return null;
        }
        class_1799 stack = MeteorClient.mc.field_1724.method_6079();
        if (Utils.hasItems(stack) || stack.method_7909() == class_1802.field_8466) {
            return stack;
        }
        class_1799 stack2 = MeteorClient.mc.field_1724.method_6047();
        if (Utils.hasItems(stack2) || stack2.method_7909() == class_1802.field_8466) {
            return stack2;
        }
        return null;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/InventoryHud$Background.class */
    public enum Background {
        None(Opcode.IF_ICMPGE, 54),
        Texture(Opcode.ARETURN, 67),
        Outline(Opcode.IF_ICMPGE, 54),
        Flat(Opcode.IF_ICMPGE, 54);

        private final int width;
        private final int height;

        Background(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
}
