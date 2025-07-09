package org.example.ui;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import java.util.*;

public class GameController {
    @FXML private Canvas gameCanvas;
    @FXML private Label lblScore;

    public void initialize() {
        GraphicsContext g = gameCanvas.getGraphicsContext2D();
        g.setFill(Color.YELLOW);
        g.fillOval(180, 180, 40, 40);
    }
}
