// GameFrame.java
package pacman;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;
import javax.swing.*;
import pacman.Entidades.Game;

public class GameFrame extends JFrame {
    private JLabel lblId;
    private JLabel lblScore;
    private JLabel lblLives;
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
        infoPanel.add(lblId);
        infoPanel.add(lblScore);
        infoPanel.add(lblLives);

        add(infoPanel, BorderLayout.NORTH);

        // Panel de juego
        gamePanel = new GamePanel(); // Ahora usa el constructor que inicia el Timer
        gamePanel.setGame(initialGame);
        add(gamePanel, BorderLayout.CENTER);

        setupKeyBindings();
        startGameLoop();
    }

    private void setupKeyBindings() {
        InputMap inputMap = gamePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = gamePanel.getActionMap();

        // 1. Definimos las acciones para cada dirección una sola vez.
        //    Cada acción imprime la dirección y la envía al servidor.
        String[] directions = {"up", "down", "left", "right"};
        for (String dir : directions) {
            actionMap.put(dir, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // La clase NetworkClient ya imprime el JSON que se envía.
                    System.out.println(">> [GameFrame] Tecla presionada: " + dir);
                    networkClient.enviarMovimiento(dir);
                }
            });
        }

        // 2. Mapeamos las teclas a las acciones definidas.
        // Mapeo para las teclas de flecha
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "left");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "right");

        // Mapeo para las teclas WASD
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), "up");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "down");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0), "left");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "right");
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
                        // El servidor envía el estado completo del juego en cada actualización
                        Game updatedGame = Game.parseGameState(serverResponse);
                        publish(updatedGame); // Envía el estado del juego al método process()
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
                JOptionPane.showMessageDialog(GameFrame.this, "Se ha perdido la conexión con el servidor.", "Desconectado", JOptionPane.INFORMATION_MESSAGE);
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
        gamePanel.setGame(game);
    }
}
