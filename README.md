# PixelForge Client

A Lunar/Axolotl-style Fabric utility client for **Minecraft 1.21.11** focused on polished HUD, visual customization, performance controls, account management and PvP training.

**No cheats.** Features are client-side QoL/visual/training tools intended to keep normal gameplay fair.

## Included
- Custom transparent PixelForge main menu with real bundled branding
- Lunar-style ClickGUI (Right Shift)
- Profile system: PvP, Survival, Minigames, UHC, SMP and Default
- HUD suite: FPS, CPS, coordinates, armor, potions, keystrokes, compass, ping/TPS, speed and more
- Custom crosshair renderer with multiple styles, color, opacity, outline and sizing controls
- Account management and Microsoft/legacy session helpers
- Modrinth browsing/install support
- Fullbright, NoHurtCam, Zoom, AutoTool, ToggleSprint and utility tools
- Performance controls including Dynamic FPS, adaptive render distance and memory cleanup
- PvP training modules and configurable keybinds
- Persistent module/profile configuration
- Fabric 1.21.11 mixin integration

## Requirements
- Minecraft **1.21.11**
- Fabric Loader **0.19.3+**
- Fabric API **0.141.6+1.21.11**
- Java **21**

## Build

```bash
./gradlew build
```

The release jar is written to `build/libs/pixelforge-1.0.0.jar`.
The build also packages the PixelForge asset payload so the distributable is not accidentally produced as a tiny source-only jar.

## Controls

- **Right Shift** — Open/close ClickGUI
- Use the ClickGUI to toggle modules and switch profiles.

## Project

Author: **ViperXC132**

Repository: `ViperXC132/pixel-forge-client1`
