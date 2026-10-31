# Motion Suite

Make movement feel smooth and keep fights readable. Motion Suite adds a motion blur, a gliding camera for ender pearls, a crystal PvP guard, a render optimizer with tunable fullbright, TNT explode removal, item drop removal and a farm lag guard, free look and key-bound command macros, all in one small client-side mod.

## Highlights

- **Motion blur for every renderer.** Works on vanilla and on VulkanMod, and picks the right path by itself.
- **Adaptive blur.** The blur keeps the same length in time whether you run at 60 or 240 FPS.
- **Pearl camera glide.** Throw a pearl and the camera slides to where you land instead of snapping.
- **Settings screen.** Press M, or use the Configure button in Mod Menu, to open a hub with a slider or toggle for every option. No commands needed, and on OpenGL the blur stays visible behind it.
- **Smart blur.** Optional mode that follows how much you turn and move and stays sharp while you stand still. Set "Blur when still" to 0 to skip the blur pass entirely when idle.
- **Crystal guard.** Holding or aiming at crystals, obsidian or bedrock tightens the camera, and can ease the blur too.
- **Tunable fullbright with two modes.** Night Vision mode lights caves and night like the potion does, with a steady glow and no flicker; Gamma mode pushes the brightness setting far past the vanilla slider cap. A strength slider from 0 to 100 percent (70 by default) tunes both, and existing configs keep Night Vision until you switch.
- **Clean no weather.** The sky, fog and lightmap keep the storm look, and only the rain, the snow and their sounds go away.
- **Remove TNT explode.** Every primed TNT is deleted the moment it reaches the client, so no fuse, no blast and no explosion sound, and the blast particles are filtered too.
- **Remove item drop.** Dropped item stacks are deleted on sight, so nothing is drawn and no caps are needed.
- **Farm lag guard.** Hide XP orbs and thrown items on sight, silence farm noise group by group with 29 switches, hide entities beyond a range you pick (players stay visible, 0 turns it off), and freeze 70 guard blocks so redstone farms stop flickering while you idle.
- **Free look.** Look around without turning your character, hold or toggle, with an optional third person view. The key can be set right in the hub.
- **Command key.** Chat macros with their own keys. Starts with one slot, more can be added, each with its own bind and text box in the hub. A cooldown against spam kicks, 256 character limit, text starting with `/` runs as a command, and each server address can keep its own slot text.
- **No ghosting after a jump.** Respawns, dimension changes and teleports clear the blur so the old place never lingers on screen.
- **Safe by design.** One feature failing never takes down another, and the game never waits on the config file.

## Quick start

Install it with Fabric API, join a world and press **M** to open the settings screen. You can also open it from Mod Menu with the Configure button, or with `/motionsuite`.

The hub has four sections: **Motion Pearl**, **Motion Blur**, **Render Optimizer** and **Other**. Long pages scroll with the mouse wheel. Every key bind the mod registers can be set right there with a "press a key" button.

Every setting is stored in `config/motionsuite.json`. After editing it by hand, run `/motionsuite reload`.

## Compatibility

- Minecraft 1.21.11 with Fabric Loader 0.19.2 or newer, Fabric API and Java 21
- VulkanMod is supported but not required, and Mod Menu is optional

## Good to know

- Everything is client-side. The only thing sent to a server is a message or command you trigger yourself with Command key.
- On VulkanMod the HUD is blurred together with the world.
- Bug reports are welcome on the issue tracker: https://github.com/0r3vy/Motionsuite/issues
