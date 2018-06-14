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

    private static List<Image> backImages;
    private static List<Image> backgroundsImages;
    private static List<Card> cards;
    private static List<Card> backgrounds;
    private static int[] choosenSuits = new int[2];


    public static void menuPopUp(Stage stage, com.codecool.klondike.Game game) {
        init();
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        VBox dialogVbox = new VBox(40);
        HBox backImages = new HBox(20);
        HBox backGroundImages = new HBox(20);
        HBox buttons = new HBox(20);
        dialogVbox.getChildren().add(backImages);
        dialogVbox.getChildren().add(backGroundImages);
        dialogVbox.getChildren().add(buttons);
        backImages.setAlignment(Pos.CENTER);
        backGroundImages.setAlignment(Pos.CENTER);
        buttons.setAlignment(Pos.CENTER);
        Scene dialogScene = new Scene(dialogVbox, 700, 420);
        initImages(Menu.backImages, backImages, cards);
        initImages(Menu.backgroundsImages, backGroundImages, backgrounds);
        initButtons(stage, buttons, game);
        dialog.setScene(dialogScene);
        dialog.show();

    }

    private static EventHandler<MouseEvent> onMouseClickedHandler = e -> {
        Card card = (Card) e.getSource();
        boolean isBackground = card.getBackFaceWidth() == 150.0;
        List<Card> alist = isBackground ? backgrounds : cards;
        changeShadow(alist, 2, 0.35, isBackground);
        choosenSuits[isBackground ? 1 : 0] = card.getSuit();
        changeShadow(alist, 20, 1, isBackground);
    };

    public static void changeShadow(List<Card> alist, int r, double o, boolean isBackground) {
        DropShadow dropShadow = new DropShadow(r, Color.gray(0, o));
        alist.get((choosenSuits[isBackground ? 1 : 0]) - 1).setEffect(dropShadow);
    }

    public static void initImages(List<Image> backImages, HBox dialogBox, List<Card> alist) {
        for (int i = 0; i < 4; i++) {
            Card card = new Card(i + 1, backImages.get(i));
            card.setOnMouseClicked(onMouseClickedHandler);
            dialogBox.getChildren().add(card);
            if (i == 0) {
                card.setEffect(new DropShadow(20, Color.gray(0, 1)));
            }
            alist.add(card);
        }
    }


    public static void initButtons(Stage stage, HBox dialogBox, Game game) {
        Button menu_btn = new Button();
        menu_btn.setLayoutX(460);
        menu_btn.setLayoutY(30);
        menu_btn.setPrefWidth(300);
        menu_btn.setPrefHeight(50);
        menu_btn.setText("SAVE & RESTART");
        menu_btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                game.restartGame(choosenSuits[0], choosenSuits[1]);
            }
        });
        dialogBox.getChildren().add(menu_btn);
    }

    public static List<Image> getAllBackgrounds() {
        List<Image> backgrounds = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            backgrounds.add(new Image("table/background" + (i + 1) + ".png", 150, 100, false, false));
        }
        return backgrounds;
    }

    private static void init() {

        backImages = Card.getAllBackImages();
        backgroundsImages = getAllBackgrounds();
        cards = new ArrayList<>();
        backgrounds = new ArrayList<>();
        choosenSuits[0] = 1;
        choosenSuits[1] = 1;

    }


}
