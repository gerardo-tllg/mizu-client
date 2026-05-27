# AutoCrystal Rotation Fix - Version 2

## Problem
AutoCrystal was not breaking crystals automatically. The client required the player to manually look at crystals for them to be broken.

## Root Cause
The issue was with the rotation method being used. AutoCrystal was using `requestRotation()` which is **asynchronous**:

1. `requestRotation()` registers a rotation request that gets applied in the **next movement packet cycle**
2. AutoCrystal would call `requestRotation()` and then **immediately** send the attack packet
3. The attack packet was sent **before** the rotation was applied to the server
4. Result: The server sees the attack packet with the old rotation, not the new one

## Solution
Changed AutoCrystal to use `snapAt()` instead of `requestRotation()` for immediate actions:

- `snapAt()` **immediately sends a rotation packet** to the server
- The attack/place packet is sent right after the rotation packet
- This ensures the server receives the rotation before the attack/place action

This is the same pattern used by other combat modules like SwordAura.

## Changes Made

### `placeCrystal()` method:
**Before:**
```java
if (rotatePlace.get() && rotationTiming.get() == RotationTiming.Pre) {
    MeteorClient.ROTATION.requestRotation(currentTarget, 880);
}
```

**After:**
```java
if (rotatePlace.get() && rotationTiming.get() == RotationTiming.Pre) {
    MeteorClient.ROTATION.snapAt(currentTarget);
}
```

### `breakCrystal()` method:
**Before:**
```java
if (rotateBreak.get() && rotationTiming.get() == RotationTiming.Pre) {
    MeteorClient.ROTATION.requestRotation(currentTarget, 880);
}
```

**After:**
```java
if (rotateBreak.get() && rotationTiming.get() == RotationTiming.Pre) {
    MeteorClient.ROTATION.snapAt(currentTarget);
}
```

Both Pre and Post rotation timings were updated to use `snapAt()`.

## How It Works Now

### Breaking Crystals:
1. AutoCrystal detects a crystal to break
2. Calls `snapAt(crystal.getPos())` - **immediately sends rotation packet**
3. Sends attack packet
4. Server receives rotation first, then attack - ? Crystal breaks successfully

### Placing Crystals:
1. AutoCrystal finds a position to place
2. Calls `snapAt(blockPos.toCenterPos())` - **immediately sends rotation packet**  
3. Sends place packet
4. Server receives rotation first, then placement - ? Crystal places successfully

## Technical Details

### `snapAt()` vs `requestRotation()`:

**`snapAt(Vec3d target)`**:
- Immediately sends `PlayerMoveC2SPacket.LookAndOnGround` or `PlayerMoveC2SPacket.Full`
- Synchronous - happens right away
- Use for: Immediate actions (attacking, placing, breaking)

**`requestRotation(Vec3d target, double priority)`**:
- Registers a rotation request with a priority
- Applied in the next `RotateEvent` / movement packet cycle
- Asynchronous - happens in the next tick
- Use for: Smooth rotations, visual rotations, non-immediate actions

## Result
AutoCrystal now properly rotates to and breaks crystals automatically without requiring the player to look at them. The rotation is sent to the server immediately before the attack packet, ensuring the server processes the rotation first.

## Testing
? Code compiles successfully  
? No errors introduced  
? Rotation timing (Pre/Post) still works correctly
