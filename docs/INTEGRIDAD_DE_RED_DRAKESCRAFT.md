# Integridad de Networks DrakesCraft

Esta es la referencia operativa del fork. Describe las defensas vigentes sin
depender de reportes ni documentación externa que puede no representar este JAR.

## Principios

- Una ubicación deja de pertenecer a la red cuando su bloque deja de ser NTW.
- Un inventario externo nunca puede ser interpretado como una celda de Networks.
- Una retirada solo se confirma después de persistir y sincronizar el origen.
- Los clics de inventario ambiguos se cancelan en grids y terminales.
- Cada mutación de una red se marca para guardado antes de un apagado.

## Vectores cubiertos

| Área | Defensa activa |
|---|---|
| Nodos rotos o reemplazados | `unregisterNode`, `onForeignBlockOccupied` y `purgeGhostMembership` |
| Celdas y contenedores externos | Validación estricta de celda y exclusión de inventarios ajenos |
| Grabbers, pushers e importación | `NetworkTransportUtils`, consumo confirmado y guardado del origen |
| Grid y terminales | `GridDupeGuardListener`, bloqueo de middle-click, doble clic y extracción masiva ambigua |
| Quantum storage | `markDirty` y `syncBlock` en entradas y retiros |
| Agregación de stacks | `NetworkStackAggregator` y comparación segura de metadata |
| Pociones o metadata incompleta | Comparación null-safe en `StackUtils` |
| Reinicios | `Networks.onDisable()` fuerza persistencia de mutaciones pendientes |
| Concurrencia de ticker | Colecciones concurrentes en la topología de red |

## Smoke test obligatorio

1. Rompe una celda indirectamente y verifica que el grid deje de verla.
2. Coloca un inventario externo en esa ubicación; nunca debe quedar accesible
   desde Networks.
3. Retira ítems de un Quantum, reinicia y verifica cantidades.
4. Prueba middle-click, doble clic y shift-click en el grid.
5. Añade una máquina o storage a una red existente y confirma que el controller
   detecta la topología sin tener que recolocarlo.
6. Detén el servidor con operaciones pendientes y valida que no haya pérdidas
   ni membresías huérfanas al volver a iniciar.

## Reportes

Abre un issue en este repositorio con versión del JAR, Paper/Purpur, mundo,
coordenadas, componentes de la red y pasos exactos de reproducción. No publiques
datos privados, tokens ni respaldos completos del servidor.
