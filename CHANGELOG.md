# Changelog

Mutil Refreshed is the Minecraft 26.1.2 NeoForge port of [mutil](https://github.com/mickelus/mutil)
by Mikael Eriksson Vikner. Only the port is recorded here. Upstream's own history is in its
repository.

## 7.0.0-pre.0

First build for 26.1.2. Ported from 1.21.1, MIT throughout, as upstream is.

**This is a test build.** It has been launched and works alongside Tetra Refreshed, but the library
has never been exercised by anything else, and nothing here has seen more than an evening of play.

### Ported

* **Data stores.** A store carries a listener id, which the reload events now require. Both
  `AddServerReloadListenersEvent` and `AddClientReloadListenersEvent` sort listeners by name and
  take an identifier alongside each one, so `DataStore.getListenerId` derives one from the namespace
  and directory it already held.
* **Networking.** Each packet derives its own payload type rather than every subclass being asked
  for one. Registration moved onto `PayloadRegistrar`.
* **Storage.** Block entity and item persistence moved onto `ValueInput` and `ValueOutput`, which
  carry the registry context themselves.
* **Gui.** Screens moved onto the extract and submit pipeline, `GuiGraphics` became
  `GuiGraphicsExtractor`, and the gui transform stack is a two dimensional matrix stack where depth
  is a stratum rather than a z offset.

### Fixed

* **Clientbound packets had no client handler.** Loading failed with "Some clientbound payloads are
  missing client-side handlers", naming all 25 of Tetra's packets. The single handler overload of
  `playBidirectional` leaves the clientbound direction uncovered, so both directions take the
  handler now.
* **The library could not load on a dedicated server.** NeoForge no longer strips `@OnlyIn` from mod
  classes, so `PacketHandler` carried a reference to `Minecraft` in its own bytecode and failed to
  load, taking the whole mod with it. The client lookup moved to `ClientPacketSender`.
* **ToggleableSlot** was restored, having been dropped in the move off Forge.

### Known

* The gui toolkit has no world space drawing path, so a consumer cannot render a gui onto a block
  face. Tetra's interactive block overlay is the one thing that needed it, and it does not draw.
