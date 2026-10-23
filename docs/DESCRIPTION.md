# Motion Suite

Make movement feel smooth and keep fights readable. Motion Suite adds a motion blur, a gliding camera for ender pearls, and a crystal PvP guard, all in one small client-side mod.

## Highlights

- **Motion blur for every renderer.** Works on vanilla and on VulkanMod, and picks the right path by itself.
- **Adaptive blur.** The blur keeps the same length in time whether you run at 60 or 240 FPS.
- **Pearl camera glide.** Throw a pearl and the camera slides to where you land instead of snapping.
- **Crystal guard.** Holding or aiming at crystals, obsidian or bedrock tightens the camera and eases the blur, so you can see what you are doing.
- **No ghosting after a jump.** Respawns, dimension changes and teleports clear the blur so the old place never lingers on screen.
- **Safe by design.** One feature failing never takes down another, and the game never waits on the config file.

## Quick start

Install it with Fabric API, join a world and try:

- `/motionblur 40` sets the blur strength
- `/motionblur adaptive on` keeps the blur consistent across frame rates
- `/motioncamera toggle` turns the pearl glide on or off
- `/motionsuite status` shows every setting

Every setting is also stored in `config/motionsuite.json`.

## Compatibility

- Minecraft 1.21.11 with Fabric Loader 0.18.1 or newer, Fabric API and Java 21
- VulkanMod is supported but not required

## Good to know

- Everything is visual and client-side. The server is never involved.
- On VulkanMod the HUD is blurred together with the world.
- This is an early release, so bug reports are welcome on the issue tracker: https://github.com/0r3vy/Motionsuite/issues

## Credits

Created by r3vy. Released under the MIT License.
