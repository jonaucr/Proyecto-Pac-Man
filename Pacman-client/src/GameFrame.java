//Ventana principal

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class GameFrame extends JFrame {

    private PrintWriter out;
    private BufferedReader in;

    public GameFrame() {
        setTitle("Pac-Man");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Crear conexión con el servidor
        try {
            Socket socket = new Socket("localhost", 5000);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "No se pudo conectar al servidor.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }

        // Panel principal
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 200));

        JButton btnCrear = new JButton("Crear partida");
        JButton btnUnirse = new JButton("Unirse a partida");

        // Acción para crear partida
        btnCrear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String json = "{\"type\":\"create_game\",\"payload\":{\"direccion\":\"arriba\"}}";
                out.println(json);
                out.flush();
                JOptionPane.showMessageDialog(panel, "Solicitud de creación enviada.");
            }
        });

        // Acción para unirse a partida
        btnUnirse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    out.println("{\"type\":\"list_games\",\"payload\":{}}");
                    out.flush();

                    String response = in.readLine();  // Leer respuesta JSON del servidor
                    JOptionPane.showMessageDialog(panel, "Partidas activas: " + response);

                    // Aquí podrías parsear la respuesta y mostrar una lista para elegir partida

                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(panel, "Error al obtener partidas activas.");
                }
            }
        });

        panel.add(btnCrear);
        panel.add(btnUnirse);
        add(panel);
    }
}
