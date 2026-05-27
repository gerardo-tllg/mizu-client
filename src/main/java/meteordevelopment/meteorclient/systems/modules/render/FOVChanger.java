package meteordevelopment.meteorclient.systems.modules.render;

import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Field;

public class FOVChanger extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> fov = sgGeneral.add(new DoubleSetting.Builder()
        .name("fov")
        .description("The FOV value.")
        .defaultValue(90)
        .range(30, 1000)
        .sliderRange(30, 360)
        .onChanged(value -> updateFOV())
        .build()
    );

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public FOVChanger() {
        super(Categories.Render, "fov-changer", "Allows modification of the FOV and aspect ratio.");
    }


    @Override
    public void onDeactivate() {
        setFOVUnrestricted(90);
        GL11.glViewport(0, 0, mc.getWindow().getWidth(), mc.getWindow().getHeight()); // Reset viewport
    }

    private void updateFOV() {
        if (isActive()) {
            double newFOV = fov.get();
            if (newFOV < 30) newFOV = 30;
            if (newFOV > 1000) newFOV = 1000;

            setFOVUnrestricted((int) newFOV);
        }
    }


    private void setFOVUnrestricted(int fov) {
        try {
            Field fovField = mc.options.getClass().getDeclaredField("fov");
            fovField.setAccessible(true);
            SimpleOption<Integer> fovOption = (SimpleOption<Integer>) fovField.get(mc.options);

            Field valueField = SimpleOption.class.getDeclaredField("value");
            valueField.setAccessible(true);
            valueField.set(fovOption, fov);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
