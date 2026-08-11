# Portabilidad desde Gugu y NetworksExpansion

NetworksV6-Drake es la implementación canónica de DrakesCraft para Paper/Purpur
1.21.11. Los forks históricos de Slimefun Gugu y NetworksExpansion se mantienen
solamente como referencias de código bajo GPLv3; no son artefactos de producción.

## Regla de integración

Una mejora externa se porta como código pequeño, revisado y probado contra la API
`com.github.drakescraft_labs.slimefun4`. No se relocalizan JARs ni se cambia el
namespace del core para hacerlos arrancar: DrakesCraft mantiene addons que usan
los namespaces Drake, legacy y upstream mediante puentes de compatibilidad.

## Mejoras ya portadas

- Quantum Storage registra un espejo SQLite optativo, de sólo escritura, para
  auditoría. `BlockStorage` continúa siendo la fuente de verdad y no hay
  restauración automática ni migración implícita.
- Las mutaciones de cada `QuantumCache` son atómicas, evitando que retiros e
  inserciones consecutivos operen sobre una cantidad obsoleta.

## Pendiente de evaluar por módulos

- Las máquinas Cargo, Storage Units y transferencias avanzadas de
  NetworksExpansion se evaluarán como contenido independiente, compilado contra
  Slimefun Drake y con recetas/economía propias.
- El almacenamiento SQL global de Gugu no se porta como reemplazo directo:
  su modelo y sus namespaces son distintos. Cualquier importador futuro debe
  ser explícito, idempotente, respaldado y probado con todos los addons activos.

## Límites de seguridad

No ejecutar `/sf migrate` mientras un arranque con todos los addons no sea limpio.
La migración de Slimefun es destructiva para el formato legacy y no forma parte
de la activación del espejo SQL de Networks.
