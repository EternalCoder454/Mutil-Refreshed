# Mutil Refreshed

A library that other mods are built on. It does nothing on its own, and you have it because
something else asked for it, usually Tetra.

![Minecraft](https://img.shields.io/badge/minecraft-26.1.2-brightgreen.svg)
![Loader](https://img.shields.io/badge/loader-NeoForge-orange.svg)
![License](https://img.shields.io/badge/license-MIT-blue.svg)

## 📚 About

Helpers for data management, networking and GUI setup, formerly known as mgui.

Mutil Refreshed is the Minecraft 26.1.2 NeoForge port of
[mutil](https://github.com/mickelus/mutil) by Mickelus. Same mod, newer Minecraft.

## 📦 Installing

Drop `mutil-*.jar` into `mods/`. Nothing else is required.

| Thing | Value |
|---|---|
| Minecraft | 26.1.2 |
| Loader | NeoForge 26.1.2.95 or newer |
| Java | 25 |
| Mod id | `mutil` |

The mod id stays `mutil` on purpose. Dependents resolve it by id, so renaming it would break every
mod that asks for it. Only the display name is rebranded.

## 🩹 If something goes wrong

A mod that needs mutil and cannot find it fails at load with a missing dependency naming `mutil`.
Check that the jar is in `mods/` and that its version satisfies what the dependent asked for.

If you build from source, publish after every change. Tetra resolves mutil by maven coordinate, so
rebuilding without `publishToMavenLocal` leaves the consumer on the previous jar and your change
never appears.

## 📝 Credit

Written by **Mickelus**. Copyright 2018 Mikael Eriksson Vikner. MIT.

The only thing credited to this repository is the 26.1.2 NeoForge port, by **EternalHell**.
Authorship, design and every line of the original are his.

Upstream stays on the `upstream` remote. Rebasing onto a newer upstream branch is the intended way
to take future changes, not copying files.

## 💻 For developers

| File | Covers |
|---|---|
| [DEV.md](DEV.md) | building, publishing, and what each of the five packages gives a consumer |
| [PORT-STATUS.md](PORT-STATUS.md) | the 1.21.1 to 26.1.2 port, and the one behaviour risk that never surfaces as a compile error |
| [CHANGELOG.md](CHANGELOG.md) | what changed, per release |
| [PLAYTESTING.md](PLAYTESTING.md) | a checklist for testing a build |
