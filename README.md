# Motion Suite

Smoother motion, clearer fights. Motion Suite is a client-side Fabric mod for Minecraft 1.21.11 that adds a motion blur, a gliding camera for ender pearls, and a crystal PvP guard that keeps the screen readable when it matters.

Source and issues: https://github.com/0r3vy/Motionsuite

## What it does

### Motion blur
- A frame-blending blur that works on vanilla OpenGL and on VulkanMod, picked automatically at startup
- Strength from 0 to 100 percent
- Adaptive mode keeps the length of the blur constant in time, so it feels the same at 60 and at 240 FPS
- Cheap shader math and a fix for faint trails that would otherwise never fade
- Stays out of the way of other post effects, such as the spectator creeper view

### Ender pearl camera
- After you throw a pearl the camera glides toward your new position instead of snapping
- Separate speeds for first and third person
- Limits how far the camera may lag behind, and how long the glide lasts
- Finishes early once the camera has caught up, taking your ping into account

### Crystal guard
- Holding an end crystal, or aiming at a crystal, obsidian or bedrock, makes the camera follow you tightly
- The blur eases off at the same time, so crystal fights stay sharp

### Clean cuts
- When the view jumps, for example after a respawn, a dimension change or a teleport, the blur forgets the previous frame so no ghost of the old place is left on screen
- A fault in one feature never takes the others down

Everything is visual and runs on your client only. Nothing is sent to the server.

## Requirements

- Minecraft 1.21.11
- Fabric Loader 0.18.1 or newer
- Fabric API
- Java 21
- VulkanMod is optional

## Installation

1. Install Fabric Loader and Fabric API for Minecraft 1.21.11.
2. Put the Motion Suite jar in your `mods` folder.
3. Start the game and use the commands below to adjust the settings.

## Commands

### Blur

| Command | Effect |
| --- | --- |
| `/motionblur` | Show the blur settings |
| `/motionblur <0-100>` | Blur strength in percent, 0 turns it off |
| `/motionblur adaptive on\|off` | Keep the blur length constant when the frame rate changes |
| `/motionblur guardcut <0-100>` | How much the blur is reduced while the crystal guard is active |

### Camera

| Command | Effect |
| --- | --- |
| `/motioncamera` or `/motioncamera status` | Show the camera settings |
| `/motioncamera toggle` | Turn the pearl glide on or off |
| `/motioncamera guard` | Turn the crystal guard on or off |
| `/motioncamera speed <0.02-1>` | Glide speed in third person |
| `/motioncamera fpspeed <0.02-1>` | Glide speed in first person |
| `/motioncamera maxoffset <0-64>` | Largest allowed lag in blocks, 0 means 64 |
| `/motioncamera guardoffset <0.1-8>` | Largest allowed lag while the guard is active |
| `/motioncamera guardspeed <0.02-1>` | Glide speed while the guard is active |
| `/motioncamera duration <100-10000>` | How long the glide lasts after the last pearl, in milliseconds |
| `/motioncamera reload` | Reload the config file |

### Overview

| Command | Effect |
| --- | --- |
| `/motionsuite` or `/motionsuite status` | Show every setting at once |
| `/motionsuite reload` | Reload the config file |

Any option run without a value prints its current setting.

## Configuration

Settings are stored in `config/motionsuite.json`. Defaults:

```json
{
  "blur": {
    "amount": 50,
    "adaptive": false,
    "guardCut": 60
  },
  "camera": {
    "enabled": true,
    "speed": 0.25,
    "firstPersonSpeed": 0.3,
    "exceptionDuration": 1200,
    "maxOffset": 4.0,
    "crystalGuard": true,
    "guardMaxOffset": 1.0,
    "guardSpeed": 0.7
  }
}
```

Out-of-range values are clamped. A file that cannot be read is renamed to `motionsuite.json.bad` and the defaults are restored.

## Building from source

```
gradle wrapper --gradle-version 9.4.1
./gradlew build
```

The jar is written to `build/libs`. To include the Vulkan path, place a VulkanMod jar at `libs/vulkanmod.jar` before building. Without it the Vulkan sources are skipped and only the OpenGL path is built.

Tool versions are listed in `gradle.properties`. If one cannot be resolved, use the current value from fabricmc.net/develop.

## Status and limitations

- This is an early release. Please report problems on the issue tracker.
- With VulkanMod the blur is applied after the interface is drawn, so the HUD is blurred together with the world.
- The OpenGL blur uses three full-screen passes per frame, which is the minimum for a static post-effect chain.

## License

MIT License, copyright (c) 2026 r3vy. See `LICENSE`.
