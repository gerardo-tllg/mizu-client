# AntiCheatConfig Null Check Removal & ViaFabricPlus Update

## Changes Made

### 1. Removed AntiCheatConfig Null Checks

Removed all null checks on `AntiCheatConfig.get()` from three manager classes:

#### RotationManager.java
- **Removed**: `getAntiCheatConfig()` helper method with null check
- **Updated**: All calls now use `AntiCheatConfig.get()` directly
- **Changed methods**:
  - `snapAt(Vec3d target)` - Direct call to `AntiCheatConfig.get().grimSnapRotation.get()`
  - `snapAt(float yaw, float pitch)` - Direct call to `AntiCheatConfig.get().grimSnapRotation.get()`
  - `onMovementPacket()` - Direct call to `AntiCheatConfig.get().tickSync.get()`
  - Rotation sync flags - Direct calls to `grimSync.get()` and `grimRotation.get()`

#### SwapManager.java
- **Removed**: `getAntiCheatConfig()` helper method with null check
- **Updated**: All calls now use `AntiCheatConfig.get()` directly
- **Changed methods**:
  - `beginSwap()` - Auto mode: Direct call to `swapAntiScreenClose.get()`
  - `beginSwap()` - SilentSwap mode: Direct call to `swapAntiScreenClose.get()`
  - `getItemSwapMode()` - Direct call to `AntiCheatConfig.get().swapMode.get()`

#### BlockPlacementManager.java
- **Removed**: `getAntiCheatConfig()` helper method with null check
- **Updated**: All calls now use `AntiCheatConfig.get()` directly
- **Changed methods**:
  - `placeBlock()` - Direct calls to `blocksPerSecondCap.get()`, `blockRotatePlace.get()`, `blockPlacePerBlockCooldown.get()`, `blockPlaceAirPlace.get()`, and `forceAirPlace.get()`
  - `checkPlacement()` - Direct call to `blockPlaceAirPlace.get()`
  - `checkLimit()` - Direct call to `blockPacketLimit.get()`

### 2. Updated ViaFabricPlus Dependency

**File**: `gradle.properties`

**Changed**:
```properties
# Before
viafabricplus_version=4.1.0

# After
viafabricplus_version=4.1.1
```

This updates ViaFabricPlus to version 4.1.1 which includes 1.21.5 compatibility fixes.

## Rationale

### Null Check Removal
The null checks were unnecessary defensive programming that:
1. Added complexity to the code
2. Provided confusing fallback behavior that could mask actual initialization issues
3. Made the code harder to read and maintain

`AntiCheatConfig.get()` is initialized early in the client startup and should always be available when these managers are in use. If it's not available, it's better to fail fast with a clear NPE than silently fall back to inconsistent behavior.

### ViaFabricPlus Update
Version 4.1.1 includes important fixes and compatibility updates for Minecraft 1.21.5.

## Testing

? All changes compiled successfully with no errors
? Build completed with only expected deprecation warnings (unrelated to these changes)

## Impact

These changes:
- Simplify the codebase
- Make behavior more predictable
- Improve code readability
- Ensure ViaFabricPlus compatibility with 1.21.5
