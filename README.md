<div align="center">

<img src="https://raw.githubusercontent.com/DrakesCraft-Labs/NetworksV6-drake/main/networks_banner.svg" alt="NetworksV6-Drake Banner" width="920" />

# 📦 NetworksV6-Drake

**Sistema de Logística, Almacenamiento Masivo y Enrutamiento de Ítems en Tiempo Real Acelerado con Rust**

<p>
  <a href="https://github.com/DrakesCraft-Labs/NetworksV6-drake"><img src="https://img.shields.io/badge/GitHub-NetworksV6--Drake-181717?style=for-the-badge&logo=github" alt="GitHub"/></a>
  <img src="https://img.shields.io/badge/Java-21_FFM_Panama-F89820?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21 FFM"/>
  <img src="https://img.shields.io/badge/Rust-FFM_Accelerated-FF4500?style=for-the-badge&logo=rust&logoColor=white" alt="Rust Native"/>
  <img src="https://img.shields.io/badge/Paper-1.21.11-38BDF8?style=for-the-badge&logo=minecraft&logoColor=white" alt="Paper 1.21.11"/>
</p>

</div>

---

## ⚡ Novedades del Modelo Híbrido (Opción A: Rust Native Bridge)

`NetworksV6-Drake` incluye el puente **`RustNativeBridge`** utilizando **Java 21 Project Panama (FFM API)**.

Delegación directa a la librería nativa C/Rust `slimefun_ffi`:
- 🚀 **Enrutamiento de CargoNet en Nanosegundos**: Cero micro-stutters o pausas de Garbage Collector durante picos de transferencia.
- 🛡️ **Compatibilidad Total sin Reset**: Interfaz 1:1 con las recetas, cofres y bloques existentes en `stored-blocks.db`.

---

## 🛠️ Compilación

```bash
# Compilar paquete con Maven
mvn clean package
```

---

<div align="center">

**DrakesCraft Labs** · Mantenido por [**JackStar6677-1**](https://github.com/JackStar6677-1)

</div>
