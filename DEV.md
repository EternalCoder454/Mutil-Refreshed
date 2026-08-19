# Developer guide

Building mutil, and what its five packages give a consumer. For the port itself see
[PORT-STATUS.md](PORT-STATUS.md), and for what a player notices see [CHANGELOG.md](CHANGELOG.md).

Mutil is a library. It does nothing on its own and is installed because something else needs it,
which is almost always Tetra. That shapes everything below: the honest test of a change here is
whether a consumer still builds and still loads, not whether this project does.

## Building

Gradle 9.7.0, ModDevGradle 2.0.144, Java 25, NeoForge 26.1.2.95, Parchment 1.21.11 with 2025.12.20
mappings.

```powershell
$env:TMP="C:\gtmp"; $env:TEMP="C:\gtmp"
.\gradlew.bat build
```

Output is `build\libs\mutil-26.1.2-7.0.0-pre.0.jar`.

**Publish after every change that a consumer will pick up.** Tetra resolves mutil by maven
coordinate, so rebuilding this without republishing leaves Tetra compiling against the previous jar
and the change simply does not appear:

```bash
./gradlew.bat publishToMavenLocal
```

That puts `se.mickelus.mutil:mutil:26.1.2-7.0.0-pre.0` in mavenLocal, which is the coordinate
Tetra's `mutil_version` names.

## The packages

| Package | Classes | What it is |
|---|---|---|
| `gui` | 30 | the gui toolkit, an element tree drawn onto the extract and submit pipeline |
| `util` | 10 | small helpers, optionals, casts, inventory streams, particles, rotation, resource handlers |
| `data` | 4 | datapack backed stores that reload and sync themselves |
| `network` | 4 | packets and the channel that carries them |
| `scheduling` | 3 | run something later, on the right side |
| `effect` | 1 | tooltip rendering for mob effects |

### data

`DataStore` reads one directory of json under one namespace and hands back parsed objects. Its
constructor takes the gson, the namespace, the directory, the class to parse into, and a
`DataDistributor` for syncing to clients.

## What is still owed

**Three `this-escape` warnings**, in `GuiButton`, `GuiItem` and `GuiText`. Each calls an
overridable method from its own constructor, so a subclass sees itself half built. Nothing subclasses
those three today, which is why it has never bitten.

**`getDataIn` scans the whole store per call.** Seven call sites in Tetra, none per frame, so it
has not mattered. It would matter if one moved into a loop.

## Surviving bad input

Everything below is the same shape: mutil does work for another mod, that work throws, and the
failure escapes into a reload or a tick loop and takes far more with it than the thing that was
wrong.

**One unparseable file no longer empties the store.** `parseData` used to collect every file in a
single stream, so a parse failure threw out of the collect and left the store with nothing in it. A
malformed improvement shipped by one Tetra addon meant every improvement in the game was missing,
and the datapack reload died on the way into a world without naming the file. Each file is parsed on
its own now, and a failure is logged with its name and dropped.

**`MergingDataStore` overrides `parseData` and needed the same guard separately.** It is the base
for modules, schematics, materials and crafting effects, which are the four stores an addon is most
likely to extend and the four with the most files, so it was the half that mattered more.

**A synced store is guarded too.** `loadFromPacket` had the same unguarded collect on the path a
client takes when a server sends it a store, where the symptom would have been a client missing
content the server has.

**A reload listener that throws no longer stops the ones after it.** They ran in a `forEach`, so the
first failure ended the reload, and a fault in one feature took out features unrelated to it.

**A duplicate id is a warning rather than a throw.** It threw `IllegalStateException`, which the
catch below it does not name, so it escaped `prepare` and killed the whole store rather than the
one file.

**A scheduled task that throws is dropped rather than retried forever.** The queue removed a task
after running it, so one that threw was never removed and threw again on every tick after that.

**`getSources` is cached per pack.** It streamed every mod file in the pack once per resource read,
to answer a question whose answer only depends on which pack the file came from. With Tetra's
thousand or so data files, twice per load for client and server, that was a lot of passes over the
same list.

`MergingDataStore` is the one to use when several mods contribute to the same logical set. Rather
than the last file winning, entries with the same name are merged, which is how Tetra lets an addon
extend a schematic it did not write. A file that sets `"replace": false` merges into what is already
there instead of replacing it.

**A store carries a listener id.** Both `AddServerReloadListenersEvent` and
`AddClientReloadListenersEvent` sort listeners by name and want an identifier alongside each one, so
`getListenerId` derives one from the namespace and directory the store already held. A new store
needs no extra work here, but a store registered without going through the usual path will not sort.

### network

`PacketHandler` takes a namespace, a channel id and a protocol version. Register each packet class
with a supplier, then register the handler itself against `RegisterPayloadHandlersEvent`.

```java
packetHandler.registerPacket(MyPacket.class, MyPacket::new);
```

Sending is `sendTo`, `sendToAllPlayers`, `sendToAllPlayersNear` in two forms, and `sendToServer`.

**Each packet derives its own payload type** rather than every subclass being asked to supply one.
Subclass `AbstractPacket`, or `BlockPosPacket` when the packet is about a position.

**Client to server sending lives in `ClientPacketSender`, and that is deliberate.** NeoForge no
longer strips `@OnlyIn` from mod classes, so a common class that so much as names `Minecraft` in its
bytecode fails to load on a dedicated server and takes the whole mod down with it. `PacketHandler`
did exactly that once and could not load at all. Keep client only lookups behind their own class.

**Register both directions.** The single handler overload of `playBidirectional` leaves the
clientbound direction uncovered, which fails at load with "Some clientbound payloads are missing
client-side handlers" and names every packet the consumer registered.

### gui

An element tree. `GuiElement` is the base, `GuiRoot` is what a screen attaches, and the rest are
leaves and containers: `GuiString` and its small and outlined variants, `GuiTexture`, `GuiRect`,
`GuiItem`, `GuiButton`, `GuiClickable`, `ScrollBarGui`, `ClipRectGui`. `GuiAlignment` and
`GuiAttachment` place them. Subpackages hold animation, a hud layer and some implementations.

Two things about this package are load bearing.

**Depth is a stratum, not a z offset.** The transform stack is `org.joml.Matrix3x2fStack` now, so
`translate` and `scale` have no z argument and `pushPose` and `popPose` are `pushMatrix` and
`popMatrix`. Anything that used to lean on z for layering needs draw order instead. This does not
show up as a compile error. It shows up as something drawn behind something else.

**The per frame paths use indexed loops, not streams.** Every element draws its children, so
`drawChildren`, `updateFocusState` and `getTooltipLines` run once per element per frame, and a
stream there allocates a pipeline, a spliterator and a lambda every time. The duplication between
`GuiElement` and the classes overriding those methods is intentional. Undoing it puts the cost back.

**Tinting goes through the draw call.** `RenderSystem.setShaderColor` is gone with the Blaze3D
rewrite, so `blit` carries an ARGB colour itself. Same result, and it does not leave global state set
for whatever draws next.

### scheduling

`ClientScheduler` and `ServerScheduler` over a shared `AbstractScheduler`, for running something a
number of ticks from now on the correct side.

## Checking

There is no test suite. The checks that catch real problems here live one directory up:

```bash
python ../../tools/check-mixin-targets.py "Mickelus Mods/Mutil Refreshed"
python ../../tools/check-writing-rules.py DEV.md
```

Then the one that actually matters: publish, build Tetra against it, and launch. A compiling
library says nothing about whether its consumer loads, and every failure in this port so far
surfaced only on launch. See `Tetra Refreshed/tools/run.sh`.

Work through [PLAYTESTING.md](PLAYTESTING.md) before calling a build good.

## Repository rules

1. Minecraft 26.1.2, NeoForge only. Java 25.
2. MIT. Keep `LICENSE` and the line `Copyright (c) 2018 Mikael Eriksson Vikner` exactly as they are.
3. Credit Mikael Eriksson Vikner as author. Credit EternalHell for the 26.1.2 port only, never as
   author.
4. Standalone repo rather than a fork, because MIT permits it. Tetra is the opposite case, and the
   brief in `Mickelus Mods/README.md` explains why.
5. `upstream` stays pointed at `mickelus/mutil`. Take future changes by rebasing onto a newer
   upstream branch rather than copying files across.
6. Fixes that belong in mutil go in mutil, not around it in the consumer. Several have already come
   up that way, and each was invisible until Tetra became the first real consumer.
7. Writing rules: run `check-writing-rules.py --rules` from [mc-tools](https://github.com/EternalCoder454/mc-tools), checked out at `Minecraft/tools`. Nothing else states them.
