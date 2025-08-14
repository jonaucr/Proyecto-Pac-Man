// GamePanel.java
package pacman;

import java.awt.*;
import javax.swing.*;
import pacman.Entidades.*;

public class GamePanel extends JPanel {
    private Game game;
    private boolean isMouthOpen = true;
    private Timer animationTimer;

    public GamePanel() {
        // Animación de la boca de Pac-Man
        animationTimer = new Timer(150, e -> {
            isMouthOpen = !isMouthOpen;
            repaint();
        });
        animationTimer.start();
    }

    public void setGame(Game game) {
        this.game = game;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (game == null) return;

        // Dibuja el mapa
        for (int y = 0; y < game.map.length; y++) {
            for (int x = 0; x < game.map[y].length; x++) {
                int cell = game.map[y][x];
                // Fondo
                g.setColor(Color.BLACK);
                g.fillRect(x * 20, y * 20, 20, 20);

                // Pared
                if (cell == 1) {
                    g.setColor(Color.BLUE);
                    g.fillRect(x * 20, y * 20, 20, 20);
                }

                // Punto
                if (cell == 2) {
                    g.setColor(Color.WHITE);
                    int px = x * 20 + 8;
                    int py = y * 20 + 8;
                    g.fillOval(px, py, 4, 4);
                }
            }
        }

        // Dibuja el jugador
        for (Player p : game.players) {
            g.setColor(Color.YELLOW);
            drawPacman(g, p);
        }
        // Dibuja los fantasmas
        for (Ghost ghost : game.ghosts) {
            drawGhost(g, ghost.x, ghost.y, ghost.color);
        }
    }

    // Dibuja Pac-Man
    private void drawPacman(Graphics g, Player p) {
        int startAngle = 0;
        int arcAngle = 360;
        if (isMouthOpen) {
            arcAngle = 280;
            startAngle = 20;
        }
        g.fillArc(p.x * 20, p.y * 20, 20, 20, startAngle, arcAngle);

        // Dibuja las frutas
        g.setColor(Color.GREEN);
        for (Fruit fruit : game.fruits) {
            g.fillOval(fruit.x * 20, fruit.y * 20, 20, 20);
        }
    }

    // Dibuja un fantasma clásico
    private void drawGhost(Graphics g, int x, int y, String color) {
        Graphics2D g2 = (Graphics2D) g;
        Color ghostColor;
        switch (color.toLowerCase()) {
            case "red": ghostColor = Color.RED; break;
            case "pink": ghostColor = Color.PINK; break;
            case "cyan": ghostColor = Color.CYAN; break;
            case "orange": ghostColor = Color.ORANGE; break;
            default: ghostColor = Color.GRAY; break;
        }
        int px = x * 20;
        int py = y * 20;
        g2.setColor(ghostColor);
        g2.fillArc(px, py, 20, 20, 0, 180);
        g2.fillRect(px, py + 10, 20, 10);

        // ondas de la base
        for (int i = 0; i < 3; i++) {
            g2.fillOval(px + i * 7, py + 15, 7, 7);
        }

        // ojos
        g2.setColor(Color.WHITE);
        g2.fillOval(px + 4, py + 7, 5, 7);
        g2.fillOval(px + 11, py + 7, 5, 7);

        // pupilas
        g2.setColor(Color.BLUE);
        g2.fillOval(px + 6, py + 11, 2, 3);
        g2.fillOval(px + 13, py + 11, 2, 3);
    }
}