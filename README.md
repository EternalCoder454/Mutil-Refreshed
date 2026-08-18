# Mutil Refreshed

A library mod. Helpers for data management, networking and GUI setup. Formerly mgui.

Mutil Refreshed is the Minecraft 26.1.2 NeoForge port of [mutil](https://github.com/mickelus/mutil)
by Mickelus. Same mod, newer Minecraft. It does nothing on its own and is installed because
something else needs it, usually Tetra.

## Credit

Written by Mickelus. Copyright 2018 Mikael Eriksson Vikner. MIT.

The only thing credited to this repository is the 26.1.2 NeoForge port, by EternalHell. Authorship, design and
every line of the original are his.

## Install

| Thing | Value |
|---|---|
| Minecraft | 26.1.2 |
| Loader | NeoForge 26.1.2.95 or newer |
| Java | 25 |
| Mod id | `mutil` |

The mod id stays `mutil` on purpose. Dependents resolve it by id, so renaming it would break
every mod that asks for it. Only the display name is rebranded.

## Build

```powershell
$env:TMP="C:\gtmp"; $env:TEMP="C:\gtmp"
.\gradlew.bat build
```

Output is `build\libs\mutil-26.1.2-7.0.0-pre.0.jar`.

## Porting notes

`PORT-STATUS.md` records what changed between 1.21.1 and 26.1.2, why, and the one behaviour
risk that does not surface as a compile error.

Upstream stays on the `upstream` remote. Rebasing onto a newer upstream branch is the intended
way to take future changes, not copying files.
