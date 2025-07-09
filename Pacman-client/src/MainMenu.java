import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainMenu extends Application {

    @Override
    public void start(Stage primaryStage) {
        Button btnCrear = new Button("Crear Partida");
        Button btnUnirse = new Button("Unirse a Partida");

        btnCrear.setOnAction(e -> {
            System.out.println("Iniciando como servidor...");
            // Aquí puedes llamar la clase que lanza el servidor o el juego en modo host
        });

        btnUnirse.setOnAction(e -> {
            System.out.println("Unirse como cliente...");
            // Aquí puedes mostrar un input para ingresar IP o iniciar cliente directamente
        });

        VBox layout = new VBox(20);
        layout.setStyle("-fx-padding: 40; -fx-alignment: center;");
        layout.getChildren().addAll(btnCrear, btnUnirse);

        Scene scene = new Scene(layout, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Menú Principal - Pac-Man Multiplayer");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
