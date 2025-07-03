#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <cjson/cJSON.h>
#include <pthread.h>
#include "server.h"
#include "../protocol/protocol.h"
#include "../handler/handler.h"

//Configura socket y escucha conexiones
int inicializar_servidor(int port) {

    int server_fd;
    struct sockaddr_in address;

      // Crear socket TCP
    server_fd = socket(AF_INET, SOCK_STREAM, 0);
    if (server_fd == -1) {
        perror("Error al crear el socket");
        exit(EXIT_FAILURE);
    }

     // Configurar dirección
    address.sin_family = AF_INET;
    address.sin_addr.s_addr = INADDR_ANY;
    address.sin_port = htons(port);

    // Asociar socket a dirección y puerto
    if (bind(server_fd, (struct sockaddr*)&address, sizeof(address)) < 0) {
        perror("Error en bind");
        close(server_fd);
        exit(EXIT_FAILURE);
    }

    // Escuchar conexiones entrantes
    if (listen(server_fd, 3) < 0) {
        perror("Error en listen");
        close(server_fd);
        exit(EXIT_FAILURE);
    }

    printf("Servidor escuchando en el puerto %d...\n", port);
    return server_fd;

}


// Maneja la comunicación con un cliente en un hilo separado
// Recibe un mensaje JSON, lo analiza y despacha al handler correspondiente
void* manejar_cliente(void* arg) {
    int client_fd = *(int*)arg;
    free(arg);

    cJSON* mensaje = recibir_json(client_fd);
    if (!mensaje) {
        printf("Mensaje inválido\n");
        close(client_fd);
        return NULL;
    }

    cJSON* type = cJSON_GetObjectItemCaseSensitive(mensaje, "type");
    cJSON* payload = cJSON_GetObjectItemCaseSensitive(mensaje, "payload");

    if (cJSON_IsString(type) && type->valuestring) {
        dispatch_message(type->valuestring, payload, client_fd);
    } else {
        printf("Mensaje sin campo 'type' válido\n");
    }

    cJSON_Delete(mensaje);
    close(client_fd);
    printf("Cliente desconectado.\n");
    return NULL;
}


// Espera conexiones de clientes y crea un hilo para cada uno
// Cada hilo maneja la comunicación con el cliente de forma independiente
void esperar_cliente(int server_fd) {
    struct sockaddr_in cliente_addr;
    socklen_t cliente_len = sizeof(cliente_addr);

    while (1) {
        int* client_fd = malloc(sizeof(int));
        *client_fd = accept(server_fd, (struct sockaddr *)&cliente_addr, &cliente_len);
        if (*client_fd < 0) {
            perror("Error al aceptar conexión");
            free(client_fd);
            continue;
        }
        printf("Cliente conectado.\n");

        pthread_t thread_id;
        pthread_create(&thread_id, NULL, manejar_cliente, client_fd);
        pthread_detach(thread_id); // Libera recursos del hilo automáticamente al terminar
    }
}