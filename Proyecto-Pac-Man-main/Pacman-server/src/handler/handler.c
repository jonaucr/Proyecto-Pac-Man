#include "handler.h"
#include <string.h>
#include <stdio.h>
#include "game/game.h"
#include "protocol/protocol.h"

// Tabla de handlers
static MessageHandlerEntry handlers[] = {
    {"create_game", handle_create_game},
    {"disconnect", handle_disconnect},
    {"move", handle_move},
    {"update_score", handle_update_score},
    {"join_game", handle_join_game},
    {"leave_game", handle_leave_game},
    // ...agrega aquí todos los tipos que necesites
    {NULL, NULL} // Fin de la tabla
};

// Dispatcher: busca el handler por type y lo llama
void dispatch_message(const char* type, cJSON* payload, int client_fd) {
    for (int i = 0; handlers[i].type != NULL; ++i) {
        if (strcmp(type, handlers[i].type) == 0) {
            handlers[i].handler(payload, client_fd);
            return;
        }
    }
    // Handler por defecto si no se encuentra el type
    printf("Tipo de mensaje desconocido: %s\n", type);
}

//Handle para crear una partida
void handle_create_game(cJSON* payload, int client_fd) {
    printf("Handler: create_game\n");
    Game mi_partida = crear_nueva_partida("abc123");

    // Mostrar el mapa en consola
    for (int y = 0; y < MAP_HEIGHT; y++) {
        for (int x = 0; x < MAP_WIDTH; x++) {
            printf("%d ", mi_partida.map[y][x]);
        }
        printf("\n");
    }
    printf("Partida creada con ID: %s\n", mi_partida.id);
}

// Handle para desconectar un cliente
void handle_disconnect(cJSON* payload, int client_fd) {
    cJSON* respuesta = cJSON_CreateObject();
    cJSON_AddStringToObject(respuesta, "type", "disconnected");
    cJSON* payload_resp = cJSON_CreateObject();
    cJSON_AddStringToObject(payload_resp, "mensaje", "Desconectado correctamente");
    cJSON_AddItemToObject(respuesta, "payload", payload_resp);
    enviar_json(client_fd, respuesta);
    cJSON_Delete(respuesta);
}


// Implementación vacía de los handlers (solo estructura)
void handle_move(cJSON* payload, int client_fd) {
    printf("Handler: move\n");
    // Aquí irá la lógica de movimiento
}
void handle_update_score(cJSON* payload, int client_fd) {
    printf("Handler: update_score\n");
    // Aquí irá la lógica de puntaje
}
void handle_join_game(cJSON* payload, int client_fd) {
    printf("Handler: join_game\n");
    // Aquí irá la lógica para unirse al juego
}
void handle_leave_game(cJSON* payload, int client_fd) {
    printf("Handler: leave_game\n");
    // Aquí irá la lógica para salir del juego
}