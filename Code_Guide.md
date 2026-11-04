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

`Command` defines `name()`, `usage()`, `description()`, and
`execute(context, arguments)`. `SimpleCommand` is an immutable command definition
with a checked-exception-capable action, used to group related small commands.
`CommandDispatcher` registers command objects by case-insensitive name, rejects
duplicate registration, and splits input on whitespace. A bare `/` opens help;
unknown commands direct the player to `/help`. `commands()` returns an immutable
registration-order list, and `find()` performs case-insensitive lookup for help.
`CommandContext` supplies the
current player, local world, chat replies, and red error messages.
Expected input errors use `CommandException` and do not escape into gameplay.

To add a command, implement `Command` in `command.commands`, or register a
`SimpleCommand` in the appropriate command group. Register standalone commands
and groups in `CommandDispatcher.createDefault()`. Chat and gameplay mixins need
no changes unless the new command itself requires another game hook.
`Arguments` centralizes argument counts, bounded whole numbers, finite numbers,
and absolute/relative coordinates (`~`, `~5`, `~-2.5`). Relative additions that
overflow are rejected before accessing the world.

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
  The optional amount defaults to 1 and is bounded to 1–32. Each entity is placed
  independently so existing and newly spawned mobs occupy space. If space runs
  out, the command reports the actual count without rolling back successful spawns.
- `GodCommand`: takes no arguments and toggles the player's `GodModeState`.
- `HelpCommand`: `/help [command or page]` displays six command usages per page
  or one command's usage and description. It derives help from registered commands,
  including itself, and accepts command names with or without the leading slash.
- `WorldCommands`: registers `/heal`, `/time`, `/weather`, `/pos`, and `/seed`.
  Healing uses Beta's native `heal(20)` (which caps health at 20). Time accepts day
  (1000), night (13000), or 0–23999 within the current day. It checks long overflow
  and uses `World.synchronizeTimeAndUpdates` to retain pending block-update delays,
  then updates sky brightness. Weather requires dimension 0 and updates both
  raining/thundering flags and their timers through `WorldProperties`; 6000 ticks
  is five minutes at normal tick rate. `/pos` uses bounding-box feet Y and
  locale-independent decimals; `/seed` reads the native world seed.
- `TravelCommands`: registers `/tp`, `/spawn`, `/setspawn`, `/sethome`, `/home`,
  `/homes`, `/delhome`, and `/back`. Spawn commands are Overworld-only;
  `/setspawn` uses floored feet coordinates and updates world spawn, leaving bed
  respawn points alone. `/spawn` searches the exact spawn height and surface height
  in columns within a four-block square radius, trying the closest columns first.
  Homes default to `home`, names normalize with `Locale.ROOT`, and valid names
  match `[a-z0-9_-]{1,32}`. There are at most 32 homes per world, with replacement
  allowed at the limit. `/homes [page]` sorts names and displays six per page.
- `PlayerCommands`: registers `/repair`, `/fly`, `/run`, and `/speed`, plus `ClearCommand`.
  Repair applies only to the selected held damageable item, resets its damage,
  and marks inventory dirty. Speed is a finite multiplier from 0.1 to 4, default 1,
  and affects flight alone. `/fly` toggles `FlightState`, refusing activation while
  mounted and clearing velocity/fall distance when toggled.
  `/run` takes no arguments and toggles `RunningState` with enabled/disabled chat
  feedback. Run is independent of the flight speed setting and needs no new keybind.
- `ClearCommand`: `/clear` snapshots the main inventory, armor, and cursor stack
  and requests `/clear confirm` within 30 seconds. Confirmation is per player,
  uses monotonic time, and is consumed when attempted. Item ID, count, damage, and
  slot positions must still match the snapshot (`ItemStack.areEqual`); otherwise
  the command requires a fresh confirmation. Only a successful confirmation clears
  slots, clears the cursor, and marks inventory dirty. Crafting-grid/container
  inventories are not part of this command. Empty inventory needs no confirmation.

## Teleports and saved homes

`TeleportPosition` stores feet coordinates, yaw, pitch, and dimension. It validates
finite numbers and X/Z within +/-30,000,000, with feet Y in [0,128). Teleportation
also checks the player's full height against the ceiling. Destinations must be in
the current dimension; cross-dimension travel is left to normal portals.

`Teleportation` loads a 3x3 group of destination chunks before collision checks.
It constructs a candidate player box without moving the player, rejects solid
collisions and collidable entities, and checks the body and surface underfoot
for fluids, fire, cactus, and portals. It requires support within 0.1 blocks below
the feet unless flight is enabled. Players with vehicles/passengers are rejected.
After validation, it clears the camera offset and uses Beta's
`setPositionAndAnglesKeepPrevAngles`, whose Y argument is feet height and which
adds `standingEyeHeight` itself. It clears velocity/fall distance and calls
`World.updateEntity(player, false)` to update chunk membership without ticking the
player. A weak-key per-player map records the previous position only after a
successful teleport. `/back` swaps between the last two locations; failed attempts
preserve the previous location. Replacing the player resets this transient state.

`HomeState` extends vanilla `PersistentState` and uses state ID
`betasinglechat_homes`, producing `data/betasinglechat_homes.dat` inside each world.
It has the public `(String)` constructor needed by vanilla's reflective loading.
Home positions and dimensions are stored in a `Homes` NBT list. Changes mark the
state dirty and save through normal world saving, including save-and-quit and
dimension transitions. Beta constructs a new saved-data manager when changing
dimensions and saves the outgoing world before using the new one. Home names are
shared across dimensions within a world, and `/home` requires the saved dimension.
No global home cache is used, so worlds with matching seeds/names remain separate.
Loading replaces in-memory entries, skips invalid names/positions, and enforces
the home count limit. Callers receive immutable map snapshots.

## Gameplay hooks

`PlayerEntityMixin` implements `GodModeState` with a unique per-player boolean.
The state is active only in a non-remote world. It cancels
`PlayerEntity.damage(Entity, int)` at HEAD with a false result, preventing normal
damage, hurt effects, and damage-related armor loss. It also cancels
`PlayerEntity.applyDamage(int)` to protect direct calls to health/armor damage.
These are Beta methods verified against BINY mappings and game bytecode.

The same mixin extends `LivingEntity` to access inherited movement/fall fields
and implements `FlightState` with unique flight-enabled and speed fields.
Its cancellable `PlayerEntity.travel(float, float)` injection handles only a
living, unmounted local `ClientPlayerEntity` in a non-remote world with flight on.
It derives horizontal motion from the current input and yaw, normalizes diagonal
input, and uses jump/sneak for ascent/descent. Default horizontal/vertical speed
is 0.35 blocks per tick times the configured multiplier. Sneak retains vanilla's
horizontal input slowdown. Vertical movement stays within Y 0–128. `Entity.move`
retains collision handling; velocity and fall distance are cleared around movement
to prevent gravity, drift, and accumulated fall damage while flying. Vanilla
releases movement keys when opening a screen. Disabling flight restores normal
travel/gravity. Other damage remains governed by vanilla or the separate god mode.
Flight is initially off, speed starts at 1, and neither is saved to NBT; player
replacement (respawn/reload) resets them. Portal travel that reuses the player
retains its flight settings.

`PlayerEntityMixin` also implements `RunningState` with a unique, initially false
per-player toggle. Its sprint eligibility check requires a living local
`ClientPlayerEntity` in a non-remote world, forward input of at least 0.8, and no
sneaking, sleeping, vehicle, horizontal collision, water, lava, ladder, or flight.
The toggle remains enabled while sprinting is temporarily ineligible and resumes
the boost when eligible. It is not saved to NBT, resets when the player is replaced,
and survives normal portal travel that reuses the player.

`LivingEntityMixin` modifies only the acceleration argument of the third
`LivingEntity.moveNonSolid(float,float,float)` invocation in `travel(float,float)`:
Beta's normal ground/air branch follows separate water and lava calls. Eligible
players receive a 1.3 multiplier without overwriting velocity or changing vanilla
friction, collision, gravity, or fall handling. Scaling raw movement inputs would
be defeated by Beta's input normalization. The mixin leaves other entities and
ineligible players untouched. A RETURN injection into `PlayerEntity.jump()` adds
0.2 blocks/tick of horizontal velocity along the player's yaw while sprinting,
preserving the vanilla vertical jump and jump statistics. Flight cancels vanilla
travel as before, and both run hooks explicitly exclude flight. This supplies
command-controlled sprint movement and sprint jumps; hunger mechanics, sprint
key/double-tap controls, camera FOV effects, and combat knockback are not added.

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

Command expansion validation on 2026-11-04:

- Registered 21 commands: the original three plus 18 new commands. Documented
  syntax, limits, persistence, dimension restrictions, flight, and confirmation
  behavior in README and this guide.
- Added JUnit 4.13.2 as a test-only dependency and configured the existing Gradle
  test task to use JUnit. Four `ArgumentsTest` cases cover absolute/relative
  coordinates, malformed/non-finite values, relative overflow, integer bounds,
  integer overflow, and missing/excess arguments. Five `HomeStateTest` cases cover
  NBT round-trips with fractional feet positions, facing and multiple dimensions,
  independent world states, dirty/deletion behavior, invalid saved entries,
  replacement-on-load, load limits, immutable snapshots, and position validation.
- `JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64 bash ./gradlew check`:
  **BUILD SUCCESSFUL**, with all nine tests passing. Existing toolchain/Gradle
  deprecation warnings remain. No other test commands or build tasks were run
  for this change. No in-game GUI session was run.

Running command validation on 2026-11-04:

- Added `/run` to the existing player command group and automatic help listing;
  the dispatcher now registers 22 commands. Added `RunningState`, the normal-travel
  acceleration hook, the sprint-jump hook, and `LivingEntityMixin` in the client
  mixin configuration. Updated README with usage, eligibility, and reset behavior.
- Inspected the mapped Beta bytecode to confirm the three `moveNonSolid(FFF)V`
  call sites in `LivingEntity.travel`, the normal branch's acceleration argument,
  and `PlayerEntity.jump()` calling the vanilla jump before recording its stat.
- `JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64 bash ./gradlew check`:
  **BUILD SUCCESSFUL** with all nine existing regression tests passing. These
  tests cover command inputs and home data; they do not exercise sprint movement
  or mixin application. Existing Gradle deprecation warnings remain. No in-game
  session or other build/test command was run.
- In-game verification should compare forward movement with run off/on, toggle
  run repeatedly to check that speed does not stack, check forward diagonals and
  sprint jumps at different facing angles, and verify normal backward/sideways
  motion, sneak, water/lava, ladders, riding, wall collisions, fall damage, and
  `/fly`/`/speed` interactions. Check no-input hovering in flight with run enabled,
  respawn/reload resetting run, portal travel retaining it, `/help run`, invalid
  `/run` arguments, and unchanged multiplayer/mob movement.

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

In-game command verification should cover every help page and individual command
help, invalid/extra arguments, healing, both time presets and numeric boundaries,
scheduled updates after clock changes, all weather modes, Nether restrictions,
position/seed output, positive/negative/relative teleports, chunk boundaries and
distant destinations, slabs and blocked/hazardous destinations, mounted players,
spawn changes and existing beds, home replacement/deletion/limits, save-and-reload,
different worlds and both dimensions, failed teleports preserving `/back`, and
repeated `/back`. Also cover held-item repair and item metadata, fresh/expired/
changed/reused clear confirmations, armor/cursor deletion, flight toggle, remapped
movement/jump/sneak keys, diagonal movement, collisions and height limits, flight
speed boundaries and non-finite values, falls after disabling flight, respawn/
reload, and mob amounts with partial spawning when space runs out. Unit tests do
not exercise runtime mixin application, rendered weather, or player movement.
