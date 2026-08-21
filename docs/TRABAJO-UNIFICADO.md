# Rama `trabajo/networks-unificado`

Aquí se junta el Networks chino (NetworksExpansion) con las mejoras que Chagui hizo en
MultiverseNets. Es una rama de trabajo: **`main` sigue siendo lo desplegado** y no se toca hasta
que esto pase las comprobaciones de abajo.

## De dónde sale cada cosa

**Base: `integracion/expansion`** — NetworksExpansion de balugaq/ytdd9527, ya con el trabajo de
Jack encima (GuizhanLib eliminado como dependencia, `cl.jackstar.networks.compat.TextoItems`
sustituyendo sus ayudantes, y `lang/es-ES.yml` empezado). Aporta **315 IDs de ítem frente a los
74 de V6**.

**A traer de MultiverseNets** (`DrakesCraft-Labs/MultiverseNets`): la forma de decidir qué nodos
pertenecen a la red.

## Por qué merece la pena traer eso, y no otra cosa

Es la diferencia que de verdad importa, y no se arregla sola al cambiar de fork.

**Cómo lo hace Networks, aquí y en Expansion.** Cada nodo guarda una referencia a su
`NetworkRoot`. Cuando el controlador reconstruye, crea una raíz nueva; los nodos a los que no
llega se quedan apuntando a la vieja. Para Networks el nodo existe, pero para su propia red no.
Eso es el clásico *"lo tengo todo conectado y la máquina no saca"*. Hay **232 referencias a ese
modelo** en la base de Expansion: el fork chino no lo cambia.

**Cómo lo hace MultiverseNets.** `Network.scan()` rehace la pertenencia entera por BFS desde el
controlador, cada `scan-interval-ticks`. No hay estado por nodo que sobreviva al escaneo, así que
**un nodo huérfano es estructuralmente imposible**. El precio es un recorrido acotado y
predecible en vez de un estado que se puede corromper.

**Lo que hay que traer, en orden:**

1. El modelo de escaneo completo (`Network.scan()` de MultiverseNets) adaptado a `NetworkRoot`.
2. El índice por tipo de dispositivo, para que localizar "todos los grabbers" no recorra la red
   entera. En MultiverseNets el ticker lo pedía siete veces por pasada.
3. Que el diagnóstico salga de fábrica y no como parche: `/networks doctor` se añadió aquí
   después, `/mvnets doctor` estaba desde el primer commit.

## Lo que bloquea promover esto a `main`

**1. Nueve ítems se perderían.** Existen en V6 y no en Expansion:

```
NTW_ADVANCED_AUTO_CRAFTER      NTW_ADVANCED_AUTO_CRAFTER_WITHHOLDING
NTW_ADVANCED_EXPORT            NTW_ADVANCED_GRABBER
NTW_ADVANCED_GREEDY_BLOCK      NTW_ADVANCED_IMPORT
NTW_ADVANCED_PURGER            NTW_ADVANCED_PUSHER
NTW_ADVANCED_VACUUM
```

Los nueve están registrados en el `Items.yml` de producción y **hay jugadores con ellos
colocados**: el profiler del 21-08 contó **18 `NTW_ADVANCED_AUTO_CRAFTER` tickeando**. Si esto
sube sin portarlos, esas máquinas se convierten en bloques desconocidos.

**2. La traducción va por la mitad.** 744 de 1.294 claves en `es-ES.yml`; faltan **552 (42%)**,
incluidos bloques enteros como `displays.quantum_storage.*`.

## Lo que NO es un problema

Los **65 IDs que Expansion comparte con V6** (`NTW_GRABBER`, `NTW_PUSHER`, `NTW_AUTO_CRAFTER`...).
Al ser sustitución y no convivencia, las máquinas ya colocadas **conservan su identificador y
siguen funcionando**. Ese solape es la razón por la que el cambio es viable.

## Antes de tocar producción

- Servidor vacío.
- Respaldo del `Items.yml` y del almacén de bloques de Slimefun.
- Comprobar con `/networks doctor` **antes y después**: si el número de huérfanos no baja, el
  punto 1 de arriba no se trajo bien.
