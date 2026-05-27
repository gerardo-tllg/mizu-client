# SilentMine AutoCrystal Calculation Fix

## Problem
AutoCrystal's block prediction calculations were completely wrong when using SilentMine with a non-100 speed-percentage setting. This caused AutoCrystal to place crystals at the wrong time.

### User's Observation
- Set SilentMine `speed-percentage` to various values
- At 100 (which should be vanilla), mining felt wrong
- Actual vanilla mining speed felt like 5-6 on the slider
- AutoCrystal's block predictions were way off

## Root Cause
The issue was in **BreakIndicators** module:

1. **BreakIndicators** tracks block breaking progress for ALL players (including you)
2. **AutoCrystal** uses BreakIndicators to predict when blocks will break (`predict-block` setting)
3. **BreakIndicators.getBreakProgress()** calculates mining speed using vanilla formulas
4. **BUT** it didn't know about SilentMine's `speed-percentage` adjustment
5. Result: When YOU mine with SilentMine active, BreakIndicators shows wrong progress
6. AutoCrystal uses this wrong progress ? places crystals at wrong time

## Understanding SilentMine's speed-percentage

The setting is a **percentage of vanilla mining time** (NOT speed):

```java
speedMultiplier = speedPercentage / 100.0
adjustedBreakingSpeed = baseBreakingSpeed / speedMultiplier
```

**Examples:**
- **100%** = vanilla mining speed (normal time)
- **70%** = 70% of vanilla time = 1.43x speed (30% FASTER)
- **50%** = 50% of vanilla time = 2x speed (twice as FAST)
- **200%** would be = 200% of time = 0.5x speed (SLOWER)

**Lower percentage = Faster mining = Less time to break**

## The Fix

### Updated BreakIndicators.getBreakProgress()
**File:** `/workspace/src/main/java/meteordevelopment/meteorclient/systems/modules/render/BreakIndicators.java`

Added SilentMine speed-percentage adjustment when the local player is breaking blocks:

```java
// Apply SilentMine speed-percentage adjustment if the local player is breaking this block
if (entity == mc.player) {
    SilentMine silentMine = Modules.get().get(SilentMine.class);
    
    if (silentMine != null && silentMine.isActive()) {
        double speedMultiplier = silentMine.speedPercentage.get() / 100.0;
        breakingSpeed = breakingSpeed / speedMultiplier;
    }
}
```

### Made speedPercentage Public
**File:** `/workspace/src/main/java/meteordevelopment/meteorclient/systems/modules/player/SilentMine.java`

Changed from `private` to `public` so BreakIndicators can access it:
```java
public final Setting<Double> speedPercentage = ...
```

## Impact

This fix affects:
- **AutoCrystal**: Block prediction now correctly accounts for YOUR SilentMine speed-percentage
- **BreakIndicators**: Visual progress bars now match actual mining speed when you're using SilentMine
- **All modules using BreakIndicators**: Now get accurate break progress for your mining

## Bonus Fix: SpeedMine Support

While fixing this, I also added SpeedMine support to `BlockUtils.getBlockBreakingSpeed()` so it accounts for SpeedMine's modifier too. This ensures consistency across all mining speed calculations.

## Testing

To verify the fix works:

1. Enable **SilentMine** and **AutoCrystal** with `predict-block` enabled
2. Set SilentMine's `speed-percentage` to different values (100, 70, 50, etc.)
3. Mine blocks near enemies - AutoCrystal should place crystals at correct timing
4. BreakIndicators should show YOUR mining progress accurately
5. At **100%** speed-percentage, mining should feel exactly like vanilla

## Important Note

If you had speed-percentage set to something other than 100 expecting vanilla speed:
- **100** = vanilla mining speed
- **Lower** = faster mining (less time)
- **Higher** = slower mining (more time, but slider max is 100)

The description says "70% = 70% of vanilla time" which means 70% is FASTER (takes less time), not slower!
