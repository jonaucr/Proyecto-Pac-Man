#include "handler.h"
#include <string.h>
#include <stdio.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include "game/game.h"
#include "protocol/protocol.h"
#include <stdlib.h> // Para rand() y srand()
#include <limits.h> // Para INT_MAX


void broadcast_game_state(Game* partida);
void broadcast_message(Game* partida, cJSON* message);
void enviar_estado_juego(Game* partida, int client_fd);

// Tabla de handlers
static MessageHandlerEntry handlers[] = {
    {"create_game", handle_create_game},
    {"disconnect", handle_disconnect},
    {"move", handle_move},
    {"join_spectator", handle_join_spectator},
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
    partida->ghosts[0].start_x = pos[0][0];
    partida->ghosts[0].start_y = pos[0][1];
    strcpy(partida->ghosts[0].color, "red"); // Blinky

    partida->ghosts[1].x = pos[1][0];
    partida->ghosts[1].y = pos[1][1];
    partida->ghosts[1].start_x = pos[1][0];
    partida->ghosts[1].start_y = pos[1][1];
    strcpy(partida->ghosts[1].color, "magenta"); // Pinky

    partida->ghosts[2].x = pos[2][0];
    partida->ghosts[2].y = pos[2][1];
    partida->ghosts[2].start_x = pos[2][0];
    partida->ghosts[2].start_y = pos[2][1];
    strcpy(partida->ghosts[2].color, "blue"); // Inky (azul claro)

    partida->ghosts[3].x = pos[3][0];
    partida->ghosts[3].y = pos[3][1];
    partida->ghosts[3].start_x = pos[3][0];
    partida->ghosts[3].start_y = pos[3][1];
    strcpy(partida->ghosts[3].color, "orange"); // Clyde

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
    nuevo_jugador.socket = client_fd; // Usar el file descriptor como ID único
    nuevo_jugador.lives = 3; // Inicializa vidas
    nuevo_jugador.invulnerable = 0;
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

    // Buscar la partida y el jugador por el file descriptor del socket
    Game* partida = NULL;
    Player* jugador = NULL;

    for (int i = 0; i < num_partidas; i++) {
        for (int j = 0; j < partidas[i].num_players; j++) {
            if (partidas[i].players[j].socket == client_fd) {
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

    // Calcular nueva posición del jugador
    int new_x = jugador->x;
    int new_y = jugador->y;
    if (strcmp(direction, "up") == 0) new_y--;
    else if (strcmp(direction, "down") == 0) new_y++;
    else if (strcmp(direction, "left") == 0) new_x--;
    else if (strcmp(direction, "right") == 0) new_x++;

    // Validar movimiento del jugador (no muro, dentro del mapa)
    if (new_x >= 0 && new_x < MAP_WIDTH && new_y >= 0 && new_y < MAP_HEIGHT &&
        partida->map[new_y][new_x] != WALL) {

        // --- LÓGICA DE PUNTUACIÓN Y VICTORIA ---
        // 1. Verificar si la nueva casilla tiene un punto (valor 2)
        if (partida->map[new_y][new_x] == DOT) {
            jugador->score += 1; // Incrementar puntaje
            partida->map[new_y][new_x] = PATH; // Eliminar el punto del mapa (convertirlo en camino)
            printf("Jugador comió un punto. Score: %d\n", jugador->score);

            // 2. Comprobar si quedan más puntos en el mapa (condición de victoria)
            int puntos_restantes = 0;
            for (int y = 0; y < MAP_HEIGHT; y++) {
                for (int x = 0; x < MAP_WIDTH; x++) {
                    if (partida->map[y][x] == DOT) {
                        puntos_restantes = 1; // Encontramos un punto, el juego sigue
                        break;
                    }
                }
                if (puntos_restantes) break;
            }

            // 3. Si no quedan puntos, el jugador gana
            if (!puntos_restantes) {
                cJSON* win_msg = cJSON_CreateObject();
                cJSON_AddStringToObject(win_msg, "type", "game_win");
                cJSON* payload_win = cJSON_CreateObject();
                cJSON_AddStringToObject(payload_win, "mensaje", "¡Felicidades! Has comido todos los puntos.");
                cJSON_AddNumberToObject(payload_win, "final_score", jugador->score);
                cJSON_AddItemToObject(win_msg, "payload", payload_win);
                broadcast_message(partida, win_msg); // <-- CAMBIO: Notificar a todos
                cJSON_Delete(win_msg);
                printf("¡Juego ganado! Puntaje final: %d\n", jugador->score);
                return; // Termina el handler aquí, ya no es necesario enviar game_state
            }
        }

        jugador->x = new_x;
        jugador->y = new_y;
        printf("Jugador movido a (%d, %d)\n", jugador->x, jugador->y);
    } else {
        printf("Movimiento bloqueado por pared o fuera de límites\n");
    }

    // --- LÓGICA DE MOVIMIENTO DE FANTASMAS (HÍBRIDO: INTELIGENTE + ALEATORIO) ---
    for (int i = 0; i < partida->num_ghosts; i++) {
        Ghost* fantasma = &partida->ghosts[i];
        int px = jugador->x;
        int py = jugador->y;
        int posibles_movimientos[4][2] = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        int chance = rand() % 100; // Genera un número entre 0 y 99

        // 50% de probabilidad de moverse inteligentemente, 50% de moverse al azar
        if (chance < 50) {
            // --- LÓGICA INTELIGENTE (PERSECUCIÓN) ---
            int mejor_movimiento = -1, mejor_distancia = INT_MAX;
            for (int j = 0; j < 4; j++) {
                int n_gx = fantasma->x + posibles_movimientos[j][0], n_gy = fantasma->y + posibles_movimientos[j][1];
                if (n_gx >= 0 && n_gx < MAP_WIDTH && n_gy >= 0 && n_gy < MAP_HEIGHT && partida->map[n_gy][n_gx] != WALL) {
                    int dist = abs(n_gx - px) + abs(n_gy - py);
                    if (dist < mejor_distancia) {
                        mejor_distancia = dist;
                        mejor_movimiento = j;
                    }
                }
            }
            if (mejor_movimiento != -1) {
                fantasma->x += posibles_movimientos[mejor_movimiento][0];
                fantasma->y += posibles_movimientos[mejor_movimiento][1];
            }
        } else {
            // --- LÓGICA ALEATORIA ---
            int dir = rand() % 4;
            int n_gx = fantasma->x + posibles_movimientos[dir][0], n_gy = fantasma->y + posibles_movimientos[dir][1];
            if (n_gx >= 0 && n_gx < MAP_WIDTH && n_gy >= 0 && n_gy < MAP_HEIGHT && partida->map[n_gy][n_gx] != WALL) {
                fantasma->x = n_gx;
                fantasma->y = n_gy;
            }
        }
    }

    // --- LÓGICA DE COLISIÓN SIMPLE ---
    for (int i = 0; i < partida->num_ghosts; i++) {
        if (jugador->x == partida->ghosts[i].x && jugador->y == partida->ghosts[i].y) {
            if (jugador->invulnerable == 0) {
                jugador->lives--;
                jugador->invulnerable = 10; // Ticks de invulnerabilidad post-golpe
                printf("¡Pac-Man perdió una vida! Vidas restantes: %d\n", jugador->lives);

                // Enviar mensaje "player_hit"
                cJSON* hit_msg = cJSON_CreateObject();
                cJSON_AddStringToObject(hit_msg, "type", "player_hit");
                cJSON* payload_hit = cJSON_CreateObject();
                cJSON_AddStringToObject(payload_hit, "mensaje", "Pac-Man fue golpeado");
                cJSON_AddNumberToObject(payload_hit, "lives", jugador->lives);
                cJSON_AddNumberToObject(payload_hit, "score", jugador->score);
                cJSON_AddItemToObject(hit_msg, "payload", payload_hit);
                enviar_json(client_fd, hit_msg);
                cJSON_Delete(hit_msg);

                // Si se quedó sin vidas, terminar el juego
                if (jugador->lives <= 0) {
                    cJSON* respuesta = cJSON_CreateObject();
                    cJSON_AddStringToObject(respuesta, "type", "game_over");
                    cJSON* payload_resp = cJSON_CreateObject();
                    cJSON_AddStringToObject(payload_resp, "mensaje", "Juego terminado");
                    cJSON_AddNumberToObject(payload_resp, "final_score", jugador->score);
                    cJSON_AddItemToObject(respuesta, "payload", payload_resp);
                    broadcast_message(partida, respuesta); // <-- CAMBIO: Notificar a todos
                    cJSON_Delete(respuesta);
                    printf("Juego terminado. Puntaje final: %d\n", jugador->score);
                    return; // Terminar handler
                }
                break; // Solo un golpe por tick
            }
        }
    }

    // --- ACTUALIZACIÓN DE ESTADO DEL JUGADOR AL FINAL DEL TICK ---
    if (jugador->invulnerable > 0) jugador->invulnerable--;

    // Enviar el nuevo estado a todos en la partida (jugadores y observadores)
    broadcast_game_state(partida);
}

void enviar_estado_juego(Game* partida, int client_fd) {
    cJSON* respuesta = cJSON_CreateObject();
    cJSON_AddStringToObject(respuesta, "type", "game_state");
    cJSON* payload_resp = cJSON_CreateObject();

    // Datos básicos
    cJSON_AddStringToObject(payload_resp, "id", partida->id);
    cJSON_AddNumberToObject(payload_resp, "spectators", partida->num_observers);

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
}

void broadcast_game_state(Game* partida) {
    // Enviar a todos los jugadores
    for (int i = 0; i < partida->num_players; i++) {
        enviar_estado_juego(partida, partida->players[i].socket);
    }
    // Enviar a todos los observadores
    for (int i = 0; i < partida->num_observers; i++) {
        enviar_estado_juego(partida, partida->observers[i]);
    }
}

void broadcast_message(Game* partida, cJSON* message) {
    // Enviar a todos los jugadores
    for (int i = 0; i < partida->num_players; i++) {
        enviar_json(partida->players[i].socket, message);
    }
    // Enviar a todos los observadores
    for (int i = 0; i < partida->num_observers; i++) {
        enviar_json(partida->observers[i], message);
    }
}

void handle_join_spectator(cJSON* payload, int client_fd) {
    printf("Handler: join_spectator\n");

    cJSON* id_json = cJSON_GetObjectItemCaseSensitive(payload, "id");
    if (!cJSON_IsString(id_json) || id_json->valuestring == NULL) {
        printf("ID de partida inválido en la solicitud de observador\n");
        return;
    }
    const char* game_id = id_json->valuestring;

    Game* partida = NULL;
    for (int i = 0; i < num_partidas; i++) {
        if (strcmp(partidas[i].id, game_id) == 0) {
            partida = &partidas[i];
            break;
        }
    }

    if (partida) {
        if (partida->num_observers < MAX_OBSERVERS) {
            partida->observers[partida->num_observers] = client_fd;
            partida->num_observers++;
            printf("Nuevo observador se unió a la partida %s. Total: %d\n", game_id, partida->num_observers);
            
            // Notificar a todos en la partida, incluyendo al nuevo observador
            broadcast_game_state(partida);
        } else {
            printf("La partida %s está llena de observadores.\n", game_id);
            cJSON* respuesta = cJSON_CreateObject();
            cJSON_AddStringToObject(respuesta, "type", "error");
            cJSON* payload_resp = cJSON_CreateObject();
            cJSON_AddStringToObject(payload_resp, "mensaje", "La partida está llena de observadores.");
            cJSON_AddItemToObject(respuesta, "payload", payload_resp);
            enviar_json(client_fd, respuesta);
            cJSON_Delete(respuesta);
        }
    } else {
        printf("Partida no encontrada con ID: %s\n", game_id);
        cJSON* respuesta = cJSON_CreateObject();
        cJSON_AddStringToObject(respuesta, "type", "error");
        cJSON* payload_resp = cJSON_CreateObject();
        cJSON_AddStringToObject(payload_resp, "mensaje", "Partida no encontrada.");
        cJSON_AddItemToObject(respuesta, "payload", payload_resp);
        enviar_json(client_fd, respuesta);
        cJSON_Delete(respuesta);
    }
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