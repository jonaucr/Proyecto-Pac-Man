// GamePanel.java
package pacman;

import javax.swing.*;

import pacman.Entidades.*;

import java.awt.*;

public class GamePanel extends JPanel {
    private Game game;
    private boolean isMouthOpen = true;
    private Timer animationTimer;

    public GamePanel() {
        // Este temporizador se ejecutará cada 150ms en el hilo de la UI.
        // Su única tarea es alternar el estado de la boca de Pac-Man y forzar un repintado.
        animationTimer = new Timer(150, e -> {
            isMouthOpen = !isMouthOpen;
            repaint(); // Forzamos el repintado para mostrar la animación
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
            for (int x = 0; x < game.map[0].length; x++) {
                int cell = game.map[y][x];
                // Dibuja el fondo negro primero
                g.setColor(Color.BLACK);
                g.fillRect(x * 20, y * 20, 20, 20);

                switch (cell) {
                    case 1: // Muro
                        g.setColor(Color.BLUE);
                        g.fillRect(x * 20, y * 20, 20, 20);
                        break;
                    case 2: // Punto (Pac-dot)
                        g.setColor(Color.WHITE);
                        // Dibuja un pequeño círculo en el centro de la celda
                        g.fillOval(x * 20 + 8, y * 20 + 8, 4, 4);
                        break;
                    // case 0 y otros son espacio vacío, ya dibujado en negro.
                }
            }
        }

        // Dibuja el jugador
        for (Player p : game.players) {
            g.setColor(Color.YELLOW);
            drawPacman(g, p);
        }

        // Dibuja los fantasmas
        g.setColor(Color.RED);
        for (Ghost ghost : game.ghosts) {
            g.fillRect(ghost.x * 20, ghost.y * 20, 20, 20);
        }
    }

    private void drawPacman(Graphics g, Player p) {
        int startAngle = 0;
        int arcAngle = 360;

        if (isMouthOpen) {
            arcAngle = 280; // Boca abierta
            // Asumimos que el objeto Player tiene una propiedad 'direction'
            // que el servidor nos envía. Como el campo 'direction' no existe en la clase Player,
            // la boca apuntará a la derecha por ahora para que el código compile.
            // La solución real es añadir 'public String direction;' a la clase Player.
            startAngle = 20; // Apuntar a la derecha

        }
        // Si la boca está cerrada, startAngle=0 y arcAngle=360 dibujan un círculo completo.
        g.fillArc(p.x * 20, p.y * 20, 20, 20, startAngle, arcAngle);

        // Dibuja las frutas
        g.setColor(Color.GREEN);
        for (Fruit fruit : game.fruits) {
            g.fillOval(fruit.x * 20, fruit.y * 20, 20, 20);
        }
    }

}
