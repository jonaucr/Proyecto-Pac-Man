//// Envío y recepción de mensajes json
#include "protocol.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/socket.h>



cJSON* recibir_json(int fd) {
    char buffer[1024] = {0};
    int bytes_read = read(fd, buffer, sizeof(buffer) - 1);
    if (bytes_read <= 0) return NULL;
    buffer[bytes_read] = '\0';
    return cJSON_Parse(buffer);
}

int enviar_json(int fd, cJSON* json) {
    char* json_str = cJSON_PrintUnformatted(json);
    int len = strlen(json_str);

    // Reservar espacio para el JSON + '\n' + '\0'
    char* buffer = malloc(len + 2);
    strcpy(buffer, json_str);
    buffer[len] = '\n';
    buffer[len + 1] = '\0';

    int result = send(fd, buffer, strlen(buffer), 0);

    free(json_str);
    free(buffer);
    return result;
}