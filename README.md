# NetworksV6-Drake

[![Branch](https://img.shields.io/badge/branch-1.21--latin-blue)](https://github.com/DrakesCraft-Labs/NetworksV6-drake/tree/1.21-latin)
[![License](https://img.shields.io/badge/license-GPL--3.0-green)](LICENSE)
[![Java](https://img.shields.io/badge/java-21-orange)](https://adoptium.net/)
[![Minecraft](https://img.shields.io/badge/minecraft-1.21.11-brightgreen)](https://papermc.io/)

Addon de Slimefun orientado a redes de almacenamiento y automatizacion de items.  
Este repo contiene el port de `Networks-Exp (b1)` adaptado al stack Drake para Paper 1.21.x.

## Que agrega a Slimefun

- Red logica de inventarios con controlador, nodos y extensiones.
- Grid de visualizacion y crafting conectado al almacenamiento de red.
- Celdas y Quantum Storage para almacenamiento profundo.
- Import/Export, Grabber/Pusher, Wireless Tx/Rx y utilidades de red.
- Autocrafters con blueprints y pipeline de recipes en red.

## Estado tecnico del port

- Base funcional importada desde `Networks-Exp` y ajustada a namespaces Drake.
- Compilacion validada en Java 21 y Paper API 1.21.
- Integraciones opcionales externas se dejan desacopladas para evitar hard-fails de arranque.
- `NetworkControlX` y `NetworkControlV` quedaron en modo compat temporal mientras se restaura puente NMS dedicado para 1.21.11.

## Compatibilidad

| Componente | Version objetivo |
|---|---|
| Minecraft | 1.21.11 |
| Paper API | 1.21.x |
| Java | 21 |
| Slimefun | Drake 11-SNAPSHOT |

## Instalacion

1. Descargar el `.jar` desde Releases.
2. Copiar a `plugins/`.
3. Reiniciar el servidor.
4. Verificar en consola que cargue `NetworksV6-Drake`.

## Build local

```bash
mvn -B -ntp -DskipTests clean package
```

Artifact esperado:

- `target/NetworksV6-Drake-v11-SNAPSHOT.jar`

## Release

- El workflow `drake-release.yml` publica artifacts al crear un tag `v*`.
- Solo se publica release si el build termina correctamente.

## Creditos

- Autor original: Sefiraat
- Variantes comunitarias y fixes intermedios: Chagui68, mmmjjkx
- Port y mantenimiento Drake: DrakesCraft-Labs

## Licencia

GPL-3.0. Ver `LICENSE`.
