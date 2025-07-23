
import java.io.PrintWriter;
import java.awt.event.KeyEvent;
//leer las teclas wasd


public class InputHandler {
    private PrintWriter out;

    public InputHandler(PrintWriter out) {
        this.out = out;
    }


    /**
     * Detecta las teclas presionadas y las imprime en la consola.
     * Las teclas W, A, S, D corresponden a arriba, izquierda, abajo y derecha respectivamente.
     *
     * @param e el evento de teclado
     */
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        String direccion = null;
        if (key == KeyEvent.VK_W) {
            direccion = "arriba";
        } else if (key == KeyEvent.VK_A) {
            direccion = "izquierda";
        } else if (key == KeyEvent.VK_S) {
            direccion = "abajo";
        } else if (key == KeyEvent.VK_D) {
            direccion = "derecha";
        }
        if (direccion != null) {
            String json = String.format("{\"type\":\"move\",\"payload\":{\"direccion\":\"%s\"}}", direccion);
            out.println(json);
            out.flush();
            System.out.println("Enviado: " + json);
        }
    }

    /**
     * Este método se llama cuando se suelta una tecla.
     * Ahora detecta cuando se dejan de presionar las teclas W, A, S, D.
     *
     * @param e el evento de teclado
     */
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        String direccion = null;
        if (key == KeyEvent.VK_W) {
            direccion = "arriba";
        } else if (key == KeyEvent.VK_A) {
            direccion = "izquierda";
        } else if (key == KeyEvent.VK_S) {
            direccion = "abajo";
        } else if (key == KeyEvent.VK_D) {
            direccion = "derecha";
        }
        if (direccion != null) {
            String json = String.format("{\"type\":\"release\",\"payload\":{\"direccion\":\"%s\"}}", direccion);
            out.println(json);
            out.flush();
            System.out.println("Enviado (release): " + json);
        }
    }
}
