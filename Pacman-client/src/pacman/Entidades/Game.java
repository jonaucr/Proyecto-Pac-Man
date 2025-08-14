package pacman.Entidades;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

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
        JsonObject payload = rootObject.getAsJsonObject("payload");

        Game game = new Game();
        game.id = payload.get("id").getAsString();

        // Parsear el mapa manualmente
        JsonArray mapArray = payload.getAsJsonArray("map");
        int height = mapArray.size();
        int width = mapArray.get(0).getAsJsonArray().size();
        game.map = new int[height][width];
        for (int y = 0; y < height; y++) {
            JsonArray row = mapArray.get(y).getAsJsonArray();
            for (int x = 0; x < width; x++) {
                game.map[y][x] = row.get(x).getAsInt();
            }
        }

        // Parsear jugadores
        game.players = new ArrayList<>();
        JsonArray playersArray = payload.getAsJsonArray("players");
        for (int i = 0; i < playersArray.size(); i++) {
            game.players.add(gson.fromJson(playersArray.get(i), Player.class));
        }

        // Parsear fantasmas
        game.ghosts = new ArrayList<>();
        JsonArray ghostsArray = payload.getAsJsonArray("ghosts");
        for (int i = 0; i < ghostsArray.size(); i++) {
            game.ghosts.add(gson.fromJson(ghostsArray.get(i), Ghost.class));
        }

        // Parsear frutas
        game.fruits = new ArrayList<>();
        JsonArray fruitsArray = payload.getAsJsonArray("fruits");
        for (int i = 0; i < fruitsArray.size(); i++) {
            game.fruits.add(gson.fromJson(fruitsArray.get(i), Fruit.class));
        }

        return game;
    }
}