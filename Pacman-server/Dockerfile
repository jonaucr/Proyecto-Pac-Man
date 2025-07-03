# Imagen base con GCC y herramientas necesarias
FROM gcc:latest

# Carpeta de trabajo dentro del contenedor
WORKDIR /app

# Copiar el código fuente al contenedor
COPY ./src /app

# Instalar librería de cJSON si la vas a usar
RUN apt-get update && apt-get install -y libcjson-dev

# Compilar el programa
RUN gcc -o pacman_server main.c server/server.c protocol/protocol.c handler/handler.c -I. -lcjson -lpthread

# Exponer el puerto para comunicación
EXPOSE 5000

# Ejecutar el servidor al iniciar el contenedor
CMD ["./pacman_server"]
