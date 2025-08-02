package pacman;

import javax.swing.*;

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
        panel.setLayout(new GridBagLayout()); // Usamos GridBagLayout para centrar componentes fácilmente
        GridBagConstraints gbc = new GridBagConstraints();

        // 3. Creación de los botones
        JButton btnIniciarJuego = new JButton("Iniciar Juego");
        JButton btnUnirsePartida = new JButton("Unirse a una Partida");

        // Estilo opcional para los botones
        btnIniciarJuego.setFont(new Font("Arial", Font.BOLD, 16));
        btnUnirsePartida.setFont(new Font("Arial", Font.BOLD, 16));

        // 4. Añadir "Action Listeners" a los botones

        btnIniciarJuego.addActionListener(e -> {
            // Creamos una nueva instancia de NetworkClient para cada intento de conexión.
            // Esto asegura que no haya estados residuales de intentos anteriores.
            NetworkClient networkClient = new NetworkClient();
            if (networkClient.conectar("localhost", 5000)) {
                networkClient.enviarCrearPartida();
                // Puedes leer la respuesta en un hilo aparte si quieres actualizar la interfaz
                new Thread(() -> {
                    try {
                        String respuesta = networkClient.recibirMensaje();
                        System.out.println("Respuesta del servidor: " + respuesta);
                        // Aquí puedes parsear el JSON y abrir el frame del juego
                        Game game = Game.parseGameState(respuesta);

                        SwingUtilities.invokeLater(() -> {
                            // Pasamos el estado inicial del juego y el cliente de red al GameFrame
                            // También pasamos una "acción" a ejecutar cuando el juego termine,
                            // que en este caso es volver a mostrar el menú principal.
                            GameFrame frame = new GameFrame(game, networkClient, () -> new MenuPrincipal().setVisible(true));
                            frame.setVisible(true);
                            // Cerramos la ventana del menú principal
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

        btnUnirsePartida.addActionListener(e -> {
            String gameId = JOptionPane.showInputDialog(this, "Introduce el ID de la partida:", "Unirse a Partida", JOptionPane.PLAIN_MESSAGE);

            if (gameId != null && !gameId.trim().isEmpty()) {
                // Creamos una nueva instancia de NetworkClient para cada intento de conexión.
                NetworkClient networkClient = new NetworkClient();
                if (networkClient.conectar("localhost", 5000)) {
                    networkClient.enviarUnirsePartida(gameId.trim());

                    new Thread(() -> {
                        try {
                            String respuesta = networkClient.recibirMensaje();
                            System.out.println("Respuesta del servidor: " + respuesta);
                            
                            // El servidor podría enviar un error si el ID no es válido
                            if (respuesta == null || respuesta.contains("error")) {
                                SwingUtilities.invokeLater(() -> 
                                    JOptionPane.showMessageDialog(MenuPrincipal.this, "No se pudo unir a la partida. Verifique el ID.", "Error al Unirse", JOptionPane.ERROR_MESSAGE)
                                );
                                return; // No continuar
                            }

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
            }
        });

        // 5. Posicionamiento de los botones en el panel usando GridBagLayout
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10); // Margen entre componentes
        panel.add(btnIniciarJuego, gbc);

        gbc.gridy = 1;
        panel.add(btnUnirsePartida, gbc);

        // 6. Añadir el panel a la ventana
        add(panel);
    }
}
