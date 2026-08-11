<div align="center">

  <img src="banner.svg" alt="NetworksV6-drake Banner" width="920" />

# NetworksV6-Drake

**DrakesCraft's logistics, storage, routing and controlled automation addon for Slimefun.**

<p>
  <a href="https://github.com/DrakesCraft-Labs/NetworksV6-drake"><img src="https://img.shields.io/badge/GitHub-NetworksV6--Drake-181717?style=for-the-badge&logo=github" alt="GitHub"/></a>
  <img src="https://img.shields.io/badge/Java-21_FFM_Panama-F89820?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21 FFM"/>
  <img src="https://img.shields.io/badge/Rust-FFM_Accelerated-FF4500?style=for-the-badge&logo=rust&logoColor=white" alt="Rust Native"/>
  <img src="https://img.shields.io/badge/Paper-1.21.11-38BDF8?style=for-the-badge&logo=minecraft&logoColor=white" alt="Paper 1.21.11"/>
</p>

</div>

---

## What It Does

`NetworksV6-Drake` connects chests, Slimefun machines and storage components into a
single digital network. It is maintained by DrakesCraft Labs for Paper/Purpur
**1.21.11** and the Drake Slimefun API.

## Gameplay Systems


| Area | Included components |
| --- | --- |
| Network topology | Controller, bridge, storage monitor, input-only monitor, output-only monitor, cells, power nodes, capacitors, power outlet and display |
| Storage | Terminal/grid, quantum workbench, standard and nine-slot greedy blocks, quantum storage tiers and stack-aware searches |
| Transport | Import/export, standard and high-throughput grabber/pusher, vanilla grabber/pusher, filtered vacuum and purger |
| Automation | Recipe encoder, crafting blueprints, standard and advanced auto crafters, filtered advanced vacuums, and withholding variants |
| Remote work | Wireless transmitter/receiver, remote terminal, probe, configurators, crayon and rake |
| World operations | Controlled `X`/`V` block actions, monitor and administrative debugger |

Items are intentionally registered through the in-game Slimefun guide. Recipes,
power costs and research requirements remain server-owned balancing decisions.

## Drake Reliability Layer

This is not a renamed upstream jar. The Drake line adds operational safeguards
for a large, long-lived survival world:

- **Topology recovery:** startup reindexing restores valid network membership
  after a protected shutdown without retaining ghost nodes.
- **Stale-node cleanup:** replaced, broken or foreign blocks are removed from a
  network before they can be read as storage.
- **Grid anti-dupe guard:** ambiguous inventory actions such as middle-click,
  collect-to-cursor and hotbar re-add are blocked in network interfaces.
- **Atomic Quantum Storage:** insertions and withdrawals are serialized per
  storage cell; state is synchronised and marked dirty after mutations.
- **Controlled auto-crafting:** blueprint validation, output withholding and
  network reconstruction are protected from stale cache state. Advanced
  crafters process blueprint stacks atomically while charging every blueprint
  and rejecting batches whose result cannot fit in one output stack.
- **Restart safety:** pending state is persisted during shutdown and runtime
  caches are discarded only after data has been saved.
- **Data-safe SQL preparation:** an optional, write-only SQLite mirror can audit
  Quantum Storage. It never replaces Slimefun BlockStorage or restores items.

Read [the integrity guide](docs/INTEGRIDAD_DE_RED_DRAKESCRAFT.md) for the
concrete invariants and smoke tests that protect inventories.

## Persistence

Slimefun `BlockStorage` is authoritative. The default configuration is:

```yml
persistence:
  quantum:
    mode: slimefun
```

Set `mode: mirror-sql` only when a server operator explicitly wants a local
audit mirror at `plugins/NetworksV6-Drake/quantum-storage-mirror.db`. It batches
the latest state per location and remains one-way by design. It does **not** run
`/sf migrate`, delete legacy data, or create a second source of truth.

## Compatibility

- Paper or Purpur 1.21.11
- Java 21
- Slimefun Drake `11.0-Drake-1.21.11-SNAPSHOT`
- Existing DrakesCraft addons compiled against the Drake, legacy and upstream
  bridge namespaces

Do not install Gugu's `Networks` or `NetworksExpansion` jars beside this addon:
they are whole Networks forks with the same plugin identity. Useful upstream
features are ported as reviewed source changes instead.

See [upstream attribution and portability status](docs/UPSTREAM_ATTRIBUTION.md)
for the source baseline and the compatibility boundary used by this repository.

---

## Build

```bash
# Build and run the test suite
mvn clean test package
```

The resulting artifact is `target/NetworksV6-Drake-v11-SNAPSHOT.jar`. Deploy
exactly one active Networks jar, verify its SHA-256, run `save-all flush`, then
perform a full restart. Plugin reloads are not supported for network state.

## Operations and Support

This branch is the operational reference for DrakesCraft. The in-game guide
should take precedence for recipes; this repository documents compatibility,
storage and safety behavior.

- [Integrity and anti-dupe guide](docs/INTEGRIDAD_DE_RED_DRAKESCRAFT.md)
- [Gugu source portability policy](docs/PORTABILIDAD_GUGU.md)
- [CI and release process](docs/CI_AND_RELEASE.md)

When reporting an issue, include the jar version, world, coordinates, network
components, expected result and exact reproduction steps. Never attach live
backups, credentials or player data.

---

<div align="center">

**DrakesCraft Labs** · Mantenido por [**JackStar6677-1**](https://github.com/JackStar6677-1)

</div>
