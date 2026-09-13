# Beta Single Chat — Code Guide

## Identity and toolchain

- Mod ID: `betasinglechat`; display name: **Beta Single Chat**.
- All mod Java sources use `com.aksulightning.vibecode.betasinglechat` as their root package.
- Minecraft `b1.7.3`, BINY `b1.7.3+4cbd9c8:v2`, Fabric Loader `0.19.5`,
  Fabric Loom `1.17.20`, and Babric Loom Extension `1.17.4`.
- Java 17 bytecode; JDK 21 or newer runs Gradle 9.7.1.
- Removed the old initializer, demonstration mixin, assets, and interactive setup task.
  Configured real mod properties, client-only metadata, and `betasinglechat.mixins.json`.
  Retained the wrapper, Babric build configuration, and license packaging.
- Changed the project license to MIT at the user's request. `LICENSE` contains
  the MIT terms with `Copyright (c) 2026 aksulightning`; mod metadata and README
  identify MIT. Distribution JARs include the license through the existing build task.
- No StationAPI entrypoints, modern Minecraft command API, or integrated server.
  Beta singleplayer runs its world and player on the client thread.

## Chat path

`MinecraftMixin` injects into `Minecraft.tick()` immediately before the
`isWorldRemote()` call that guards vanilla chat-key handling. At this point the
game is processing a pressed LWJGL keyboard event. `SingleplayerChat` checks
the actual configured `options.chatKey.code`, a living and awake local player,
and the absence of another screen before opening vanilla `ChatScreen`.

`ChatScreenMixin` intercepts `keyPressed(char, int)` for Enter and numpad Enter
in a local world. It passes the screen's existing text to `SingleplayerChat.submit`,
closes the screen, and cancels vanilla sending. Vanilla editing, the 100-character
input limit, keyboard repeat cleanup, Escape, and chat rendering are retained.
The hook does not process multiplayer submissions.

`SingleplayerChat` rejects missing or remote worlds and mismatched player worlds,
trims input, ignores empty messages, displays ordinary messages through
`InGameHud.addChatMessage`, and routes `/` inputs to `CommandDispatcher`.

## Command system

`Command` defines `name()`, `usage()`, and `execute(context, arguments)`.
`CommandDispatcher` registers command objects by case-insensitive name, rejects
duplicate registration, and splits input on whitespace. A bare `/` or unknown
command displays the registered command names. `CommandContext` supplies the
current player, local world, chat replies, and red error messages.
Expected input errors use `CommandException` and do not escape into gameplay.

To add a command, implement `Command` in `command.commands` and register it in
`CommandDispatcher.createDefault()`. Chat and gameplay mixins need no changes
unless the new command itself requires another game hook.

- `GiveCommand`: validates the argument count, integer item ID, an actual entry in
  `Item.ITEMS`, and an integer amount from 1 to 2304 (default 1). It creates Beta
  `ItemStack(id, count, 0)` stacks respecting item limits and calls
  `PlayerInventory.addStack`. The remaining `stack.count`, rather than the return
  value, determines overflow because Beta can accept only part of a stack.
  Overflow uses `PlayerEntity.dropItem(stack, false)`; inventory is marked dirty.
- `SpawnMobCommand`: reads the actual `EntityRegistry.idToClass` through a static
  mixin accessor, matches registered names without case sensitivity, and accepts
  concrete `LivingEntity` subclasses while excluding the registry's generic
  `Mob` and `Monster` base types. It creates the entity through
  `EntityRegistry.create(String, World)`. It tries eight directions three blocks
  away at the player's feet and height offsets 0, 1, -1, 2, -2, checks Beta's
  0–128 world bounds and collision/occupancy APIs, then calls `World.spawnEntity`.
- `GodCommand`: takes no arguments and toggles the player's `GodModeState`.

## Gameplay hooks

`PlayerEntityMixin` implements `GodModeState` with a unique per-player boolean.
The state is active only in a non-remote world. It cancels
`PlayerEntity.damage(Entity, int)` at HEAD with a false result, preventing normal
damage, hurt effects, and damage-related armor loss. It also cancels
`PlayerEntity.applyDamage(int)` to protect direct calls to health/armor damage.
These are Beta methods verified against BINY mappings and game bytecode.

God mode is initially off and is not stored in NBT. A replacement player starts
with god mode off, including respawn and world reload. It does not restore health,
stop fire visuals, or teleport a player who falls into the void. Other mods that
write health directly can bypass these hooks.

## Validation and limits

Validation completed on 2026-09-13:

- `JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64 bash ./gradlew build`:
  **BUILD SUCCESSFUL**, including Java compilation, resource processing,
  `remapJar`, and `remapSourcesJar` (7 tasks executed).
- Output: `build/libs/beta-single-chat-1.0.0.jar` and the corresponding sources JAR.
- Inspected the packaged metadata and all four mixins. Their target methods,
  invocation descriptors, and registry accessor field were correctly remapped
  into Beta's intermediary namespace.
- Confirmed all 13 Java classes use the required root package and there are no
  remaining previous mod names, packages, or setup placeholders in sources,
  build configuration, or documentation.
- The existing build configuration produces Gradle deprecation warnings;
  these do not prevent the Gradle 9.7.1 build. No in-game GUI session was run.
- Rebuilt successfully after switching to MIT (5 tasks executed, 2 up-to-date).
  Confirmed the distribution JAR declares `MIT` and contains the MIT license text.

No modern-version Fabric subprojects exist in this workspace.

Manual in-game verification should cover configured chat keys, ordinary messages,
Escape/empty submission, unknown commands, `/give 1`, `/give 1 64`, invalid IDs and
amounts, full-inventory overflow, `/spawnmob pig`, `/spawnmob zombie`, invalid mobs,
blocked spawn space, both `/god` toggles, environmental/mob damage, world reload,
and multiplayer chat. Compilation alone does not verify GUI behavior in a running
Minecraft client. Chat history is local to the running session. Item metadata,
tab completion, and mob-specific habitat checks are not implemented.
