package meteordevelopment.meteorclient.systems.modules.hunting;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

public class ItemScanner extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender  = settings.createGroup("Render");

    private final Setting<List<Item>> items = sgGeneral.add(new ItemListSetting.Builder()
        .name("items")
        .description("Items to scan for on the ground.")
        .defaultValue(new ArrayList<>(List.of(Items.TOTEM_OF_UNDYING, Items.ELYTRA, Items.NETHERITE_INGOT)))
        .build()
    );

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("Maximum distance to scan for items.")
        .defaultValue(64)
        .min(8).sliderMax(128)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How to render item highlights.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("Fill color for item highlight.")
        .defaultValue(new SettingColor(29, 158, 117, 45))
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("Outline color for item highlight.")
        .defaultValue(new SettingColor(29, 158, 117, 200))
        .build()
    );

    public ItemScanner() {
        super(Categories.Hunting, "item-scanner", "Highlights target items lying on the ground within range.");
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;

        for (ItemEntity entity : mc.world.getEntitiesByClass(ItemEntity.class,
            mc.player.getBoundingBox().expand(range.get()), e -> true)) {

            if (!items.get().contains(entity.getStack().getItem())) continue;
            if (mc.player.distanceTo(entity) > range.get()) continue;

            Box box = entity.getBoundingBox().offset(-entity.getX(), -entity.getY(), -entity.getZ())
                             .offset(entity.getX() - entity.getX(), entity.getY() - entity.getY(), entity.getZ() - entity.getZ());
            box = entity.getBoundingBox();

            event.renderer.box(box, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
        }
    }
}
