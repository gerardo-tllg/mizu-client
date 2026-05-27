# ?? New Modules & Commands - Complete Guide

**Last Updated:** October 31, 2025  
**Version:** 1.21.5  
**Total Additions:** 20 Modules + 6 Commands

This guide covers all newly implemented modules and commands added to Meteor Client.

---

## ?? Table of Contents

### Modules
- [Hunting Category (13 Modules)](#hunting-category)
  - [AutoRegear](#autoregear) ? NEW
  - [AFKVanillaFly](#afkvanillafly)
  - [AutoEXPPlus](#autoexpplus)
  - [AutoLogPlus](#autologplus)
  - [AutoPortal](#autoportal)
  - [BetterStashFinder](#betterstashfinder)
  - [DiscordNotifs](#discordnotifs)
  - [ElytraFlyPlusPlus](#elytraflyplusplus)
  - [GotoPosition](#gotoposition)
  - [HighlightOldLava](#highlightoldlava)
  - [Pitch40Util](#pitch40util)
  - [TrailFollower](#trailfollower)
  - [VanityESP](#vanityesp)

- [World Category (1 Module)](#world-category)
  - [ChestScanner](#chestscanner)

- [Misc Category (6 Modules)](#misc-category)
  - [WheelPicker](#wheelpicker) ? NEW
  - [AdBlocker](#adblocker)
  - [ChatSigns](#chatsigns)
  - [Loadouts](#loadouts)
  - [RoadTrip](#roadtrip)
  - [StashBrander](#stashbrander)

### Commands
- [Commands Overview](#commands)
  - [.autoregear](#autoregear-command) ? NEW
  - [.stats2b2t](#stats2b2t)
  - [.lastseen2b2t](#lastseen2b2t)
  - [.firstseen2b2t](#firstseen2b2t)
  - [.playtime2b2t](#playtime2b2t)
  - [.loadout](#loadout)

---

## ?? Hunting Category

The Hunting category contains modules specifically designed for AFK hunting, stash finding, and long-distance elytra travel on anarchy servers.

### AutoRegear

**Category:** Hunting  
**Type:** Automation  
**Dependencies:** Baritone (optional)

#### Description
Automatically regears your inventory from an ender chest when items are missing or low. Perfect for AFK hunting sessions and PvP. Builds obsidian safety box, pauses pathfinding, and restores your inventory to a saved snapshot.

#### Features
- **Snapshot System:** Saves your inventory state and restores it exactly
- **5 Regear Modes:** Emergency, Conservative, Balanced, Aggressive, Top-Off
- **Persistent:** Snapshot survives disconnects, crashes, and restarts
- **Safety:** Builds obsidian box, checks for nearby players
- **Smart Detection:** Auto-triggers when elytra durability or inventory differs
- **Integration:** Pauses Baritone and TrailFollower automatically
- **Manual Control:** Force regear with `.ar now` command

#### Settings

**Regear Control:**
- `regear-mode` - Choose mode (EMERGENCY/CONSERVATIVE/BALANCED/AGGRESSIVE/TOP_OFF/CUSTOM)
  - **Emergency:** Only when critical (20+ slots missing, 5% elytra)
  - **Conservative:** Low supplies (15+ slots, 10% elytra)
  - **Balanced:** Default (10+ slots, 20% elytra) ? Recommended
  - **Aggressive:** Early regear (5+ slots, 40% elytra)
  - **Top-Off:** PvP mode (1+ slot missing, 50% elytra)
  - **Custom:** Set your own thresholds
- `custom-elytra-durability` - Custom elytra durability % (if mode = CUSTOM)
- `custom-slot-difference` - Custom slot difference count (if mode = CUSTOM)
- `check-interval` - How often to check (ticks)

**Safety Settings:**
- `safe-radius` - Minimum distance from players (blocks)
- `safe-spot-distance` - How far to move to safe spot (blocks)
- `max-safe-spot-attempts` - Retries before giving up

**Shulker Settings:**
- `named-shulkers-only` - Only use named shulker boxes
- `elytra-shulker-name` - Name of elytra shulker (if named-only enabled)
- `food-shulker-name` - Name of food shulker
- `rockets-shulker-name` - Name of rockets shulker
- `general-shulker-name` - Name of general items shulker

**Actions:**
- `pause-baritone` - Pause Baritone during regear
- `pause-trail-follower` - Pause TrailFollower during regear

#### Usage

**Step 1: Save Snapshot**
```bash
# Fill your inventory with hunting supplies
# Rockets, elytras, food, totems, echest, obsidian, etc.
.ar save
```

**Step 2: Enable Module**
```bash
# In GUI: Hunting ? AutoRegear ? Toggle ON
# Choose mode: Balanced (recommended for AFK hunting)
```

**Step 3: AFK Hunt**
```bash
# Module auto-triggers when needed:
# - Elytra below 20% durability
# - 10+ inventory slots different from snapshot
# Process: Pause ? Move to safety ? Build box ? Regear ? Resume
```

**Manual Regear:**
```bash
.ar now          # Force immediate regear
```

**Other Commands:**
```bash
.ar save         # Update snapshot with current inventory
.ar clear        # Delete saved snapshot
.ar status       # Check snapshot size
```

#### Typical Workflow
1. **Setup:** Fill inventory ? `.ar save`
2. **Configure:** Set mode (Balanced for AFK, Top-Off for PvP)
3. **Hunt:** Start flying/pathfinding
4. **Auto:** Module triggers when low
5. **Manual:** Use `.ar now` if needed

#### Tips
- Use **Balanced** mode for AFK hunting (triggers at 20% elytra)
- Use **Top-Off** mode for PvP (regears even with 1 missing item)
- Use **named-shulkers-only** if your echest has multiple shulkers
- Snapshot persists through crashes/kicks - safe for long sessions
- Works with Baritone and TrailFollower automatically

---

### AFKVanillaFly

**Category:** Hunting  
**Type:** Movement

#### Description
Maintains level flight at a specific Y-coordinate using vanilla mechanics and firework rockets. Perfect for long-distance AFK travel in the Nether or Overworld.

#### Features
- Maintains constant Y-level
- Auto-fires rockets when falling
- Adjusts pitch to prevent altitude loss
- Works in vanilla Minecraft (no fly bypass)

#### Settings
- `target-y-level` - Y coordinate to maintain
- `pitch-adjustment` - How aggressively to adjust pitch
- `rocket-delay` - Minimum ticks between rockets
- `tolerance` - Y-level tolerance (blocks)

#### Usage
```bash
# Enable module
# Set target Y level (e.g., 120 for Nether roof)
# Start flying with elytra
# Module maintains altitude automatically
```

---

### AutoEXPPlus

**Category:** Hunting  
**Type:** Automation

#### Description
Automatically uses experience bottles to repair equipped armor and tools. Enhanced version with better detection and timing.

#### Features
- Auto-repairs when durability below threshold
- Prioritizes most damaged items
- Configurable durability thresholds
- Smart bottle usage (no waste)

#### Settings
- `durability-threshold` - Repair when below this %
- `delay` - Ticks between repair attempts
- `only-when-standing` - Only repair when not moving
- `helmet` - Enable helmet repair
- `chestplate` - Enable chestplate repair
- `leggings` - Enable leggings repair
- `boots` - Enable boots repair
- `held-item` - Enable held item repair

#### Usage
```bash
# Enable module
# Set durability threshold (e.g., 30%)
# Module auto-repairs when items drop below threshold
# Ensure exp bottles in hotbar
```

---

### AutoLogPlus

**Category:** Hunting  
**Type:** Safety

#### Description
Advanced automatic logout system with multiple triggers. Protects your account during AFK sessions.

#### Features
- **Multiple Triggers:**
  - Y-Level (logout at specific height)
  - Durability (logout when item breaks)
  - Lag/TPS detection
  - Position-based (logout at coordinates)
  - Entity proximity
- Configurable delays
- Action on trigger (disconnect or save & quit)

#### Settings
- `y-level-enable` - Enable Y-level trigger
- `y-level-min` - Minimum Y level
- `y-level-max` - Maximum Y level
- `durability-enable` - Enable durability trigger
- `durability-threshold` - Durability % to trigger
- `lag-enable` - Enable lag/TPS trigger
- `lag-threshold` - TPS threshold
- `position-enable` - Enable position trigger
- `position-x`, `position-z` - Target coordinates
- `position-radius` - Trigger radius

#### Usage
```bash
# Example: Logout if fall below Y=100
Enable AutoLogPlus
Set y-level-enable: ON
Set y-level-min: 100

# Example: Logout if elytra breaks
Set durability-enable: ON
Set durability-threshold: 5
```

---

### AutoPortal

**Category:** Hunting  
**Type:** Automation

#### Description
Automatically builds Nether portals when enabled. Useful for emergency portal creation during hunting.

#### Features
- Auto-places obsidian portal frame
- Lights portal with flint & steel
- Configurable portal orientation
- Safety checks

#### Settings
- `orientation` - North/South or East/West
- `delay` - Ticks between placements
- `swing-hand` - Show hand swing animation

#### Usage
```bash
# Enable when you need portal
# Module builds 4x5 portal frame
# Auto-lights with flint & steel
# Disable after portal created
```

---

### BetterStashFinder

**Category:** Hunting  
**Type:** ESP/Detection  
**Dependencies:** XaeroPlus (runtime)

#### Description
Enhanced stash finder that integrates with XaeroPlus for waypoint creation. Detects container clusters and highlights potential stashes.

#### Features
- Container detection (chests, shulkers, barrels, etc.)
- Auto-creates XaeroPlus waypoints
- Customizable detection radius
- Minimum container threshold
- Tracer rendering
- Name tags with distance
- Configurable colors

#### Settings
- `detection-radius` - How far to scan (chunks)
- `minimum-containers` - Minimum containers to mark as stash
- `create-waypoints` - Auto-create XaeroPlus waypoints
- `render-tracers` - Draw lines to stashes
- `render-nametags` - Show distance labels
- `color` - Highlight color

#### Usage
```bash
# Enable module
# Fly around searching
# Module auto-detects container clusters
# Creates XaeroPlus waypoints automatically
# Waypoints persist in XaeroPlus
```

**Note:** Requires XaeroPlus installed for waypoint features. Works without it for visual detection only.

---

### DiscordNotifs

**Category:** Hunting  
**Type:** Notifications

#### Description
Sends game events to Discord webhooks. Get notifications for deaths, logouts, stash finds, and more.

#### Features
- Discord webhook integration
- Customizable events
- Player mentions
- Coordinate logging
- Screenshot attachments (optional)

#### Settings
- `webhook-url` - Your Discord webhook URL
- `notify-death` - Notify on death
- `notify-logout` - Notify on disconnect
- `notify-stash-find` - Notify when stash found
- `notify-player-nearby` - Notify when player nearby
- `player-radius` - Radius for player detection
- `include-screenshots` - Attach screenshots

#### Usage
```bash
# 1. Create Discord webhook:
#    Server Settings ? Integrations ? Webhooks ? New Webhook
#    Copy webhook URL

# 2. Enable DiscordNotifs
#    Paste webhook URL in settings

# 3. Configure events:
#    Enable: notify-death, notify-stash-find, etc.

# 4. Get notifications in Discord!
```

**Example Notification:**
```
?? Player Detected!
Player: Hausemaster
Distance: 47 blocks
Location: [12534, 64, -8432]
Time: 14:32:18
```

---

### ElytraFlyPlusPlus

**Category:** Hunting  
**Type:** Movement  
**Dependencies:** Baritone (optional)

#### Description
Advanced elytra flight module with multiple flight modes, auto-pitch, and Baritone integration.

#### Features
- Multiple flight modes (pitch, control, auto)
- Auto-fires rockets
- Maintains pitch angle
- Speed control
- Baritone pathfinding integration
- Auto-takeoff
- Obstacle avoidance

#### Settings
- `flight-mode` - Choose mode (PITCH/CONTROL/AUTO)
- `pitch-angle` - Target pitch (for PITCH mode)
- `speed` - Flight speed multiplier
- `auto-rocket` - Auto-fire rockets
- `rocket-delay` - Ticks between rockets
- `maintain-height` - Keep constant Y-level
- `target-height` - Y-level to maintain
- `avoid-obstacles` - Auto-avoid blocks

#### Usage
```bash
# Basic flight:
Enable ElytraFlyPlusPlus
Set flight-mode: AUTO
Start gliding
Module handles everything

# Pitch 40 flight:
Set flight-mode: PITCH
Set pitch-angle: 40
Great for long-distance efficient travel

# With Baritone:
Enable ElytraFlyPlusPlus
Use Baritone commands (#goto X Y Z)
Module handles flight, Baritone handles navigation
```

---

### GotoPosition

**Category:** Hunting  
**Type:** Navigation

#### Description
Travels straight to specified coordinates with ETA display. Simple point-to-point travel.

#### Features
- Straight-line pathfinding
- ETA calculation
- Speed display
- Distance remaining
- Auto-rocket usage
- Obstacle detection

#### Settings
- `target-x` - Target X coordinate
- `target-y` - Target Y coordinate
- `target-z` - Target Z coordinate
- `use-rockets` - Auto-use rockets
- `speed` - Travel speed
- `stop-distance` - Stop when this close (blocks)

#### Usage
```bash
# Set target coordinates in settings
Enable GotoPosition
Start gliding with elytra
Module flies you to coordinates

# HUD shows:
# Distance: 12,534 blocks
# ETA: 8 min 23 sec
# Speed: 24.7 m/s
```

---

### HighlightOldLava

**Category:** Hunting  
**Type:** ESP

#### Description
Highlights old lava flows that indicate old chunks and potential stash locations. Also sends Discord webhooks.

#### Features
- Detects old lava (Y < 10 in Overworld)
- ESP highlighting
- Tracer rendering
- Discord webhook notifications
- Chunk age indication

#### Settings
- `y-threshold` - Y-level to consider "old" (default: 10)
- `render-tracers` - Draw lines to lava
- `render-boxes` - Highlight lava blocks
- `color` - Highlight color
- `webhook-url` - Discord webhook (optional)
- `notify-only-first` - Only notify first find

#### Usage
```bash
# Enable module
# Set webhook URL (optional)
# Fly around in Overworld
# Old lava flows highlight automatically
# Indicates chunks loaded before Y-level changes
```

---

### Pitch40Util

**Category:** Hunting  
**Type:** Movement

#### Description
Manages pitch 40 elytra flying for optimal speed and rocket efficiency.

#### Features
- Maintains 40-degree pitch
- Auto-rocket timing
- Speed optimization
- Smooth pitch adjustments

#### Settings
- `auto-rocket` - Auto-fire rockets
- `rocket-timing` - When to fire (based on speed)
- `pitch-tolerance` - Acceptable pitch variance
- `adjust-speed` - How fast to adjust pitch

#### Usage
```bash
# Enable module
# Start gliding with elytra
# Module maintains pitch 40 automatically
# Fires rockets at optimal times
# Efficient long-distance travel
```

---

### TrailFollower

**Category:** Hunting  
**Type:** Navigation  
**Dependencies:** XaeroPlus, Baritone (runtime)

#### Description
Automatically follows player trails detected in old chunks. Uses XaeroPlus old chunk detection and Baritone for pathfinding.

#### Features
- Detects old chunk trails
- Auto-follows trail direction
- Baritone pathfinding integration
- Trail age estimation
- Waypoint creation
- Configurable follow distance

#### Settings
- `follow-distance` - How close to follow trail (blocks)
- `trail-color` - Trail highlight color
- `create-waypoints` - Create XaeroPlus waypoints
- `waypoint-interval` - Distance between waypoints
- `auto-pause` - Pause when player nearby
- `pause-radius` - Player detection radius

#### Usage
```bash
# Enable module
# Fly around until trail detected
# Module automatically follows trail
# Creates waypoints as you go
# Pauses if players detected (safety)

# Manual control:
# Pause: Toggle module OFF
# Resume: Toggle module ON
```

**Note:** Requires XaeroPlus and Baritone installed.

---

### VanityESP

**Category:** Hunting  
**Type:** ESP

#### Description
Highlights map art and banners to help find decorated bases and item frames.

#### Features
- Map art detection
- Banner detection
- Item frame highlighting
- Configurable colors
- Distance labels
- Tracer rendering

#### Settings
- `detect-maps` - Highlight map art
- `detect-banners` - Highlight banners
- `map-color` - Map art highlight color
- `banner-color` - Banner highlight color
- `render-tracers` - Draw lines to items
- `render-nametags` - Show distance labels
- `max-distance` - Max render distance

#### Usage
```bash
# Enable module
# Fly around looking for bases
# Map art and banners highlight automatically
# Helps find decorated bases
```

---

## ?? World Category

### ChestScanner

**Category:** World  
**Type:** Utility

#### Description
Scans chests and uploads contents to a database API. Useful for cataloging loot, tracking items, or building databases.

#### Features
- Scans chest contents
- Uploads to API
- Item categorization
- Auto-scan mode
- API key authentication
- Batch scanning

#### Settings
- `api-url` - API endpoint URL
- `api-key` - Your API key (optional)
- `auto-scan` - Auto-scan opened chests
- `scan-delay` - Delay between scans (ticks)
- `include-empty` - Upload empty chests
- `categorize` - Auto-categorize items

#### Usage
```bash
# Configure API settings
Set api-url: "https://your-api.com/chests"
Set api-key: "your-key-here"

# Enable module
Enable auto-scan

# Open chests
# Module auto-scans and uploads contents
# Check your API/database for results
```

---

## ?? Misc Category

### WheelPicker

**Category:** Misc  
**Type:** Utility  
**? FEATURED MODULE**

#### Description
GTA-style radial menu for quick actions. Hold a key to open wheel, move mouse in direction, release to execute. Perfect for fast module toggles, messages, and commands without memorizing keybinds.

#### Features
- **8 Customizable Slots:** Each direction can have different action
- **3 Action Types:**
  - Toggle Module (turn any module on/off)
  - Send Message (with spam protection)
  - Run Command (any Meteor command)
- **Visual Indicators:** Active modules show green ?
- **Anti-Spam:** Random text insertion, delays, invisible unicode
- **Full Customization:** Colors, sizes, icons, text
- **Muscle Memory:** Same layout = instant actions

#### Settings

**General:**
- `activation-key` - Key to hold (default: V)
- `wheel-radius` - Size of wheel (60-200)
- `wheel-x-offset` - Move left/right from center
- `wheel-y-offset` - Move up/down from center

**Slot Actions:** (8 slots: Top, Top-Right, Right, etc.)
For each slot:
- `[slot]-action` - NONE/TOGGLE_MODULE/SEND_MESSAGE/RUN_COMMAND
- `[slot]-icon` - Item to display
- `[slot]-custom-text` - Label (empty = auto)
- `[slot]-module` - Module name (if action = TOGGLE_MODULE)
- `[slot]-message` - Message text (if action = SEND_MESSAGE)
- `[slot]-command` - Command text (if action = RUN_COMMAND)

**Spam Protection:**
- `spam-protection` - Enable anti-spam features
- `message-delay` - Min ms between messages (100-5000)
- `insert-random-brackets` - Add random text in []

**Render:**
- `background-color` - Wheel background
- `selected-color` - Highlighted section
- `border-color` - Lines between sections
- `text-color` - Normal text
- `module-active-color` - Active module text
- `text-scale` - Label size (0.1-3.0)
- `icon-scale` - Icon size (0.1-3.0)
- `show-icons` - Display item icons
- `show-text` - Display text labels

#### Configuration Examples

**Example 1: AFK Hunting Wheel**
```
Top:          AutoRegear (Ender Chest)
Top-Right:    TrailFollower (Compass)
Right:        Command: "ar now" (Firework)
Bottom-Right: AutoLogPlus (Bed)
Bottom:       BetterStashFinder (Chest)
Bottom-Left:  HighlightOldLava (Lava Bucket)
Left:         Freecam (Ender Eye)
Top-Left:     AFKVanillaFly (Elytra)
```

**Example 2: PvP Quick Access**
```
Top:          AutoTotem (Totem)
Top-Right:    Surround (Obsidian)
Right:        Command: "ar now" (Ender Chest)
Bottom-Right: AutoTrap (String)
Bottom:       Offhand (Shield)
Bottom-Left:  Message: "gf" (Book)
Left:         Freecam (Ender Eye)
Top-Left:     KillAura (Diamond Sword)
```

**Example 3: Social/Chat Wheel**
```
Top:          Message: "gg" (Book)
Top-Right:    Message: "coords?" (Map)
Right:        Message: "ez []" (Paper) [spam bypass]
Bottom-Right: Message: "gf" (Written Book)
Bottom:       Message: "[RANDOM]" (Feather) [full random]
Bottom-Left:  Command: "home" (Bed)
Left:         Command: "spawn" (Compass)
Top-Left:     Message: "ty" (Cookie)
```

#### Usage

**Basic Usage:**
```bash
# 1. Enable WheelPicker module

# 2. Configure slots (in GUI settings)

# 3. In-game: Hold V key

# 4. Move mouse in desired direction

# 5. Release V to execute action
```

**Quick Setup:**
```bash
# For hunting:
Top-Right: TrailFollower
Right: Command "ar now"
Bottom-Right: AutoLogPlus

# Now while hunting:
Hold V ? Flick mouse right ? Release
= Instant regear!
```

**Spam Protection Usage:**
```bash
# Send message with random text:
Message: "hello there []"
Output: "hello there [kJ8x]"

# Use RANDOM placeholder:
Message: "coords [RANDOM]"
Output: "coords mK83pLq"

# Multiple randoms:
Message: "[RANDOM] selling stuff [RANDOM]"
Output: "[bKx93] selling stuff [m2Po8]"
```

#### Tips
- Use same layout always for muscle memory
- Icons help visual recognition
- Green ? shows active modules
- 20px dead zone in center (prevents accidents)
- Works while cursor locked (unlocks temporarily)
- Perfect for fast hunting controls
- Faster than opening GUI or memorizing keybinds

---

### AdBlocker

**Category:** Misc  
**Type:** Chat

#### Description
Blocks advertisers in chat including death message advertisers. Keeps chat clean on busy servers.

#### Features
- Chat message filtering
- Death message ad blocking
- Configurable filter patterns
- Whitelist system
- Hover text detection

#### Settings
- `enabled` - Enable ad blocking
- `block-death-ads` - Block death message ads
- `custom-patterns` - Add custom filter regex
- `whitelist` - Players to never block

#### Usage
```bash
# Enable module
# Chat ads automatically blocked
# Configure custom patterns for specific ads
# Add trusted players to whitelist
```

---

### ChatSigns

**Category:** Misc  
**Type:** Utility

#### Description
Reads nearby signs in chat and highlights potentially old signs (placed before 1.8). Useful for finding old bases.

#### Features
- Auto-reads signs in radius
- Highlights old signs (different color)
- ESP rendering
- Click to teleport (if allowed)
- Distance sorting

#### Settings
- `scan-radius` - How far to scan (blocks)
- `show-in-chat` - Print sign text in chat
- `highlight-old` - Highlight pre-1.8 signs
- `old-sign-color` - Color for old signs
- `new-sign-color` - Color for new signs
- `render-esp` - Draw ESP boxes

#### Usage
```bash
# Enable module
# Walk/fly near signs
# Sign contents print in chat
# Old signs highlight differently
# Use for base dating/finding
```

---

### Loadouts

**Category:** Misc  
**Type:** Inventory

#### Description
Save and load complete inventory configurations. Perfect for switching between PvP, mining, hunting, and building kits.

#### Features
- Save inventory snapshots
- Load saved loadouts
- Multiple named loadouts
- Includes hotbar positions
- Preserves armor slots
- Command-based control

#### Settings
- `auto-equip-armor` - Auto-equip armor when loading
- `auto-sort-hotbar` - Match hotbar positions exactly
- `delay-between-moves` - Ticks between item moves

#### Usage
```bash
# Save current inventory:
.loadout save pvp

# Load saved loadout:
.loadout load pvp

# List all loadouts:
.loadout list

# Remove loadout:
.loadout remove pvp
```

**Example Workflow:**
```bash
# Setup different kits:
[Equip PvP gear] ? .loadout save pvp
[Equip mining gear] ? .loadout save mining
[Equip hunting gear] ? .loadout save hunting

# Switch instantly:
.loadout load pvp      # Full PvP kit
.loadout load mining   # Full mining kit
.loadout load hunting  # Full hunting kit
```

---

### RoadTrip

**Category:** Misc  
**Type:** Travel

#### Description
Tools for AFK long-distance travel. Displays ETA, notifications, auto-disconnect when destination reached.

#### Features
- ETA calculation
- Distance tracking
- Speed monitoring
- Auto-disconnect at destination
- Durability warnings
- Food alerts
- HUD overlay

#### Settings
- `destination-x` - Target X coordinate
- `destination-z` - Target Z coordinate
- `auto-disconnect` - Disconnect when arrived
- `arrival-radius` - How close = "arrived" (blocks)
- `notify-durability` - Warn when elytra low
- `durability-threshold` - Durability % for warning
- `notify-food` - Warn when hunger low
- `food-threshold` - Hunger level for warning

#### Usage
```bash
# Setup trip:
Set destination-x: 100000
Set destination-z: -50000
Enable auto-disconnect

# Enable RoadTrip
# Start traveling (elytra/horse/ice boat)

# HUD shows:
# Distance: 85,432 blocks
# ETA: 2h 14m
# Speed: 18.3 m/s
# Elytra: 67%

# Auto-disconnects on arrival
```

---

### StashBrander

**Category:** Misc  
**Type:** Automation

#### Description
Automatically renames desired items in bulk when using anvils. Perfect for branding stash items with your name or clan tag.

#### Features
- Auto-renames items in anvil
- Configurable name templates
- Item type filtering
- Bulk renaming
- Counter support
- Color code support

#### Settings
- `name-template` - Template for names (e.g., "[CLAN] {item} #{count}")
- `use-counter` - Add incrementing counter
- `counter-start` - Starting number
- `item-whitelist` - Only rename specific items
- `use-whitelist` - Enable item filtering
- `color-codes` - Enable & color codes

#### Usage
```bash
# Configure template:
Set name-template: "[MyStash] {item} #{count}"

# Enable StashBrander
# Open anvil
# Place items to rename
# Module auto-renames them

# Example results:
# Diamond Sword ? [MyStash] Diamond Sword #1
# Diamond Sword ? [MyStash] Diamond Sword #2
# Ender Chest ? [MyStash] Ender Chest #3
```

**Template Variables:**
- `{item}` - Original item name
- `{count}` - Auto-incrementing counter
- `&` - Color code prefix (e.g., &c = red)

---

## ?? Commands

### .autoregear Command

**Aliases:** `.ar`

Manage AutoRegear module settings and actions.

#### Subcommands

**`.ar save`**
- Saves current inventory as regear snapshot
- Overwrites previous snapshot
- Snapshot persists through restarts

```bash
.ar save
# Output: Inventory snapshot saved (36 items)
```

**`.ar now`**
- Forces immediate regear
- Ignores thresholds
- Auto-enables module if disabled

```bash
.ar now
# Output: Manual regear started!
```

**`.ar clear`**
- Deletes saved snapshot
- Disables auto-regear triggers

```bash
.ar clear
# Output: Inventory snapshot cleared
```

**`.ar status`**
- Shows snapshot information
- Displays item count

```bash
.ar status
# Output: Snapshot loaded: 36 items
# OR: No snapshot saved
```

#### Usage Examples

```bash
# Setup for AFK hunting:
[Fill inventory with rockets, elytras, food, etc.]
.ar save
[Enable AutoRegear module in GUI]
[Set mode to "Balanced"]

# During hunting:
.ar now          # Force regear if needed

# Update snapshot:
[Adjust inventory]
.ar save         # Overwrite previous snapshot

# Clear and start over:
.ar clear
```

---

### .stats2b2t

Fetch comprehensive 2b2t player statistics from api.2b2t.vc.

#### Usage
```bash
.stats2b2t <player>
```

#### Output
```
===== Stats for Hausemaster =====
First Seen: Jan 15, 2015 (3892 days ago)
Last Seen: Oct 30, 2025 (1 day ago)
Playtime: 2847 hours (118 days)
Joins: 4,234
Leaves: 4,189
Deaths: 892
Kills: 1,543
UUID: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
```

---

### .lastseen2b2t

Show when player was last seen on 2b2t.

#### Usage
```bash
.lastseen2b2t <player>
```

#### Output
```
Hausemaster was last seen:
Oct 30, 2025 14:32:18 (1 day ago)
```

---

### .firstseen2b2t

Show when player first joined 2b2t.

#### Usage
```bash
.firstseen2b2t <player>
```

#### Output
```
Hausemaster first joined:
Jan 15, 2015 (3892 days ago)
```

---

### .playtime2b2t

Show total playtime on 2b2t.

#### Usage
```bash
.playtime2b2t <player>
```

#### Output
```
Hausemaster playtime:
2847 hours (118 days)
```

---

### .loadout

Manage inventory loadouts.

#### Subcommands

**`.loadout save <name>`**
- Saves current inventory as named loadout

```bash
.loadout save pvp
# Output: Loadout 'pvp' saved
```

**`.loadout load <name>`**
- Loads named loadout into inventory

```bash
.loadout load pvp
# Output: Loading loadout 'pvp'...
# Output: Loadout loaded successfully
```

**`.loadout list`**
- Lists all saved loadouts

```bash
.loadout list
# Output: Saved loadouts:
# - pvp
# - mining
# - hunting
```

**`.loadout remove <name>`**
- Deletes named loadout

```bash
.loadout remove pvp
# Output: Loadout 'pvp' removed
```

---

## ?? Dependencies

Some modules require external mods to be installed **at runtime** (when you play). These are **optional** but unlock full functionality.

### Required for Full Hunting Features:

**XaeroPlus** + **Xaero's Minimap** + **Xaero's World Map**
- Used by: BetterStashFinder, TrailFollower
- Feature: Waypoint creation, old chunk detection
- Download: Modrinth
- Without: Modules work for detection but no waypoints

**Baritone**
- Used by: ElytraFlyPlusPlus, TrailFollower, AutoRegear (pause/resume)
- Feature: Pathfinding
- Download: Included in Meteor Client
- Without: Manual navigation only

### Optional:

**Discord Webhook**
- Used by: DiscordNotifs, HighlightOldLava
- Feature: Send notifications to Discord
- Setup: Create webhook in Discord server settings

---

## ?? Setup Guides

### AFK Hunting Complete Setup

**Step 1: Install Dependencies**
```
Install XaeroPlus + Xaero's mods (optional but recommended)
Meteor already includes Baritone
```

**Step 2: Configure AutoRegear**
```bash
# Fill inventory with hunting supplies:
- Rockets (10+ stacks)
- Elytras (2-3)
- Food (5+ stacks)
- Ender chest
- Obsidian (8+ blocks)
- Optionally: Shulkers with backup supplies

# Save snapshot:
.ar save

# Configure module:
GUI ? Hunting ? AutoRegear
Set mode: Balanced
Enable pause-baritone: ON
Enable pause-trail-follower: ON
```

**Step 3: Configure WheelPicker**
```
GUI ? Misc ? WheelPicker
Set activation-key: V

Configure slots:
Top: AutoRegear (Ender Chest icon)
Top-Right: TrailFollower (Compass icon)
Right: RUN_COMMAND: "ar now" (Firework icon)
Bottom-Right: AutoLogPlus (Bed icon)
Bottom: BetterStashFinder (Chest icon)
Left: Freecam (Ender Eye icon)
```

**Step 4: Enable Modules**
```
Enable: AutoRegear
Enable: TrailFollower (or use Baritone #goto)
Enable: BetterStashFinder
Enable: WheelPicker
Enable: DiscordNotifs (optional)
Enable: AutoLogPlus (optional, for safety)
```

**Step 5: Start Hunting**
```bash
# If using TrailFollower:
Fly around until trail detected
Module follows automatically

# If using Baritone:
#goto X Y Z
Module flies you there

# AutoRegear will trigger automatically when:
- Elytra below 20% durability
- 10+ slots different from snapshot
```

**Step 6: Manual Control**
```
Hold V (WheelPicker)
Flick right: Force regear now (.ar now)
Flick top-right: Toggle TrailFollower
Flick bottom-right: Emergency logout
```

---

### PvP Quick Regear Setup

**Step 1: Prepare PvP Kit**
```
Full PvP loadout:
- Armor (enchanted)
- Totems (hotbar + inventory)
- Crystals (multiple stacks)
- Gaps (2-3 stacks)
- EXP bottles (2-3 stacks)
- Obsidian
- Ender chest
```

**Step 2: Configure AutoRegear**
```bash
.ar save

GUI ? Hunting ? AutoRegear
Set mode: Top-Off (regears when missing 1+ item)
Set custom-elytra-durability: 50
```

**Step 3: Configure WheelPicker**
```
Quick combat actions:
Top: AutoTotem
Top-Right: Surround
Right: RUN_COMMAND: "ar now"
Bottom: Offhand
Left: Freecam
```

**Step 4: Use in Combat**
```
Fight normally
Missing items? Module auto-regears
OR Hold V ? Flick right for manual regear
Back to full kit in seconds
```

---

## ?? Troubleshooting

### AutoRegear not triggering

**Check:**
- ? Module enabled?
- ? Snapshot saved? (`.ar status`)
- ? Mode set correctly?
- ? Ender chest in inventory?
- ? Items actually missing/different?

**Solution:**
```bash
.ar clear
.ar save
# Ensure inventory matches snapshot
# Wait for actual changes
```

---

### TrailFollower not working

**Check:**
- ? XaeroPlus installed?
- ? Old chunks visible in XaeroPlus?
- ? Baritone installed?
- ? Elytra equipped?

**Solution:**
Install XaeroPlus + Xaero's mods
Enable old chunk detection in XaeroPlus settings

---

### BetterStashFinder no waypoints

**Check:**
- ? XaeroPlus installed?
- ? create-waypoints enabled in settings?
- ? Containers nearby?

**Solution:**
Module works for detection without XaeroPlus
Install XaeroPlus for waypoint features

---

### WheelPicker not appearing

**Check:**
- ? Module enabled?
- ? Holding activation key?
- ? No screen open (GUI/inventory)?

**Solution:**
Wheel only shows when key held
Close all screens before activating

---

### 2b2t commands not working

**Check:**
- ? Internet connection?
- ? api.2b2t.vc accessible?
- ? Player name correct?
- ? Player exists on 2b2t?

**Solution:**
API may be temporarily down
Wait and try again
Check player name spelling

---

## ?? Performance Tips

### For AFK Hunting:

**Optimize render distance:**
```
Lower render distance = better FPS
6-12 chunks recommended for hunting
```

**Disable unnecessary modules:**
```
Only enable what you need:
? AutoRegear
? TrailFollower/BetterStashFinder
? WheelPicker
? Visual ESP (if not needed)
```

**Configure AutoRegear efficiently:**
```
Use Balanced mode
Don't set too aggressive (wastes time)
Don't set too conservative (risk running out)
```

---

## ?? Advanced Tips

### AutoRegear Optimization:

**Named Shulkers:**
```
Enable named-shulkers-only
Name shulkers exactly:
- "Elytras" (elytra-shulker-name)
- "Rockets" (rockets-shulker-name)
- "Food" (food-shulker-name)

Module only opens matching shulkers
Faster regear, no clutter
```

**Custom Modes:**
```
Set mode: CUSTOM
Tune exact thresholds:
- custom-elytra-durability: 25
- custom-slot-difference: 8

Perfect for specific needs
```

**Safety:**
```
Increase safe-radius if paranoid
Module won't regear if players within radius
Protects against combat logging
```

---

### WheelPicker Pro Usage:

**Muscle Memory Training:**
```
Use same layout always
Practice 5-10 times
Eventually: instant action without looking
Hold V, flick direction, release
< 0.5 second activation
```

**Context Wheels:**
```
Different profiles for different activities:
- Hunting wheel (AutoRegear, TrailFollower, etc.)
- PvP wheel (AutoTotem, Surround, etc.)
- Social wheel (messages, commands)

Switch layouts based on what you're doing
```

---

### Spam Protection Bypass:

**Message Templates:**
```
Use [RANDOM] for full random:
"hello [RANDOM]" ? "hello kJ8x3Pm"

Use [] with random insertion:
"hello []" ? "hello [kJ8x]"

Combine both:
"[RANDOM] hello [] there [RANDOM]"
```

---

## ?? Complete Workflow Examples

### Example 1: Highway Stash Hunt

```bash
# 1. Prepare
Fill inventory: rockets, elytras, food, echest, obsidian
.ar save

# 2. Configure
Enable: AutoRegear (Balanced)
Enable: BetterStashFinder
Enable: HighlightOldLava
Enable: WheelPicker
Enable: DiscordNotifs (optional)

# 3. Travel
#goto X Y Z (on highway)
OR Enable TrailFollower

# 4. Hunt
Fly along highway
BetterStashFinder detects stashes
HighlightOldLava shows old chunks
AutoRegear triggers when low

# 5. Quick Control
Hold V:
  Right: Force regear
  Top-Right: Pause/resume trail
  Bottom-Right: Emergency logout
```

---

### Example 2: PvP Combat with Regear

```bash
# 1. Prepare PvP Kit
Full combat loadout
.loadout save pvp
.ar save

# 2. Configure
AutoRegear: Top-Off mode
WheelPicker: Combat wheel

# 3. Fight
Engage combat
Use items normally

# 4. Regear Mid-Fight
Missing totems/exp?
Hold V ? Flick right
Instant regear
Back in fight

# 5. After Combat
.loadout load pvp
.ar save
Ready for next fight
```

---

### Example 3: Base Building with Loadouts

```bash
# 1. Setup Loadouts
[Mining gear] ? .loadout save mining
[Building gear] ? .loadout save building
[PvP gear] ? .loadout save pvp

# 2. Work Efficiently
.loadout load mining     # Go mine materials
.loadout load building   # Build structures
.loadout load pvp        # Defend if needed

# 3. Quick Switch
Configure WheelPicker:
  Top: RUN_COMMAND "loadout load mining"
  Right: RUN_COMMAND "loadout load building"
  Bottom: RUN_COMMAND "loadout load pvp"

Hold V ? Flick direction ? Instant kit change!
```

---

## ?? Summary

**You now have access to:**

? **13 Hunting Modules** - Complete AFK hunting automation
? **AutoRegear** - Revolutionary auto-regearing system
? **WheelPicker** - GTA-style quick access menu
? **6 Misc Modules** - Quality of life improvements
? **1 World Module** - Chest scanning
? **6 Commands** - 2b2t stats + loadouts + autoregear

**Perfect for:**
- ?? AFK hunting & stash finding
- ?? PvP with quick regearing
- ?? Long-distance travel
- ?? Social interactions
- ?? General utility

**All modules are:**
- ? 1.21.5 compatible
- ? Security verified
- ? Fully functional
- ? Well integrated

---

## ?? Credits

**Hunting Modules:** miles352/meteor-stashhunting-addon  
**Stardust Modules:** 0xTas/stardust  
**WheelPicker:** dekrom/BepHaxAddon  
**ChestScanner:** swavezdev/FroglightCityDB  
**AutoRegear:** Custom implementation  

**2b2t Stats API:** rfresh (api.2b2t.vc)

---

**Last Updated:** October 31, 2025  
**Build Status:** ? SUCCESS  
**Ready to use!**
