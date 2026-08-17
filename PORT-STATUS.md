# Mutil Refreshed, port status

Port of mutil from 1.21.1 NeoForge to 26.1.2 NeoForge. In progress. Does not compile yet.

Base is upstream branch `1.21` at `fd5e8d5`, version 7.0.0-pre.0, which was already on 1.21.1
NeoForge with ModDevGradle. That is a far shorter jump than the `1.20` branch.

## Progress

| Stage | Errors |
|---|---|
| First compile against 26.1.2 | 200 raw, 100 unique |
| After ResourceLocation to Identifier | 150 raw, 73 unique |
| After byte buf, dist and event bus fixes | 140 raw, 68 unique |
| After GuiGraphics and 2D matrix migration | 96 raw, 46 unique |
| Now | 78 raw, 37 unique |

## Done

Toolchain moved to Duty's known good set. Gradle 9.6.1, ModDevGradle 2.0.144, Java 25,
NeoForge 26.1.2.95, Parchment 1.21.11 with 2025.12.20 mappings.

Build fixes:

- `project.archivesBaseName` is gone in Gradle 9. Now `project.base.archivesName.get()`.
- Publishing pointed at a maven named by the `local_maven` environment variable, which
  upstream's Jenkins sets. Unset here, so the url became `file://null` and killed the
  configuration phase before any compile. Registered only when the variable is present.
- The wrapper jar and scripts did not launch under Gradle 9. Replaced with Duty's.

API fixes:

- `ResourceLocation` to `Identifier` across 9 files. `parse` and `fromNamespaceAndPath` kept
  their names, so it was a pure class rename.
- `FriendlyByteBuf.readResourceLocation` and `writeResourceLocation` to `readIdentifier` and
  `writeIdentifier`.
- `FMLEnvironment.dist.isClient()` to `FMLEnvironment.getDist() == Dist.CLIENT`.
- `@EventBusSubscriber` lost its `bus` attribute. There is one bus now.
- `GuiGraphics` is gone. `GuiGraphicsExtractor` replaces it and keeps `fill`, `text`,
  `centeredText`, `blit` and `textWithWordWrap`, so the drawing surface survived the rename.
- `pose()` returns `org.joml.Matrix3x2fStack` now, not a `PoseStack`. `pushPose` and `popPose`
  became `pushMatrix` and `popMatrix`. `translate` and `scale` lost their z argument.
- `drawString` became `text`.

## Left, 37 unique errors

Grouped by what the work actually is.

**Two dimensional rotation, 15 errors in `gui/hud/GuiRootHud.java`.** Five `mulPose(Quaternionf)`
calls. `Matrix3x2fStack` has no quaternion. 2D rotation is `rotate(float radians)`. Each site
needs reading rather than renaming, because a quaternion around an arbitrary axis does not
always reduce to a single 2D angle.

**Item rendering, 7 errors in `gui/GuiItem.java`.** `renderItem` moved with the render state
split.

**Tooltips, 3 errors in `effect/EffectTooltipRenderer.java`.** `renderTooltip` signature
changed and `EffectRenderingInventoryScreen` moved.

**Blaze3D, 4 errors.** `setShaderColor` and `GlStateManager.applyModelViewMatrix` are gone with
the pipeline rewrite. Colour is now part of the draw call or the pipeline.

**Blit, 2 errors in `gui/GuiTexture.java`.** `blit` takes a `RenderPipeline` as its first
argument now.

**Assorted, 6 errors.** Deserializers, `ItemHandlerWrapper`, `PacketHandler`, `ParticleHelper`,
`DataStore`. Small and unrelated to each other.

## Watch out

Dropping the z argument from `translate` changed what it means. z used to carry layer ordering
in the 3D pose. Anything that relied on z for depth now needs draw order instead. Nothing here
has been checked for that, and it will not show up as a compile error. It will show up as
something drawn behind something else.

## Rules for this repo

MIT. Keep `LICENSE` and the line `Copyright (c) 2018 Mikael Eriksson Vikner` exactly as they
are. Credit for this work is the 26.1.2 NeoForge port only, never authorship.

Standalone repo, not a fork, because MIT permits it. Tetra is the opposite case. See the brief
in `Mickelus Mods/README.md`.

`origin` still points at `mickelus/mutil` and is renamed `upstream` once the new remote exists.

## Next

Finish the 37. Then build, then load. Do not deploy to the test pack before it compiles,
and remember that mutil alone does nothing visible. It is a library. Until Tetra is ported
the only thing a deploy proves is that the jar loads.
