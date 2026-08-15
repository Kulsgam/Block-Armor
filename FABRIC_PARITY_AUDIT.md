# Fabric parity audit against `forge-version`

Date: 2026-08-16

> Resolution update (2026-08-16): The findings below describe the pre-repair port. The implementation now reconnects Fabric armor ticking, stack-aware attributes, dynamic configured durability, dropped/break lifecycle handling, server config and cooldown synchronization, corrected Lucky/AutoSmelt context, Forge-tier Slimey/Rocky behavior, override texture stitching, reload-safe renderer caches, thick item geometry/glint, Shift tooltips, custom Fabric item groups, developer colors, and the developer chat bridge. A clean remapped build, dedicated-server initialization, and client atlas/main-menu initialization pass on Java 17. In-world behavioral and visual comparison should still be performed in Prism Launcher because the WSL smoke test cannot substitute for interactive play.

## Executive summary

The Fabric port starts, registers 1,420 generated armor items, creates recipes, and has working replacements for several Forge events. It is **not yet functionally equivalent** to the Forge version.

The largest problem is architectural: four methods copied from Forge are not vanilla/Fabric hooks and have no callers:

- `BlockArmorItem.onArmorTick(...)`
- `BlockArmorItem.getAttributeModifiers(EquipmentSlot, ItemStack)`
- `BlockArmorItem.onEntityItemUpdate(...)`
- `BlockArmorItem.setDamage(ItemStack, int)`

Consequently, most set effects never tick, all set-effect attributes and configured armor attributes are ignored by gameplay, dropped-item cleanup is absent, and Hoarder contents are not recovered when armor breaks. This is a critical parity gap even though the project compiles and the client starts.

The Fabric-specific rendering and loot implementations also differ from Forge in observable ways. Override textures are not stitched, renderer caches survive resource reloads, dev colors and some glint/baby rendering behavior are missing, and Lucky/AutoSmelt do not preserve Silk Touch behavior.

## Audit method and scope

The audit compared every Java and resource component under `forge-version/src/main` with its Fabric counterpart under `src/main`, then traced each Forge event/extension method to a Fabric callback, mixin, vanilla override, or caller. The current Fabric branch was compiled and packaged successfully with Java 17; the supplied Prism logs were used to inspect client initialization and registration. JEI is excluded as an actionable gap because no suitable JEI Fabric build exists for Minecraft 1.18.1.

Severity meanings:

- **Critical**: a major subsystem or broad class of features does not function.
- **High**: a named feature is materially broken or behaves incorrectly in ordinary play.
- **Medium**: visible inconsistency, compatibility issue, or important edge case.
- **Low**: polish or development-only parity issue.

## Critical findings

### C1. Continuous armor/set-effect ticking is disconnected

Forge calls `ArmorItem#onArmorTick` for equipped armor. Fabric/vanilla 1.18.1 does not define that hook. The method remains at `BlockArmorItem.java:172`, but the only call to it is its own definition; neither `BlockArmorFabric` nor a mixin/event invokes it.

Impact:

- Base potion application in `SetEffect.onArmorTick` never runs (`SetEffect.java:235-243`).
- Button-controlled effects do not react to the key even though key packets are sent.
- Almost every environmental or periodic effect is inactive.
- Hoarder and Ender Hoarder cannot open from their activation key.
- The Health Boost login/respawn queue is populated but never processed (`SetEffectHealth_Boost.java:39-64`).

A complete effect matrix appears below.

Recommended repair: add a server player tick hook and a client player tick hook which iterate equipped `BlockArmorItem` stacks and call a common, side-correct armor tick function once per piece. Ensure the call order matches Forge closely enough for cooldowns and the worn-effect cache.

### C2. Armor and set-effect attribute modifiers are disconnected

Forge adds a stack-aware `getAttributeModifiers(EquipmentSlot, ItemStack)` extension. Fabric/vanilla uses `Item#getDefaultAttributeModifiers(EquipmentSlot)`. The Fabric method at `BlockArmorItem.java:91-100` is therefore an ordinary overload and is never consulted when equipment attributes are built.

Impact:

- Configured damage reduction, toughness, and knockback resistance changes do not update worn armor attributes.
- Global damage-reduction/toughness/knockback modifiers do not affect actual equipment attributes.
- Set effects based on attributes do not work: Speedy, Powerful, Immovable, Lucky's +Luck, and Health Boost.
- The `wearingFullSet` NBT maintained every tick (`ArmorSet.java:754-775`) currently has no gameplay consumer.

`ArmorItem` captured its private `defaultModifiers` during construction. Calling the port's `setMaterial` later only updates the port's unused `attributes` map (`BlockArmorItem.java:193-206`).

Recommended repair: override `getDefaultAttributeModifiers(EquipmentSlot)` with a correctly rebuilt immutable map, or apply/remove transient entity attribute modifiers from equipment-change/tick hooks. The latter is needed if modifiers remain stack/full-set dependent.

### C3. Configured durability cannot change registered items

Fabric sets durability once in the item constructor (`BlockArmorItem.java:52-55`). `Item#getMaxDamage()` is final in this version, and the Forge access-transformer behavior that changed `maxDamage` was removed. Later `Config.load()` calls `ArmorSet#createMaterial`, but this cannot change the registered item's maximum damage.

Impact:

- Per-set `Armor_Durability` does not affect items after registration.
- `Global Durability Modifier` is not applied to maximum damage.
- Client and server may display/use defaults despite the config containing other values.

Recommended repair: load all durability configuration before constructing items, or use a supported stack/component-based durability strategy. Since generated items must be registered early, this likely requires a pre-registration config pass keyed by registry identifiers.

### C4. Forge item lifecycle hooks are copied but inactive

`BlockArmorItem.onEntityItemUpdate` (`BlockArmorItem.java:158-169`) and `BlockArmorItem.setDamage` (`BlockArmorItem.java:75-82`) are Forge hooks, not Fabric overrides, and have no callers.

Impact:

- Disabled or developer-spawned armor entities are not removed after being dropped.
- Hoarder `onBreak` is never called through item damage, so stored contents can be lost when the armor breaks.

Recommended repair: use an item-entity tick mixin/event for dropped cleanup and intercept the actual `ItemStack` damage/break path for Hoarder recovery.

## High-severity findings

### H1. AutoSmelt and Lucky violate Silk Touch and other Forge loot rules

The Fabric `LootTableMixin` forwards block loot without the tool (`LootTableMixin.java:20-39`). Neither `SetEffectAutoSmelt.transformLoot` nor `SetEffectLucky.transformOreLoot` checks Silk Touch (`SetEffectAutoSmelt.java:44-50`, `SetEffectLucky.java:24-29`).

Differences from Forge:

- AutoSmelt processes Silk Touch drops; Forge explicitly skips them.
- Lucky doubles Silk Touch ore blocks; Forge explicitly skips them.
- Lucky removed Forge's safeguard that avoided doubling when a block drops itself.
- AutoSmelt removed Forge's fallback that attempts to smelt the mined block when none of its generated drops can be smelted.
- AutoSmelt particles/sounds use the miner or killer position, while Forge uses the loot origin/dead entity position.
- Lucky's +4 looting is injected only into vanilla `LootingEnchantFunction`; Forge's `LootingLevelEvent` also covered compatible modded loot logic and played its feedback effect.

Recommended repair: pass `LootContextParams.TOOL`, `ORIGIN`, and `BLOCK_STATE` into parity helpers and reproduce the Forge guards before transforming loot.

### H2. Slimey fall behavior is only a partial approximation

The Fabric helper at `SetEffectSlimey.java:58-61` applies one uniform bounce and cancels fall damage. Forge used three fall-distance tiers, played different sounds, updated ground/impulse/network flags, restored vertical motion at the end of the client tick, and used different server/client handling.

Additionally, Slimey's air acceleration and wall bounce remain inside the disconnected `onArmorTick` method (`SetEffectSlimey.java:27-56`).

Recommended repair: port the full fall-event state machine into `LivingEntityMixin` plus an end-client-tick callback, retaining the original distance and sound logic.

### H3. Rocky no longer forces sinking/normal underwater movement

Forge cleared the entity's internal `wasTouchingWater` state and temporarily used first-tick behavior so the player sank and avoided repeated bubble/sound effects. Those operations were removed. The remaining logic at `SetEffectRocky.java:20-38` only disables swimming and applies some horizontal slowdown—and is itself currently unreachable because armor ticking is disconnected.

Recommended repair: expose the two entity fields with mapped mixin accessors and reproduce the Forge state changes after restoring armor ticks.

### H4. Override armor textures are not stitched into the block atlas

`BlockArmorTextures` attempts to use `ArmorSet.TEXTURE_OVERRIDES` (`BlockArmorTextures.java:24-31`), but `BlockArmorItemRenderer.register` only registers the eight base/cover sprites (`BlockArmorItemRenderer.java:30-37`). Forge explicitly stitched every override sprite from `TEXTURE_OVERRIDES`.

Impact: cactus, sugar cane, enchanting table, chest, ender chest, beds, shulker boxes, composter, and related special textures fall back to ordinary block-model faces instead of their Forge override assets.

Recommended repair: register every `TextureOverrideInfo.Info.shortLoc` in the block-atlas sprite callback.

### H5. Config is not server-synchronized and does not preserve Forge config semantics

The Fabric config is loaded independently from `config/blockarmor.properties` in each process (`Config.java:27-47`). There is no server-to-client synchronization.

Impact in multiplayer:

- Client worn-effect calculation and tooltips can disagree with the server about `piecesForSet`, enabled sets, and configured effect lists.
- Client rendering/GUI state can disagree with authoritative server behavior.
- Config reload events are absent; changes require restart.
- Forge's config version migration/reset behavior was removed.
- The Forge key was misspelled `Global Tougness Modifier`; Fabric uses `Global Toughness Modifier`, so an existing Forge value is not migrated automatically.
- Sets registered after the initial file is created are applied from current values but are never appended to the file.

Recommended repair: use a server config with explicit login synchronization, add versioned migration, and rewrite/merge newly discovered set keys without deleting user values.

### H6. Resource reload leaves stale worn models and icon masks

`BlockArmorRenderer.models` is never cleared (`BlockArmorRenderer.java:18-34`). Its cache key contains sprite identity by name but not atlas generation, so cached models retain old `TextureAtlasSprite` instances after resource reload. `BlockArmorItemRenderer.MASKS` is also never cleared (`BlockArmorItemRenderer.java:27-49`). Forge cleared model and generated-icon caches when textures were remapped.

Recommended repair: register a client resource-reload listener that clears both caches and any texture-selection caches.

### H7. Enchantment glint and special rendering do not match Forge

- Forge suppressed worn glint when every enchantment was internally added by a set effect. Fabric calls `ArmorRenderer.renderPart` directly, so those internal enchantments produce visible worn glint.
- The dynamic item renderer uses a plain translucent buffer (`BlockArmorItemRenderer.java:50`) and never requests a foil/glint buffer, so genuinely enchanted inventory armor may have no item glint.
- Forge's dynamic item model generated front, back, and pixel-edge geometry. Fabric emits only front-facing quads (`BlockArmorItemRenderer.java:52-58`), producing a flat/inconsistent held or dropped model.

Recommended repair: inspect the stack's enchant NBT exactly as Forge did, select foil buffers appropriately, and generate the back/edge quads used by the original item model.

## Medium-severity findings

### M1. Tooltip interaction changed from Shift to Advanced Tooltips

Forge expanded descriptions while Shift was held. Fabric checks `TooltipFlag#isAdvanced` and passes a null player (`BlockArmorItem.java:120-134`). Users must toggle F3+H instead of holding Shift, and active effects are never bolded because no client player is supplied.

Recommended repair: move the client-only Shift/player query into a client helper or mixin and preserve the original interaction.

### M2. Missing-texture sets are no longer disabled

Forge mapped all four directional sprites after atlas creation and disabled a set if any required texture was missing. Fabric falls back to the block model's particle sprite (`BlockArmorTextures.java:45-52`) and never marks `ArmorSet.missingTextures`.

Impact: armor can remain enabled with incorrect or missing visuals, particularly for unusual modded models.

### M3. Custom creative tabs were removed

Forge kept separately ordered Vanilla Block Armor and Modded Block Armor tabs. Fabric assigns generated armor to the Combat tab (`BlockArmorItem.java:52-64`, `ArmorSet.java:700-723`). This is a deliberate implementation simplification, but it is a visible parity loss and can make 1,420+ items difficult to browse.

### M4. Worn rendering omits Forge-only presentation details

The restored plane layout is close to Forge, but the Fabric model does not reproduce:

- developer rainbow/pulse colors from `CommandDev.devColors`;
- Forge's explicit young-entity scaling path;
- animation cross-fade between the current and next texture frame (Fabric atlas animation changes frames, but does not perform the Forge overlay blend).

The dev color packet is still sent and decoded, but the values are no longer consumed by `ModelBAArmor` or `BlockArmorRenderer`.

### M5. Cooldown dimension resynchronization was removed

Forge sent `SSyncCooldownsPacket` after dimension changes because the client forgot custom cooldowns. Fabric's packet class is an empty placeholder and no dimension-change callback exists. Vanilla cooldown packets may handle ordinary add/remove updates, but this does not reproduce the explicit full-state repair from Forge and should be tested across portals while an effect cooldown is active.

### M6. Common code depends on a client package

`BlockArmor` constructs `client.key.KeyActivateSetEffect` in common initialization (`BlockArmor.java:6-15`), and the server network receiver calls that client-package class (`BlockArmorNetwork.java:14-19`). The methods currently used happen not to execute client-only instructions, but the class contains direct `Minecraft`, `KeyMapping`, and client Fabric API references.

This is a dedicated-server classloading/verification hazard and violates Fabric environment separation. Move shared key state into a common class and leave key registration/sending in the client package.

### M7. Dev armor colors are synchronized but have no visual effect

`/dev color` broadcasts the color map correctly, and the client decodes it. The Fabric armor renderer never reads `CommandDev.devColors`, so rainbow/pulse/custom-color commands no longer affect armor.

### M8. The developer GUI opening bridge is unused

`OpenGuiEvent.openFor` exists, but nothing calls it. Forge listened to client chat events. `DISPLAY_ARMOR_GUI` is currently false, so this is a dormant development feature rather than normal-player breakage.

## Set-effect parity matrix

“Inactive” below means its main behavior is trapped behind the uncalled `BlockArmorItem.onArmorTick` path. Some effects retain a secondary mixin/enchantment behavior as noted.

| Set effect | Fabric status | Main difference |
|---|---|---|
| Absorbent | Inactive | Water/lava absorption tick never runs. |
| Arrow Defence | Inactive | Projectile-clearing tick never runs. |
| AutoSmelt | Partial | Loot transform runs; activation toggle does not. Silk Touch and fallback rules differ. |
| Bonemealer | Inactive | Growth tick never runs. |
| Crafter | Inactive | Activation/opening behavior never runs. |
| Diving Suit | Partial | Set-granted enchantments can update; Night Vision potion tick does not run. |
| Ender | Inactive | Teleport activation never runs. |
| Ender Hoarder | Inactive | Transfer/opening behavior never runs. |
| Experience Giving | Inactive | XP generation tick never runs. |
| Explosive | Inactive | Activation/explosion tick never runs. |
| Falling | Inactive | Downward-motion behavior never runs. |
| Feeder | Inactive | Feeding tick never runs. |
| Fiery | Mostly active | Attack mixin runs; needs gameplay validation for event timing. |
| Flame Resistant | Partial/mostly active | Set-added Fire Protection enchantments update through inventory ticking. |
| Frosty | Partial/mostly active | Set-added Frost Walker enchantment updates through inventory ticking. |
| Health Boost | Inactive | Attribute overload is unused and login/respawn restoration queue is never processed. |
| Hoarder | Inactive/unsafe | Cannot open via tick; break recovery hook is inactive; container implementation itself is registered. |
| Illuminated | Inactive | Moving-light placement/update tick never runs. |
| Immovable | Inactive | Attribute overload is unused. |
| Invisibility | Inactive | Base potion-effect tick never runs. |
| Lightweight | Partial | Fall-damage mixin runs; glide/drift tick does not. |
| Lucky | Partial | Ore/looting mixins run; +Luck attribute is inactive; Silk Touch/self-drop rules differ. |
| Musical | Inactive | Note-playing tick never runs. |
| Powerful | Inactive | Attack attributes are in the unused overload. |
| Prickly | Partial | Set-added Thorns can update; nearby cactus-damage behavior does not run. |
| Puller | Inactive | Entity-pulling tick never runs. |
| Pusher | Inactive | Entity-pushing tick never runs. |
| Regrowth | Inactive | Repair/regrowth tick never runs. |
| Respawn | Active with caveats | Fabric death callback is wired; dimension/client-state transitions need multiplayer testing. |
| Rocky | Inactive plus incomplete port | Tick is disconnected; internal water-state behavior was also removed. |
| Sleepy | Inactive | Sleep/time activation never runs. Direct API replacement itself is reasonable. |
| Slimey | Partial/inaccurate | Fall cancellation/bounce approximation runs; tiered bounce, sounds, flags, air acceleration, and wall bounce do not. |
| Slippery | Inactive | Sliding tick never runs. |
| Slow Motion | Inactive | Movement/potion tick never runs. |
| Snowy | Inactive | Snow/rain/ground behavior never runs. |
| Soft Fall | Active | Fall mixin reproduces cancellation and sound closely. |
| Speedy | Inactive | Movement-speed attribute is in the unused overload. |
| Time Control | Inactive | Activation/time tick never runs. Client/server time setter replacement is otherwise equivalent. |
| Undying | Active | Fabric death callback is wired; interaction ordering with Respawn/custom configs should be tested. |

## Areas with good or near-equivalent coverage

- Generated armor registration covers the initially loaded item registry and attempts to handle later item entries.
- Four armor pieces are registered for each eligible set, and startup reports the expected 1,420 items / 355 sets for vanilla 1.18.1.
- Dynamic shaped recipes reproduce the Forge helmet, chestplate, leggings, and boots patterns.
- Worn-effect counting and `onStart`/`onStop` cache maintenance were ported to Fabric server/client tick callbacks.
- Key state is sent only when it changes, matching the intended Forge behavior.
- Hoarder screen-handler types and client screens are registered.
- Soft Fall, Fiery, Undying, Respawn, Lucky loot, AutoSmelt loot, Lightweight fall reduction, and Slimey fall handling have explicit Fabric hooks, although several need the corrections described above.
- Set-granted enchantment NBT is still maintained by `inventoryTick`, including removal markers.
- Fabric API is declared as a required mod dependency in `fabric.mod.json`.
- Common resources are largely present and match the Forge resource set; Forge metadata/access-transformer files are correctly loader-specific omissions.

## Recommended repair order

1. Restore equipped armor ticking on both logical sides. This reconnects the majority of effects and makes further testing meaningful.
2. Replace the unused attribute overload and solve pre-registration durability configuration.
3. Restore Hoarder break recovery and dropped-item lifecycle handling before testing Hoarder inventories.
4. Correct Lucky/AutoSmelt loot context and Silk Touch behavior.
5. Stitch override sprites and add resource-reload cache invalidation.
6. Restore tooltip Shift behavior, item/worn glint rules, dev colors, young rendering, and item-model thickness.
7. Synchronize server config and cooldown state to clients.
8. Split shared key state out of the client package and run a production dedicated-server test.

## Verification still required after fixes

Compilation and reaching a world are insufficient for this mod because many failures are silent. A parity test world should exercise every effect with two, three, and four required pieces; armor breakage; resource reload; dimension changes; death/respawn across dimensions; Silk Touch/Fortune/Looting; all Hoarder row sizes; multiplayer client/server config mismatch; baby armor stands/mobs; animated and tinted textures; and disabled/missing-texture sets.

## Build verification

The audited tree passes `gradle build` with OpenJDK 17.0.x and Fabric Loom 0.10.66. This verifies compilation, resource processing, remapping, and packaging only; the silent hook disconnections documented above require gameplay tests after they are repaired.
