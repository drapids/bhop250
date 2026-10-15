# BHOP250

Source-engine movement for Minecraft (Fabric). It brings over SurfsUp / CS:GO style **air-strafing**, **perfect bunny hops** that keep your momentum, and a minimal **colour-coded speedometer** in units per second.

It's built for fun rather than for CS 1.6 accuracy. Normal movement is 250–325 UPS, and with clean strafing you can chain past **2000 UPS** (about 34.5 blocks/s).

| | |
|---|---|
| Minecraft | 26.3 |
| Loader | Fabric Loader ≥ 0.19.5 + Fabric API |
| Java | 25 |
| Side | Client only (works on vanilla servers, see [Multiplayer](#multiplayer)) |
| Optional | [Cloth Config](https://modrinth.com/mod/cloth-config) (in-game settings screen), [Mod Menu](https://modrinth.com/mod/modmenu) (settings button) |

## How to bhop

1. Sprint and hold **jump**. With Auto Bhop on, you re-jump on the exact tick you land, so you lose no speed.
2. In the air, **let go of W**, hold **A**, and turn your mouse smoothly **left**. Then hold **D** and turn **right**. Switch sides once per hop.
3. Turn smoothly rather than flicking. The faster you're going, the slower you need to turn: about 180°/s at 500 UPS and about 60°/s at 1500 UPS.
4. Holding W in the air, or pressing the key opposite to your motion, doesn't add speed. Touching the ground outside the perfect-hop window applies friction.

The speedometer is **green** while you're gaining speed, **red** while you're losing it, and **white** at normal movement speed or when holding steady.

## Controls

| Key | Default | Action |
|---|---|---|
| Open BHOP250 Config | `O` | Opens the Cloth Config screen. Without Cloth Config it tells you where the JSON file is. |
| Toggle Source Movement | unbound | Switches between Source and vanilla physics. |

Both keys can be rebound under *Options → Controls → BHOP250*.

## Configuration

Settings live in `config/bhop250.json`. With Cloth Config installed, press `O` or use the Mod Menu button to edit them in game. Movement values use Source units and match the `sv_*` cvars.

| Option | Default | Source equivalent | What it does |
|---|---|---|---|
| `airAccelerate` | 100 | `sv_airaccelerate` | How sharply you can redirect in the air. High values are forgiving but stop you instantly if you press against your motion. |
| `airSpeedCap` | 80 | `sv_air_max_wishspeed` | **The main strafe-gain knob.** A perfect strafe tick adds cap² to speed². |
| `groundFriction` | 5.2 | `sv_friction` | The friction penalty for every tick on the ground outside the perfect-hop window. |
| `perfectHopWindowTicks` | 2 | — | Ground ticks after landing with no friction, so a slightly late jump still counts (1 tick = 50 ms). |
| `hopSpeedLossPercent` | 0 | — | Optional per-hop speed tax. It never drops you below normal ground speed. |
| `autoBhop` | true | `sv_autobunnyhopping` | Holding jump re-jumps on the landing tick. |
| `groundAccelerate` | 10 | `sv_accelerate` | Ground acceleration. |
| `stopSpeed` | 80 | `sv_stopspeed` | Friction acts as if you were at least this fast, so you stop crisply. |
| `baseGroundSpeed` | 250 | `sv_maxspeed` | Walk speed in UPS at the vanilla speed attribute. Sprinting is ×1.3, and Speed/Slowness potions scale it too. |
| `maxSpeed` | 3500 | `sv_maxvelocity` | Hard horizontal speed cap. |
| `sprintJumpBoost` | false | — | Keeps vanilla's extra push on each sprint jump. Off means speed only comes from strafing. |
| `hudEnabled`, `hudAnchor`, `hudOffsetX/Y`, `hudScale`, `hudShowUnits` | top-right, 4/4 px, 150 % | — | Speedometer placement and size. |
| `hudAvoidEffectIcons` | true | — | In the top-right corner, moves below the potion icons when they're showing. |
| `hudBaselineSpeed` | 330 | — | At or below this the text stays white (vanilla sprint is 325). |
| `hudColorSensitivity` | 0.5 | — | Smoothed UPS change per tick needed to turn green or red. |

### Presets

Minecraft runs at 20 ticks per second and Source at 64–128, so the air cap is scaled up to give a similar feel. Here's how long it takes to reach 2000 UPS with *perfect* strafing (expect roughly double that for a human):

| Setting | Default (SurfsUp-ish) | CS:GO-like | Bhop server | Casual |
|---|---|---|---|---|
| `airAccelerate` | 100 | 12 | 1000 | 150 |
| `airSpeedCap` | 80 | 54 | 80 | 120 |
| `groundFriction` | 5.2 | 5.2 | 4 | 4 |
| `groundAccelerate` | 10 | 5.5 | 10 | 10 |
| `perfectHopWindowTicks` | 2 | 0 | 1 | 4 |
| `autoBhop` | true | false | true | true |
| Time to 2000 UPS (perfect) | ~30 s | ~67 s | ~30 s | ~14 s |

## How it works

Units: **250 UPS = 4.317 blocks/s** (vanilla walking speed), so 1 block ≈ 57.9 units.

`LivingEntityMixin` hooks vanilla's per-tick movement (`LivingEntity#travel`) for the local player only:

1. **`moveRelative` is replaced.** Inside `handleRelativeFrictionAndCalculateMovement`, the vanilla input acceleration becomes Source's `Friction` + `Accelerate` on the ground, or `AirAccelerate` in the air (see `SourceMovement`). The wish direction comes from your WASD input rotated by your camera yaw. Air acceleration only caps the velocity *projected onto* that direction, so holding A/D while turning keeps adding speed sideways and steers your velocity around.
2. **Vanilla friction and air drag are undone.** After vanilla moves the player and multiplies horizontal velocity by its friction (×0.546 on the ground, ×0.91 in the air), the mixin restores the post-move horizontal velocity. Gravity, the jump arc, collisions, step-up, cobwebs and soul sand stay vanilla.
3. **Perfect bunny hops.** Minecraft jumps *before* it moves on a given tick. When you land with jump held, that tick is flagged as an air tick, so no friction is applied and all momentum carries into the next hop. Vanilla's sprint-jump boost is removed by default so hops don't snowball speed by themselves.

Creative flight, spectator mode, swimming, lava, elytra, ladders and vehicles always use vanilla physics.

## Multiplayer

Minecraft movement is client-authoritative, so BHOP250 is **client-only** and nothing is installed on the server. On a vanilla server the "moved too quickly" check allows about 10 blocks per tick, and even `maxSpeed` 3500 UPS is only about 3 blocks per tick. **Anti-cheat plugins will likely flag or rubber-band you.** Only use it where the server owner is fine with it.

## Building

Requires JDK 25.

```sh
./gradlew build      # jar in build/libs/bhop250-<version>.jar
./gradlew test       # physics + speedometer unit tests (pure Java)
./gradlew runClient  # dev client (includes Cloth Config + Mod Menu)
```

Dependency versions are in `gradle.properties`. `cloth_config_version` uses a `26.3.+` range because Cloth publishes CI build numbers. Pin it to the exact version Gradle resolves.

## Project layout

```
src/main/java/com/bhop250/
├─ Bhop250Client.java            client entrypoint: config, keybinds, HUD registration
├─ mixin/LivingEntityMixin.java  hooks vanilla travel/jump for the local player
├─ movement/SourceMovement.java  pure Source math: Friction, Accelerate, AirAccelerate
├─ movement/MovementController.java  MC <-> Source glue (wish direction, units, surfaces)
├─ hud/SpeedometerHud.java       UPS text renderer (Fabric HudElement)
├─ hud/SpeedTracker.java         per-tick speed + green/red/white colour logic
├─ config/Bhop250Config.java     Gson config (config/bhop250.json)
├─ config/Bhop250ConfigScreen.java  Cloth Config screen
└─ compat/ModMenuIntegration.java   Mod Menu button
```

## License

MIT, see [LICENSE](LICENSE).
