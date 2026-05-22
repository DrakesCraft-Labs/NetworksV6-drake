# Inventarios, persistencia y dupes conocidos

Auditoría Drake (Chagui + monorepo) integrada en `NetworksV6-drake`.

## Corregido en este repo

| Área | Qué hacía fallar | Mitigación |
|---|---|---|
| Apagado del servidor | Celdas/NTW no marcaban dirty → pérdida o estado incoherente al reiniciar | `Networks.onDisable()` + `markNetworkInventoriesDirty()` |
| Quantum Storage | Cambios de cantidad/void sin persistir en BlockStorage | `markDirty()` en push, sync y `createCache` |
| Grid / Export / Pusher / Wireless | Retiros e inserciones sin dirty | `markDirty()` tras mutar menús |
| Vanilla Grabber/Pusher | Slots de red no guardados | `markDirty()` tras mover items |
| AutoCrafter / Encoder / Quantum Workbench | Salidas craft sin dirty | `markDirty()` tras `pushItem` |
| `NetworkRoot` (celdas/crafters) | Retiros parciales sin marcar menú | `markDirty()` en cada mutación de stack |
| Crecimiento de estructuras (árboles, etc.) | Nodos NTW bajo bloques nuevos → dupes al romper | `SyncListener.onStructureGrow` (comentario upstream: "Fixed a dupe") |
| Persistencia NBT | `morepersistentdatatypes` vendored inconsistente con Drake | `dev.drake.sefilib.persistence.PersistenceTypes` |
| Concurrencia de nodos | Sets compartidos en tick | `ConcurrentHashMap` / `newKeySet()` en `NetworkRoot` |

## Riesgos que siguen vigilados (smoke en servidor)

1. **Crafting grid con autocraft rápido**: encadenar craft + refill de red puede duplicar si otro addon cancela eventos de inventario. Probar con lag artificial (`/mspt`).
2. **Quantum void + export simultáneo**: con void activo y exportador en la misma red, validar que no se extraiga más de lo contado en cache.
3. **Wireless TX/RX bajo chunk unload**: enlaces guardados en PDC; al volver a cargar chunk, comprobar que el receptor no acepte doble push en el mismo tick.
4. **Greedy block + celdas llenas**: el root hace `markDirty` antes de `pushItem`; si la celda rechaza el item, verificar que no quede "fantasma" en memoria (Netex `getItemStack0`).
5. **Addons que reescriben `BlockMenu`**: Slimefun HUD / menús de terceros abiertos sobre NTW pueden impedir dirty; cerrar GUI antes de `/stop`.

## Prueba mínima recomendada (producción)

1. Llenar una celda y un quantum storage, reiniciar servidor → cantidades iguales.
2. Craftear 64 ítems en grid con red llena y vacía.
3. Colocar sapling + bone meal cerca de bridge NTW → no debe duplicar al crecer árbol.
4. Exportador + importador en bucle cerrado → estable (sin crecimiento de items).

## Reportar

Issues en: https://github.com/DrakesCraft-Labs/NetworksV6-drake/issues
