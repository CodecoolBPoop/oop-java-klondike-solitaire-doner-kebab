package com.codecool.klondike;

import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.*;

public class Game extends Pane {

    private List<Card> deck = new ArrayList<>();

    private Pile stockPile;
    private Pile discardPile;
    private List<Pile> foundationPiles = FXCollections.observableArrayList();
    private List<Pile> tableauPiles = FXCollections.observableArrayList();

    private double dragStartX, dragStartY;
    private List<Card> draggedCards = FXCollections.observableArrayList();

    private static double STOCK_GAP = 1;
    private static double FOUNDATION_GAP = 0;
    private static double TABLEAU_GAP = 30;

    private Pile validMoveSrcPile;
    private boolean doubleClick = false;
    private List<Move> previousMoves = new ArrayList<>();

    private Stage stage;

    private int cardBackImage = 1;
    private int backgroundImage= 1;


    private EventHandler<MouseEvent> onMouseClickedHandler = e -> {
        if (e.getButton() == MouseButton.SECONDARY)
            return;
        Card card = (Card) e.getSource();
        if (card.getContainingPile().getPileType() == Pile.PileType.STOCK) {
            //Part of UNDO
            List<Card> discardedCard = new ArrayList<>();
            discardedCard.add(card);
            Move discardMove = new Move(discardedCard,stockPile);
            previousMoves.add(discardMove);
            //-------

            card.moveToPile(discardPile);
            card.flip();
            card.setMouseTransparent(false);
            System.out.println("Placed " + card + " to the waste.");
        } else if (e.getClickCount() == 2 && !card.isFaceDown()) {
            Card currentPileTopCard = card.getContainingPile().getTopCard();
            if (Card.isSameSuit(card, currentPileTopCard) &&
                    card.getRank() == currentPileTopCard.getRank()) {
                Pile destination;
                if (String.valueOf(card.getRank()).equals(Card.Rank.valueOf("ACE").toString()))
                    destination = foundationPiles.get(card.getSuit() - 1);
                else
                    destination = getValidFoundationDestinationPile(card);
                autoMoveCard(card, destination);
            }
        }
    };

    public void handleRightMouseClick() {
        Pile destination;
        Card topCard;
        int kings = 0;

        topCard = discardPile.getTopCard();
        if (topCard != null) {
            if (String.valueOf(topCard.getRank()).equals(Card.Rank.valueOf("ACE").toString()))
                destination = foundationPiles.get(topCard.getSuit() - 1);
            else
                destination = getValidFoundationDestinationPile(topCard);
            autoMoveCard(topCard, destination);
        }

        for (Pile pile : tableauPiles) {
            if (pile.isEmpty())
                continue;
            topCard = pile.getTopCard();
            validMoveSrcPile = topCard.getContainingPile();
            if (String.valueOf(topCard.getRank()).equals(Card.Rank.valueOf("ACE").toString()))
                destination = foundationPiles.get(topCard.getSuit() - 1);
            else
                destination = getValidFoundationDestinationPile(topCard);
            autoMoveCard(topCard, destination);
            if (String.valueOf(topCard.getRank()).equals(Card.Rank.valueOf("KING").toString()) &&
                    destination != null)
                kings++;
        }

        if (kings == 4)
            gameWon(topCard);
    }

    private void autoMoveCard(Card card, Pile destination) {
        if (destination != null) {
            ArrayList<Card> slideCard = new ArrayList<>();
            slideCard.add(card);
            MouseUtil.slideToDest(slideCard, destination);
            doubleClick = true;
            handleValidMove(card, destination);
        }
    }

    private EventHandler<MouseEvent> stockReverseCardsHandler = e -> {
        refillStockFromDiscard();
    };

    private EventHandler<MouseEvent> onMousePressedHandler = e -> {
        if (e.getButton() == MouseButton.SECONDARY)
            return;

        dragStartX = e.getSceneX();
        dragStartY = e.getSceneY();
        Pile srcPile;

        Card card = (Card) e.getSource();
        srcPile = card.getContainingPile();
        validMoveSrcPile = srcPile;
    };

    private EventHandler<MouseEvent> onMouseDraggedHandler = e -> {
        if (e.getButton() == MouseButton.SECONDARY)
            return;

        Card card = (Card) e.getSource();
        if (card.isFaceDown()) {
            return;
        }
        Pile activePile = card.getContainingPile();
        if (activePile.getPileType() == Pile.PileType.STOCK)
            return;
        double offsetX = e.getSceneX() - dragStartX;
        double offsetY = e.getSceneY() - dragStartY;

        draggedCards.clear();

        List<Card> cardsOfActivePile = FXCollections.observableArrayList();
        cardsOfActivePile = activePile.getCards();
        boolean isUnder = false;
        for (int i = 0; i < cardsOfActivePile.size(); i++) {
            if (cardsOfActivePile.get(i) == card) {
                isUnder = true;
            }
            if (isUnder) {
                draggedCards.add(cardsOfActivePile.get(i));
                cardsOfActivePile.get(i).setTranslateX(offsetX);
                cardsOfActivePile.get(i).setTranslateY(offsetY);
                cardsOfActivePile.get(i).getDropShadow().setOffsetX(10);
                cardsOfActivePile.get(i).getDropShadow().setOffsetY(10);
                cardsOfActivePile.get(i).toFront();
            }
        }
    };

    private EventHandler<MouseEvent> onMouseReleasedHandler = e -> {
        if (draggedCards.isEmpty())
            return;
        Card card = (Card) e.getSource();
        Pile pile = getValidIntersectingPile(card, tableauPiles);
        if (pile == null) {
            pile = getValidIntersectingPile(card, foundationPiles);
        }
        if (pile != null) {
            if (String.valueOf(card.getRank()).equals(Card.Rank.valueOf("ACE").toString()))
                pile = foundationPiles.get(card.getSuit() - 1);
            handleValidMove(card, pile);
            if (isGameWon(card, pile)) {
                gameWon(card);
            }
        } else {
            draggedCards.forEach(MouseUtil::slideBack);
            draggedCards.clear();
        }
    };

    private boolean isGameWon(Card card, Pile destPile) {
        for (Pile pile : foundationPiles) {
            if (pile.isEmpty()) {
                return false;
            } else if (destPile != pile && pile.numOfCards() != 13) {
                return false;
            } else {
                    String kingRank = Card.Rank.valueOf("KING").toString();
                    String queenRank = Card.Rank.valueOf("QUEEN").toString();
                    String cardRank = String.valueOf(card.getRank());
                    String topCardRank = String.valueOf(destPile.getTopCard().getRank());
                    if (!cardRank.equals(kingRank) || !topCardRank.equals(queenRank)) {
                        return false;
                    }
                }
        }
        return true;
    }

    private void gameWon(Card card) {
        System.out.println("You have won!");
        removeMouseEventHandlers(card);
        for (Pile pile : foundationPiles) {
            List<Card> cards = pile.getCards();
            for (Card pileCard : cards) {
                removeMouseEventHandlers(pileCard);
            }
        }
        for (Pile pile : tableauPiles) {
            List<Card> cards = pile.getCards();
            for (Card pileCard : cards) {
                removeMouseEventHandlers(pileCard);
            }
        }
        winPopUp();
    }

    public Game() {
        deck = Card.createNewDeck();
        initPiles();
        dealCards();
        initButtons();
    }

    public void winPopUp() {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        VBox dialogVbox = new VBox(20);
        dialogVbox.setAlignment(Pos.CENTER);
        dialogVbox.getChildren().add(new Text("!!! C O N G R A T U L A T I O N S !!!"));
        dialogVbox.getChildren().add(new Text("You have won the game!"));
        Scene dialogScene = new Scene(dialogVbox, 300, 200);
        dialog.setScene(dialogScene);
        dialog.show();
    }


    public void addMouseEventHandlers(Card card) {
        card.setOnMousePressed(onMousePressedHandler);
        card.setOnMouseDragged(onMouseDraggedHandler);
        card.setOnMouseReleased(onMouseReleasedHandler);
        card.setOnMouseClicked(onMouseClickedHandler);
    }

    public void removeMouseEventHandlers(Card card) {
        card.setOnMousePressed(null);
        card.setOnMouseDragged(null);
        card.setOnMouseReleased(null);
        card.setOnMouseClicked(null);
    }

    public void refillStockFromDiscard() {
        System.out.println("Stock refilled from discard pile.");
        List<Card> discardedCards = FXCollections.observableArrayList();
        discardedCards = discardPile.getCards();
        Collections.reverse(discardedCards);
        for (Card card : discardedCards) {
            card.flip();
        }

        //Part of UNDO
        List<Card> undoCards = stockPile.getCards();
        //Collections.reverse(undoCards);
        previousMoves.add(new Move(undoCards,discardPile));
        //------
        MouseUtil.slideToDest(discardedCards, stockPile);
    }

    public boolean isMoveValid(Card card, Pile destPile) {
        if (destPile.getPileType() == Pile.PileType.FOUNDATION) {
            Card topCard = destPile.getTopCard();
            if (topCard == null && card.getRank() == 1) {
                return true;
            } else if (topCard != null && card.getSuit() == topCard.getSuit() && card.getRank() == topCard.getRank() + 1) {
                return true;
            }
        }
        if (destPile.getPileType() == Pile.PileType.TABLEAU) {
            Card topCard = destPile.getTopCard();
            if (topCard == null && card.getRank() == 13) {
                return true;
            } else {
                return topCard != null && topCard.getRank() - card.getRank() == 1 && Card.isOppositeColor(topCard, card);
            }
        }
        return false;
    }

    private Pile getValidIntersectingPile(Card card, List<Pile> piles) {
        Pile result = null;
        for (Pile pile : piles) {
            if (!pile.equals(card.getContainingPile()) &&
                    isOverPile(card, pile) &&
                    isMoveValid(card, pile))
                result = pile;
        }
        return result;
    }

    private boolean isOverPile(Card card, Pile pile) {
        if (pile.isEmpty())
            return card.getBoundsInParent().intersects(pile.getBoundsInParent());
        else
            return card.getBoundsInParent().intersects(pile.getTopCard().getBoundsInParent());
    }

    private Pile getValidFoundationDestinationPile(Card card) {
        Pile result = null;
        for (Pile pile : foundationPiles) {
            if (card.getRank() == 1 && pile.isEmpty()) {
                result = pile;
            } else if (!pile.isEmpty() && Card.isSameSuit(card, pile.getTopCard()) &&
                    card.getRank() == pile.getTopCard().getRank() + 1) {
                result = pile;
            }
        }
        return result;
    }

    private void handleValidMove(Card card, Pile destPile) {
        String msg = null;
        if (destPile.isEmpty()) {
            if (destPile.getPileType().equals(Pile.PileType.FOUNDATION))
                msg = String.format("Placed %s to the foundation.", card);
            if (destPile.getPileType().equals(Pile.PileType.TABLEAU))
                msg = String.format("Placed %s to a new pile.", card);
        } else {
            msg = String.format("Placed %s to %s.", card, destPile.getTopCard());
        }

        //Part of UNDO
        List<Card> currentDraggedCards = new ArrayList<>();
        if(draggedCards.size() > 0) {
            currentDraggedCards.addAll(draggedCards);
        }else{
            currentDraggedCards.add(card);
        }

        Move currentMove;
        currentMove = new Move(currentDraggedCards,validMoveSrcPile);

        previousMoves.add(currentMove);

        MouseUtil.slideToDest(draggedCards, destPile);

        if(validMoveSrcPile.getPileType() == Pile.PileType.TABLEAU && validMoveSrcPile.numOfCards() > 1) {
            Card newTopCard;

            if(!doubleClick) {
                newTopCard = validMoveSrcPile.getNthTopCard(draggedCards.size());
            }else {
                newTopCard = validMoveSrcPile.getNthTopCard(draggedCards.size()+1);
            }

            if (newTopCard != null && newTopCard.isFaceDown()) {
                newTopCard.flip();
            }
        }
        doubleClick = false;
        draggedCards.clear();
    }

    private void undo() {
        if(previousMoves.size() > 0) {
            Move moveToUndo = previousMoves.get(previousMoves.size() - 1);
            previousMoves.remove(previousMoves.size()-1);
            Pile destPile = moveToUndo.getDestPile();
            List<Card> cards = moveToUndo.getCards();
            Pile srcPile = cards.get(0).getContainingPile();

            if(destPile.getPileType() == Pile.PileType.STOCK){
                for(Card card: cards){
                    card.flip();
                }
            }

            if(destPile.getPileType() == Pile.PileType.TABLEAU && destPile.numOfCards() > 0){
                Card cardToUnflip;

                if(cards.size() == 1) {
                    cardToUnflip = destPile.getNthTopCard(cards.size() - 1);
                    //cardToUnflip = destPile.getTopCard();
                    cardToUnflip.flip();
                }else {
                    cardToUnflip = destPile.getTopCard();
                    cardToUnflip.flip();
                }

            }

            if(destPile.getPileType() == Pile.PileType.DISCARD && cards.size() > 1){
                for(Card card: cards){
                    card.flip();
                }
                Collections.reverse(cards);
            }

            MouseUtil.slideToDest(cards,destPile);

            if(srcPile.getPileType() == Pile.PileType.TABLEAU  && srcPile.getNthTopCard(1).isFaceDown()){
                srcPile.getNthTopCard(1).flip();
            }




        }else{
            System.out.println("Nothing to undo");
        }

    }

    private void initPiles() {
        stockPile = new Pile(Pile.PileType.STOCK, "Stock", STOCK_GAP);
        stockPile.setBlurredBackground();
        stockPile.setLayoutX(95);
        stockPile.setLayoutY(20);
        stockPile.setOnMouseClicked(stockReverseCardsHandler);
        getChildren().add(stockPile);

        discardPile = new Pile(Pile.PileType.DISCARD, "Discard", STOCK_GAP);
        discardPile.setBlurredBackground();
        discardPile.setLayoutX(285);
        discardPile.setLayoutY(20);
        getChildren().add(discardPile);

        for (int i = 0; i < 4; i++) {
            Pile foundationPile = new Pile(Pile.PileType.FOUNDATION, "Foundation " + i, FOUNDATION_GAP);
            foundationPile.setBlurredBackground();
            foundationPile.setLayoutX(610 + i * 180);
            foundationPile.setLayoutY(20);
            foundationPiles.add(foundationPile);
            getChildren().add(foundationPile);
        }
        for (int i = 0; i < 7; i++) {
            Pile tableauPile = new Pile(Pile.PileType.TABLEAU, "Tableau " + i, TABLEAU_GAP);
            tableauPile.setBlurredBackground();
            tableauPile.setLayoutX(95 + i * 180);
            tableauPile.setLayoutY(275);
            tableauPiles.add(tableauPile);
            getChildren().add(tableauPile);
        }
    }

    public void dealCards() {
//        Collections.shuffle(deck);
        ArrayList<Card> slidingCard = new ArrayList<>();
        Iterator<Card> deckIterator = deck.iterator();
        for (int i = 0; i < 52; i++) {
            Card card = deckIterator.next();
            stockPile.addCard(card);
            addMouseEventHandlers(card);
            getChildren().add(card);
        }
//        for (int i = 0; i < 7; i++) {
//            for (int j = 0; j < i + 1; j++) {
//                Card card = deckIterator.next();
//                addMouseEventHandlers(card);
//                stockPile.addCard(card);
//                getChildren().add(card);
//                if (i == j) {
//                    card.flip();
//                }
//                slidingCard.add(card);
//            }
//            MouseUtil.slideToDest(slidingCard, tableauPiles.get(i));
//            slidingCard.clear();
//        }
    }

    public void initButtons() {
        Button menu_btn = new Button();
        Game tempGame = this;
        menu_btn.setLayoutX(455);
        menu_btn.setLayoutY(30);
        menu_btn.setPrefWidth(130);
        menu_btn.setPrefHeight(50);
        menu_btn.setText("MENU");
        menu_btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Menu.menuPopUp(stage, tempGame);
            }
        });
        getChildren().add(menu_btn);


        Button restart_btn = new Button();
        restart_btn.setLayoutX(455);
        restart_btn.setLayoutY(100);
        restart_btn.setPrefWidth(130);
        restart_btn.setPrefHeight(50);
        restart_btn.setText("RESTART");
        restart_btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                restartGame(cardBackImage, backgroundImage);
            }
        });
        getChildren().add(restart_btn);

        Button undo_btn = new Button();
        undo_btn.setLayoutX(455);
        undo_btn.setLayoutY(170);
        undo_btn.setPrefWidth(130);
        undo_btn.setPrefHeight(50);
        undo_btn.setText("UNDO");
        undo_btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                undo();
            }
        });
        getChildren().add(undo_btn);
    }

    public void setTableBackground(Image tableBackground) {
        setBackground(new Background(new BackgroundImage(tableBackground,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }

    public void restartGame(int cardBack, int background) {
        cardBackImage = cardBack;
        backgroundImage = background;
        stockPile.clear();
        for (int i=0; i < 7; i++) {
            tableauPiles.get(i).clear();
        }
        for (int i=0; i < 4; i++) {
            foundationPiles.get(i).clear();
        }
        discardPile.clear();
        foundationPiles.clear();
        tableauPiles.clear();
        this.setTableBackground(new Image("/table/background" + background + ".png"));
        Card.loadCardImages(cardBack);
        deck = Card.createNewDeck();
        getChildren().clear();
        initPiles();
        dealCards();
        initButtons();
    }

    public int getBackgroundImage() {
        return backgroundImage;
    }

    public int getCardBackImage() {
        return cardBackImage;
    }


}
