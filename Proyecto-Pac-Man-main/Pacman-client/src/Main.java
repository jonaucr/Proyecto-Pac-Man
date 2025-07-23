public class Main {
    public static void main(String[] args) {
        Mapa mapaJuego = new MapaPacman();
        new VentanaMapa(mapaJuego.obtenerMapa());
    }
}
