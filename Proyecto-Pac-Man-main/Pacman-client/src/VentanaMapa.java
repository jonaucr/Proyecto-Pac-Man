import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class VentanaMapa extends JFrame implements KeyListener {

    private final char[][] mapa;
    private int pacmanFila = 1;
    private int pacmanCol = 1;
    private int puntaje = 0;
    private int vidas = 3;

    private final java.util.List<Point> fantasmas = new ArrayList<>();
    private final javax.swing.Timer timer;

    public VentanaMapa(char[][] mapa) {
        this.mapa = mapa;

        fantasmas.add(new Point(5, 5));
        fantasmas.add(new Point(3, 8));
        fantasmas.add(new Point(8, 2));

        setTitle("Pac-Man");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        add(new PanelMapa());

        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();

        timer = new javax.swing.Timer(300, e -> moverFantasmas());
        timer.start();

        setVisible(true);
    }

    class PanelMapa extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int tamano = 30;

            for (int f = 0; f < mapa.length; f++) {
                for (int c = 0; c < mapa[f].length; c++) {
                    char celda = mapa[f][c];

                    switch (celda) {
                        case '#':
                            g.setColor(new Color(0, 0, 128));
                            break;
                        case '.':
                            g.setColor(Color.BLACK);
                            g.fillRect(c * tamano, f * tamano, tamano, tamano);
                            g.setColor(Color.YELLOW);
                            g.fillOval(c * tamano + 10, f * tamano + 10, 10, 10);
                            continue;
                        case ' ':
                            g.setColor(Color.BLACK);
                            break;
                        default:
                            g.setColor(Color.GRAY);
                            break;
                    }

                    g.fillRect(c * tamano, f * tamano, tamano, tamano);
                }
            }

            g.setColor(Color.YELLOW);
            g.fillOval(pacmanCol * tamano + 5, pacmanFila * tamano + 5, 20, 20);

            g.setColor(Color.RED);
            for (Point f : fantasmas) {
                g.fillOval(f.x * tamano + 5, f.y * tamano + 5, 20, 20);
            }

            g.setColor(Color.WHITE);
            g.drawString("Puntaje: " + puntaje, 10, 20);
            g.drawString("Vidas: " + vidas, 10, 40);
        }
    }

    private void moverFantasmas() {
        Random r = new Random();

        for (Point f : fantasmas) {
            int dx = r.nextInt(3) - 1;
            int dy = r.nextInt(3) - 1;
            int nx = f.x + dx;
            int ny = f.y + dy;

            if (ny >= 0 && ny < mapa.length && nx >= 0 && nx < mapa[0].length && mapa[ny][nx] != '#') {
                f.setLocation(nx, ny);
            }

            if (f.x == pacmanCol && f.y == pacmanFila) {
                perderVida();
            }
        }

        repaint();
    }

    private void perderVida() {
        vidas--;
        if (vidas <= 0) {
            timer.stop();
            JOptionPane.showMessageDialog(this, "¡Perdiste! Te atrapó un fantasma.");
            System.exit(0);
        } else {
            pacmanFila = 1;
            pacmanCol = 1;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int t = e.getKeyCode();
        int nf = pacmanFila;
        int nc = pacmanCol;

        switch (t) {
            case KeyEvent.VK_UP: nf--; break;
            case KeyEvent.VK_DOWN: nf++; break;
            case KeyEvent.VK_LEFT: nc--; break;
            case KeyEvent.VK_RIGHT: nc++; break;
        }

        if (nf >= 0 && nf < mapa.length && nc >= 0 && nc < mapa[0].length && mapa[nf][nc] != '#') {
            pacmanFila = nf;
            pacmanCol = nc;

            if (mapa[nf][nc] == '.') {
                puntaje += 10;
                mapa[nf][nc] = ' ';
            }

            for (Point f : fantasmas) {
                if (f.x == pacmanCol && f.y == pacmanFila) {
                    perderVida();
                    break;
                }
            }

            if (!hayPuntos()) {
                timer.stop();
                JOptionPane.showMessageDialog(this, "¡Ganaste! Todos los puntos fueron comidos.");
                System.exit(0);
            }

            repaint();
        }
    }

    private boolean hayPuntos() {
        for (char[] fila : mapa) {
            for (char c : fila) {
                if (c == '.') return true;
            }
        }
        return false;
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
