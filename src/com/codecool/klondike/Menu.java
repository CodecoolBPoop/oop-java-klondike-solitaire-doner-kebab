package com.codecool.klondike;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import java.util.ArrayList;
import java.util.List;

public class Menu {

    private static int choosenCardSuit = 1;
    private static List<Image> backImages = Card.getAllBackImages();
    private static List<Card> cards = new ArrayList<>();


    public static void menuPopUp(Stage stage) {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        VBox dialogVbox = new VBox(20);
        HBox backImages = new HBox(20);
        HBox backGroundImages = new HBox(20);
        HBox buttons = new HBox(20);
        dialogVbox.getChildren().add(backImages);
        dialogVbox.getChildren().add(backGroundImages);
        dialogVbox.getChildren().add(buttons);
        backImages.setAlignment(Pos.CENTER);
        Scene dialogScene = new Scene(dialogVbox, 960, 480);
        initImages(Menu.backImages, backImages);
        initImages(Menu.backImages, backGroundImages);
        initImages(Menu.backImages, buttons);
        dialog.setScene(dialogScene);
        dialog.show();

    }

    private static EventHandler<MouseEvent> onMouseClickedHandler = e -> {
        Card image = (Card) e.getSource();
        changeShadow(2, 0.35);
        choosenCardSuit = image.getSuit();
        changeShadow(20, 1);
    };

    public static void changeShadow(int r, double o) {
        DropShadow dropShadow = new DropShadow(r, Color.gray(0, o));
        cards.get(choosenCardSuit-1).setEffect(dropShadow);
    }

    public static void initImages(List<Image> backImages, HBox dialogBox) {
        for (int i = 0; i < 4; i++) {
            Card card =  new Card(i+1, backImages.get(i));
            card.setOnMouseClicked(onMouseClickedHandler);
            dialogBox.getChildren().add(card);
            if (i == 0) {
                card.setEffect(new DropShadow(20, Color.gray(0, 1)));
            }
            cards.add(card);
        }
    }


    public static void initButtons(Stage stage, HBox dialogBox) {
        Button menu_btn = new Button();
        menu_btn.setLayoutX(460);
        menu_btn.setLayoutY(30);
        menu_btn.setPrefWidth(300);
        menu_btn.setPrefHeight(50);
        menu_btn.setText("MENU");
        menu_btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Menu.menuPopUp(stage);
            }
        });
        dialogBox.getChildren().add(menu_btn);
    }

}
