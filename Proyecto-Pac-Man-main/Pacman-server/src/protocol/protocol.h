//// Envío y recepción de mensajes json
#ifndef PROTOCOL_H
#define PROTOCOL_H
#include <cjson/cJSON.h>

cJSON* recibir_json(int fd);
int enviar_json(int fd, cJSON* json);

#endif