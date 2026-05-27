/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.hunting.AutoRegear;
import net.minecraft.command.CommandSource;

public class AutoRegearCommand extends Command {
    public AutoRegearCommand() {
        super("autoregear", "Manage AutoRegear inventory snapshots.", "ar");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("now").executes(context -> {
            AutoRegear autoRegear = Modules.get().get(AutoRegear.class);
            if (autoRegear == null) {
                error("AutoRegear module not found");
                return SINGLE_SUCCESS;
            }
            
            if (!autoRegear.isActive()) {
                warning("AutoRegear module is not enabled. Enabling now...");
                autoRegear.toggle();
            }
            
            autoRegear.forceRegear();
            info("Manual regear started!");
            
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("save").executes(context -> {
            AutoRegear autoRegear = Modules.get().get(AutoRegear.class);
            if (autoRegear == null) {
                error("AutoRegear module not found");
                return SINGLE_SUCCESS;
            }
            
            autoRegear.saveSnapshot();
            info("Inventory snapshot saved! (" + autoRegear.getSnapshotSize() + " slots)");
            info("AutoRegear will now trigger when inventory differs from this snapshot.");
            
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("clear").executes(context -> {
            AutoRegear autoRegear = Modules.get().get(AutoRegear.class);
            if (autoRegear == null) {
                error("AutoRegear module not found");
                return SINGLE_SUCCESS;
            }
            
            autoRegear.clearSnapshot();
            info("Inventory snapshot cleared.");
            
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("status").executes(context -> {
            AutoRegear autoRegear = Modules.get().get(AutoRegear.class);
            if (autoRegear == null) {
                error("AutoRegear module not found");
                return SINGLE_SUCCESS;
            }
            
            int size = autoRegear.getSnapshotSize();
            if (size > 0) {
                info("Snapshot status: " + size + " slots saved");
                info("Module active: " + (autoRegear.isActive() ? "Yes" : "No"));
            } else {
                warning("No snapshot saved. Use '.autoregear save' when inventory is ready.");
            }
            
            return SINGLE_SUCCESS;
        }));

        builder.executes(context -> {
            info("AutoRegear Commands:");
            info("  .autoregear save - Save current inventory as regear snapshot");
            info("  .autoregear now - Force immediate regear (manual trigger)");
            info("  .autoregear clear - Clear saved snapshot");
            info("  .autoregear status - Check snapshot status");
            info("");
            info("Usage:");
            info("1. Fill inventory with desired items");
            info("2. Use '.autoregear save' to save snapshot");
            info("3. Enable AutoRegear module");
            info("4. Module auto-triggers OR use '.ar now' to force");
            info("");
            info("Regear Modes (change in GUI):");
            info("  - Emergency: Only critical (20+ slots, 5%% elytra)");
            info("  - Conservative: Low supplies (15+ slots, 10%% elytra)");
            info("  - Balanced: Default (10+ slots, 20%% elytra)");
            info("  - Aggressive: Early regear (5+ slots, 40%% elytra)");
            info("  - Top-Off: PvP mode (1+ slot, 50%% elytra)");
            info("  - Custom: Set your own thresholds");
            
            return SINGLE_SUCCESS;
        });
    }
}
