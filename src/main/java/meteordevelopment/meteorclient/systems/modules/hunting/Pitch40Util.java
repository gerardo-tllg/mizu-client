/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.hunting;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFly;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightModes;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;

public class Pitch40Util extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Boolean> autoBoundAdjust = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-adjust-bounds")
        .description("Adjusts your bounds to make you continue to gain height. Good for fixing falling on reconnect or lag, etc.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Double> boundGap = sgGeneral.add(new DoubleSetting.Builder()
        .name("bound-gap")
        .description("The gap between the upper and lower bounds. Used when reconnecting, or when at max height if Auto Adjust Bounds is enabled.")
        .defaultValue(60)
        .sliderRange(50, 100)
        .build()
    );

    public final Setting<Boolean> autoFirework = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-firework")
        .description("Uses a firework automatically if your velocity is too low.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Double> velocityThreshold = sgGeneral.add(new DoubleSetting.Builder()
        .name("velocity-threshold")
        .description("Velocity must be below this value when going up for firework to activate.")
        .defaultValue(-0.05)
        .sliderRange(-0.5, 1)
        .visible(autoFirework::get)
        .build()
    );

    public final Setting<Integer> fireworkCooldown = sgGeneral.add(new IntSetting.Builder()
        .name("firework-cooldown")
        .description("Minimum amount of ticks to wait between using fireworks")
        .defaultValue(20)
        .min(1)
        .sliderRange(5, 100)
        .visible(autoFirework::get)
        .build()
    );

    private boolean fallenLastTick = false;
    private boolean increasedLowerThisTick = false;
    private int ticksSinceLastFirework = 0;

    private double minY = Double.NEGATIVE_INFINITY;
    private double maxY = Double.POSITIVE_INFINITY;
    private double lastY = Double.NEGATIVE_INFINITY;

    public Pitch40Util() {
        super(Categories.Hunting, "pitch-40-util", "Auto-manages pitch 40 elytra flying for optimal long-distance travel.");
    }

    @Override
    public void onActivate() {
        fallenLastTick = false;
        increasedLowerThisTick = false;
        ticksSinceLastFirework = 0;
        minY = Double.NEGATIVE_INFINITY;
        maxY = Double.POSITIVE_INFINITY;
        lastY = mc.player.getY();
    }

    @EventHandler
    @SuppressWarnings("unchecked")
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        // Auto-adjust bounds
        if (autoBoundAdjust.get()) {
            if (!fallenLastTick && lastY > mc.player.getY()) {
                // Started falling
                fallenLastTick = true;
                maxY = lastY;

                if (maxY - minY > boundGap.get() * 2 && !increasedLowerThisTick) {
                    minY = maxY - boundGap.get();
                }
                increasedLowerThisTick = false;
            } else if (fallenLastTick && lastY < mc.player.getY()) {
                // Started ascending
                fallenLastTick = false;
                minY = lastY;
                increasedLowerThisTick = true;
            }
        }

        lastY = mc.player.getY();

        // Auto-firework when velocity too low
        if (autoFirework.get() && ticksSinceLastFirework >= fireworkCooldown.get()) {
            double velocity = mc.player.getVelocity().y;
            
            // Only use firework when ascending and velocity is too low
            if (!fallenLastTick && velocity < velocityThreshold.get()) {
                HuntingUtils.firework();
                ticksSinceLastFirework = 0;
            }
        }

        ticksSinceLastFirework++;
    }

    @Override
    public String getInfoString() {
        if (mc.player == null) return null;
        return String.format("%.0f/%.0f", minY, maxY);
    }
}
