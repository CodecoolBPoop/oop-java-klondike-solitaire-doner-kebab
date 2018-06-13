package com.codecool.klondike;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;

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


    private EventHandler<MouseEvent> onMouseClickedHandler = e -> {
        Card card = (Card) e.getSource();
        if (card.getContainingPile().getPileType() == Pile.PileType.STOCK) {
            card.moveToPile(discardPile);
            card.flip();
            card.setMouseTransparent(false);
            System.out.println("Placed " + card + " to the waste.");
        } else if (e.getClickCount() == 2 && !card.isFaceDown()) {
            Card currentPileTopCard = card.getContainingPile().getTopCard();
            if (Card.isSameSuit(card, currentPileTopCard) &&
                    card.getRank() == currentPileTopCard.getRank()) {
                Pile destination = getValidFoundationDestinationPile(card);
                if (destination != null) {
                    ArrayList<Card> slideCard = new ArrayList<>();
                    slideCard.add(card);
                    MouseUtil.slideToDest(slideCard, destination);
                    doubleClick = true;
                    handleValidMove(card, destination);

                    
                }
            }
        }
    };

    private EventHandler<MouseEvent> stockReverseCardsHandler = e -> {
        refillStockFromDiscard();
    };

    private EventHandler<MouseEvent> onMousePressedHandler = e -> {
        dragStartX = e.getSceneX();
        dragStartY = e.getSceneY();
        Pile srcPile;

        Card card = (Card) e.getSource();
        srcPile = card.getContainingPile();
        validMoveSrcPile = srcPile;
        System.out.println("On mouse pressed handler: " + validMoveSrcPile.getName());
        //System.out.println(validMoveSrcPile.numOfCards());
    };

    private EventHandler<MouseEvent> onMouseDraggedHandler = e -> {
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
        //TODO
        if (pile == null) {
            pile = getValidIntersectingPile(card, foundationPiles);
        }
        if (pile != null) {
            handleValidMove(card, pile);
        } else {
            draggedCards.forEach(MouseUtil::slideBack);
            draggedCards.clear();
        }



    };

    public boolean isGameWon() {
        //TODO
        return false;
    }

    public Game() {
        deck = Card.createNewDeck();
        initPiles();
        dealCards();
    }

    public void addMouseEventHandlers(Card card) {
        card.setOnMousePressed(onMousePressedHandler);
        card.setOnMouseDragged(onMouseDraggedHandler);
        card.setOnMouseReleased(onMouseReleasedHandler);
        card.setOnMouseClicked(onMouseClickedHandler);
    }

    public void refillStockFromDiscard() {
        System.out.println("Stock refilled from discard pile.");
        List<Card> discardedCards = FXCollections.observableArrayList();
        discardedCards = discardPile.getCards();
        Collections.reverse(discardedCards);
        for (Card card : discardedCards) {
            card.flip();
        }
        MouseUtil.slideToDest(discardedCards, stockPile);
    }

    public boolean isMoveValid(Card card, Pile destPile) {
        //TODO
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
        System.out.println(msg);
        //System.out.println("Elements of validmovesrcpile: "+ validMoveSrcPile.getCards() + "validmovesrcpile: " + validMoveSrcPile.getName());
        MouseUtil.slideToDest(draggedCards, destPile);


        //System.out.println("Elements of validmovesrcpile: "+ validMoveSrcPile.getCards() + "validmovesrcpile: " + validMoveSrcPile.getName());


        if(validMoveSrcPile.getPileType() == Pile.PileType.TABLEAU && validMoveSrcPile.numOfCards() > 1) {
            Card newTopCard;

            if(!doubleClick) {
                newTopCard = validMoveSrcPile.getNthTopCard(draggedCards.size());
            }else {
                newTopCard = validMoveSrcPile.getNthTopCard(draggedCards.size()+1);
            }

            System.out.println("Handlevalidmove: " + validMoveSrcPile.getName());
            if (newTopCard != null && newTopCard.isFaceDown()) {
                newTopCard.flip();
                System.out.println("handlevalidmove: " + newTopCard.getShortName());
                //System.out.println(validMoveSrcPile.numOfCards());
            }
        }
        doubleClick = false;
        draggedCards.clear();





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
        Collections.shuffle(deck);
        ArrayList<Card> slidingCard = new ArrayList<>();
        Iterator<Card> deckIterator = deck.iterator();
        for (int i = 0; i < 24; i++) {
            Card card = deckIterator.next();
            stockPile.addCard(card);
            addMouseEventHandlers(card);
            getChildren().add(card);
        }
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < i + 1; j++) {
                Card card = deckIterator.next();
                stockPile.addCard(card);
                addMouseEventHandlers(card);
                getChildren().add(card);
                if (i == j) {
                    card.flip();
                }
                slidingCard.add(card);
            }
            MouseUtil.slideToDest(slidingCard, tableauPiles.get(i));
            slidingCard.clear();
        }
    }

    public void setTableBackground(Image tableBackground) {
        setBackground(new Background(new BackgroundImage(tableBackground,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }

}
