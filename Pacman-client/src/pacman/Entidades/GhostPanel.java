package pacman.Entidades;

import javax.swing.*;
import java.awt.*;

public class GhostPanel extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Cuerpo del fantasma (rojo)
        g2.setColor(Color.RED);
        g2.fillArc(50, 50, 100, 100, 0, 180); // parte superior redondeada
        g2.fillRect(50, 100, 100, 50); // parte inferior

        // Ondas de la base
        for (int i = 0; i < 4; i++) {
            g2.fillOval(50 + i * 25, 130, 25, 25);
        }

        // Ojos
        g2.setColor(Color.WHITE);
        g2.fillOval(75, 90, 20, 25);
        g2.fillOval(105, 90, 20, 25);

        // Pupilas
        g2.setColor(Color.BLUE);
        g2.fillOval(80, 100, 10, 10);
        g2.fillOval(110, 100, 10, 10);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Fantasma Pac-Man");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(220, 250);
        frame.add(new GhostPanel());
        frame.setVisible(true);
    }
}