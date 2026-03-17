# 📋 Comandos de Zanate Nunchi

Zanate Nunchi proporciona comandos de consola para configurar la URL y modelo de Ollama en tiempo de ejecución, sin necesidad de reiniciar el servidor.

## Comandos Disponibles

### 1. Cambiar URL de Ollama
```
/zanate url <nueva_url>
```

**Descripción:** Actualiza la URL del servidor Ollama.

**Uso:**
```
/zanate url http://localhost:11434
/zanate url http://192.168.1.100:11434
/zanate url http://remote-server.com:11434
```

**Permisos requeridos:** Nivel 2 (Operador)

**Ejemplo de respuesta:**
```
[Zanate] URL de Ollama actualizada a: http://localhost:11434
```

---

### 2. Cambiar Modelo de Ollama
```
/zanate modelo <nuevo_modelo>
```

**Descripción:** Cambia el modelo de LLM a utilizar.

**Uso:**
```
/zanate modelo llama3.2
/zanate modelo mistral
/zanate modelo neural-chat
```

**Permisos requeridos:** Nivel 2 (Operador)

**Ejemplo de respuesta:**
```
[Zanate] Modelo de Ollama actualizado a: llama3.2
```

---

### 3. Ver Estado Actual
```
/zanate status
```

**Descripción:** Muestra la configuración actual del bot (URL y modelo).

**Uso:**
```
/zanate status
```

**Permisos requeridos:** Ninguno (accesible para todos)

**Ejemplo de respuesta:**
```
[Zanate Status]
URL: http://localhost:11434
Modelo: llama3.2
```

---

## Casos de Uso Comunes

### Caso 1: Puerto Incorrecto
Si el servidor Ollama está en un puerto diferente al configurado:

```
/zanate url http://localhost:8080
```

### Caso 2: Cambiar a Servidor Remoto
Si tienes Ollama instalado en otra máquina de la red:

```
/zanate url http://192.168.1.50:11434
```

### Caso 3: Cambiar Modelo sin Reiniciar
Para usar un modelo más rápido o potente:

```
/zanate modelo mistral
```

---

## Requisitos de Permisos

Para ejecutar comandos que modifiquen la configuración (url, modelo), necesitas:
- **Nivel de operador 2 (Op)** en el servidor

Para ver el estado (status), no se necesitan permisos especiales.

---

## Notas Técnicas

- Los cambios se guardan automáticamente en `ollama_bot.json`
- La configuración persiste entre reinicios del servidor
- Los errores se registran en los logs del servidor para debugging
- Se recomienda verificar con `/zanate status` después de cambiar la URL


