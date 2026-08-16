# Minecraft 26.1.x Fabric parity report

Date: 2026-08-16

Primary reference: the checked-in, working `1.18.1-fabric` implementation at
`HEAD`. Secondary reference: `forge-version/` (Minecraft 1.18.1 / Forge
39.0.5). The Fabric version is the authority where the two old implementations
differ.

## Target and compatibility

- Compiled target: Minecraft 26.1.2, Fabric Loader 0.19.3, Fabric API
  0.155.2+26.1.2, Java 25.
- Metadata requires Fabric API 0.145.4 or newer, matching the latest official
  26.1.1 API line while permitting the 26.1.2 build used for compilation.
- Published Minecraft dependency: `>=26.1.1 <26.2`, so Loader accepts 26.1.1,
  26.1.2, and later 26.1.x releases but rejects 26.2.
- Mod version: `26.1.2-2.7.0-fabric`.

## Effect assignment parity

The port retains all 40 set-effect classes and the reference registration
order. All `isValid` assignment predicates were compared against both old
implementations.

The major assignment defect was in `SetEffect.registryNameContains`: a display
name lookup could throw while 26.1 item components were still unbound, and the
shared exception handler discarded an already-valid registry-name match. That
silently removed effects from many name-derived sets. Registry matching is now
independent of the optional display-name lookup. Config version 1.3 regenerates
only stale `Set_Effects` entries created by the broken port while retaining
other user settings.

Runtime-generated configuration contains 418 sets, of which 310 have default
effects. Verified examples include:

- Obsidian: `Immovable (0.75);Flame Resistant;Health Boost (20.0)`
- Crying Obsidian: `Illuminated (10);Immovable (0.75);Flame Resistant;Health Boost (20.0)`

Regrowth preserves the reference class/name matching and adds modern plant-tag
coverage only when tags are bound; it no longer queries tags during early mod
initialization.

## Effect behavior parity repairs

- Restored every central dispatch path: equipped ticks, activation key,
  equipment/full-set changes, login/respawn, death prevention, attack, fall,
  block/entity loot, item damage/break, cooldown sync, and Hoarder flushing.
- Lucky now hooks 26.1 enchantment-level lookup for virtual Looting +4 and uses
  modern ore block successors while preserving Silk Touch and self-drop checks.
- AutoSmelt preserves unsmeltable drops, avoids duplicate fallback drops,
  multiplies recipe output correctly, splits oversized results into legal
  stacks, and retains the reference mob/block feedback volumes.
- Absorbent follows the old water-plant restriction and does not delete
  arbitrary waterlogged structures.
- Time Control and Sleepy modify the dimension's default world clock rather
  than monotonic game age and respect the advance-time rule.
- Illuminated moving-light blocks are no longer marked as air, preventing the
  light from being replaced every tick.
- Musical uses the supporting block's note instrument; Prickly triggers
  feedback only after successful damage; Rocky syncs modern water/motion state;
  Slimey retains the tiered bounce path; set-injected enchantments are removed
  or restored without destroying player enchantments.
- Hoarder retains an owning entity after removal of
  `ItemStack#getEntityRepresentation`, drops stored contents on break, and
  closes an open Hoarder container to prevent duplication.

## Rendering parity repairs

- Restored the original 58-plane equipped mesh and corrected the cube polygon
  mapping to `EAST, WEST, DOWN, UP, NORTH, SOUTH`. The previous mapping
  scrambled faces and UVs.
- Worn armor again binds the source PNG directly. The mesh intentionally uses
  UV coordinates outside one sprite; remapping it through the block atlas
  sampled neighbouring atlas entries and corrupted every worn texture.
- Restored the working Fabric child transform, real-enchantment-only worn
  glint, developer colors, block/item tint lookup, combined textures, and exact
  Forge item-display transforms.
- The 26.1 item model matches Forge's composition: extruded base, two masked
  source-texture planes, transparent cover, per-source tint, combined
  left/right sources, animation state, and foil state.
- Corrected atlas directory sources and generated-item model references.
- Fixed the 26.1 shared-model hook and render-state identity: it now replaces
  the shared placeholder and keys cached rendering by the actual armor item
  and combined data instead of reusing the first chestplate for every stack.
- Removed the vanilla iron equipment asset from both constructor-time and
  rebound default components, so iron geometry is never a fallback.

## Anvil combination and JEI

- Restored the 1.18.1 anvil combination path, including distinct left/right
  visual sources, half-texture composition, merged effects and attributes,
  durability repair, compatible enchantment merging, and repair-cost growth.
- Added the Fabric `jei_mod_plugin` entrypoint for JEI 29. Enabled generated
  armor is registered as extra ingredients and disabled sets are removed from
  JEI at runtime.

## Runtime evidence

On Minecraft 26.1.2 the development client:

- applied every required common and client Mixin;
- registered 1,672 generated armor items and initialized 418 sets;
- completed resource reload;
- validated all 1,672 inventory render states and direct worn source textures;
- validated combined-source rendering and non-empty meshes for all four slots;
- started an integrated server, loaded the existing world, and joined it
  without a Block Armor exception.

The same source was then compiled against the exact Minecraft 26.1.1 / Fabric
API 0.145.4+26.1.1 toolchain. That client also registered all 1,672 items,
initialized all 418 sets, completed the full render-resource validation,
started an integrated 26.1.1 server, reached `Block Armor server ready`, and
joined the world without a Block Armor exception. This verifies actual 26.1.1
loading and gameplay startup in addition to Loader's metadata-range check.

A clean 26.1.2 dedicated-server launch also applied the server Mixins,
registered all 1,672 items, and initialized all 418 sets before the standard
development EULA gate. Full dedicated-server interaction testing remains in
the manual acceptance matrix below.

The WSL test host logs a missing optional Linux `flite` narrator library and an
X11 cursor warning; neither originates from Block Armor and neither prevents
world startup.

## Remaining release validation

Source comparison and automated/runtime structural validation cannot prove the
appearance of every block under every resource pack or exercise every random,
multiplayer, and interaction branch. Before publishing, the remaining manual
acceptance matrix is:

- exercise all 40 effects below/at/above the configured piece threshold;
- activation, cooldown, armor damage/break, death/respawn, Hoarder recovery,
  block loot, and mob loot on a dedicated server;
- inventory, held, dropped, armor-stand, adult/baby worn rendering, animated
  and tinted sources, combined armor, glint, and resource reload with the
  intended resource packs;
- repeat the visual/effect acceptance matrix with the packaged jar in the
  intended Prism modpack on both 26.1.1 and 26.1.2.
