# Motion Suite

Smoother motion, clearer fights. Motion Suite is a client-side Fabric mod for Minecraft 1.21.11 that adds a motion blur, a gliding camera for ender pearls, and a crystal PvP guard that keeps the screen readable when it matters.

Source and issues: https://github.com/0r3vy/Motionsuite

## What it does

### Motion blur
- A frame-blending blur that works on vanilla OpenGL and on VulkanMod, picked automatically at startup
- Strength from 0 to 100 percent
- Adaptive mode keeps the length of the blur constant in time, so it feels the same at 60 and at 240 FPS
- Cheap shader math and a fix for faint trails that would otherwise never fade
- Smart blur (optional): the blur follows how much you turn and move, and stays sharp while you stand still. With "Blur when still" at 0 the blur pass is skipped completely while idle, which saves GPU time
- Stays out of the way of other post effects, such as the spectator creeper view

### Ender pearl camera
- After you throw a pearl the camera glides toward your new position instead of snapping
- Separate speeds for first and third person
- Limits how far the camera may lag behind, and how long the glide lasts
- Finishes early once the camera has caught up, taking your ping into account

### Crystal guard
- Holding an end crystal, or aiming at a crystal, obsidian or bedrock, makes the camera follow you tightly
- Optionally eases the blur at the same time, so crystal fights stay sharp (off by default)

### Hub
- Press **M** (rebindable under Controls, category Motion Suite) to open a settings screen with a slider or toggle for every option, so no commands are needed
- On OpenGL the blur keeps running behind the hub, so you can judge a change at once

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
- Mod Menu is optional and adds a Configure button that opens the settings screen

## Installation

1. Install Fabric Loader and Fabric API for Minecraft 1.21.11.
2. Put the Motion Suite jar in your `mods` folder.
3. Start the game and use the commands below to adjust the settings.

## Settings screen

Press **M** in game, run `/motionsuite`, or open Mod Menu, select Motion Suite and press its **Configure** button (Mod Menu is optional). Every option is available there as a slider or toggle, and changes are saved when you close the screen.

## Commands

The settings screen replaces the old per-option commands. Two commands remain:

| Command | Effect |
| --- | --- |
| `/motionsuite` or `/motionsuite hub` | Open the settings screen |
| `/motionsuite reload` | Reload `config/motionsuite.json` after editing it by hand |

## Configuration

Settings are stored in `config/motionsuite.json`. Defaults:

```json
{
  "blur": {
    "amount": 50,
    "adaptive": false,
    "guardCut": 0,
    "smart": false,
    "stillBlur": 35
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
