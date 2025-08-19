//// Lógica del juego y estados por partida
#ifndef GAME_H
#define GAME_H

#define MAP_WIDTH 20
#define MAP_HEIGHT 15
#define MAX_PLAYERS 2
#define MAX_OBSERVERS 10
#define MAX_GHOSTS 4
#define MAX_FRUITS 5
#define WALL  1
#define PATH  0
#define FRUIT 2
#define MAX_GAMES 5


typedef struct {
    int x;
    int y;
    int score;
    int lives;
    int invulnerable; // ticks de invulnerabilidad (0 = puede recibir daño)
    int socket;
} Player;

typedef struct {
    int x, y;
    char color[10];
}Ghost;

typedef struct {
    int x, y;
    char type[16];
} Fruit;

typedef struct {
    char id[10];
    int map[MAP_HEIGHT][MAP_WIDTH];

    Player  players[MAX_PLAYERS];
    int num_players;

    int observers[MAX_OBSERVERS];
    int num_observers;

    Ghost ghosts[MAX_GHOSTS];
    int num_ghosts; 
    Fruit fruits[MAX_FRUITS];
    int num_fruits; 

    int game_state; // 0 = en curso, 1 = finalizada
} Game;

extern Game partidas[MAX_GAMES];
extern int num_partidas;


Game crear_nueva_partida(char *id);

#endif