package meteordevelopment.meteorclient.pathing;

import java.util.function.Predicate;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.Settings;
import net.minecraft.class_1297;
import net.minecraft.class_2248;
import net.minecraft.class_2338;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/pathing/IPathManager.class */
public interface IPathManager {

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/pathing/IPathManager$ISettings.class */
    public interface ISettings {
        Settings get();

        Setting<Boolean> getWalkOnWater();

        Setting<Boolean> getWalkOnLava();

        Setting<Boolean> getStep();

        Setting<Boolean> getNoFall();

        void save();
    }

    String getName();

    boolean isPathing();

    void pause();

    void resume();

    void stop();

    void moveTo(class_2338 class_2338Var, boolean z);

    void moveInDirection(float f);

    void mine(class_2248... class_2248VarArr);

    void follow(Predicate<class_1297> predicate);

    float getTargetYaw();

    float getTargetPitch();

    ISettings getSettings();

    default void moveTo(class_2338 pos) {
        moveTo(pos, false);
    }
}
