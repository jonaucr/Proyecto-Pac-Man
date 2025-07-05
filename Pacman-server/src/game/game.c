//// Lógica del juego y estados por partida

#include "game.h"
#include <string.h>
#include <stdio.h>
#include <stdlib.h>

#define WALL 1
#define PATH 0
#define FRUIT 2



void inicializar_mapa(int map[MAP_HEIGHT][MAP_WIDTH]) {
    for (int y = 0; y < MAP_HEIGHT; y++) {
        for (int x = 0; x < MAP_WIDTH; x++) {
            if (y == 0 || y == MAP_HEIGHT - 1 || x == 0 || x == MAP_WIDTH - 1) {
                map[y][x] = WALL; // Bordes del mapa
            } else {
                map[y][x] = PATH; // Espacio vacío
            }
        }
    }

    //map[MAP_HEIGHT/2][MAP_WIDTH/2] = FRUIT; // Ejemplo de fruta en una posición específica

}

Game crear_nueva_partida(char *id) {
    Game game;
    strncpy(game.id, id, sizeof(game.id) - 1);
    game.id[sizeof(game.id) - 1] = '\0';

    //Inicializar mapa
    inicializar_mapa(game.map);

    game.num_players = 0;
    game.num_observers = 0;
    game.game_state = 0; // 0 = en curso, 1 = finalizada

    // Inicializa jugadores, observadores, fantasmas, frutas
    memset(game.players, 0, sizeof(game.players));
    memset(game.observers, 0, sizeof(game.observers));
    memset(game.ghosts, 0, sizeof(game.ghosts));
    memset(game.fruits, 0, sizeof(game.fruits));

   

    return game;
}