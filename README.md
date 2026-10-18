# Havoc Client 26.2

Utility and PvP client for **Minecraft 26.2 on Fabric**.

GPL-3.0 fork of the [Combatant Client](https://github.com/pivosos2007/combatant-client) by pivosos2007 — rebranded, package renamed to `havoc.*`, jar renamed to `havoc-client-*.jar`.

## Features

Full module set across five categories (90+ modules):

**Combat** — KillAura, AutoCrystal, AutoAnchor, AutoBed, AutoAttack, AutoBow, AutoTrap, AutoWeb, AimAssist, AttributeSwap, AntiBot, Backtrack, Criticals, ElytraTarget, Hitbox, HoleFill, KTLeave, MaceKill, ProjectilePuncher, PvpCooldowns, Reach, RubberHand, SelfTrap, SpearAssist, Surround, TPSSync, TargetPearl, AutoClicker

**Movement** — Flight, Speed, Sprint, Step, Strafe, Timer, Velocity, Phase, Parkour, NoFall, NoSlow, NoPush, NoStun, SafeWalk, AirJump, AntiVoid, AutoDodge, AutoSneak, AutoWalk, AvoidHazards, ElytraFly, ElytraHelper, Freeze, HoleSnap, InventoryMove, ReverseStep, SuperFirework, TargetStrafe, WaterSpeed, WindJump

**Player** — Scaffold, Blink, ChestStealer, AutoArmor, AutoTool, AutoMend, AutoFish, AutoMount, AutoPotion, AutoReplenish, AirPlace, AntiHunger, ChorusExploit, ClickPearl, FakeLag, ItemScroller, LiquidInteract, NoDelay, NoInteract, NoRotate, Offhand, PingSpoof, PortalGodMode, PortalScreens, ShitDropper, XCarry

**Visuals** — ESP, Chams, Tracers, NameTags, Freecam, FreeLook, Zoom, FullBright, Crosshair, HoleESP, BlockESP, BedwarsESP, DropESP, TargetESP, SoundESP, HitEffect, KillEffect, LogoutSpots, DamageTint, MotionBlur, PostFX, Predictions, Trails, TotemFX, SeeInvisibles, JumpCircles, ReimaginedVisual, TazikHat, WorldParticles, WorldTweaks, ViewModel, FovControl, CameraClip, BlockHighlight, AspectRatio, NoRender

**Misc** — ClickGui, AntiAFK, AutoBreed, AutoReconnect, AutoRespawn, BetterMinecraft, DefineTarget, FakePlayer, HitSounds, MessageFilter, NameProtect, NoSound, Panic, PopCounter, VisualRange

Plus: custom ClickGUI, HUD editor, theme system, config profiles (`.cbcfg`), client command system, scripting infrastructure, addon API, optional integrations for Sodium / Iris / Xaero / ViaFabricPlus / ImmediatelyFast / EntityCulling / HMI.

## Build on GitHub (recommended)

Two workflows under `.github/workflows/`:

- **`build.yml`** — fires on every push / PR / manual dispatch. Compiles on Ubuntu with Java 25 and uploads `build/libs/havoc-client-1.0.0.jar` as a run artifact. Grab it from **Actions** → run → **Artifacts**.
- **`release.yml`** — fires on tag push (`v*`) or manual dispatch. Builds the jar and attaches it to a GitHub Release.

### Push and build

```bash
unzip havoc-client-1.0.0-source.zip
cd havoc-client
git init
git add -A
git commit -m "havoc: initial"
git branch -M main
git remote add origin https://github.com/<you>/havoc-client.git
git push -u origin main
```

The `Build` workflow fires on push. Open **Actions → Build → latest run → Artifacts** and download `havoc-client-1.0.0.jar`.

### Cut a release

```bash
git tag v1.0.0
git push origin v1.0.0
```

The `Release` workflow builds the jar and attaches it to `https://github.com/<you>/havoc-client/releases/tag/v1.0.0`.

### Build locally

```bash
./gradlew build
# -> build/libs/havoc-client-1.0.0.jar
```

Java 25 required. Loom 1.17.12. First build pulls Minecraft 26.2 mappings and Fabric API dependencies from Fabric / Modrinth / ViaVersion / JitPack / Xaero mavens.

## Install (runtime)

- Minecraft 26.2
- Fabric Loader 0.19.3
- Fabric API `0.152.2+26.2`
- Java 25
- Sodium `0.9.2+mc26.2`

Drop `havoc-client-1.0.0.jar` into `.minecraft/mods/` alongside Fabric API and Sodium.

## Guides

- [`CONFIGS_AND_HUD.md`](CONFIGS_AND_HUD.md) — config profiles and HUD setup
- [`CLIENT_COMMANDS.md`](CLIENT_COMMANDS.md) — in-client command reference
- [`BUILDING_GUIDELINES.md`](BUILDING_GUIDELINES.md) — build options and source sets
- [`SHADERPACK_PATCHES.md`](SHADERPACK_PATCHES.md), [`SOUND_SYSTEM.md`](SOUND_SYSTEM.md)
- [`CREDITS.md`](CREDITS.md), [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md)

## License

GPL-3.0-only. Base code copyright pivosos2007 (Combatant Client). This fork preserves the license and the credits — see [`LICENSE`](LICENSE) and [`CREDITS.md`](CREDITS.md). Derivative works must stay GPL-3.0 and distribute source.
