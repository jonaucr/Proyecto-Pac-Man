#include "handler.h"
#include <string.h>
#include <stdio.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include "game/game.h"
#include "protocol/protocol.h"
#include <stdlib.h> // Para rand() y srand()


void enviar_estado_juego(Game* partida, int client_fd);

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

// Genera cuatro fantasmas en posiciones aleatorias válidas
void generar_fantasmas(Game* partida, int player_x, int player_y) {
    int pos[4][2];
    int count = 0;
    while (count < 4) {
        int x = rand() % MAP_WIDTH;
        int y = rand() % MAP_HEIGHT;
        int ocupado = 0;
        // No sobre el jugador
        if (x == player_x && y == player_y) continue;
        // No sobre una pared
        if (partida->map[y][x] == 1) continue;
        // No sobre otro fantasma
        for (int i = 0; i < count; i++) {
            if (pos[i][0] == x && pos[i][1] == y) {
                ocupado = 1;
                break;
            }
        }
        if (ocupado) continue;
        pos[count][0] = x;
        pos[count][1] = y;
        count++;
    }

    // Asignar posiciones y colores
    partida->ghosts[0].x = pos[0][0];
    partida->ghosts[0].y = pos[0][1];
    strcpy(partida->ghosts[0].color, "red");      // Blinky

    partida->ghosts[1].x = pos[1][0];
    partida->ghosts[1].y = pos[1][1];
    strcpy(partida->ghosts[1].color, "magenta");     // Pinky

    partida->ghosts[2].x = pos[2][0];
    partida->ghosts[2].y = pos[2][1];
    strcpy(partida->ghosts[2].color, "blue");     // Inky (azul claro)

    partida->ghosts[3].x = pos[3][0];
    partida->ghosts[3].y = pos[3][1];
    strcpy(partida->ghosts[3].color, "orange");   // Clyde

    partida->num_ghosts = 4;
}

//Handle para crear una partida
void handle_create_game(cJSON* payload, int client_fd) {
    printf("Handler: create_game\n");

        // Obtener el puerto del cliente
    struct sockaddr_in addr;
    socklen_t addr_len = sizeof(addr);
    int client_port = 0;
    if (getpeername(client_fd, (struct sockaddr*)&addr, &addr_len) == 0) {
        client_port = ntohs(addr.sin_port);
        printf("Puerto del cliente: %d\n", client_port);
    }

    // Crear partida con un ID
    char partida_id[16];
    snprintf(partida_id, sizeof(partida_id), "%d", client_port);

    if (num_partidas < MAX_GAMES) {
    partidas[num_partidas] = crear_nueva_partida(partida_id);
    Game* partida = &partidas[num_partidas];
    num_partidas++;

    // Crear jugador y asignar posición inicial
    Player nuevo_jugador;
    nuevo_jugador.x = 2;
    nuevo_jugador.y = 1;
    nuevo_jugador.score = 0;
    nuevo_jugador.socket = client_port;
    nuevo_jugador.lives = 3; // Inicializa vidas
    partida->players[0] = nuevo_jugador;
    partida->num_players = 1;

    // Ejemplo de ghost y fruit crearlos y la posición inicial
    //partida->ghosts[0].x = 4;
   // partida->ghosts[0].y = 3;
   // strcpy(partida->ghosts[0].color, "red");
   //partida->num_ghosts = 1;
    
   generar_fantasmas(partida, nuevo_jugador.x, nuevo_jugador.y);
   

    partida->fruits[0].x = 4;
    partida->fruits[0].y = 5;
    strcpy(partida->fruits[0].type, "cherry");
    partida->num_fruits = 1;



    
    // Construir el JSON único
    cJSON* respuesta = cJSON_CreateObject();
    cJSON_AddStringToObject(respuesta, "type", "game_state");
    cJSON* payload_resp = cJSON_CreateObject();

    // Datos básicos
    cJSON_AddStringToObject(payload_resp, "id", partida->id);
    cJSON_AddNumberToObject(payload_resp, "port", client_port);

    // Mapa
    cJSON* mapa_arr = cJSON_CreateArray();
    for (int y = 0; y < MAP_HEIGHT; y++) {
        cJSON* fila = cJSON_CreateArray();
        for (int x = 0; x < MAP_WIDTH; x++) {
            cJSON_AddItemToArray(fila, cJSON_CreateNumber(partida->map[y][x]));
        }
        cJSON_AddItemToArray(mapa_arr, fila);
    }
    cJSON_AddItemToObject(payload_resp, "map", mapa_arr);

    // Players
    cJSON* players_arr = cJSON_CreateArray();
    for (int i = 0; i < partida->num_players; i++) {
        cJSON* player_json = cJSON_CreateObject();
        cJSON_AddNumberToObject(player_json, "x", partida->players[i].x);
        cJSON_AddNumberToObject(player_json, "y", partida->players[i].y);
        cJSON_AddNumberToObject(player_json, "score", partida->players[i].score);
        cJSON_AddNumberToObject(player_json, "lives", partida->players[i].lives);
        cJSON_AddItemToArray(players_arr, player_json);
    }
    cJSON_AddItemToObject(payload_resp, "players", players_arr);

    // Ghosts
    cJSON* ghosts_arr = cJSON_CreateArray();
    for (int i = 0; i < partida->num_ghosts; i++) {
        cJSON* ghost_json = cJSON_CreateObject();
        cJSON_AddNumberToObject(ghost_json, "x", partida->ghosts[i].x);
        cJSON_AddNumberToObject(ghost_json, "y", partida->ghosts[i].y);
        cJSON_AddStringToObject(ghost_json, "color", partida->ghosts[i].color);
        cJSON_AddItemToArray(ghosts_arr, ghost_json);
    }
    cJSON_AddItemToObject(payload_resp, "ghosts", ghosts_arr);

    // Fruits
    cJSON* fruits_arr = cJSON_CreateArray();
    for (int i = 0; i < partida->num_fruits; i++) {
        cJSON* fruit_json = cJSON_CreateObject();
        cJSON_AddNumberToObject(fruit_json, "x", partida->fruits[i].x);
        cJSON_AddNumberToObject(fruit_json, "y", partida->fruits[i].y);
        cJSON_AddStringToObject(fruit_json, "type", partida->fruits[i].type);
        cJSON_AddItemToArray(fruits_arr, fruit_json);
    }
    cJSON_AddItemToObject(payload_resp, "fruits", fruits_arr);

    cJSON_AddItemToObject(respuesta, "payload", payload_resp);

    enviar_json(client_fd, respuesta);
    cJSON_Delete(respuesta);

    printf("Partida creada con ID: %s\n", partida->id);
    printf("Jugador creado en posición (%d, %d), puerto %d\n", nuevo_jugador.x, nuevo_jugador.y, client_port);


    printf("Partida creada: %s\n", partida->id);
    } else {
    printf("No se pueden crear más partidas\n");
    }


 


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

//Handle para mover un jugador
void handle_move(cJSON* payload, int client_fd) {
    printf("Handler: move\n");

    cJSON* dir_json = cJSON_GetObjectItemCaseSensitive(payload, "direction");
    if (!cJSON_IsString(dir_json) || dir_json->valuestring == NULL) {
        printf("Dirección inválida\n");
        return;
    }
    const char* direction = dir_json->valuestring;

    // Obtener el puerto del cliente
    struct sockaddr_in addr;
    socklen_t addr_len = sizeof(addr);
    int client_port = 0;
    if (getpeername(client_fd, (struct sockaddr*)&addr, &addr_len) == 0) {
        client_port = ntohs(addr.sin_port);
        printf("Puerto del cliente: %d\n", client_port);
    }

    printf("Dirección: %s\n", direction);
    printf("ID de la partida: %s\n", partidas[0].id);
    printf("ID del jugador: %d\n", partidas[0].players[0].x);
    printf("ID del cliente: %d\n", client_port);
    printf(num_partidas > 0 ? "Número de partidas: %d\n" : "No hay partidas\n", num_partidas);
    
    // Buscar la partida y el jugador por el socket
    Game* partida = NULL;
    Player* jugador = NULL;
    
    for (int i = 0; i < num_partidas; i++) {
        for (int j = 0; j < partidas[i].num_players; j++) {
            if (partidas[i].players[j].socket == client_port) {
                printf("Jugador encontrado en partida %s\n", partidas[i].id);
                partida = &partidas[i];
                jugador = &partidas[i].players[j];
                break;
            }
        }
        if (jugador) break;
    }
    if (!jugador || !partida) {
        printf("Jugador no encontrado\n");
        return;
    }

    // Calcular nueva posición
    int new_x = jugador->x;
    int new_y = jugador->y;
    if (strcmp(direction, "up") == 0) new_y--;
    else if (strcmp(direction, "down") == 0) new_y++;
    else if (strcmp(direction, "left") == 0) new_x--;
    else if (strcmp(direction, "right") == 0) new_x++;

    // Validar que no se mueva a una pared
    if (new_x >= 0 && new_x < MAP_WIDTH && new_y >= 0 && new_y < MAP_HEIGHT &&
        partida->map[new_y][new_x] != 1) { // 1 representa una pared
        // Actualizar posición del jugador
        jugador->x = new_x;
        jugador->y = new_y;
        printf("Jugador movido a (%d, %d)\n", jugador->x, jugador->y);
    } else {
        printf("Movimiento bloqueado por pared o fuera de límites\n");
    }

     // Mover fantasmas después de mover al jugador
    for (int i = 0; i < partida->num_ghosts; i++) {
        int dir = rand() % 4; // 0: arriba, 1: abajo, 2: izquierda, 3: derecha
        int gx = partida->ghosts[i].x;
        int gy = partida->ghosts[i].y;
        int new_gx = gx, new_gy = gy;
        if (dir == 0) new_gy--;
        else if (dir == 1) new_gy++;
        else if (dir == 2) new_gx--;
        else if (dir == 3) new_gx++;

        // Validar que no se mueva a una pared ni salga del mapa
        if (new_gx >= 0 && new_gx < MAP_WIDTH && new_gy >= 0 && new_gy < MAP_HEIGHT &&
            partida->map[new_gy][new_gx] != 1) {
            partida->ghosts[i].x = new_gx;
            partida->ghosts[i].y = new_gy;
        }
    }

    // Opcional: enviar el nuevo estado al cliente
    enviar_estado_juego(partida, client_fd);
}

void enviar_estado_juego(Game* partida, int client_fd) {
    cJSON* respuesta = cJSON_CreateObject();
    cJSON_AddStringToObject(respuesta, "type", "game_state");
    cJSON* payload_resp = cJSON_CreateObject();

    // Datos básicos
    cJSON_AddStringToObject(payload_resp, "id", partida->id);

    // Mapa
    cJSON* mapa_arr = cJSON_CreateArray();
    for (int y = 0; y < MAP_HEIGHT; y++) {
        cJSON* fila = cJSON_CreateArray();
        for (int x = 0; x < MAP_WIDTH; x++) {
            cJSON_AddItemToArray(fila, cJSON_CreateNumber(partida->map[y][x]));
        }
        cJSON_AddItemToArray(mapa_arr, fila);
    }
    cJSON_AddItemToObject(payload_resp, "map", mapa_arr);

    // Players
    cJSON* players_arr = cJSON_CreateArray();
    for (int i = 0; i < partida->num_players; i++) {
        cJSON* player_json = cJSON_CreateObject();
        cJSON_AddNumberToObject(player_json, "x", partida->players[i].x);
        cJSON_AddNumberToObject(player_json, "y", partida->players[i].y);
        cJSON_AddNumberToObject(player_json, "score", partida->players[i].score);
        cJSON_AddItemToArray(players_arr, player_json);
    }
    cJSON_AddItemToObject(payload_resp, "players", players_arr);

    // Ghosts
    cJSON* ghosts_arr = cJSON_CreateArray();
    for (int i = 0; i < partida->num_ghosts; i++) {
        cJSON* ghost_json = cJSON_CreateObject();
        cJSON_AddNumberToObject(ghost_json, "x", partida->ghosts[i].x);
        cJSON_AddNumberToObject(ghost_json, "y", partida->ghosts[i].y);
        cJSON_AddStringToObject(ghost_json, "color", partida->ghosts[i].color);
        cJSON_AddItemToArray(ghosts_arr, ghost_json);
    }
    cJSON_AddItemToObject(payload_resp, "ghosts", ghosts_arr);

    // Fruits
    cJSON* fruits_arr = cJSON_CreateArray();
    for (int i = 0; i < partida->num_fruits; i++) {
        cJSON* fruit_json = cJSON_CreateObject();
        cJSON_AddNumberToObject(fruit_json, "x", partida->fruits[i].x);
        cJSON_AddNumberToObject(fruit_json, "y", partida->fruits[i].y);
        cJSON_AddStringToObject(fruit_json, "type", partida->fruits[i].type);
        cJSON_AddItemToArray(fruits_arr, fruit_json);
    }
    cJSON_AddItemToObject(payload_resp, "fruits", fruits_arr);

    cJSON_AddItemToObject(respuesta, "payload", payload_resp);

    enviar_json(client_fd, respuesta);
    cJSON_Delete(respuesta);
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