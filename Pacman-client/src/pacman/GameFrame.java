// GameFrame.java
package pacman;

import javax.swing.*;
import java.awt.*;
import pacman.Entidades.Game;

public class GameFrame extends JFrame {
    private JLabel lblId;
    private JLabel lblScore;
    private JLabel lblLives;
    private GamePanel gamePanel;

    public GameFrame(Game game) {
        setTitle("Pac-Man");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel superior con datos
        JPanel infoPanel = new JPanel();
        lblId = new JLabel("ID Partida: " + game.id);
        lblScore = new JLabel("Puntos: " + game.players.get(0).score);
        lblLives = new JLabel("Vidas: " + game.players.get(0).lives); 
        infoPanel.add(lblId);
        infoPanel.add(lblScore);
        infoPanel.add(lblLives);

        add(infoPanel, BorderLayout.NORTH);

        // Panel de juego
        gamePanel = new GamePanel();
        gamePanel.setGame(game);
        add(gamePanel, BorderLayout.CENTER);
    }

    public void updateGame(Game game) {
        lblId.setText("ID Partida: " + game.id);
        lblScore.setText("Puntos: " + game.players.get(0).score);
        // lblLives.setText("Vidas: " + ...); // Actualiza si tienes el campo
        gamePanel.setGame(game);
    }
}
