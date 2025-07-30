import java.io.*;
import java.net.Socket;

public class test {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 5000);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Enviar create_game
            String createGameJson = "{\"type\":\"create_game\",\"payload\":{}}";
            out.println(createGameJson);

            // (Opcional) Esperar la respuesta del servidor antes de mover
            String respuesta = in.readLine();
            System.out.println("Respuesta del servidor: " + respuesta);

            // Enviar move
            String moveJson = "{\"type\":\"move\",\"payload\":{\"direction\":\"left\"}}";
            out.println(moveJson);

            // Leer respuestas del servidor (puedes usar un bucle si quieres seguir
            // recibiendo mensajes)
            while ((respuesta = in.readLine()) != null) {
                System.out.println("Respuesta del servidor: " + respuesta);
                // Puedes agregar lógica para salir del bucle si recibes un tipo específico
            }

        } catch (IOException e) {
            System.err.println("Error al conectar con el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}