package pacman;
//maneja el socket y envio de mensajes json

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NetworkClient {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
   

    public boolean conectar(String host, int puerto) {
        try {
            socket = new Socket(host, puerto);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return true;
        } catch (IOException e) {
            System.err.println("Error al conectar: " + e.getMessage());
            return false;
        }
    }

    public void enviarCrearPartida(){
        if (out != null) {
            String json = "{\"type\":\"create_game\",\"payload\":{}}";
            out.println(json);
            System.out.println("Solicitud de creación de partida enviada.");
        } else {
            System.err.println("Error: No se ha establecido conexión.");
        }
    }

    public void enviarUnirsePartida(String idPartida) {
        if (out != null) {
            String json = "{\"type\":\"join_game\",\"payload\":{\"game_id\":\"" + idPartida + "\"}}";
            out.println(json);
            System.out.println("Solicitud de unirse a la partida enviada.");
        } else {
            System.err.println("Error: No se ha establecido conexión.");
        }
    }


    public String recibirMensaje() throws IOException {
        return in.readLine();
    }
        
}
