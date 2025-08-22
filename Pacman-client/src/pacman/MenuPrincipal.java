package pacman;

import javax.swing.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import pacman.Entidades.Game;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Representa la ventana del menú principal del juego.
 * Desde aquí el usuario puede decidir iniciar una nueva partida o unirse a una
 * existente.
 */
public class MenuPrincipal extends JFrame {

    public MenuPrincipal() {
        // 1. Configuración básica de la ventana (JFrame)
        setTitle("Pac-Man - Menú Principal");
        setSize(400, 300); // Tamaño de la ventana
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Terminar la aplicación al cerrar la ventana
        setLocationRelativeTo(null); // Centrar la ventana en la pantalla
        setResizable(false); // Evitar que se pueda cambiar el tamaño

        // 2. Creación del panel principal que contendrá los botones
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout()); 
        GridBagConstraints gbc = new GridBagConstraints();

        // 3. Creación de los botones
        JButton btnIniciarJuego = new JButton("Iniciar Juego");
       
        JButton btnObservarPartida = new JButton("Observar Partida");

       
        btnIniciarJuego.setFont(new Font("Arial", Font.BOLD, 16));
      
        btnObservarPartida.setFont(new Font("Arial", Font.BOLD, 16));

        // 4. Añadir "Action Listeners" a los botones

        btnIniciarJuego.addActionListener(e -> {
           
            NetworkClient networkClient = new NetworkClient();
            if (networkClient.conectar("localhost", 5000)) {
                networkClient.enviarCrearPartida();
                
                new Thread(() -> {
                    try {
                        String respuesta = networkClient.recibirMensaje();
                        System.out.println("Respuesta del servidor: " + respuesta);
                        
                        Game game = Game.parseGameState(respuesta);

                        SwingUtilities.invokeLater(() -> {
                            
                            GameFrame frame = new GameFrame(game, networkClient, () -> new MenuPrincipal().setVisible(true));
                            frame.setVisible(true);
                           
                            MenuPrincipal.this.dispose();
                        });
                    } catch (IOException ex) {
                        SwingUtilities.invokeLater(() -> 
                            JOptionPane.showMessageDialog(MenuPrincipal.this, "Error al recibir datos del servidor: " + ex.getMessage(), "Error de Comunicación", JOptionPane.ERROR_MESSAGE)
                        );
                    }
                }).start();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo conectar al servidor.", "Error de Conexión", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnObservarPartida.addActionListener(e -> {
            String gameId = JOptionPane.showInputDialog(this, "Introduce el ID de la partida a observar:", "Observar Partida", JOptionPane.PLAIN_MESSAGE);

            if (gameId != null && !gameId.trim().isEmpty()) {
                // Creamos una nueva instancia de NetworkClient para cada intento de conexión.
                NetworkClient networkClient = new NetworkClient();
                if (networkClient.conectar("localhost", 5000)) {
                    networkClient.enviarJoinSpectator(gameId.trim());

                    new Thread(() -> {
                        try {
                            String serverResponse = networkClient.recibirMensaje();
                            if (serverResponse != null) {
                                JsonObject json = JsonParser.parseString(serverResponse).getAsJsonObject();
                                String type = json.get("type").getAsString();

                                if ("game_state".equals(type)) {
                                    Game initialGame = Game.parseGameState(serverResponse);
                                    SwingUtilities.invokeLater(() -> {
                                        GameFrame frame = new GameFrame(initialGame, networkClient, () -> new MenuPrincipal().setVisible(true));
                                        frame.setVisible(true);
                                        MenuPrincipal.this.dispose();
                                    });
                                } else if ("error".equals(type)) {
                                    String message = json.getAsJsonObject("payload").get("mensaje").getAsString();
                                    SwingUtilities.invokeLater(() ->
                                        JOptionPane.showMessageDialog(MenuPrincipal.this, message, "Error al Observar", JOptionPane.ERROR_MESSAGE)
                                    );
                                }
                            } else {
                                SwingUtilities.invokeLater(() ->
                                    JOptionPane.showMessageDialog(MenuPrincipal.this, "El servidor no respondió. Verifique el ID.", "Error", JOptionPane.ERROR_MESSAGE)
                                );
                            }
                        } catch (IOException ex) {
                             SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(MenuPrincipal.this, "Error al recibir datos del servidor: " + ex.getMessage(), "Error de Comunicación", JOptionPane.ERROR_MESSAGE)
                            );
                        }
                    }).start();
                }
            }
        });

        // 5. Posicionamiento de los botones en el panel usando GridBagLayout
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10); 
        panel.add(btnIniciarJuego, gbc);

        gbc.gridy = 1;
        
        panel.add(btnObservarPartida, gbc);

        
        add(panel);
    }
}
