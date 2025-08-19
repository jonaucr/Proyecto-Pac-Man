// GameFrame.java
package pacman;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;
import javax.swing.*;
import pacman.Entidades.Game;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser; // <-- añadir
// ...existing code...

public class GameFrame extends JFrame {
    private JLabel lblId;
    private JLabel lblScore;
    private JLabel lblLives;
    private JLabel lblSpectators; // <-- añadir campo
    private GamePanel gamePanel;
    private final NetworkClient networkClient;
    private final Runnable onGameEnd;

    public GameFrame(Game initialGame, NetworkClient networkClient, Runnable onGameEnd) {
        this.networkClient = networkClient;
        this.onGameEnd = onGameEnd;

        setTitle("Pac-Man");
        // Hacemos que el tamaño de la ventana se ajuste al mapa
        int mapWidth = initialGame.map[0].length * 20; // 20 es el tamaño de la celda
        int mapHeight = initialGame.map.length * 20;
        int infoPanelHeight = 50; // Altura aproximada para el panel de información
        // Ajustamos el tamaño total considerando los bordes de la ventana
        setSize(mapWidth + 16, mapHeight + infoPanelHeight + 39);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        // Panel superior con datos
        JPanel infoPanel = new JPanel();
        lblId = new JLabel("ID Partida: " + initialGame.id);
        lblScore = new JLabel("Puntos: " + initialGame.players.get(0).score);
        lblLives = new JLabel("Vidas: " + initialGame.players.get(0).lives);
        lblSpectators = new JLabel("Observadores: 0"); // <-- inicializar label
        infoPanel.add(lblId);
        infoPanel.add(lblScore);
        infoPanel.add(lblLives);
        infoPanel.add(lblSpectators); // <-- añadir al panel

        add(infoPanel, BorderLayout.NORTH);

        // Panel de juego
        gamePanel = new GamePanel(); // Ahora usa el constructor que inicia el Timer
        gamePanel.setGame(initialGame);
        add(gamePanel, BorderLayout.CENTER);

        setupKeyBindings();
        startGameLoop();
    }
    // ...existing code...

    private void startGameLoop() {
        SwingWorker<Void, Game> worker;
        worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    while (!isCancelled()) {
                        String serverResponse = networkClient.recibirMensaje();
                        if (serverResponse == null) {
                            break; // El servidor cerró la conexión
                        }

                        // Parsear mensaje general con Gson
                        JsonObject json = JsonParser.parseString(serverResponse).getAsJsonObject();
                        String type = json.has("type") ? json.get("type").getAsString() : "";

                        if ("game_state".equals(type)) {
                            JsonObject payload = json.getAsJsonObject("payload");
                            int spectators = payload.has("spectators") ? payload.get("spectators").getAsInt() : 0;
                            // Actualizar contador de observadores en el EDT
                            SwingUtilities.invokeLater(() -> lblSpectators.setText("Observadores: " + spectators));

                            // Convertir el estado a Game y publicar para repintar
                            Game updatedGame = Game.parseGameState(serverResponse);
                            publish(updatedGame);

                        } else if ("player_hit".equals(type)) {
                            JsonObject payload = json.getAsJsonObject("payload");
                            int lives = payload.has("lives") ? payload.get("lives").getAsInt() : 0;
                            int score = payload.has("score") ? payload.get("score").getAsInt() : 0;
                            String mensaje = payload.has("mensaje") ? payload.get("mensaje").getAsString() : "Golpeado";

                            // Actualizar UI (vidas/puntos) y mostrar notificación breve
                            SwingUtilities.invokeLater(() -> {
                                lblLives.setText("Vidas: " + lives);
                                lblScore.setText("Puntos: " + score);
                                JOptionPane.showMessageDialog(GameFrame.this, mensaje + " (Vidas: " + lives + ")", "Golpe", JOptionPane.INFORMATION_MESSAGE);
                            });

                        } else if ("game_over".equals(type)) {
                            JsonObject payload = json.getAsJsonObject("payload");
                            int finalScore = payload.has("final_score") ? payload.get("final_score").getAsInt() : 0;
                            String mensaje = payload.has("mensaje") ? payload.get("mensaje").getAsString() : "Juego terminado";

                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(GameFrame.this, mensaje + "\nPuntaje final: " + finalScore, "Fin del juego", JOptionPane.INFORMATION_MESSAGE);
                                // Ejecutar finalización y cerrar ventana
                                onGameEnd.run();
                                GameFrame.this.dispose();
                            });
                            // Después de game_over salimos del loop
                            break;
                        } else {
                            // Mensaje desconocido: ignorar o loggear
                            System.out.println("Mensaje desconocido recibido: " + type);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Conexión perdida con el servidor: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void process(List<Game> chunks) {
                // Tomamos solo la última actualización para evitar repintar en exceso
                if (!chunks.isEmpty()) {
                    Game latestGame = chunks.get(chunks.size() - 1);
                    updateGame(latestGame);
                }
            }

            @Override
            protected void done() {
                // Se ejecuta cuando el bucle en doInBackground termina
                JOptionPane.showMessageDialog(GameFrame.this, "Se ha perdido las vidas por tocar fantasmas, inicia de nuevo.", "Desconectado", JOptionPane.INFORMATION_MESSAGE);
                // Ejecuta la acción de finalización que nos pasaron (ej: mostrar el menú principal)
                onGameEnd.run();
                GameFrame.this.dispose();
            }
        };
        worker.execute();
    }

    public void updateGame(Game game) {
        lblId.setText("ID Partida: " + game.id);
        lblScore.setText("Puntos: " + game.players.get(0).score);
        lblLives.setText("Vidas: " + game.players.get(0).lives);
        lblSpectators.setText("Observadores: " + game.num_spectators); // Actualizar contador de observadores
        gamePanel.setGame(game);
    }
}
