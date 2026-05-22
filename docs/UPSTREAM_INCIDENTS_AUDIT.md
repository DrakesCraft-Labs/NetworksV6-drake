# Auditoría de incidencias (web / upstream)

Inventario de reportes públicos sobre **Sefiraat/Networks**, forks (**balugaq/Netex**, **Chagui**), y vectores relacionados.  
Estado en **NetworksV6-drake** (`main`) a mayo 2026.

Fuentes: [GitHub Issues Sefiraat/Networks](https://github.com/Sefiraat/Networks/issues), [FluffyMachines#163](https://github.com/NCBPFluffyBear/FluffyMachines/issues/163), [Slimefun#4318](https://github.com/Slimefun/Slimefun4/issues/4318), comunidad Discord (citada en issues).

---

## Dupes confirmados o muy documentados

| Issue | Título | Vector | Estado Drake |
|-------|--------|--------|--------------|
| [#240](https://github.com/Sefiraat/Networks/issues/240) | Grabber duplica items SF | `addItemStack0` sin consumir menú origen | **Mitigado** — `NetworkTransportUtils`, no extraer de `NTW_*` |
| [#230](https://github.com/Sefiraat/Networks/issues/230) | Dupe #5 (Fluffy Barrel + grid) | Celda rota (#229) + barrel encima + middle/double/Extract All | **Mitigado** — `unregisterNode`, `onForeignBlockOccupied`, celdas estrictas, grid guard + lore `Amount:` |
| [#229](https://github.com/Sefiraat/Networks/issues/229) | Máquinas NTW huérfanas en grafo | Pico explosivo, wither, dripleaf, Android | **Mitigado** — listeners + `purgeGhostMembership` |
| [#226](https://github.com/Sefiraat/Networks/issues/226) | Terracotta x N | Agregación grid / `HashMap<ItemStack>` | **Mitigado** — `NetworkStackAggregator` |
| [#208](https://github.com/Sefiraat/Networks/issues/208) | Quantum 4k desync | Retiro por grid sin abrir menú | **Mitigado** — `markDirty` + `syncBlock` |
| [#233](https://github.com/Sefiraat/Networks/issues/233) | Control X + shulker | Corte NMS | **Mitigado (compat)** — sin corte NMS hasta puente 1.21.11 |
| [#223](https://github.com/Sefiraat/Networks/issues/223) | Pociones sin base data | NPE en `StackUtils` → crash + dupe al retirar | **Mitigado** — `Objects.equals` en `BasePotionType` / `BasePotionData` ([PR #224](https://github.com/Sefiraat/Networks/pull/224) upstream) |
| [#106](https://github.com/Sefiraat/Networks/issues/106) | Rake duplica al quitar Monitor | Rake no consumía carga / generaba otro rake | **Mitigado** — `NetworkRake` llama `clearNetwork` antes de vaciar el bloque |
| Fluffy [#163](https://github.com/NCBPFluffyBear/FluffyMachines/issues/163) | Barrel dupe (referencia #230) | Misma cadena celda rota + GUI | **Mitigado** — igual que #230 |

### Cadena #229 → #230 (resumen técnico)

1. Romper **Network Cell** sin limpiar membresía en `NetworkRoot`.
2. Colocar **Fluffy Barrel** (u otro inventario) en la misma posición lógica.
3. El **grid** sigue viendo esa ubicación como celda → retiros desde GUI ajena.
4. Abusos de clic: middle, doble clic, “Extract All”, shift-replace en slots de entrada.

**Drake:** prune + `unregisterNode` al desmontar + `onForeignBlockOccupied` al colocar bloque ajeno + grid solo retira ítems con lore de display.

---

## Inventarios rotos / lentitud / pérdida

| Issue | Título | Síntoma | Estado Drake |
|-------|--------|---------|--------------|
| [#235](https://github.com/Sefiraat/Networks/issues/235) | Vanilla Grabber lento/atascado | Buffer slot 25, no vacía a red | **Mitigado** — inyección directa + flush |
| [#208](https://github.com/Sefiraat/Networks/issues/208) | Quantum tras reinicio | Cantidad incoherente | **Mitigado** — ver arriba |
| Apagado servidor | Pérdida celdas/quantum | Sin `markDirty` / sin flush | **Mitigado** — `Networks.onDisable()` |
| [#223](https://github.com/Sefiraat/Networks/issues/223) | Crash al clic grid | PotionMeta null | **Mitigado** — StackUtils |
| [#205](https://github.com/Sefiraat/Networks/issues/205) | Encoder no funciona | (cerrado upstream) | Revisar si reproduce en Drake 11 |
| [#172](https://github.com/Sefiraat/Networks/issues/172) | Grid filtro vacío | (cerrado) | Probar con `GridCache` actual |

---

## Interacción con otros addons / Slimefun core

| Fuente | Problema | Relación Networks |
|--------|----------|-------------------|
| [SF #4318](https://github.com/Slimefun/Slimefun4/issues/4318) | Cargo “mismo nombre” copia metadata | No es Networks; mismo tipo de bug de matching débil |
| [SF #4020](https://github.com/Slimefun/Slimefun4/issues/4020) | Libros/cabezas en cargo | Idem |
| GlobalWarming CO2 ([#223](https://github.com/Sefiraat/Networks/issues/223)) | Item sin potion base | Afecta grabber/grid vía StackUtils |
| Infinity barrel ([#230](https://github.com/Sefiraat/Networks/issues/230) comentarios) | Misma técnica dupe | Monitor + storage externo sobre celda rota |
| Programmable Android ([#229](https://github.com/Sefiraat/Networks/issues/229)) | NTW → aire, grafo sucio | Limpieza parcial con integrity |

---

## Reportes sin fix upstream (vigilar)

| ID | Notas |
|----|--------|
| [#240](https://github.com/Sefiraat/Networks/issues/240) | Vídeos en Streamable/Discord; técnica no publicada — asumir grabber hasta build con `NetworkTransportUtils` |
| [#226](https://github.com/Sefiraat/Networks/issues/226) | Vídeo YouTube; terracotta — cubierto por agregador + tests |
| Chagui / producción | “Aún no listos” — smoke en DrakesCraft obligatorio |

---

## Prioridad siguiente en Drake

1. **Smoke en DrakesCraft** — #230 con Fluffy Barrel tras romper celda indirectamente.
2. **Wireless** — desync tras reinicio si se reporta en servidor.
3. **Autocrafter / withholding** — `markDirty` en salidas si reaparece pérdida.
4. Release **v11.0.0-drake.2** tras validación en producción.

---

## Enlaces útiles

- [Networks Issues abiertos (dupe)](https://github.com/Sefiraat/Networks/issues?q=is%3Aopen+dupe)
- [Documentación Sefiraat](https://sefiraat.dev)
- [Drake — inventarios y dupes](./INVENTORY_AND_DUPES.md)
- [Release JAR](https://github.com/DrakesCraft-Labs/NetworksV6-drake/releases)

Reportar nuevos casos: https://github.com/DrakesCraft-Labs/NetworksV6-drake/issues (incluir versión JAR, Paper, pasos, vídeo si es dupe).
