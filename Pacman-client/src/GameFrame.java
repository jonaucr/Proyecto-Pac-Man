import javax.swing.JFrame;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.IOException;

import javax.swing.JPanel;

public class GameFrame extends JFrame {
    public GameFrame() {
        setTitle("Pac-Man");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel principal con fondo blanco y botones
        JPanel panel = new JPanel();
        panel.setBackground(java.awt.Color.WHITE);
        panel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 30, 200));

        javax.swing.JButton btnCrear = new javax.swing.JButton("Crear partida");
        javax.swing.JButton btnUnirse = new javax.swing.JButton("Unirse a partida");
        panel.add(btnCrear);
        panel.add(btnUnirse);
        add(panel);

        try {
            Socket socket = new Socket("localhost", 5000);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            InputHandler inputHandler = new InputHandler(out);
            addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyPressed(java.awt.event.KeyEvent e) {
                    inputHandler.keyPressed(e);
                }
                public void keyReleased(java.awt.event.KeyEvent e) {
                    inputHandler.keyReleased(e);
                }
            });
            setFocusable(true);
            requestFocusInWindow();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            GameFrame frame = new GameFrame();
            frame.setVisible(true);
        });
    }
}
