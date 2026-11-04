# Beta Single Chat

A client mod for Minecraft Beta 1.7.3 using Babric and BINY mappings. Adds local
singleplayer chat and commands without requiring StationAPI for example.

## Usage

Open chat with the configured chat key (T by default), type a message or command,
and press Enter. Escape closes chat without sending. Regular messages appear
locally as `<player name> message`. Multiplayer chat keeps its normal behavior.

Press Up to recall older sent messages and commands, and Down to move toward
newer ones. Moving past the newest entry restores the draft you were typing.
Recalled text can be edited and sent again. History keeps the last 100 non-empty
singleplayer submissions for the running game session, skipping consecutive
duplicates. It is shared across singleplayer worlds and clears when the game exits.
Escape discards the current draft without adding it to history.

| Command | Behavior |
| --- | --- |
| `/help [command or page]` | Lists commands six at a time, or shows usage and a description for one command. A bare `/` opens help. |
| `/give <item ID> [amount]` | Gives an item by its numeric Beta ID. Amount defaults to 1 and must be 1–2304. Stacks respect the item's stack limit; overflow drops near the player. |
| `/spawnmob <mob name or 49> [amount]` | Spawns 1–32 registered mobs nearby (default 1). Names ignore case: `pig`, `zombie`, `PigZombie`, `wolf`, etc. `monster` and `49` select Beta's generic Monster entity. Reports how many spawned if space runs out. |
| `/god` | Toggles damage protection and prints `God mode enabled.` or `God mode disabled.` |
| `/heal` | Restores all ten hearts. |
| `/time <day\|night\|ticks>` | Sets the current day's time: day = 1000, night = 13000, or a whole number from 0–23999. Keeps scheduled block updates at their existing relative delays. |
| `/weather <clear\|rain\|thunder>` | Sets Overworld weather for five minutes, then normal weather cycles resume. |
| `/pos` | Shows feet coordinates and dimension ID (0 = Overworld, -1 = Nether). |
| `/seed` | Shows the world seed. |
| `/tp <x> <y> <z>` | Teleports to feet coordinates. Supports decimals and relative coordinates, such as `/tp ~5 ~ ~-2`. |
| `/spawn` | Teleports to a safe position within four blocks of Overworld world spawn. |
| `/setspawn` | Sets Overworld world spawn at your current block. Existing bed respawn points still apply. |
| `/sethome [name]` | Saves or replaces a named home, including position, facing, and dimension. The default name is `home`; maximum 32 homes per world. |
| `/home [name]` | Teleports to a saved home in the current dimension. Defaults to `home`. |
| `/homes [page]` | Lists saved homes alphabetically, six per page, with their dimension IDs. |
| `/delhome <name>` | Deletes a saved home. |
| `/back` | Returns to the location before your last successful command teleport. Repeating it switches between the two locations. |
| `/repair` | Repairs the selected held item if it has durability. |
| `/clear [confirm]` | `/clear` requests confirmation; `/clear confirm` deletes main inventory, armor, and cursor items within 30 seconds. Inventory changes invalidate confirmation. |
| `/fly` | Toggles collision-aware flight. Use movement keys to move, jump to rise, and sneak to descend. |
| `/run` | Toggles running. Moving forward gains a 30% speed boost and extra forward momentum when jumping. |
| `/speed <value>` | Sets flight speed from 0.1–4 times the default speed. Default is 1; walking speed is unaffected. |

Homes are saved in the world's `data/betasinglechat_homes.dat` through vanilla
world saving. Names ignore case and allow 1–32 ASCII letters, digits, underscores,
or hyphens. Home names are shared across dimensions within each world; enter a
home's dimension before using `/home`. `/spawn` and `/setspawn` require the Overworld.

Teleports check destination chunks, player space, solid support, collidable entities,
and hazards (fluids, fire, cactus, and portals). Unsupported air destinations are
allowed while flight is enabled. X/Z must be within +/-30,000,000 and the player
must fit between Y 0 and 128. Mounted players and players carrying passengers
must dismount before teleporting. Failed teleports leave `/back` unchanged.
`/back` and pending clear confirmations belong to the player session and are not saved.

Flight retains normal block collisions, stops vertical movement at the world's
height limits, and prevents accumulating fall damage while flying. Disabling it
restores gravity; flight does not provide god mode. Flight and speed are not saved
and reset on player replacement, including respawn and reopening a world.

Running starts disabled. Type `/run` to enable it, then use your normal forward
key to sprint; type `/run` again to disable it. The boost pauses when moving only
backward/sideways, sneaking, swimming, climbing ladders, riding, flying, or pressing
against a wall, and resumes when forward movement is eligible again. It preserves
normal collision, gravity, and fall damage. The toggle is not saved and resets on
respawn or reopening a world. `/speed` continues to control flight alone.

God mode belongs to the current player entity and is not saved. Respawning,
reopening a world, or a dimension change that replaces the player resets it.
It prevents damage; it does not heal or rescue a player from the void.
Hostile mobs still follow Beta's difficulty rules, including despawning in Peaceful.

## Build

Use JDK 21 or newer to run this project's Gradle 9.7.1 / Loom toolchain.
The mod targets Java 17, matching the existing Babric configuration.

Install Temurin from [here](https://adoptium.net/temurin/releases/?version=21).

Temurin for Debian-like distros (one line copy&paste):

```sh
sudo apt-get update && sudo apt-get install -y curl gpg ca-certificates && sudo install -d -m 0755 /etc/apt/keyrings && curl -fsSL https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo gpg --dearmor --yes -o /etc/apt/keyrings/adoptium.gpg && echo "deb [signed-by=/etc/apt/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb $(. /etc/os-release; echo ${UBUNTU_CODENAME:-$VERSION_CODENAME}) main" | sudo tee /etc/apt/sources.list.d/adoptium.list > /dev/null && sudo apt-get update && sudo apt-get install -y temurin-21-jdk && java -version
```

Then build it:

```sh
./build.sh
```

The script uses `JAVA_HOME` when set. Otherwise it looks for JDK 21 or newer
under Linux's `/usr/lib/jvm`, falling back to Gradle's normal Java selection.
It works from any working directory and forwards additional Gradle arguments,
for example `./build.sh --offline`. The direct `bash ./gradlew build` command
also remains available.

Install `build/libs/beta-single-chat-1.0.0.jar` in a Babric Beta 1.7.3 client's
mods folder. See [Code_Guide.md](Code_Guide.md) for implementation details.

## License

MIT; see [LICENSE](LICENSE).
