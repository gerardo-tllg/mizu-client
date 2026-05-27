/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.player;

import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.entity.effect.StatusEffect;

public class NoStatusEffects extends Module {
    public NoStatusEffects() {
        super(Categories.Player, "no-status-effects", "Hides status effects from your HUD.");
    }

    public boolean shouldBlock(StatusEffect effect) {
        return false; // Placeholder - implement as needed
    }
}
