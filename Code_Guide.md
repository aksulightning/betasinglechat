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

`ChatScreenMixin` intercepts `keyPressed(char, int)` for Up, Down, Enter, and
numpad Enter in a local world. Enter passes the screen's existing text to
`SingleplayerChat.submit`, closes the screen, and cancels vanilla sending.
Up recalls older submissions; Down recalls newer submissions and restores the
original draft after the newest entry. Each screen has its own history index and
draft, so reopening chat starts at the newest end. Navigation stops at either end
without wrapping. Recalled text remains editable through vanilla input handling.
Vanilla editing, the 100-character input limit, keyboard repeat cleanup, Escape,
and chat rendering are retained. The hook does not process multiplayer input.

`SingleplayerChat` rejects missing or remote worlds and mismatched player worlds,
trims input, ignores empty messages, records submissions in `ChatHistory`, displays
ordinary messages through `InGameHud.addChatMessage`, and routes `/` inputs to
`CommandDispatcher`. Commands are recorded before execution so invalid commands
can also be recalled and corrected. Replies and canceled drafts are not recorded.

`ChatHistory` retains at most 100 sent messages and commands, dropping the oldest
entry when full and skipping consecutive duplicates. `SingleplayerChat` owns one
static history for the running game session, shared across singleplayer worlds.
It is not written to disk and does not collect multiplayer messages. Per-screen
navigation and draft fields in `ChatScreenMixin` use `@Unique` to avoid collisions.

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
  `Mob` base type. Beta's `MonsterEntity` is concrete and registered as `Monster`
  with raw ID 49, so `/spawnmob monster` (case-insensitive) and `/spawnmob 49`
  both resolve to that entry. The numeric `49` alias is handled before name lookup;
  other numeric aliases are not added. It creates the entity through
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

`build.sh` is an executable Bash wrapper for `bash ./gradlew build`. It changes
to the project directory, forwards arguments unchanged, and uses `exec` to preserve
Gradle's exit status. It respects an explicit `JAVA_HOME`; when unset, it searches
Linux's `/usr/lib/jvm` for a JDK with Java and javac executables and a compiler
major version of at least 21. If none is found, the Gradle wrapper selects Java
normally. This supports the local environment where the default Java is 8.

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
- Rebuilt successfully after adding the `49`/`monster` spawn support using the
  same JDK 21 `bash ./gradlew build` command (5 tasks executed, 2 up-to-date).
  Confirmed in mapped Beta bytecode that ID 49 registers the concrete
  `MonsterEntity(World)` class as `Monster`, with a vanilla inherited renderer.
  In-game spawning was not tested.
- Executed `build.sh --offline` from `/tmp` without setting `JAVA_HOME`:
  **BUILD SUCCESSFUL** (7 tasks up-to-date). This verified executable permissions,
  automatic JDK selection, project-directory resolution, and argument forwarding.

No modern-version Fabric subprojects exist in this workspace.

Sent-message recall validation on 2026-11-04:

- `JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64 bash ./gradlew check`:
  **BUILD SUCCESSFUL**, including compilation of the history storage and updated
  chat mixin. The initial sandboxed invocation could not write the Gradle wrapper
  lock outside the workspace; the same command succeeded with cache access.
- Gradle reported no test sources; this confirms compilation rather than
  automated keyboard behavior coverage. Existing Gradle deprecation warnings
  remain. No in-game GUI session was run.

Manual in-game verification should cover configured chat keys, ordinary messages,
Escape/empty submission, unknown commands, `/give 1`, `/give 1 64`, invalid IDs and
amounts, full-inventory overflow, `/spawnmob pig`, `/spawnmob zombie`,
`/spawnmob monster`, `/spawnmob 49`, invalid mobs,
blocked spawn space, both `/god` toggles, environmental/mob damage, world reload,
and multiplayer chat. Compilation alone does not verify GUI behavior in a running
Minecraft client. Sent-message recall should also be checked with empty history,
multiple messages and commands, repeated identical submissions, both history
boundaries, draft restoration (including after editing the restored draft), editing
and resending recalled commands, Escape, reopening chat, and switching worlds.
Verify that Up/Down retain vanilla behavior in multiplayer. Chat history is local
to the running session. Item metadata,
tab completion, and mob-specific habitat checks are not implemented.
