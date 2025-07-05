
#include "server/server.h"
#include "protocol/protocol.h"
#include "game/game.h"


int main() {
    

    int puerto = 5000;
    int server_fd = inicializar_servidor(puerto);


    // El servidor acepta múltiples clientes en un bucle infinito
    esperar_cliente(server_fd);

   
    return 0;
}
