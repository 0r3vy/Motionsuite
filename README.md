# Motion Suite

Smoother motion, clearer fights. Motion Suite is a client-side Fabric mod for Minecraft 1.21.11 that adds a motion blur, a gliding camera for ender pearls, a crystal PvP guard, a render optimizer with fullbright, TNT explode removal, item drop removal and lag reducers for idling at farms, free look, and key-bound command macros with per-server text.

Source and issues: https://github.com/0r3vy/Motionsuite

## What it does

The hub is split into four sections: **Motion Pearl**, **Motion Blur**, **Render Optimizer** and **Other**.

### Motion Pearl
- After you throw a pearl the camera glides toward your new position instead of snapping
- Separate speeds for first and third person
- Limits how far the camera may lag behind, and how long the glide lasts
- Finishes early once the camera has caught up, taking your ping into account

### Crystal guard (inside Motion Pearl)
- Holding an end crystal, or aiming at a crystal, obsidian or bedrock, makes the camera follow you tightly
- Optionally eases the blur at the same time, so crystal fights stay sharp (off by default)

### Motion Blur
- A frame-blending blur that works on vanilla OpenGL and on VulkanMod, picked automatically at startup
- Strength from 0 to 100 percent
- Adaptive mode keeps the length of the blur constant in time, so it feels the same at 60 and at 240 FPS
- Cheap shader math and a fix for faint trails that would otherwise never fade
- Smart blur (optional): the blur follows how much you turn and move, and stays sharp while you stand still. With "Blur when still" at 0 the blur pass is skipped completely while idle, which saves GPU time
- Stays out of the way of other post effects, such as the spectator creeper view

### Render Optimizer
Visual effects:
- Fullbright has two modes picked in the Optimizer tab: Night Vision lights caves and night with a steady potion-style glow, and Gamma pushes the brightness setting far past the vanilla slider cap. A strength slider from 0 to 100 percent tunes both (off by default)
- Optional hiding of the hurt camera tilt, fire overlay, clouds, screen background tint, particles, scoreboard, titles and hotbar text
- No weather keeps the gloomy rain sky and fog, and only the rain and snow themselves go away, together with their sounds, so no storm is heard either
- No item bounce removes the pop animation of hotbar items

Lag reducers, each with its own switch in the Optimizer tab, all off by default so nothing changes until you turn it on:
- **Remove TNT explode**: deletes every primed TNT the moment it reaches the client, so there is no primed entity, no blast particles and no fuse or explosion sound
- **Remove item drop**: deletes every dropped item stack on sight, nothing is drawn and no caps to tune
- **Hide XP orbs**: deletes every experience orb on sight, so nothing renders and nothing flies toward you when a farm collects it
- **Hide thrown items**: hides snowballs, eggs, ender pearls, experience bottles and thrown potions on sight, so pearl and bottle spam farms stay clean. The ender pearl itself stays in the world so the camera glide below keeps tracking it, it just stops rendering
- **Remove lag sound**: silences the noisy farm sounds, wither, pistons, tnt, furnace and smoker crackle, the repeated redstone torch burnout fizz, the throw sounds of snowballs, eggs, ender pearls, experience bottles and thrown potions, every block break and place, doors and chests opening, mob ambient, hurt, death and shooting calls, chicken eggs, item and xp pickup pops, note blocks, tripwires, composters, crafters, bee hives, sculk sensors, fire, portals, dripstone drips, minecarts, boats, anvils, water, lava, the lava fizz and dispensers, droppers and crafters. The sounds are split into 29 groups and each group has its own switch in a sound list inside the Optimizer tab, with quick "mute all" and "allow all" buttons, so you pick exactly which noise dies. Turning on the other removals also mutes the sounds of the things they hide, so hidden orbs and drops no longer pop while collected. Your own player sounds stay on, so you still hear yourself get hit while idling
- **Far entity cull**: hides entities beyond a range you pick, 0 turns it off, players are never hidden
- **Block render cull**: 70 guard blocks (redstone, pistons, observers, machines, rails, fluids, every door and trapdoor) hold still while it is on. Server updates that would animate pistons or blink redstone torches, dust, repeaters, comparators and observers are dropped before they reach the client, so pushed blocks no longer vanish and pop back in and farms stop flickering while you idle. Blocks right next to a moving piston hold still too, up to three blocks deep. Held positions are remembered with their real server state and synced back when the switch goes off, after a minute without updates, when the held list is full, or when you leave the world, so nothing stays out of date. Breaking, placing and using blocks keep working: the moment you mine or click a held position it unfreezes for you. The wither and its skulls stop rendering too

Every lag reducer and visual toggle above ships off, so those parts stay vanilla until you turn them on, and all of it is client-side, so the server keeps running exactly as before.

### Other
- **Free look**: hold or toggle a key to look around without turning your character, with an optional switch to third person. Sensitivity matches vanilla, the view returns to your real direction on release
- **Command key** (formerly Auto text): starts with one slot and you can add more (up to four). Each slot has its own key bind and its own text box right in the hub. A cooldown prevents spam kicks, text over 256 characters is cut, and text starting with `/` is sent as a command instead of chat
- **Per server text**: with the toggle on, each server address keeps its own slot text, so donutsmp.net can send `/shoo` while vanilapvp.net sends `/kit` from the same key. Servers without their own text fall back to the shared slots, and "Forget this server" removes the text of the server you are on
- All key binds can be set directly in the hub with a "press a key" button, no trip to the vanilla controls menu needed

### Hub
- Press **M** (rebindable in the hub) to open a settings screen with a slider or toggle for every option, so no commands are needed
- The four sections match the feature groups, long pages scroll with the mouse wheel, and every key bind the mod registers (open hub, free look and the command key slots) can be set right there
- The block render cull opens its own page with a switch for every guarded block and quick "hide all" and "show all" buttons, and remove lag sound has a matching page with a switch for every sound group plus "mute all" and "allow all"
- The mod's keys never appear in the vanilla Controls menu, so nothing litters the game settings; on first run your old picks are carried over from options.txt
- On OpenGL the blur keeps running behind the hub, so you can judge a change at once

### Clean cuts
- When the view jumps, for example after a respawn, a dimension change or a teleport, the blur forgets the previous frame so no ghost of the old place is left on screen

## Building from source

```
./gradlew build
```

VulkanMod support is compiled in only when `libs/vulkanmod.jar` (a VulkanMod build for Minecraft 1.21.11) exists. Without it the build still succeeds, but the jar has no VulkanMod blur. For a release, require it so a missing jar fails the build instead of shipping a jar without VulkanMod support:

```
./gradlew clean build "-Pmotionsuite.requireVk=true"
```

On Windows PowerShell keep the quotes around the `-P` argument. The jar in `libs/` is only used to compile and is never bundled.

## Credits

Created by **r3vy**. Released under the MIT License.
