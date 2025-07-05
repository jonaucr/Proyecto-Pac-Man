//// Lógica del juego y estados por partida
#ifndef GAME_H
#define GAME_H

#define MAP_WIDTH 20
#define MAP_HEIGHT 15
#define MAX_PLAYERS 2
#define MAX_OBSERVERS 10
#define MAX_GHOSTS 4
#define MAX_FRUITS 5

typedef struct {
    int x, y;
    int score;
    int socket;
} Player;

typedef struct {
    int x, y;
    char color[10];
} Ghost;

typedef struct {
    int x, y;
    char tipo[10];
} Fruit;

typedef struct {
    char id[10];
    int map[MAP_HEIGHT][MAP_WIDTH];

    Player  players[MAX_PLAYERS];
    int num_players;

    int observers[MAX_OBSERVERS];
    int num_observers;

    Ghost ghosts[MAX_GHOSTS];
    Fruit fruits[MAX_FRUITS];

    int game_state; // 0 = en curso, 1 = finalizada
} Game;

Game crear_nueva_partida(char *id);

#endif