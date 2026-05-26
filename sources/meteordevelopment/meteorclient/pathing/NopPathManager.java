package meteordevelopment.meteorclient.pathing;

import java.util.function.Predicate;
import meteordevelopment.meteorclient.pathing.IPathManager;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.Settings;
import net.minecraft.class_1297;
import net.minecraft.class_2248;
import net.minecraft.class_2338;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/pathing/NopPathManager.class */
public class NopPathManager implements IPathManager {
    private final NopSettings settings = new NopSettings();

    @Override // meteordevelopment.meteorclient.pathing.IPathManager
    public String getName() {
        return "none";
    }

    @Override // meteordevelopment.meteorclient.pathing.IPathManager
    public boolean isPathing() {
        return false;
    }

    @Override // meteordevelopment.meteorclient.pathing.IPathManager
    public void pause() {
    }

    @Override // meteordevelopment.meteorclient.pathing.IPathManager
    public void resume() {
    }

    @Override // meteordevelopment.meteorclient.pathing.IPathManager
    public void stop() {
    }

    @Override // meteordevelopment.meteorclient.pathing.IPathManager
    public void moveTo(class_2338 pos, boolean ignoreY) {
    }

    @Override // meteordevelopment.meteorclient.pathing.IPathManager
    public void moveInDirection(float yaw) {
    }

    @Override // meteordevelopment.meteorclient.pathing.IPathManager
    public void mine(class_2248... blocks) {
    }

    @Override // meteordevelopment.meteorclient.pathing.IPathManager
    public void follow(Predicate<class_1297> entity) {
    }

    @Override // meteordevelopment.meteorclient.pathing.IPathManager
    public float getTargetYaw() {
        return 0.0f;
    }

    @Override // meteordevelopment.meteorclient.pathing.IPathManager
    public float getTargetPitch() {
        return 0.0f;
    }

    @Override // meteordevelopment.meteorclient.pathing.IPathManager
    public IPathManager.ISettings getSettings() {
        return this.settings;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/pathing/NopPathManager$NopSettings.class */
    private static class NopSettings implements IPathManager.ISettings {
        private final Settings settings = new Settings();
        private final Setting<Boolean> setting = new BoolSetting.Builder().build();

        private NopSettings() {
        }

        @Override // meteordevelopment.meteorclient.pathing.IPathManager.ISettings
        public Settings get() {
            return this.settings;
        }

        @Override // meteordevelopment.meteorclient.pathing.IPathManager.ISettings
        public Setting<Boolean> getWalkOnWater() {
            this.setting.reset();
            return this.setting;
        }

        @Override // meteordevelopment.meteorclient.pathing.IPathManager.ISettings
        public Setting<Boolean> getWalkOnLava() {
            this.setting.reset();
            return this.setting;
        }

        @Override // meteordevelopment.meteorclient.pathing.IPathManager.ISettings
        public Setting<Boolean> getStep() {
            this.setting.reset();
            return this.setting;
        }

        @Override // meteordevelopment.meteorclient.pathing.IPathManager.ISettings
        public Setting<Boolean> getNoFall() {
            this.setting.reset();
            return this.setting;
        }

        @Override // meteordevelopment.meteorclient.pathing.IPathManager.ISettings
        public void save() {
        }
    }
}
