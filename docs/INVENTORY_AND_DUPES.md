# Inventarios, dupes y bugs conocidos (upstream + Chagui)

Referencias auditadas: [Sefiraat/Networks#229](https://github.com/Sefiraat/Networks/issues/229), [#230](https://github.com/Sefiraat/Networks/issues/230), [#208](https://github.com/Sefiraat/Networks/issues/208), [#233](https://github.com/Sefiraat/Networks/issues/233), fork Chagui / Drake monorepo.

## Mitigaciones en NetworksV6-drake

| Issue / vector | Descripción | Fix Drake |
|---|---|---|
| **#229** | Pico/shovel explosivo, wither, dripleaf, Android deja nodo NTW huérfano | `ExplosiveToolListener` protege todas las NTW; `SyncListener` + `EntityChangeBlock`; `NetworkIntegrity.purgeGhostMembership` |
| **#230** | Celda rota indirectamente → grid trata otro inventario como celda | `NetworkIntegrity.pruneStaleLocations` en `getCellMenus()`; saneo al abrir grid |
| **#208** | Retiro por grid sin abrir quantum → Slimefun no persiste salida | `markDirty` + `syncBlock(menu)` en retiros `NetworkQuantumStorage.getItemStack` |
| **Grid middle/double** | COLLECT_TO_CURSOR / middle click en GUI | `GridDupeGuardListener` cancela clics peligrosos en `AbstractGrid` |
| **Estructuras** | Árbol crece sobre NTW | `SyncListener.onStructureGrow` (upstream) |
| **Apagado** | Pérdida de celdas al reiniciar | `Networks.onDisable()` + `markDirty` en mutaciones |
| **#233 Control X** | Dupe con shulker + cutter | `NetworkControlX` sin corte NMS (modo compat); no extrae bloques ajenos |
| **Persistencia** | NBT inconsistente | `dev.drake.sefilib.persistence.PersistenceTypes` |
| **Concurrencia** | Sets compartidos en tick | `ConcurrentHashMap` en `NetworkRoot` |

## Autoupdate

Desactivado en Drake: sin `DrakesLabsReleaseUpdate` ni `BlobBuildUpdater`. Despliegue manual del JAR desde releases del repo.

## Smoke test en servidor

1. Romper celda con pico explosivo → la red no debe listar esa posición en grid.
2. Quantum lleno → retirar solo por grid → reiniciar → cantidad coherente.
3. Abrir grid: middle click y doble click no deben duplicar.
4. Sapling + bone meal sobre bridge NTW sin dupe al talar.
5. `/stop` con items en celdas y quantum → reinicio sin pérdida.

## Pendiente upstream / addons externos

- Dupe con **Fluffy Barrel** u otros inventarios de terceros encima de celda rota (#230): requiere addon cooperando o no instalar barrel sobre NTW.
- **Programmable Android** sobre aire donde había NTW: limpiar con `purgeGhostMembership` al detectar bloque no-NTW; no reemplaza validación del script del Android.

Reportes: https://github.com/DrakesCraft-Labs/NetworksV6-drake/issues
