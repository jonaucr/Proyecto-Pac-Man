
# 📘 Protocolo de Conexión (JSON) – Proyecto Pac-Man

Este documento describe los mensajes de conexión y estructura de comunicación entre el cliente (Java) y el servidor (C) del juego Pac-Man multijugador.

---

## 🔗 Estructura general de mensajes

Todos los mensajes siguen el formato JSON:

```json
{
  "type": "<tipo_de_mensaje>",
  "payload": {
    // contenido específico del mensaje
  }
}
```

---

## 🔁 Mensajes Cliente → Servidor

### 🎮 Crear una nueva partida

```json
{
  "type": "create_game",
  "payload": {
    "role": "player"
  }
}
```

El cliente solicita crear una nueva partida como jugador principal.

---

### 👀 Unirse a una partida existente

```json
{
  "type": "join_game",
  "payload": {
    "gameId": "abc123",
    "role": "observer"
  }
}
```

El cliente se une a una partida ya creada. `role` puede ser `"player"` o `"observer"`.

---

## 🔁 Mensajes Servidor → Cliente

### ✅ Confirmación de creación de partida

```json
{
  "type": "game_created",
  "payload": {
    "gameId": "abc123"
  }
}
```

El servidor confirma que la partida fue creada exitosamente.

---

### 🧩 Estado del juego (completo)

```json
{
  "type": "game_state",
  "payload": {
    "players": [
      { "x": 1, "y": 2, "score": 0 }
    ],
    "ghosts": [
      { "x": 5, "y": 3, "color": "red" }
    ],
    "fruits": [
      { "x": 4, "y": 5, "type": "cherry" }
    ],
    "map": [
      [1,1,1,1,1],
      [1,0,0,0,1],
      [1,0,2,0,1],
      [1,1,1,1,1]
    ]
  }
}
```

---

### 💣 Error de conexión o validación

```json
{
  "type": "error",
  "payload": {
    "message": "Game is full"
  }
}
```

Mensaje enviado por el servidor cuando ocurre un error o validación negativa.

---

## ✅ Recomendaciones

- Usar UTF-8 para la codificación.
- Conexión vía socket TCP persistente.
- Mantener consistencia en las claves y tipos de datos.

---

Este protocolo garantiza la interoperabilidad entre el servidor en C y los clientes en Java.
