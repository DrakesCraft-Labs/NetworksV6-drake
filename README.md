<div align="center">

  <img src="https://raw.githubusercontent.com/DrakesCraft-Labs/NetworksV6-drake/main/networks_banner.svg" alt="NetworksV6-drake Banner" width="920" />

# 📦 NetworksV6-Drake

**Sistema de Logística, Almacenamiento Masivo, Enrutamiento de Ítems en Tiempo Real y Autocrafteo para Slimefun4**

<p>
  <a href="https://github.com/DrakesCraft-Labs/NetworksV6-drake"><img src="https://img.shields.io/badge/GitHub-NetworksV6--Drake-181717?style=for-the-badge&logo=github" alt="GitHub"/></a>
  <img src="https://img.shields.io/badge/Java-21_FFM_Panama-F89820?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21 FFM"/>
  <img src="https://img.shields.io/badge/Rust-FFM_Accelerated-FF4500?style=for-the-badge&logo=rust&logoColor=white" alt="Rust Native"/>
  <img src="https://img.shields.io/badge/Paper-1.21.11-38BDF8?style=for-the-badge&logo=minecraft&logoColor=white" alt="Paper 1.21.11"/>
</p>

</div>

---

## 🚀 ¿Qué es NetworksV6-Drake?

`NetworksV6-Drake` es el addon de logística y almacenamiento automatizado definitivo para Slimefun4 en DrakesCraft. Permite conectar cientos de cofres, contenedores y máquinas en una **red digital unificada**, ofreciendo almacenamiento masivo, transporte instantáneo de ítems y autocrafteo.

---

## 🧰 Componentes y Máquinas del Addon

### 1. 🌉 Red Digital & Puentes
- **Puente de Red (Network Bridge)**: El núcleo del sistema. Conecta todos los nodos, cables y contenedores en una misma red.
- **Terminal de Red (Network Terminal)**: Interfaz de user para buscar, guardar y extraer ítems de todos los cofres conectados en tiempo real.
- **Terminal Inalámbrica (Wireless Terminal)**: Acceso remoto a la red de almacenamiento desde cualquier parte del mundo.

### 2. 🚛 Logística de Ítems (Buses de Importación y Exportación)
- **Bus de Importación (Import Bus)**: Extrae ítems de cofres o máquinas y los introduce automáticamente a la red digital.
- **Bus de Exportación (Export Bus)**: Saca ítems específicos de la red digital y los deposita en cofres o máquinas de destino.
- **Filtros Avanzados**: Support for filtrado por NBT, Lore, Nombres personalizados y coincidencia de Durabilidad.

### 3. ⚙️ Autocrafteo & Automatización
- **Unidad de Autocrafteo (Crafting Unit)**: Ejecuta recetas de crafteo de Slimefun4 y Minecraft de forma automatizada al solicitar ítems desde la terminal.
- **Monitor de Red (Network Monitor)**: Muestra el estado del flujo de ítems, almacenamiento total y consumo de energía en tiempo real.

---

## ⚡ Aceleración Nativa en Rust (Modelo Híbrido Cero-Riesgo)

`NetworksV6-Drake` incluye el puente Panama FFM **`RustNativeBridge`** que conecta la lógica de Java con el motor nativo `Slimefun-Rust` (`slimefun_ffi`):
- 🚀 **Enrutamiento de CargoNet en Nanosegundos**: Cero micro-stutters o pausas de Garbage Collector durante el transporte masivo de ítems.
- 🛡️ **Preservación Total sin Reset (SQLite 0-Reset)**: Interfaz 1:1 con la base de datos `stored-blocks.db` nativa de Slimefun4.

---

## 🛠️ Compilación e Instalación

```bash
# Compilar paquete JAR con Maven
mvn clean package
```

Ubica el archivo compilado `NetworksV6-Drake-v6.0.0.jar` en la folder `plugins/` de tu servidor Minecraft Paper/Purpur 1.21.11.

---

<div align="center">

**DrakesCraft Labs** · Mantenido por [**JackStar6677-1**](https://github.com/JackStar6677-1)

</div>