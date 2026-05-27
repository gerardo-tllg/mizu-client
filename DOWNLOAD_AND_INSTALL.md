# ?? Download & Install MasterClient (Browser/Remote Users)

## For Cursor Web Browser Users

**You're using Cursor in a browser, so your workspace is REMOTE.**

Follow these steps to get MasterClient on your local machine:

---

## ?? Quick Steps

### 1?? **JAR is Already Built!** ?

Located at:
```
/workspace/build/libs/meteor-client-X.X.X.jar
```

---

### 2?? **Download the JAR to Your Computer**

**Option A: Download Single File (Recommended)**

In Cursor:
1. Open file explorer (left sidebar)
2. Navigate to: `build/libs/`
3. Find: `meteor-client-X.X.X.jar`
4. **Right-click ? Download**

**Option B: Download Entire Workspace as ZIP**

If Cursor doesn't have download option:
1. Click the menu (three dots or hamburger menu)
2. Look for "Download Workspace" or "Export"
3. Download as ZIP
4. Extract the ZIP on your local machine
5. JAR is at: `build/libs/meteor-client-X.X.X.jar`

**Option C: Use Command to Copy (if SSH available)**

```bash
# If you have SSH access to the remote:
scp user@remote:/workspace/build/libs/meteor-client-*.jar ~/Downloads/
```

---

### 3?? **Install on Your Local Minecraft**

**Once you have the JAR on your local machine:**

#### Windows:
```cmd
# Copy JAR to Minecraft mods folder
copy meteor-client-*.jar %APPDATA%\.minecraft\mods\

# Or drag and drop to:
# %APPDATA%\.minecraft\mods\
```

#### macOS/Linux:
```bash
# Copy JAR to Minecraft mods folder
cp meteor-client-*.jar ~/.minecraft/mods/

# Or manually:
# Copy to: ~/.minecraft/mods/
```

---

### 4?? **Install Fabric Loader (if not already installed)**

1. Go to: https://fabricmc.net/use/installer/
2. Download Fabric Installer
3. Run installer
4. Select Minecraft **1.21.5**
5. Click "Install"

---

### 5?? **Launch Minecraft**

1. Open Minecraft Launcher
2. Select profile: **Fabric 1.21.5**
3. Click **Play**
4. MasterClient loads automatically!

---

## ?? Detailed File Locations

### Remote Workspace (Cursor Browser):
```
/workspace/build/libs/meteor-client-X.X.X.jar  ? DOWNLOAD THIS
```

### Your Local Machine:

#### Windows:
```
Download to: C:\Users\YourName\Downloads\meteor-client-X.X.X.jar
Install to:  %APPDATA%\.minecraft\mods\
```

#### macOS:
```
Download to: ~/Downloads/meteor-client-X.X.X.jar
Install to:  ~/.minecraft/mods/
```

#### Linux:
```
Download to: ~/Downloads/meteor-client-X.X.X.jar
Install to:  ~/.minecraft/mods/
```

---

## ?? Alternative: Build Locally

**If you want to build on your local machine instead:**

### Step 1: Download Workspace
```
Cursor ? Download Workspace as ZIP
Extract to: C:\Users\YourName\MasterClient\
```

### Step 2: Install Java 21+
```
Download from: https://adoptium.net/
Install Java 21 or higher
```

### Step 3: Build Locally
```bash
# Windows:
cd C:\Users\YourName\MasterClient
gradlew.bat build

# macOS/Linux:
cd ~/MasterClient
./gradlew build
```

### Step 4: Install
```bash
# JAR will be at:
# build/libs/meteor-client-X.X.X.jar

# Copy to Minecraft mods folder
```

---

## ?? Important Notes

### ? DO:
- Download the JAR from `build/libs/`
- Install to your LOCAL Minecraft mods folder
- Use Fabric Loader 1.21.5
- Install optional mods (XaeroPlus, etc.) if you want full features

### ? DON'T:
- Try to run `./gradlew runClient` on the remote (won't work - no display)
- Expect Minecraft to run in the browser workspace
- Forget to install Fabric Loader

---

## ?? After Installation

### First Launch:
```
1. Open Minecraft Launcher
2. Select: Fabric 1.21.5 profile
3. Click Play
4. Wait for loading
5. Look for: [MasterClient] in chat
```

### Verify Installation:
```
In Minecraft:
- Press Right Shift ? GUI opens
- Press . (period) ? Commands open
- Check mod list ? "MasterClient" appears
```

### Test Features:
```
Right Shift ? Hunting ? AutoRegear ? Toggle
. (period) ? Type: .fly 1000 1000
Hold V ? WheelPicker menu (after config)
```

---

## ?? Finding Your Minecraft Folder

### Windows:
```
Press Win+R
Type: %APPDATA%\.minecraft
Press Enter
Mods folder is inside
```

### macOS:
```
Open Finder
Press: Cmd+Shift+G
Type: ~/Library/Application Support/minecraft
Mods folder is inside
```

### Linux:
```bash
cd ~/.minecraft/mods
# Or if using MultiMC/Prism:
cd ~/.local/share/multimc/instances/YourInstance/.minecraft/mods
```

---

## ?? Optional Dependencies

**For full hunting features, also download:**

1. **XaeroPlus** - https://modrinth.com/mod/xaeroplus
2. **Xaero's Minimap** - https://modrinth.com/mod/xaeros-minimap
3. **Xaero's World Map** - https://modrinth.com/mod/xaeros-world-map

**Install these the same way:**
```
Download ? Place in ~/.minecraft/mods/
```

**Without these:**
- BetterStashFinder: Visual only (no waypoints)
- TrailFollower: Works but no waypoints
- BaritoneElytraGoto: No map integration
- Everything else: Works perfectly

---

## ?? Troubleshooting

### "Can't find the JAR file"
```
Remote location: /workspace/build/libs/
Look for: meteor-client-*.jar
File size: ~5-10 MB
```

### "Download not working in Cursor"
```
Option 1: Download entire workspace as ZIP
Option 2: Use file sync if available
Option 3: Rebuild locally after downloading source
```

### "Minecraft crashes on launch"
```
Check:
? Fabric Loader installed?
? Correct Minecraft version (1.21.5)?
? Java 21+ installed?
? No conflicting mods?
```

### "Mod doesn't show up"
```
Check:
? JAR in correct mods folder?
? Using Fabric profile (not Vanilla)?
? Minecraft version 1.21.5?
? Check logs: logs/latest.log
```

---

## ?? Quick Reference

| Step | Action | Result |
|------|--------|--------|
| 1 | Find JAR in `/workspace/build/libs/` | Located ? |
| 2 | Download to local computer | File saved ? |
| 3 | Copy to `~/.minecraft/mods/` | Installed ? |
| 4 | Install Fabric 1.21.5 | Ready ? |
| 5 | Launch Minecraft | Running ? |

---

## ? Success Indicators

**After launching Minecraft, you should see:**

```
? Mod list shows: "MasterClient"
? Chat shows: [MasterClient] prefix
? Right Shift opens GUI
? Keybind category: "MasterClient"
? Hunting category has 14 modules
```

---

## ?? Final Checklist

Before asking "Why isn't it working?":

- [ ] Downloaded JAR from remote workspace
- [ ] JAR is in local ~/.minecraft/mods/ folder
- [ ] Fabric Loader 1.21.5 installed
- [ ] Using Fabric profile in launcher
- [ ] Java 21+ installed
- [ ] Minecraft version is 1.21.5
- [ ] No conflicting mods
- [ ] Checked logs/latest.log for errors

---

## ?? You're All Set!

**Once installed:**
- MasterClient loads automatically
- All 21 modules ready
- All 7 commands available
- Rebranded to [MasterClient]
- Ready to hunt! ??

---

*Last Updated: October 31, 2025*  
*Client: MasterClient 1.21.5*  
*Status: Ready for Download & Install ?*
