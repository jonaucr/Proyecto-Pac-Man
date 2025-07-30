package pacman;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.SwingUtilities;

/**
 * Clase principal que inicia la aplicación del juego Pac-Man.
 */
public class Main {

    public static void main(String[] args) {
      

        // SwingUtilities.invokeLater asegura que la creación de la GUI
        // se ejecute en el Event Dispatch Thread (EDT) de Swing.
        SwingUtilities.invokeLater(() -> {
            new MenuPrincipal().setVisible(true);
        });
       
    }
}
