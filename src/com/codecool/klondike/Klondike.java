package com.codecool.klondike;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class Klondike extends Application {

    private static final double WINDOW_WIDTH = 1400;
    private static final double WINDOW_HEIGHT = 900;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Card.loadCardImages();
        Game game = new Game();
        game.setTableBackground(new Image("/table/green.png"));

        primaryStage.setTitle("Klondike Solitaire");
        primaryStage.setScene(new Scene(game, WINDOW_WIDTH, WINDOW_HEIGHT));
        primaryStage.show();
        /*Button restart_btn = new Button();
        restart_btn.setLayoutX(455);
        restart_btn.setLayoutY(60);
        restart_btn.setPrefWidth(130);
        restart_btn.setPrefHeight(50);
        restart_btn.setText("RESTART");
        restart_btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                game.restartGame();
            }
        });
        game.getChildren().add(restart_btn);
        */
        Button undo_btn = new Button();
        undo_btn.setLayoutX(455);
        undo_btn.setLayoutY(130);
        undo_btn.setPrefWidth(130);
        undo_btn.setPrefHeight(50);
        undo_btn.setText("UNDO");
        game.getChildren().add(undo_btn);
    }

}
