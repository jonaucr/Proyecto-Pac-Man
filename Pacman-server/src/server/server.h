//Configura socket y escucha conexiones
#ifndef SERVER_H
#define SERVER_H

int inicializar_servidor(int port);
void esperar_cliente(int server_fd);

#endif // SERVER_H

