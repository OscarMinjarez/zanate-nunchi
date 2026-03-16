# 🤖 Ollama Bot - Minecraft AI Companion

Un mod de Fabric para Minecraft 1.21.6 que añade un compañero IA por jugador, impulsado por Ollama (LLM local).

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.6-green)
![Fabric Loader](https://img.shields.io/badge/Fabric%20Loader-0.18.2-blue)
![License](https://img.shields.io/badge/License-CC0-lightgrey)

## ✨ Resumen de la versión reciente

Esta versión introduce varias mejoras importantes:

- Proveedores específicos por idioma (clases que implementan `IBotLanguageProvider`) en lugar de un proveedor genérico. Esto permite respuestas más naturales y adaptadas a cada dialecto.
- Sistema de localización pensado para integrar archivos JSON de traducción en `resources` (ruta propuesta: `assets/zanatenunchi/lang/{code}.json`).
- Detección de idioma por jugador (`LanguageObserver`) con fallback configurable.
- Corrección de NullPointerException en `PromptManager` y mejoras de robustez en la generación de prompts.
- Mejora del control de spam: cooldowns por tipo de evento, rotación de frases y reglas de prioridad (HIGH/NORMAL/LOW).

Si estás actualizando desde una versión anterior, revisa la sección "Migración" más abajo.

## ✨ Características principales

### 🎭 Personalidad única por jugador
- Cada jugador obtiene su propio compañero bot con nombre, personalidad y estilo de hablar únicos.
- La personalidad se genera automáticamente cuando el jugador se conecta por primera vez y se persiste por mundo.

### 🌐 Soporte multilenguaje (implementado)

Proveedores de idioma ya implementados (clases Java en `com.adenium.zanatenunchi.lang`):

- es_mx (MexicanSpanishProvider)
- es_es (SpanishSpainProvider)
- es_ar, es_cl, es_co, es_ve (mapeados a SpanishSpainProvider o variantes según sea necesario)
- en_us, en_gb, en_au (AmericanEnglishProvider usado como base para variantes)
- pt_br, pt_pt (PortugueseBrazilProvider)
- fr_fr, fr_ca (FrenchFranceProvider)
- de_de (GermanProvider)
- it_it (ItalianProvider)
- ja_jp (JapaneseProvider)
- ko_kr (KoreanProvider)
- zh_cn, zh_tw (ChineseSimplifiedProvider usado para ambas variantes)
- ru_ru (RussianProvider)
- pl_pl (PolishProvider)

Diseño de localización:
- Archivos JSON por idioma en recursos: `assets/zanatenunchi/lang/{code}.json` (por ejemplo `en_us.json`, `es_mx.json`).
- Un `TranslationManager` (planificado/implementable) hará carga lazy, caché thread-safe y fallback a `en_us` cuando falten claves.
- Los `IBotLanguageProvider` actuales contienen plantillas y eventos; se irá migrando la lógica de strings a los JSONs para facilitar ediciones.

### 👁️ Observadores del juego
El bot reacciona a eventos en tiempo real mediante observers:

- `CombatObserver`: muertes, daño fuerte, kills de mobs
- `WorldObserver`: cambios de bioma, clima, dimensión y hora del día
- `PlayerStatusObserver`: salud baja, hambre, peligro cercano y hallazgos
- `ChatObserver`: registro de conversaciones
- `LanguageObserver`: detección del idioma por cliente (por jugador)

### 🧠 Blackboard (arquitectura)
- Cola de eventos priorizada
- Cooldowns por tipo de evento para evitar spam
- Historial de conversación por jugador (persistente por mundo)

## 📦 Requisitos

- Minecraft: 1.21.6
- Fabric Loader: 0.18.2+
- Fabric API: 0.128.2+
- Java: 21
- Ollama: instalado y en ejecución localmente
- Modelo LLM: `llama3.2` (u otro compatible configurado)

## 🚀 Instalación rápida

### 1) Instalar Ollama (ejemplo Windows PowerShell)
```powershell
winget install Ollama.Ollama
```

En macOS / Linux usa tu gestor de paquetes preferido o el script de instalación de Ollama.

### 2) Descargar un modelo
```powershell
ollama pull llama3.2
```

### 3) Iniciar Ollama
```powershell
ollama serve
```

### 4) Compilar el mod y probar localmente
```powershell
.\gradlew.bat clean build
.\gradlew.bat runClient
```

Coloca el `.jar` resultante en la carpeta `mods/` para ejecutar en un cliente/servidor Fabric.

## ⚙️ Configuración (resumen)

El archivo de configuración principal es `ollama_bot.json` (ubicación típica: `run/config/ollama_bot.json` durante desarrollo, o `.minecraft/config/ollama_bot.json` en instalación normal).

Ejemplo mínimo:
```json
{
  "ollama": {
    "url": "http://localhost:11434",
    "model": "llama3.2",
    "timeoutSeconds": 30
  },
  "cooldowns": {
    "highEventSeconds": 8,
    "spontaneousSeconds": 120
  },
  "language": "es"
}
```

Opciones importantes:

- `ollama.url`: URL del servidor Ollama (por defecto `http://localhost:11434`).
- `ollama.model`: modelo a usar.
- `cooldowns.*`: control de spam/intervalos.
- `history.maxMessages`: máximo de mensajes mantenidos en memoria por jugador.
- `language`: valor por defecto global; cada jugador tiene su idioma detectado por `LanguageObserver` y puede sobreescribirse.

## 🛠️ Cómo añadir / mejorar un idioma

1. Añade un provider específico en `src/main/java/com/adenium/zanatenunchi/lang/` implementando `IBotLanguageProvider` si necesitas lógica personalizada.
2. Añade/edita el archivo de traducción en `src/main/resources/assets/zanatenunchi/lang/{code}.json` con las claves necesarias (ej.: `system_prompt`, `greeting_new_player`, `event_low_health`, ...).
3. Registra el provider en `LanguageManager` si es un provider Java; si solo usas JSON/TranslationManager, asegúrate de que el código de idioma existe en los archivos.

Consejo: para traducciones rápidas puedes usar traducción automática como base, luego revisarlas manualmente.

## 📁 Estructura del proyecto (actualizada)

```text
src/main/java/com/adenium/zanatenunchi/
├── ai/
│   ├── OllamaClient.java
│   ├── PromptManager.java         # Genera prompts usando IBotLanguageProvider / TranslationManager
├── blackboard/
│   └── Blackboard.java
├── config/
│   └── ModConfig.java
├── controller/
│   └── BotController.java
├── observers/
│   ├── ChatObserver.java
│   ├── CombatObserver.java
│   ├── LanguageObserver.java     # Detecta locale por jugador
│   ├── PlayerStatusObserver.java
│   └── WorldObserver.java
├── lang/
│   ├── IBotLanguageProvider.java
│   ├── AmericanEnglishProvider.java
│   ├── MexicanSpanishProvider.java
│   ├── SpanishSpainProvider.java
│   ├── PortugueseBrazilProvider.java
│   ├── FrenchFranceProvider.java
│   ├── GermanProvider.java
│   ├── ItalianProvider.java
│   ├── JapaneseProvider.java
│   ├── KoreanProvider.java
│   ├── ChineseSimplifiedProvider.java
│   ├── RussianProvider.java
│   └── PolishProvider.java
└── util/
    └── LanguageManager.java      # Mapea códigos a providers y perfiles

src/main/resources/
└── assets/zanatenunchi/lang/      # <-- aquí van los JSON de traducción ({code}.json)
```

## 📋 Eventos detectados (resumen)

Los eventos y su clasificación por impacto se mantienen (HIGH / NORMAL / LOW). La lógica de prioridad y cooldown evita spam y prioriza mensajes críticos.

## 🧪 Tests y CI

El proyecto tiene tests básicos (si existen) y se pueden ejecutar con:

```powershell
.\gradlew.bat test
```

Si integras CI, añade pasos para compilar y ejecutar pruebas, y opcionalmente ejecutar análisis estático (SpotBugs / Checkstyle).

## Migración desde versiones anteriores

- Si venías usando `GenericLanguageProvider`, ahora los proveedores son específicos y deben registrarse en `LanguageManager`.
- Si almacenabas cadenas en código, considera moverlas a `assets/zanatenunchi/lang/{code}.json` para facilitar mantenimiento.

## 🤝 Contribuir

1. Haz un fork del repositorio
2. Crea una rama (`git checkout -b feature/nueva-caracteristica`)
3. Haz commit de tus cambios (`git commit -am 'Añade nueva característica'`)
4. Haz push a la rama (`git push origin feature/nueva-caracteristica`)
5. Abre un Pull Request

## 📄 Licencia

Este proyecto está disponible bajo la licencia CC0. Siéntete libre de aprender de él e incorporarlo en tus propios proyectos.

## 🙏 Créditos

- Fabric - Mod loader (https://fabricmc.net/)
- Ollama - LLM local (https://ollama.ai/)

---

**¿Problemas?** Asegúrate de que Ollama esté ejecutándose con `ollama serve` y que el modelo esté descargado con `ollama pull llama3.2`.
