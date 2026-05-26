package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.function.Predicate;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1799;
import net.minecraft.class_2199;
import net.minecraft.class_2248;
import net.minecraft.class_471;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/SelfAnvil.class */
public class SelfAnvil extends Module {
    public SelfAnvil() {
        super(Categories.Combat, "self-anvil", "Automatically places an anvil on you to prevent other players from going into your hole.");
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (event.screen instanceof class_471) {
            event.cancel();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (BlockUtils.place(this.mc.field_1724.method_24515().method_10069(0, 2, 0), InvUtils.findInHotbar((Predicate<class_1799>) itemStack -> {
            return class_2248.method_9503(itemStack.method_7909()) instanceof class_2199;
        }), 0)) {
            toggle();
        }
    }
}
