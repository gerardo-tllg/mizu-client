# Bug Fixes: Double Mining & AutoCrystal Self-Targeting

## Summary
Fixed two critical PVP issues: AutoCrystal sometimes targeting the player instead of enemies, and double mining failing in certain scenarios.

---

## Issue 1: AutoCrystal Targeting Self Instead of Enemy

### Problem
- AutoCrystal would sometimes target the player (mc.player) instead of enemies
- This caused crystals to be placed near the player, dealing self-damage without targeting enemies
- Could result in self-kills during combat

### Root Cause
The damage calculation methods (`findBestPlacePosition`, face place logic) did not have safety checks to ensure:
1. The target was never mc.player
2. Self-damage was within acceptable limits before placing crystals

### Solution
Added multiple layers of safety checks throughout AutoCrystal:

1. **Target Validation in `findBestPlacePosition()`**:
   - Added explicit check: `if (target == mc.player) return null;`
   - Added self-damage validation for every placement position
   - Self-damage check: `if (selfDamage > maxPlace.get()) continue;`

2. **Enhanced Safety Checks in Target Selection**:
   - Updated `selectBestTarget()` with null check: `if (player == null || player == mc.player || ...)`
   - Updated `getNearestEnemyPlayer()` with same safety checks
   - Updated `shouldBreakCrystal()` to never consider mc.player as valid target

3. **Face Place Protection**:
   - Added self-damage checks to face place logic
   - Ensures even desperate face-place attempts won't suicide the player

### Files Modified
- `src/main/java/meteordevelopment/meteorclient/systems/modules/combat/autocrystal/AutoCrystal.java`

### Code Changes
```java
// Added in findBestPlacePosition()
if (target == mc.player) return null;  // Never target ourselves

// Added for each crystal position evaluation
double selfDamage = DamageUtils.crystalDamage(mc.player, crystalPos);
if (selfDamage > maxPlace.get()) continue;

// Enhanced all player loops
if (player == null || player == mc.player || Friends.get().isFriend(player) || player.isDead()) continue;
```

---

## Issue 2: Double Mining Failing Sometimes

### Problem
- Double mining coordination would fail intermittently
- AutoMine would avoid blocks that should be mined simultaneously
- Break synchronization between players was unreliable

### Root Causes
1. **Overly Aggressive Block Removal**: BreakIndicators removed blocks from `predictedDoublemine` too early (at 2+ blocks instead of 3+)
2. **Premature Penalty Application**: AutoMine penalized its own mining operations
3. **Insufficient Validation**: Missing null checks and activity checks

### Solution

#### 1. BreakIndicators Improvements
**Before**: Removed oldest block when player was breaking 2+ blocks
**After**: Only removes oldest block when breaking 3+ blocks AND block is >80% complete

```java
// Changed from: if (playerBreakingBlocks.size() >= 2)
if (playerBreakingBlocks.size() >= 3) {
    BlockBreak oldestBlock = playerBreakingBlocks.get(0);
    double oldestProgress = oldestBlock.getBreakProgress(currentGameTickCalculated);
    if (oldestProgress > 0.8) {
        predictedDoublemine.remove(oldestBlock.blockPos);
    }
}
```

**Benefits**:
- Allows proper double mining with 2 blocks
- Only cleans up when truly necessary (3+ blocks with high progress)
- More accurate prediction of which blocks are being double-mined

#### 2. AutoMine Score Penalty Improvements
Enhanced `getScorePenaltyForSync()` with:
- Null and active state checks for BreakIndicators
- Never penalizes player's own mining (`if (doubleminer == mc.player) return 0.0;`)
- Proper null checks for doubleminer entity
- Clearer logic for friend-only penalties

**Benefits**:
- Player's own double mining operations are never penalized
- Prevents scoring conflicts when mining multiple blocks
- More reliable coordination with teammates

### Files Modified
- `src/main/java/meteordevelopment/meteorclient/systems/modules/render/BreakIndicators.java`
- `src/main/java/meteordevelopment/meteorclient/systems/modules/combat/AutoMine.java`

---

## Technical Details

### AutoCrystal Self-Targeting Prevention
The multi-layered approach ensures safety even if one check fails:
- **Layer 1**: Target selection excludes mc.player
- **Layer 2**: Damage calculation validates target is not mc.player  
- **Layer 3**: Self-damage is checked for every crystal position
- **Layer 4**: Break operations verify target is not mc.player

### Double Mining Reliability
The improved logic:
1. Tracks up to 3 blocks per player without interference
2. Only removes blocks that are nearly complete (>80%)
3. Never penalizes the player's own mining operations
4. Provides accurate double-mine detection for AutoMine scoring

---

## Testing Recommendations

### AutoCrystal Self-Targeting
1. Test in various PVP scenarios with enemies at different distances
2. Verify crystals are never placed near yourself when enemies are far
3. Check that self-damage limits are properly respected
4. Test face-place mode to ensure it doesn't suicide

### Double Mining
1. Test breaking 2 blocks simultaneously (should work now)
2. Test with friends to ensure sync penalties work correctly
3. Verify AutoMine targets both blocks efficiently
4. Check that blocks don't get removed prematurely from prediction

---

## Compilation Status
? All changes compile successfully with no errors
?? Only pre-existing warnings remain (unrelated to these fixes)

---

## Impact
- **Critical Safety**: Players can no longer be targeted by their own AutoCrystal
- **Improved Performance**: Double mining is now reliable and efficient
- **Better Coordination**: Mining synchronization works correctly with teammates
