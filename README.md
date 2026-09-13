# Beta Single Chat

A client mod for Minecraft Beta 1.7.3 using Babric and BINY mappings. Adds local
singleplayer chat and commands without requiring StationAPI.

## Usage

Open chat with the configured chat key (T by default), type a message or command,
and press Enter. Escape closes chat without sending. Regular messages appear
locally as `<player name> message`. Multiplayer chat keeps its normal behavior.

| Command | Behavior |
| --- | --- |
| `/give <item ID> [amount]` | Gives an item by its numeric Beta ID. Amount defaults to 1 and must be 1–2304. Stacks respect the item's stack limit; overflow drops near the player. |
| `/spawnmob <mob name>` | Spawns a registered mob near the player. Names ignore case: `pig`, `zombie`, `PigZombie`, `wolf`, etc. Reports an error if the mob is unknown or there is no room. |
| `/god` | Toggles damage protection and prints `God mode enabled.` or `God mode disabled.` |

God mode belongs to the current player entity and is not saved. Respawning,
reopening a world, or a dimension change that replaces the player resets it.
It prevents damage; it does not heal or rescue a player from the void.
Hostile mobs still follow Beta's difficulty rules, including despawning in Peaceful.

## Build

Use JDK 21 or newer to run this project's Gradle 9.7.1 / Loom toolchain.
The mod targets Java 17, matching the existing Babric configuration.

```sh
bash ./gradlew build
```

Install `build/libs/beta-single-chat-1.0.0.jar` in a Babric Beta 1.7.3 client's
mods folder. See [Code_Guide.md](Code_Guide.md) for implementation details.

## License

MIT; see [LICENSE](LICENSE).
