# AutoRegear - Flexible Regearing Guide

## ? NEW: On-Demand Regearing & Multiple Modes!

You now have **full control** over when and how to regear!

---

## ?? 5 Pre-Set Modes (Change Anytime!)

### 1. **Emergency Mode** (Survival)
```
When: Only when critically low
- Elytra: < 5% durability
- Inventory: 20+ slots different
Use for: Maximum efficiency, minimal interruptions
```

### 2. **Conservative Mode** (Safe)
```
When: Low on supplies
- Elytra: < 10% durability  
- Inventory: 15+ slots different
Use for: Long AFK hunts, avoid running out
```

### 3. **Balanced Mode** (Default)
```
When: Moderate depletion
- Elytra: < 20% durability
- Inventory: 10+ slots different
Use for: General purpose hunting
```

### 4. **Aggressive Mode** (Frequent)
```
When: Any significant loss
- Elytra: < 40% durability
- Inventory: 5+ slots different
Use for: Keep well-stocked, regear often
```

### 5. **Top-Off Mode** (PvP/Competitive)
```
When: Missing even 1 item!
- Elytra: < 50% durability
- Inventory: 1+ slot different
Use for: PvP top-offs, stay at 100% always
```

### 6. **Custom Mode**
```
Set your own exact thresholds
- Custom elytra %
- Custom slot count
Use for: Your specific needs
```

---

## ?? Manual Trigger (Force Regear Anytime)

### Command: `.autoregear now` or `.ar now`

**Use Cases:**

**Before Big Fight:**
```bash
# You have most items but want to be 100% ready
.ar now
# ? Instantly regears to snapshot
```

**Mid-PvP:**
```bash
# Just used some totems and crystals, want to top off
.ar now  
# ? Regears without waiting for threshold
```

**Before Long Flight:**
```bash
# Elytra at 70% but you want fresh one
.ar now
# ? Forces regear even though not triggered
```

---

## ?? Real Examples

### Example 1: Hunting - Change on the Fly

**Start of session:** (Want max efficiency)
```
Mode: Emergency (20+ slots, 5% elytra)
Flying for hours without interruption
```

**Getting low on supplies:**
```
Open GUI ? Change to: Conservative (15+ slots, 10% elytra)
More frequent regears as supplies deplete
```

**Low on food/rockets:**
```
.ar now
Forces immediate regear
```

### Example 2: PvP - Multiple Regears

**Pre-fight loadout:**
```
.ar save (full PvP kit: totems, crystals, gaps, armor, etc.)
Mode: Top-Off (1+ slot, 50% elytra)
```

**After using 2 totems, 1 stack crystals:**
```
Automatic: Triggers because 3+ slots differ
OR Manual: .ar now to force it
? Builds obsidian box
? Regears from echest  
? Back to 100%
```

**Mid-fight, need quick top-off:**
```
.ar now (while behind cover)
? Quick regear
? Ready in 10-15 seconds
```

### Example 3: Variable Elytra Durability

**Scenario: 8-hour AFK hunt session**

**Hour 1-4:** (Far from base)
```
Mode: Aggressive (40% elytra threshold)
Regear early and often while far out
Always have good elytra
```

**Hour 5-7:** (Mid-range)
```
Mode: Balanced (20% elytra threshold)
Normal regear frequency
```

**Hour 8:** (Close to base)
```
Mode: Emergency (5% elytra threshold)
Squeeze every bit of durability
Can easily return to base if needed
```

### Example 4: PvP Top-Off Scenarios

**Situation: Missing 2 stacks EXP bottles, 3 totems**

**With Top-Off Mode:**
```
Mode: Top-Off
Missing 5 slots ? Auto-triggers
Regears back to full
```

**With Manual Trigger:**
```
Any mode + .ar now
Forces regear regardless of mode
```

**Both work! Choose based on preference:**
- Top-Off Mode: Fully automatic, always topped off
- Manual Mode: You control exactly when

---

## ?? How to Switch Modes

### In GUI (Real-time):
```
1. Open Meteor GUI (Right Shift)
2. Hunting ? AutoRegear
3. Triggers ? Regear Mode
4. Select mode from dropdown
5. Changes take effect immediately!
```

### Modes are Hot-Swappable:
- Change during active session
- No need to restart module
- Instant threshold updates

---

## ?? Pro Usage Patterns

### Pattern 1: Dynamic AFK Hunting
```
Start: Aggressive (regear early)
? (after a few regears)
Switch: Balanced (normal frequency)
? (running low on echest supplies)
Switch: Conservative (preserve supplies)
? (almost empty echest)
Manual: .ar now (one last regear before heading back)
```

### Pattern 2: PvP Session
```
Mode: Top-Off (always stay topped off)
Auto-trigger: ON
Every time you use a few items ? auto-regears
Always at 100% for fights
```

### Pattern 3: Hybrid Manual/Auto
```
Mode: Conservative (auto-trigger for emergencies)
Auto-trigger: ON
But also: Use .ar now when you feel like it
Best of both worlds
```

### Pattern 4: Custom Fine-Tuning
```
Mode: Custom
Elytra: 35% (your preference)
Slots: 7 (your threshold)
Perfect for your playstyle
```

---

## ?? Quick Reference Commands

```bash
.ar save      # Save inventory snapshot
.ar now       # FORCE regear RIGHT NOW (any mode)
.ar status    # Check current setup
.ar clear     # Clear snapshot

# In GUI: Change "Regear Mode" anytime!
```

---

## ?? Mode Selection Guide

**Choose based on:**

| Scenario | Recommended Mode | Why |
|----------|------------------|-----|
| Long AFK hunt (8+ hours) | Conservative | Don't run out far from base |
| Short hunt (1-2 hours) | Balanced | Good efficiency |
| Speedrunning/Racing | Aggressive | Stay well-stocked |
| PvP/Competitive | Top-Off | Always at 100% |
| Low echest supplies | Emergency | Preserve resources |
| Personal preference | Custom | Your exact thresholds |

**Or just use `.ar now` whenever you want to regear!**

---

## ? Key Features

? **Change modes mid-session** - No restart needed
? **Manual override anytime** - `.ar now` bypasses all triggers  
? **Automatic or manual** - Your choice
? **Variable thresholds** - 5% to 50% elytra, 1 to 20 slots
? **PvP top-off support** - Missing 2 exp bottles? Triggers instantly

---

## ?? Summary

**For Hunting:**
- Use pre-set modes based on how aggressive you want regearing
- Change mode as your session progresses
- Use `.ar now` for manual control

**For PvP:**
- Use **Top-Off mode** for automatic top-offs (missing 1+ item triggers)
- OR use **Balanced/Custom** + `.ar now` when you want to top off
- Missing 3 totems + 2 exp stacks? Both approaches work!

**Best of Both:**
- Enable auto-trigger with Conservative/Balanced mode
- Use `.ar now` for manual top-offs whenever you want
- Module handles both automatically AND on-demand!

---

**BUILD STATUS:** ? COMPLETE & WORKING

Enjoy your fully flexible regearing system! ??
