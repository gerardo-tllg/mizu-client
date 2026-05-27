# ?? WheelPicker - GTA-Style Radial Menu

## ? BUILD SUCCESSFUL - Module Added!

**Category:** Misc
**Source:** BepHaxAddon by dekrom
**Activation:** Hold V key (customizable)

---

## ?? What Is WheelPicker?

A **GTA-style radial menu** that appears when you hold a key. Move your mouse in different directions to quickly trigger actions - just like the weapon wheel in Grand Theft Auto!

### Visual Layout:
```
          [TOP]
            ?
  [TOP-LEFT] ? ? [TOP-RIGHT]
            
    [LEFT] ? + ? [RIGHT]
            
[BOTTOM-LEFT] ? ? [BOTTOM-RIGHT]
            ?
        [BOTTOM]
```

**8 segments = 8 customizable actions!**

---

## ?? Quick Start

### Basic Usage:
1. Enable **WheelPicker** module in GUI (Misc category)
2. Configure your 8 slots (see below)
3. In-game: Hold **V** key
4. Move mouse towards desired action
5. Release **V** to execute

### Example Setup - Hunting Wheel:

**Configure in WheelPicker settings:**

| Direction | Action | Configuration | Icon |
|-----------|--------|---------------|------|
| **Top** | Toggle Module | `AutoRegear` | Ender Chest |
| **Top-Right** | Toggle Module | `TrailFollower` | Compass |
| **Right** | Run Command | `ar now` | Firework |
| **Bottom-Right** | Toggle Module | `AutoLogPlus` | Bed |
| **Bottom** | Toggle Module | `BetterStashFinder` | Chest |
| **Bottom-Left** | Send Message | `coords?` | Map |
| **Left** | Toggle Module | `Freecam` | Ender Eye |
| **Top-Left** | Toggle Module | `AFKVanillaFly` | Elytra |

---

## ?? How to Configure

### Step 1: Open WheelPicker Settings
```
GUI ? Misc ? WheelPicker ? Settings
Navigate to "Slot Actions" group
```

### Step 2: Configure Each Slot

For each direction (Top, Top-Right, etc.):

**1. Choose Action Type:**
- `None` - No action
- `Toggle Module` - Turn a module on/off
- `Send Message` - Send chat message
- `Run Command` - Execute command

**2. Set Icon** (any Minecraft item)
- Shows in the wheel segment
- Visual reference for muscle memory

**3. Set Custom Text** (optional)
- Custom label for the slot
- Leave empty for auto-generated label

**4. Configure Action Details:**

**If Toggle Module:**
- Enter exact module name (case-sensitive)
- Example: `TrailFollower`, `AutoRegear`, `Freecam`

**If Send Message:**
- Enter message to send
- Use `[]` for random text: `"hello []"` ? `"hello [a3xK9]"`
- Use `[RANDOM]` placeholder: `"coords [RANDOM]"` ? `"coords jK83m"`

**If Run Command:**
- Enter command without `/`
- Example: `ar now`, `home`, `tpa player`

---

## ?? Visual Customization

### Appearance Settings:

**Colors:**
- Background Color - Default wheel background
- Selected Color - Highlighted segment when hovering
- Border Color - Lines between segments
- Text Color - Normal text
- Module Active Color - Text when module is ON (shows green ?)

**Sizing:**
- Wheel Radius - How big the wheel is (60-200px recommended)
- Text Scale - Size of labels (0.1-3.0x)
- Icon Scale - Size of item icons (0.1-3.0x)

**Position:**
- X Offset - Move left (-) or right (+) from center
- Y Offset - Move up (-) or down (+) from center

**Display:**
- Show Icons - Toggle item icon display
- Show Text - Toggle text label display

---

## ?? Example Configurations

### Configuration 1: AFK Hunting Controller
```
Top:        AutoRegear         (Ender Chest)
Top-Right:  TrailFollower      (Compass)
Right:      Command: "ar now"  (Firework)
Bottom-Right: AutoLogPlus      (Bed)
Bottom:     BetterStashFinder  (Chest)
Bottom-Left: HighlightOldLava  (Lava Bucket)
Left:       Freecam            (Ender Eye)
Top-Left:   AFKVanillaFly      (Elytra)
```

**Usage:** Hold V while AFK hunting, flick to toggle modules instantly!

### Configuration 2: PvP Quick Actions
```
Top:        AutoTotem          (Totem)
Top-Right:  Surround           (Obsidian)
Right:      Command: "ar now"  (Ender Chest)
Bottom-Right: AutoTrap         (String)
Bottom:     Offhand            (Shield)
Bottom-Left: Message: "gf"     (Book)
Left:       Freecam            (Ender Eye)
Top-Left:   KillAura           (Diamond Sword)
```

**Usage:** Quick combat toggles without opening GUI or memorizing keybinds!

### Configuration 3: Social/Communication
```
Top:        Message: "gg"      (Book)
Top-Right:  Message: "coords?" (Map)
Right:      Message: "ez []"   (Paper) - Random spam bypass
Bottom-Right: Message: "gf"    (Written Book)
Bottom:     Message: "[RANDOM]" (Feather) - Full random
Bottom-Left: Command: "home"   (Bed)
Left:       Command: "spawn"   (Compass)
Top-Left:   Message: "ty"      (Cookie)
```

**Usage:** Quick pre-set messages with built-in spam protection!

### Configuration 4: Utility Wheel
```
Top:        Command: "loadout save pvp"    (Chest)
Top-Right:  Command: "loadout load hunting" (Ender Chest)
Right:      Command: "ar save"              (Book)
Bottom-Right: Freecam                       (Ender Eye)
Bottom:     DiscordPresence                 (Music Disc)
Bottom-Left: Notebot                        (Note Block)
Left:       Command: "waypoint add"         (Banner)
Top-Left:   AutoReconnect                   (Totem)
```

---

## ??? Spam Protection Features

### Anti-Spam System:

**Message Delay:**
- Minimum time between messages (default 1000ms)
- Prevents accidental spam
- Configurable 100-5000ms

**Random Bracket Insertion:**
```
Input:  "selling [] cheap"
Output: "selling [k3Jx] cheap"
```
- Bypasses basic chat filters
- Different random text each time

**Random Placeholder:**
```
Input:  "coords [RANDOM]"
Output: "coords jK83mPq"
```
- 5-8 random characters
- Use multiple: "[RANDOM] hello [RANDOM]"

**Invisible Unicode:**
- Automatically adds invisible characters
- Makes each message unique to server
- Bypasses "duplicate message" filters

---

## ?? Advanced Usage

### Muscle Memory Technique:
1. Always configure same wheel layout
2. Practice a few times
3. Memorize directions
4. Eventually: instant activation without looking!

**Example:**
- "Top-Right is TrailFollower"
- Hold V, flick mouse ?, release
- TrailFollower toggles in <0.5 seconds!

### Combining with AutoRegear:
```
Right: Run Command "ar now"
? Hold V, flick right, release
? Forces immediate regear
? Faster than typing!
```

### Module Status Indicators:
- Active modules show **green text** with **? checkmark**
- Inactive modules show white text
- Visual feedback at a glance

---

## ?? Technical Details

### How It Works:

**Mouse Detection:**
1. Key held ? Unlocks cursor
2. Calculates mouse position
3. Determines angle from center (0-360?)
4. Divides into 8 segments (45? each)
5. Key released ? Executes action + re-locks cursor

**Rendering:**
- Custom circle rendering (filled)
- Triangle rendering for segment highlighting
- Line rendering for borders
- Item icon rendering with scaling
- Text rendering with scaling

**Dead Zone:**
- 20 pixel radius in center
- Prevents accidental triggers
- Must move mouse outside to select

---

## ?? Performance

**Lightweight:**
- Only renders when active
- No performance impact when not in use
- Efficient rendering algorithms

**Responsive:**
- Instant mouse tracking
- Smooth visual feedback
- No lag or stuttering

---

## ? Module Summary

**What You Got:**
- ? GTA-style radial menu
- ? 8 customizable action slots
- ? Toggle modules, send messages, run commands
- ? Full visual customization
- ? Anti-spam protection
- ? Active module indicators
- ? Professional polished UI

**Perfect For:**
- ?? AFK hunting quick controls
- ?? PvP fast module toggles
- ?? Social pre-set messages
- ?? General utility access

**Category:** Misc (6th Stardust module added)

---

## ?? Quick Reference

```bash
# Enable module
GUI ? Misc ? WheelPicker ? ON

# Use wheel
Hold V ? Move mouse ? Release V

# Example configs
Top = AutoRegear (for hunting)
Right = "ar now" command (quick regear)
Bottom = Message "coords?" (social)
```

**BUILD STATUS:** ? COMPLETE & WORKING

Enjoy your GTA-style quick access menu! ??
