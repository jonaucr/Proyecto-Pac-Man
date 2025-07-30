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

        NetworkClient networkClient = new NetworkClient();

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
                            GameFrame frame = new GameFrame(game);
                            frame.setVisible(true);
                        });
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            } else {
                JOptionPane.showMessageDialog(null, "No se pudo conectar al servidor");
            }
        });

        btnUnirsePartida.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Lógica para unirse a una partida
                System.out.println("Botón 'Unirse a una Partida' presionado. Aquí se enviaría el mensaje para unirse.");
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
