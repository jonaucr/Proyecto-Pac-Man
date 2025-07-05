import java.io.*;
import java.net.Socket;


public class Client {
    public static void main(String[] args)  {
        try {
            Socket socket = new Socket("localhost", 5000);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            
            // Enviar JSON al servidor
            String json = "{\"type\":\"create_game\",\"payload\":{\"direccion\":\"arriba\"}}";
            out.println(json);

            // Leer respuesta del servidor
            String respuesta = in.readLine();
            System.out.println("Respuesta del servidor: " + respuesta);


        
            // Enviar mensaje de desconexión
            String disconnectJson = "{\"type\":\"disconnect\",\"payload\":{}}";
            out.println(disconnectJson);
            out.flush();
            System.out.println("manda respuesta ");

            // Esperar respuesta del servidor antes de cerrar
            String respuestaDesconexion = in.readLine();
            System.out.println("Respuesta de desconexión: " + respuestaDesconexion);

        


        } catch (IOException e) {
            System.err.println("Error al conectar con el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}