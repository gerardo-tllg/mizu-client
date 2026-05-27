# ?? How to Run MasterClient

## ? Build Complete - Ready to Run!

**Build Status:** SUCCESS  
**Client Name:** MasterClient  
**Version:** 1.21.5  
**Modules:** 21 modules + 7 commands

---

## ?? Prerequisites

Before running, make sure you have:
- ? Java 21 or higher installed
- ? Minecraft 1.21.5 installed
- ? Fabric Loader installed (for production use)

---

## ?? Method 1: Run in Development (Gradle)

**Easiest method for testing:**

```bash
cd /workspace
./gradlew runClient
```

This will:
1. Launch Minecraft with MasterClient loaded
2. Use development environment
3. Enable hot-reload (for development)
4. No need to install anything

**Pros:**
- Fast and easy
- Good for testing changes
- Built-in debugging

**Cons:**
- Slower startup
- Development environment only

---

## ?? Method 2: Build & Install JAR (Production)

**Best method for actual gameplay:**

### Step 1: Build the JAR
```bash
cd /workspace
./gradlew build
```

### Step 2: Find the JAR
The built JAR is located at:
```
/workspace/build/libs/meteor-client-X.X.X.jar
```

### Step 3: Install to Minecraft

**Option A: Manual Installation**
```bash
# Copy JAR to Minecraft mods folder
cp /workspace/build/libs/meteor-client-*.jar ~/.minecraft/mods/

# Or for custom Minecraft directory:
cp /workspace/build/libs/meteor-client-*.jar /path/to/minecraft/mods/
```

**Option B: Use a Launcher**
1. Copy the JAR file to your mods folder
2. Launch Minecraft with Fabric Loader
3. Client will load automatically

### Step 4: Launch Minecraft
1. Open Minecraft Launcher
2. Select Fabric 1.21.5 profile
3. Launch game
4. MasterClient loads automatically!

---

## ??? Method 3: Run in IDE (IntelliJ IDEA / Eclipse)

**For developers:**

### IntelliJ IDEA:

1. **Import Project:**
   ```
   File ? Open ? Select /workspace folder
   ```

2. **Wait for Gradle sync**
   - IntelliJ will auto-detect Gradle project
   - Wait for dependencies to download

3. **Run Configuration:**
   ```
   Run ? Edit Configurations ? + ? Gradle
   
   Name: Minecraft Client
   Gradle project: meteor-client
   Tasks: runClient
   ```

4. **Run:**
   ```
   Click Run button (green arrow)
   OR
   Shift + F10
   ```

### Eclipse:

1. **Import Project:**
   ```
   File ? Import ? Gradle ? Existing Gradle Project
   Select /workspace folder
   ```

2. **Run:**
   ```
   Right-click project ? Gradle ? Tasks ? fabric ? runClient
   ```

---

## ? Quick Start Commands

### Development Run:
```bash
cd /workspace
./gradlew runClient
```

### Build Production JAR:
```bash
cd /workspace
./gradlew build
```

### Clean Build:
```bash
cd /workspace
./gradlew clean build
```

### Run with Debugging:
```bash
cd /workspace
./gradlew runClient --debug-jvm
```

---

## ?? Important File Locations

### Built JAR:
```
/workspace/build/libs/meteor-client-X.X.X.jar
```

### Minecraft Mods Folder:
```
Linux/Mac:   ~/.minecraft/mods/
Windows:     %APPDATA%\.minecraft\mods\
```

### Client Configuration:
```
~/.minecraft/meteor-client/
```

### Logs:
```
~/.minecraft/logs/latest.log
```

---

## ?? Runtime Dependencies (Optional)

For full hunting features, install these mods alongside MasterClient:

### Required for Hunting:
1. **XaeroPlus** (Modrinth)
2. **Xaero's Minimap** (Modrinth)
3. **Xaero's World Map** (Modrinth)

### Included:
- **Baritone** (built-in to MasterClient)

### Installation:
```bash
# Download from Modrinth:
# https://modrinth.com/mod/xaeroplus
# https://modrinth.com/mod/xaeros-minimap
# https://modrinth.com/mod/xaeros-world-map

# Place in mods folder:
cp xaeroplus-*.jar ~/.minecraft/mods/
cp xaeros-minimap-*.jar ~/.minecraft/mods/
cp xaeros-world-map-*.jar ~/.minecraft/mods/
```

**Without these mods:**
- BetterStashFinder still works (visual only)
- TrailFollower still works (no waypoints)
- BaritoneElytraGoto map integration disabled
- All other modules work fine

---

## ?? First Launch Checklist

### 1. Launch Minecraft
```bash
./gradlew runClient
# OR use installed JAR with Fabric
```

### 2. Verify MasterClient Loaded
- Look for `[MasterClient]` in chat
- Press `Right Shift` to open GUI
- Check mod list (Mod Menu)

### 3. Configure Keybinds
```
Options ? Controls ? Key Binds ? MasterClient
```

### 4. Test Basic Module
```
Right Shift (open GUI)
? Hunting ? AutoRegear ? Toggle ON
```

### 5. Test Command
```
Press . (period) to open command bar
Type: .fly 1000 1000
Press Enter
```

### 6. Verify Branding
All messages should show:
```
[MasterClient] Module: ON
[MasterClient] Command executed
```

---

## ?? Troubleshooting

### Issue: "Java version too old"
**Solution:**
```bash
java -version
# Should show Java 21+
# If not, install Java 21
```

### Issue: "Fabric not installed"
**Solution:**
```bash
# Install Fabric Loader from:
# https://fabricmc.net/use/installer/
```

### Issue: "Module not found"
**Solution:**
```bash
# Rebuild client:
cd /workspace
./gradlew clean build
./gradlew runClient
```

### Issue: "XaeroPlus features not working"
**Solution:**
```
Install XaeroPlus + Xaero's mods
(See "Runtime Dependencies" section above)
```

### Issue: "Port already in use"
**Solution:**
```bash
# Kill existing Minecraft process:
pkill -9 java
# Or on Windows:
taskkill /F /IM java.exe
```

---

## ?? Performance Tips

### For Development:
```bash
# Allocate more RAM:
./gradlew runClient -Dorg.gradle.jvmargs="-Xmx4G"
```

### For Production:
```
Minecraft Launcher:
? Installations ? Edit ? More Options
? JVM Arguments: -Xmx4G -Xms2G
```

### Recommended Settings:
```
RAM: 4GB minimum (8GB recommended)
Render Distance: 8-12 chunks
VSync: OFF (for best FPS)
```

---

## ?? Useful Commands After Launch

### Test Modules:
```
Right Shift ? GUI
. (period) ? Commands
```

### Test AutoRegear:
```
.ar save
.ar status
```

### Test BaritoneElytraGoto:
```
.fly 1000 1000
.fly status
.fly cancel
```

### Test 2b2t Stats:
```
.stats2b2t Hausemaster
.lastseen2b2t Hausemaster
```

### Test WheelPicker:
```
Hold V key ? See radial menu
(Configure first in GUI)
```

---

## ?? Example Session

```bash
# 1. Build & Run
cd /workspace
./gradlew runClient

# 2. Wait for Minecraft to load
# (First launch may take a few minutes)

# 3. Join a server or world

# 4. Test client
Right Shift ? Opens MasterClient GUI
. (period) ? Opens command bar

# 5. Configure AutoRegear
GUI ? Hunting ? AutoRegear
[Fill inventory with items]
Command: .ar save

# 6. Test flight
Command: .fly 1000 1000
[Watch it fly automatically]

# 7. Configure WheelPicker
GUI ? Misc ? WheelPicker
Configure 8 slots
Hold V key to use

# Success! ??
```

---

## ?? Quick Reference

| Action | Command/Key |
|--------|-------------|
| **Run Client** | `./gradlew runClient` |
| **Build JAR** | `./gradlew build` |
| **Open GUI** | `Right Shift` |
| **Open Commands** | `. (period)` |
| **WheelPicker** | `Hold V` |
| **Toggle Module** | Click in GUI OR keybind |

---

## ? Verification

**After launching, you should see:**

1. **In Chat:**
   ```
   [MasterClient] modules loaded
   ```

2. **In Mod List:**
   ```
   MasterClient - 1.21.5
   ```

3. **In Keybinds:**
   ```
   Category: MasterClient
   ```

4. **In GUI:**
   ```
   Categories: Hunting (14), Misc (23), World (1), etc.
   ```

---

## ?? You're Ready!

**Your custom MasterClient is ready to use!**

```
? 21 modules implemented
? 7 commands added
? MasterClient branding
? All features working
? Production ready
```

**Happy hunting!** ??

---

## ?? Support

**If you encounter issues:**

1. Check logs: `~/.minecraft/logs/latest.log`
2. Rebuild: `./gradlew clean build`
3. Verify Java version: `java -version`
4. Check dependencies installed

---

*Last Updated: October 31, 2025*  
*Version: MasterClient 1.21.5*  
*Status: Production Ready ?*
