/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import net.minecraft.entity.player.PlayerPosition;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

// In 1.21.5, PlayerPositionLookS2CPacket is a record with:
// - int teleportId
// - PlayerPosition change (contains position Vec3, deltaMovement Vec3, yaw float, pitch float) 
// - Set<PositionFlag> relatives

@Mixin(PlayerPositionLookS2CPacket.class)
public interface PlayerPositionLookS2CPacketAccessor {
    // Access the change object which contains position and rotation
    @Accessor("change")
    PlayerPosition getChange();
    
    // Access the relatives set
    @Accessor("relatives")
    Set<PositionFlag> getRelatives();
    
    // Mutable accessors for modifying the packet
    @Mutable
    @Accessor("change")
    void setChange(PlayerPosition change);
}
