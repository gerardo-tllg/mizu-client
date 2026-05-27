# AutoCrystal Rotation Fix

## Problem
AutoCrystal was only breaking crystals when the player manually looked at them. The automatic rotation system was not working properly.

## Root Cause
AutoCrystal was using **two conflicting rotation systems simultaneously**:

1. **RotationManager System**: Calling `requestRotation()` to register rotation requests (priority 880)
2. **Manual Event Handler**: Having its own `onRotationEvent` handler that manually calculated and set rotations

These two systems were interfering with each other:
- AutoCrystal would call `requestRotation()` to register a rotation request with RotationManager
- AutoCrystal's own `onRotationEvent` handler would then override or conflict with RotationManager's rotation application
- This caused rotations to not be properly applied when breaking/placing crystals

## Solution
Removed AutoCrystal's redundant rotation event handler and let the RotationManager handle all rotations through the proper `requestRotation()` system.

### Changes Made:

1. **Removed redundant rotation event handler** (`onRotationEvent` method)
   - This handler was manually calculating rotations using `MeteorClient.ROTATION.getRotation()`
   - It was overriding the RotationManager's proper rotation system

2. **Cleaned up unused rotation tracking code**:
   - Removed `moduleIsActivelyRotating()` method
   - Removed `rotationActive` field
   - Removed `lastHighPriorityRotationTime` and related cooldown logic
   - Simplified `maintainRotation()` to only clear stale targets

3. **Preserved the proper rotation system**:
   - Kept all `requestRotation()` calls in `placeCrystal()` and `breakCrystal()`
   - RotationManager now properly handles all rotation requests with priority 880
   - Rotation timing (Pre/Post) is still respected

## How It Works Now

1. When AutoCrystal needs to break/place a crystal:
   - Sets `currentTarget` to the crystal position
   - Calls `MeteorClient.ROTATION.requestRotation(currentTarget, 880)`
   
2. RotationManager's `onLastRotation` handler:
   - Checks rotation requests by priority
   - Applies the highest priority rotation request (including AutoCrystal's 880 priority)
   - Sets the yaw/pitch in the RotateEvent

3. The rotation is sent to the server through movement packets

## Result
AutoCrystal now properly rotates to crystals when breaking/placing them, even when the player is not manually looking at the crystals. The rotation system works seamlessly with other modules through the centralized RotationManager.
