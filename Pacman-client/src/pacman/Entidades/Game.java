package pacman.Entidades;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de datos del estado del juego, recibido desde el servidor.
 * Incluye un método estático para parsear el estado desde un string JSON usando Gson.
 */
public class Game {
    public String id;
    public int[][] map;
    public List<Player> players;
    public List<Ghost> ghosts;
    public List<Fruit> fruits;

    public static Game parseGameState(String jsonString) {
        Gson gson = new Gson();
        JsonObject rootObject = JsonParser.parseString(jsonString).getAsJsonObject();
        return gson.fromJson(rootObject.get("payload"), Game.class);
    }
}
