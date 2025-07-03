#include "handler.h"
#include <string.h>
#include <stdio.h>

// Tabla de handlers
static MessageHandlerEntry handlers[] = {
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