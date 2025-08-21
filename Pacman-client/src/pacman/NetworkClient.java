package pacman;
//maneja el socket y envio de mensajes json

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import com.google.gson.JsonObject;

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

    public void enviarJoinSpectator(String gameId) {
        if (out != null) {
            JsonObject request = new JsonObject();
            request.addProperty("type", "join_spectator");
            JsonObject payload = new JsonObject();
            payload.addProperty("id", gameId);
            request.add("payload", payload);
            out.println(request.toString());
            System.out.println("Solicitud de observación de partida enviada: " + request.toString());
        } else {
            System.err.println("Error: No se ha establecido conexión.");
        }
    }

    public void enviarMovimiento(String direccion) {
        if (out != null) {
            // Direcciones válidas: "up", "down", "left", "right"
            String json = "{\"type\":\"move\",\"payload\":{\"direction\":\"" + direccion + "\"}}";
            out.println(json);
            System.out.println(">> [NetworkClient] Enviando al servidor: " + json);
        } else {
            System.err.println("Error: No se ha establecido conexión para enviar movimiento.");
        }
    }


    public String recibirMensaje() throws IOException {
        return in.readLine();
    }
        
}
