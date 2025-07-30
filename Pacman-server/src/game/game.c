//// Lógica del juego y estados por partida

#include "game.h"
#include <string.h>
#include <stdio.h>
#include <stdlib.h>

#define WALL 1
#define PATH 0
#define FRUIT 2
Game partidas[MAX_GAMES];
int num_partidas = 0;


void inicializar_mapa(int map[MAP_HEIGHT][MAP_WIDTH]) {
    // Plantilla del laberinto. 1 = Muro (WALL), 0 = Pasillo (PATH)
    // Puedes diseñar visualmente tu nivel aquí.
    int level_template[MAP_HEIGHT][MAP_WIDTH] = {
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,1},
        {1,0,1,1,0,1,1,1,0,1,1,0,1,1,1,0,1,1,0,1},
        {1,0,1,1,0,1,1,1,0,1,1,0,1,1,1,0,1,1,0,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,1,1,0,1,0,1,1,1,1,1,1,0,1,0,1,1,0,1},
        {1,0,0,0,0,1,0,0,0,1,1,0,0,0,1,0,0,0,0,1},
        {1,1,1,1,0,1,1,1,0,1,1,0,1,1,1,0,1,1,1,1},
        {1,0,0,0,0,1,0,0,0,1,1,0,0,0,1,0,0,0,0,1},
        {1,0,1,1,0,1,0,1,1,1,1,1,1,0,1,0,1,1,0,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,1,1,0,1,1,1,0,1,1,0,1,1,1,0,1,1,0,1},
        {1,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,1},
        {1,0,0,0,0,1,1,1,0,1,1,0,1,1,1,0,0,0,0,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
    };

    // Copiar la plantilla al mapa del juego
    for (int y = 0; y < MAP_HEIGHT; y++) {
        for (int x = 0; x < MAP_WIDTH; x++) {
            map[y][x] = level_template[y][x];
        }
    }
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
