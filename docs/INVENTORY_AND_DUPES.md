# Inventarios, dupes y bugs conocidos (upstream + Chagui)

Referencias auditadas: [Sefiraat/Networks#229](https://github.com/Sefiraat/Networks/issues/229), [#230](https://github.com/Sefiraat/Networks/issues/230), [#208](https://github.com/Sefiraat/Networks/issues/208), [#223](https://github.com/Sefiraat/Networks/issues/223), [#233](https://github.com/Sefiraat/Networks/issues/233), [#240](https://github.com/Sefiraat/Networks/issues/240), fork Chagui / Drake monorepo.

Auditoría ampliada (búsqueda web/issues): [`UPSTREAM_INCIDENTS_AUDIT.md`](./UPSTREAM_INCIDENTS_AUDIT.md).

Rama de desarrollo: **`main`** (la rama `1.21-latin` quedó obsoleta).

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
| **#240** | Network Grabber duplica items Slimefun | `NetworkTransportUtils.pullIntoNetwork` + no extraer de menús `NTW_*`; `markDirty` en origen |
| **Greedy** | Plantilla coincide pero push falla → item “consumido” en lógica netex | `addItemStack0` solo sale del greedy si `incoming.getAmount() == 0` |
| **#226** | Terracotta / bloques vanilla duplicados en grid (clave `ItemStack` en `HashMap`) | `NetworkStackAggregator` + `StackUtils.itemsMatch` en `getAllNetworkItems` |
| **#235** | Vanilla Grabber lento / atascado en OUTPUT | Inyección directa a red + `pullFromInventory` + flush de slot 25 |
| **Grid input** | Insertar en red sin consumir slot | `NetworkTransportUtils` en `AbstractGrid.tryAddItem` |
| **Quantum input** | Slot INPUT no se vacía tras absorber | `replaceExistingItem` + `markDirty` en `tryInputItem` |
| **#223** | PotionMeta sin `BasePotionData` / crash grid | `StackUtils` null-safe en comparación de pociones |

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
