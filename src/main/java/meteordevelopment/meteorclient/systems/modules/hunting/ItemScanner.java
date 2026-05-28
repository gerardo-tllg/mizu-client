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

import java.util.List;

public class ItemScanner extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender  = settings.createGroup("Render");

    private final Setting<List<Item>> items = sgGeneral.add(new ItemListSetting.Builder()
        .name("items")
        .description("Items to highlight on the ground.")
        .defaultValue(Items.TOTEM_OF_UNDYING, Items.ELYTRA, Items.NETHERITE_INGOT)
        .build()
    );

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("Scan radius in blocks.")
        .defaultValue(64)
        .min(8).sliderMax(128)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .defaultValue(new SettingColor(29, 158, 117, 45))
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .defaultValue(new SettingColor(29, 158, 117, 200))
        .build()
    );

    public ItemScanner() {
        super(Categories.Hunting, "item-scanner", "Highlights target item entities on the ground.");
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;

        double r = range.get();
        for (ItemEntity entity : mc.world.getEntitiesByClass(ItemEntity.class,
            mc.player.getBoundingBox().expand(r), e -> true)) {

            if (!items.get().contains(entity.getStack().getItem())) continue;
            if (mc.player.distanceTo(entity) > r) continue;

            var box = entity.getBoundingBox();
            event.renderer.box(
                box.minX, box.minY, box.minZ,
                box.maxX, box.maxY, box.maxZ,
                sideColor.get(), lineColor.get(), shapeMode.get(), 0
            );
        }
    }
}
