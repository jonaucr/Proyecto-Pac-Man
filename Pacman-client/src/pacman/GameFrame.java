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
    // --- Constantes para configuración ---
    private static final int CELL_SIZE = 20; // Tamaño de cada celda del mapa en píxeles
    private static final int INFO_PANEL_HEIGHT = 50; // Altura del panel de información
    private static final int FRAME_BORDER_WIDTH = 16; // Ancho extra para los bordes de la ventana
    private static final int FRAME_TITLE_BAR_HEIGHT = 39; // Alto extra para la barra de título y bordes

    private JLabel lblId;
    private JLabel lblScore;
    private JLabel lblLives;
    private JLabel lblSpectators; // <-- añadir campo
    private GamePanel gamePanel;
    private final NetworkClient networkClient;
    private final Runnable onGameEnd;

    private boolean gameEndedGracefully = false; // Flag para controlar el final del juego
    public GameFrame(Game initialGame, NetworkClient networkClient, Runnable onGameEnd) {
        this.networkClient = networkClient;
        this.onGameEnd = onGameEnd;

        setTitle("Pac-Man");
        // Hacemos que el tamaño de la ventana se ajuste al mapa
        int mapWidth = initialGame.map[0].length * CELL_SIZE;
        int mapHeight = initialGame.map.length * CELL_SIZE;
        // Ajustamos el tamaño total considerando los bordes de la ventana
        setSize(mapWidth + FRAME_BORDER_WIDTH, mapHeight + INFO_PANEL_HEIGHT + FRAME_TITLE_BAR_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        // Panel superior con datos
        JPanel infoPanel = new JPanel();
        lblId = new JLabel("ID Partida: " + initialGame.id);
        // Si no hay jugadores (somos observadores de una partida vacía), mostrar N/A
        if (!initialGame.players.isEmpty()) {
            lblScore = new JLabel("Puntos: " + initialGame.players.get(0).score);
            lblLives = new JLabel("Vidas: " + initialGame.players.get(0).lives);
        } else {
            lblScore = new JLabel("Puntos: N/A");
            lblLives = new JLabel("Vidas: N/A");
        }
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

    private void setupKeyBindings() {
        // Usamos el InputMap y ActionMap del panel de contenido para las teclas
        JComponent contentPane = (JComponent) getContentPane();
        InputMap inputMap = contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = contentPane.getActionMap();

        // Mapeo de teclas a nombres de acción
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "move_up");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "move_down");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "move_left");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "move_right");

        // Mapeo de nombres de acción a acciones reales (enviar al servidor)
        actionMap.put("move_up", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                networkClient.enviarMovimiento("up");
            }
        });
        actionMap.put("move_down", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                networkClient.enviarMovimiento("down");
            }
        });
        actionMap.put("move_left", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                networkClient.enviarMovimiento("left");
            }
        });
        actionMap.put("move_right", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                networkClient.enviarMovimiento("right");
            }
        });
    }
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
                            gameEndedGracefully = true; // Marcar que el juego terminó de forma controlada
                            // Después de game_over salimos del loop
                            break;
                        } else if ("game_win".equals(type)) {
                            JsonObject payload = json.getAsJsonObject("payload");
                            int finalScore = payload.has("final_score") ? payload.get("final_score").getAsInt() : 0;
                            String mensaje = payload.has("mensaje") ? payload.get("mensaje").getAsString() : "¡Has ganado!";

                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(GameFrame.this, mensaje + "\nPuntaje final: " + finalScore, "¡Victoria!", JOptionPane.INFORMATION_MESSAGE);
                                // Ejecutar finalización y cerrar ventana
                                onGameEnd.run();
                                GameFrame.this.dispose();
                            });
                            gameEndedGracefully = true; // Marcar que el juego terminó de forma controlada
                            // Salimos del loop porque el juego terminó
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
                // Se ejecuta cuando el bucle en doInBackground termina.
                // Solo actuar si el juego no terminó por un mensaje "game_over" o "game_win".
                if (!gameEndedGracefully) {
                    JOptionPane.showMessageDialog(GameFrame.this, "Se perdió la conexión con el servidor.", "Desconectado", JOptionPane.WARNING_MESSAGE);
                    onGameEnd.run(); // Volver al menú
                    GameFrame.this.dispose();
                }
            }
        };
        worker.execute();
    }

    public void updateGame(Game game) {
        lblId.setText("ID Partida: " + game.id);
        // Si no hay jugadores, mostrar N/A para evitar errores
        if (!game.players.isEmpty()) {
            lblScore.setText("Puntos: " + game.players.get(0).score);
            lblLives.setText("Vidas: " + game.players.get(0).lives);
        } else {
            lblScore.setText("Puntos: N/A");
            lblLives.setText("Vidas: N/A");
        }
        gamePanel.setGame(game);
    }
}
