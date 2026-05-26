package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.hunting.AutoRegear;
import net.minecraft.class_2172;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/commands/AutoRegearCommand.class */
public class AutoRegearCommand extends Command {
    public AutoRegearCommand() {
        super("autoregear", "Manage AutoRegear inventory snapshots.", "ar");
    }

    @Override // meteordevelopment.meteorclient.commands.Command
    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(literal("now").executes(context -> {
            AutoRegear autoRegear = (AutoRegear) Modules.get().get(AutoRegear.class);
            if (autoRegear == null) {
                error("AutoRegear module not found", new Object[0]);
                return 1;
            }
            if (!autoRegear.isActive()) {
                warning("AutoRegear module is not enabled. Enabling now...", new Object[0]);
                autoRegear.toggle();
            }
            autoRegear.forceRegear();
            info("Manual regear started!", new Object[0]);
            return 1;
        }));
        builder.then(literal("save").executes(context2 -> {
            AutoRegear autoRegear = (AutoRegear) Modules.get().get(AutoRegear.class);
            if (autoRegear == null) {
                error("AutoRegear module not found", new Object[0]);
                return 1;
            }
            autoRegear.saveSnapshot();
            info("Inventory snapshot saved! (" + autoRegear.getSnapshotSize() + " slots)", new Object[0]);
            info("AutoRegear will now trigger when inventory differs from this snapshot.", new Object[0]);
            return 1;
        }));
        builder.then(literal("clear").executes(context3 -> {
            AutoRegear autoRegear = (AutoRegear) Modules.get().get(AutoRegear.class);
            if (autoRegear == null) {
                error("AutoRegear module not found", new Object[0]);
                return 1;
            }
            autoRegear.clearSnapshot();
            info("Inventory snapshot cleared.", new Object[0]);
            return 1;
        }));
        builder.then(literal("status").executes(context4 -> {
            AutoRegear autoRegear = (AutoRegear) Modules.get().get(AutoRegear.class);
            if (autoRegear == null) {
                error("AutoRegear module not found", new Object[0]);
                return 1;
            }
            int size = autoRegear.getSnapshotSize();
            if (size > 0) {
                info("Snapshot status: " + size + " slots saved", new Object[0]);
                info("Module active: " + (autoRegear.isActive() ? "Yes" : "No"), new Object[0]);
                return 1;
            }
            warning("No snapshot saved. Use '.autoregear save' when inventory is ready.", new Object[0]);
            return 1;
        }));
        builder.executes(context5 -> {
            info("AutoRegear Commands:", new Object[0]);
            info("  .autoregear save - Save current inventory as regear snapshot", new Object[0]);
            info("  .autoregear now - Force immediate regear (manual trigger)", new Object[0]);
            info("  .autoregear clear - Clear saved snapshot", new Object[0]);
            info("  .autoregear status - Check snapshot status", new Object[0]);
            info("", new Object[0]);
            info("Usage:", new Object[0]);
            info("1. Fill inventory with desired items", new Object[0]);
            info("2. Use '.autoregear save' to save snapshot", new Object[0]);
            info("3. Enable AutoRegear module", new Object[0]);
            info("4. Module auto-triggers OR use '.ar now' to force", new Object[0]);
            info("", new Object[0]);
            info("Regear Modes (change in GUI):", new Object[0]);
            info("  - Emergency: Only critical (20+ slots, 5%% elytra)", new Object[0]);
            info("  - Conservative: Low supplies (15+ slots, 10%% elytra)", new Object[0]);
            info("  - Balanced: Default (10+ slots, 20%% elytra)", new Object[0]);
            info("  - Aggressive: Early regear (5+ slots, 40%% elytra)", new Object[0]);
            info("  - Top-Off: PvP mode (1+ slot, 50%% elytra)", new Object[0]);
            info("  - Custom: Set your own thresholds", new Object[0]);
            return 1;
        });
    }
}
