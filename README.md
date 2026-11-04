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
| `/give <item ID> [amount]` | Gives an item by its numeric Beta ID. Amount defaults to 1 and must be 1–2304. Stacks respect the item's stack limit; overflow drops near the player. |
| `/spawnmob <mob name or 49>` | Spawns a registered mob near the player. Names ignore case: `pig`, `zombie`, `PigZombie`, `wolf`, etc. `/spawnmob monster` and `/spawnmob 49` spawn Beta's generic Monster entity (ID 49). Reports an error if the mob is unknown or there is no room. |
| `/god` | Toggles damage protection and prints `God mode enabled.` or `God mode disabled.` |

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
