#ifndef HANDLER_H
#define HANDLER_H

#include <cjson/cJSON.h>

// Prototipo de handler
typedef void (*message_handler_t)(cJSON* payload, int client_fd);

// Estructura para mapear type a handler
typedef struct {
    const char* type;
    message_handler_t handler;
} MessageHandlerEntry;

// Declaración de la función dispatcher
void dispatch_message(const char* type, cJSON* payload, int client_fd);



// Declaración de handlers (solo prototipos, la lógica va después)
void handle_create_game(cJSON* payload, int client_fd);
void handle_disconnect(cJSON* payload, int client_fd);
void handle_move(cJSON* payload, int client_fd);
void handle_update_score(cJSON* payload, int client_fd);
void handle_join_game(cJSON* payload, int client_fd);
void handle_leave_game(cJSON* payload, int client_fd);
void handle_join_spectator(cJSON* payload, int client_fd);
// ...agrega aquí todos los tipos que necesites

#endif