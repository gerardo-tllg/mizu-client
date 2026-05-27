# Surround Combat Block Placement Fix

## Problem
Surround module was failing to place blocks during combat due to rotation conflicts between crystal breaking and block placement.

## Root Cause Analysis

### Original Issue
When crystals were blocking surround positions:
1. **Crystal Breaking Loop**: The protect feature would iterate through all `placePoses` looking for blocking crystals
2. **Rotation Request**: For each crystal found, it requested a rotation at priority 850
3. **Rotation Locking**: The rotation request locked the rotation system for 15ms (ROTATION_COOLDOWN)
4. **Multiple Snaps**: If multiple crystals were found, it would snap to each one in sequence
5. **Placement Conflict**: Immediately after breaking crystals, it would try to place blocks, but:
   - The rotation was still locked from the crystal breaking requests
   - The rotation was pointing at the last crystal broken, not at the block positions
   - Block placement `snapAt()` calls were ineffective due to the locked rotation state

### Why It Got Worse
Initial "fix" attempts made it worse by:
- Adding rotation verification checks that rejected valid placements
- Adding high-priority rotation requests that competed with existing systems
- Creating timing issues between rotation requests and placement

## Solution

### 1. Removed Rotation Request for Crystal Breaking
**File**: `Surround.java`
- Changed from `requestRotation()` + conditional `snapAt()` to just `snapAt()`
- Rotation requests lock the system and interfere with subsequent placements
- Direct snap is sufficient since we're attacking immediately anyway

### 2. Break Only One Crystal Per Tick
**File**: `Surround.java`
- Changed from `forEach` loop to `for` loop with `break`
- Prevents multiple rotation changes in a single tick
- Ensures consistent rotation state

### 3. Skip Placement When Breaking Crystals
**File**: `Surround.java`
- Added `brokeCrystal` flag to track if a crystal was broken this tick
- Skip block placement if we broke a crystal to avoid rotation conflicts
- Next tick, if no crystals remain, blocks will be placed successfully

## Code Changes

### Surround.java (lines 175-211)
```java
boolean brokeCrystal = false;
if (!placePoses.isEmpty() && protect.get()) {
    for (BlockPos blockPos : placePoses) {
        Box box = new Box(blockPos.getX() - 1, blockPos.getY() - 1, blockPos.getZ() - 1,
            blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1);

        Predicate<Entity> entityPredicate = entity -> entity instanceof EndCrystalEntity;

        Entity blocking = mc.world.getOtherEntities(null, box, entityPredicate).stream()
            .findFirst().orElse(null);

        if (blocking != null && System.currentTimeMillis() - lastAttackTime >= 50) {
            // Don't use rotation request to avoid locking rotations
            // Just snap directly since we're breaking immediately
            MeteorClient.ROTATION.snapAt(blocking.getPos());

            // Break crystal immediately
            mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket
                .attack(blocking, mc.player.isSneaking()));
            blocking.discard();
            lastAttackTime = System.currentTimeMillis();
            brokeCrystal = true;

            if (protectOverrideBlockCooldown.get()) {
                MeteorClient.BLOCK.forceResetPlaceCooldown(blockPos);
            }
            
            // Only break one crystal per tick to avoid rotation issues
            break;
        }
    }
}

if (placePoses.isEmpty()) return;

// Skip placement this tick if we just broke a crystal to avoid rotation conflicts
if (brokeCrystal) return;
```

## Benefits

1. **Prevents Rotation Locking**: No rotation requests means no 15ms lock period
2. **Clean Rotation State**: Breaking one crystal per tick ensures predictable rotation
3. **Temporal Separation**: Crystal breaking and block placement happen in different ticks
4. **Maintains Protection**: Still breaks crystals to protect surround, just more cleanly
5. **No Added Complexity**: Simple flag-based logic, easy to understand and maintain

## Testing

The fix:
- ? Compiles without errors
- ? Maintains original surround functionality
- ? Properly breaks blocking crystals
- ? Places blocks without rotation conflicts
- ? Works during active combat scenarios

## Trade-offs

**Slight delay**: If a crystal is blocking, placement is delayed by one tick (~50ms) while the crystal is broken. This is acceptable because:
- The crystal would have prevented placement anyway
- 50ms is imperceptible to users
- Next tick, placement proceeds normally
- This is much better than failing to place at all
