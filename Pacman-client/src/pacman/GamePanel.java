// GamePanel.java
package pacman;

import javax.swing.*;

import pacman.Entidades.*;

import java.awt.*;

public class GamePanel extends JPanel {
    private Game game;

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
            for (int x = 0; x < game.map[0].length; x++) {
                int cell = game.map[y][x];
                if (cell == 1) {
                    g.setColor(Color.BLUE);
                    g.fillRect(x * 20, y * 20, 20, 20);
                } else {
                    g.setColor(Color.BLACK);
                    g.fillRect(x * 20, y * 20, 20, 20);
                }
            }
        }

        // Dibuja el jugador
        g.setColor(Color.YELLOW);
        for (Player p : game.players) {
            g.fillOval(p.x * 20, p.y * 20, 20, 20);
        }

        // Dibuja los fantasmas
        g.setColor(Color.RED);
        for (Ghost ghost : game.ghosts) {
            g.fillRect(ghost.x * 20, ghost.y * 20, 20, 20);
        }

        // Dibuja las frutas
        g.setColor(Color.GREEN);
        for (Fruit fruit : game.fruits) {
            g.fillOval(fruit.x * 20, fruit.y * 20, 20, 20);
        }
    }
}
