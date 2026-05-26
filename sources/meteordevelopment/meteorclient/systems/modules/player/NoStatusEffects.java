package meteordevelopment.meteorclient.systems.modules.player;

import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.class_1291;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/player/NoStatusEffects.class */
public class NoStatusEffects extends Module {
    public NoStatusEffects() {
        super(Categories.Player, "no-status-effects", "Hides status effects from your HUD.");
    }

    public boolean shouldBlock(class_1291 effect) {
        return false;
    }
}
