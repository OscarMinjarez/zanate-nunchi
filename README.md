# 🤖 Zanate Nunchi - Tu Compañero IA en Minecraft (Ollama Local LLM)

Un mod revolucionario para **Minecraft Fabric 1.21.6** que introduce a un compañero de aventuras impulsado por **Inteligencia Artificial Local (Ollama)**. No es un asistente aburrido ni un menú de ayuda: es un *jugador más* que reacciona a tu mundo, entiende tu idioma, tiene su propia personalidad y narra tu partida en tiempo real.

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.6-green)
![Fabric Loader](https://img.shields.io/badge/Fabric%20Loader-0.18.2-blue)
![Ollama](https://img.shields.io/badge/AI-Ollama%20(Local)-orange)
![License](https://img.shields.io/badge/License-CC0-lightgrey)

---

## ✨ Características Destacadas

### 🎭 Personalidad Única y Orgánica
Zanate Nunchi no es un "bot" genérico. **Cada jugador que se conecta recibe un compañero con una personalidad generada proceduralmente**:
- **Nombre propio** (ej. "Diego", "Sofía", "Max").
- **Edad y Género**.
- **Rasgos de personalidad** (ej. *sarcástico*, *tímido*, *alegre*, *valiente*).
- **Estilo de habla** (ej. *directo*, *poético*, *informal*).
Esta personalidad altera **todas** sus respuestas. Si tu bot es sarcástico, se burlará de ti cuando recibas daño; si es protector, entrará en pánico.

<img width="1334" height="674" alt="image" src="https://github.com/user-attachments/assets/39c6afd3-8a0f-4946-8134-8261a6cb9a36" />
<img width="1600" height="899" alt="image" src="https://github.com/user-attachments/assets/ca023102-4cc7-4c3b-8f0d-3652c3272d20" />

### 🧠 Arquitectura de Pizarra (Blackboard) y Contexto Espacial
El bot no solo responde cuando le hablas. Observa constantemente tu partida de forma silenciosa e inteligente:
- **Combate**: Detecta cuándo mueres, qué mob te mata, o si estás haciendo una masacre (multiplicadores de kills).
- **Supervivencia**: Sabe si tienes hambre (menos de 8 muslos) o si tu salud es crítica (menos de 4 corazones).
- **Entorno**: Reacciona a transiciones de día/noche, cambios de bioma, lluvia, tormentas y viajes entre dimensiones (Nether/End).
- **Alerta Temprana**: Calcula la distancia entre tú y los mobs hostiles cercanos, advirtiéndote si te están flanqueando.
- **Logros**: Celebra (o critica) cuando desbloqueas advancements.

### 🌐 Dialectos Regionales Estrictos (Anti-Drift)
El mod detecta automáticamente el idioma de tu cliente de Minecraft y obliga al Modelo de Lenguaje (LLM) a hablar en tu **dialecto regional exacto**, prohibiendo el cruce de modismos:
- 🇲🇽 **Español (México)**: El bot usará "wey", "neta", "chido". Tiene prohibido usar modismos argentinos o españoles.
- 🇪🇸 **Español (España)**: Usará "tío", "mola", "chaval".
- 🇦🇷 **Español (Argentina)**: Usará "vos", "che", "boludo".
- 🇬🇧 **English (UK)** vs 🇺🇸 **English (US)** vs 🇦🇺 **English (AU)**: Distingue perfectamente entre "mate", "dude" y "cobber".
- *Soporta 13 idiomas y 17 variantes regionales en total.*

### ⚡ Motor de LLM Local Optimizado para Gaming
El mod se comunica con un servidor local de **Ollama** (recomendado `llama3.2`), lo que significa **cero latencia de red, cero costos de API y total privacidad a tus datos**.
- **Guardrails de Tokens:** El mod ajusta dinámicamente cuántos tokens (palabras) puede generar la IA. 
  - *Situación normal:* Frases conversacionales cortas.
  - *Peligro Inmediato:* La IA entra en modo pánico, generando respuestas ultracortas de latencia casi cero.
- **Control de Spam y Cooldowns:** Un sistema matemático evita que el bot hable sin parar. Si estás minando, hará un comentario ocasional; si estás peleando, solo gritará si tu salud baja de un umbral crítico.
- **Anti-Corte de Oraciones:** Si la IA excede el límite de tokens, el mod recorta la frase limpia hasta el último signo de puntuación válido para evitar frases a medias.

---

## 🚀 Instalación y Uso

### Requisitos Previos
1. **Minecraft 1.21.6** con **Fabric Loader 0.18.2+** y **Fabric API 0.128.2+**.
2. **Java 21**.
3. **Ollama instalado en tu PC** (o en tu red local).

### Configurando la IA (Ollama)
1. Descarga e instala Ollama desde [ollama.com](https://ollama.com).
2. Abre tu terminal y descarga el modelo rápido recomendado:
   ```bash
   ollama pull llama3.2
   ```
3. Asegúrate de que Ollama esté corriendo:
   ```bash
   ollama serve
   ```

### Iniciando el Mod
Coloca el archivo `.jar` del mod en tu carpeta `mods/`. Al iniciar un mundo, la configuración se generará en `config/ollama_bot.json`.
Si juegas en multijugador, ¡cada jugador en el servidor de Fabric tendrá su propia IA privada susurrándole al oído!

---

## 📑 Comandos del Mod

Consulta la documentación completa de comandos aquí:

[📋 Ver comandos y ejemplos de uso](./COMMANDS.md)

---

## ⚙️ Configuración (`ollama_bot.json`)

El mod es altamente personalizable desde su archivo de configuración JSON:

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
  "history": {
    "maxMessages": 20
  },
  "language": "es"
}
```

- **url / model:** Ajusta esto si corres Ollama en otra PC de tu red o si quieres usar un modelo más pesado (ej. `llama3:8b`).
- **cooldowns:** Controla qué tan "callado" es tu bot. Aumenta los segundos si te resulta molesto.
- **maxMessages:** Cuántos mensajes recuerda la IA antes de olvidarlos (memoria a corto plazo).

---

## 📁 Estructura para Desarrolladores

El mod está diseñado con un modelo arquitectónico robusto basado en **Observers** y una **Pizarra Compartida (Blackboard)**.

```text
com/adenium/zanatenunchi/
├── ai/
│   ├── OllamaClient.java         # Comunicación HTTP asíncrona con Ollama AI. Parametriza tokens según peligro.
│   ├── PersonalityGenerator.java # Forja personalidades únicas persistentes en JSON.
│   └── PromptManager.java        # Ingenieria de Prompts maestra: inyecta personalidad y reglas dialectales estrictas.
├── blackboard/
│   ├── Blackboard.java           # Almacenamiento en memoria de estados, cooldowns y memoria de los jugadores.
│   └── BotEvent.java             # Clasificación de eventos (LOW, NORMAL, HIGH).
├── controller/
│   └── BotController.java        # Cerebro central. Decide qué eventos leer, aplica guardrails anti-corte y delega a IA.
├── lang/
│   └── ...Provider.java          # Cadenas hardcodeadas estructuradas y deterministicas para velocidades Ultrabajas.
├── observers/
│   ├── ChatObserver.java         # Lee el chat y extrae nombres.
│   ├── CombatObserver.java       # Manejo de Health y entidades de daño de Minecraft.
│   ├── LanguageObserver.java     # Lee Options del cliente de MC para setear provider.
│   ├── PlayerStatusObserver.java # Inventario, TickEvents, Hambre.
│   └── WorldObserver.java        # BiomeKeys, DimensionKeys, Weather.
└── util/
    └── LanguageManager.java      # Mapeos dialectales estrictos (anti-drift).
```

## 🛠️ Compilación desde el Código Fuente
1. Clona el repositorio.
2. Compila el `.jar`:
   ```bash
   ./gradlew build
   ```
3. Ejecuta el cliente en el entorno de desarrollo de Fabric:
   ```bash
   ./gradlew runClient
   ```

---

## 🤝 Contribuciones
¡Los PRs para añadir más dialectos, mejores triggers en los observers o interacciones de IA (como hacer que el bot sugiera crafteos) son totalmente bienvenidos!

## 📄 Licencia
Distribuido bajo la Licencia **CC0**. Libre para la comunidad.

---
*Desarrollado con ❤️ para llevar una experiencia Next-Gen RPG a Minecraft Clásico.*
