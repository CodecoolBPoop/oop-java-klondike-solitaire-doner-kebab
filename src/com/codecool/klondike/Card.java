package com.codecool.klondike;

import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.util.*;

public class Card extends ImageView {

    private int suit;
    private int rank;
    private boolean faceDown;

    private Image backFace;
    private Image frontFace;
    private Pile containingPile;
    private DropShadow dropShadow;

    static Image cardBackImage;
    private static final Map<String, Image> cardFaceImages = new HashMap<>();
    public static final int WIDTH = 150;
    public static final int HEIGHT = 215;

    public Card(int suit, int rank, boolean faceDown) {
        this.suit = suit;
        this.rank = rank;
        this.faceDown = faceDown;
        this.dropShadow = new DropShadow(2, Color.gray(0, 0.75));
        backFace = cardBackImage;
        frontFace = cardFaceImages.get(getShortName());
        setImage(faceDown ? backFace : frontFace);
        setEffect(dropShadow);
    }

    public Card(int suit, Image backImage) {
        this.suit = suit;
        this.dropShadow = new DropShadow(2, Color.gray(0, 0.35));
        setImage(backImage);
        setEffect(dropShadow);
    }

    public int getSuit() {
        return suit;
    }

    public int getRank() {
        return rank;
    }

    public boolean isFaceDown() {
        return faceDown;
    }

    public String getShortName() {
        return "S" + suit + "R" + rank;
    }

    public DropShadow getDropShadow() {
        return dropShadow;
    }

    public Pile getContainingPile() {
        return containingPile;
    }

    public void setContainingPile(Pile containingPile) {
        this.containingPile = containingPile;
    }

    public void moveToPile(Pile destPile) {

        if(destPile.getPileType() == Pile.PileType.TABLEAU) {
            Pile contPile;
            contPile = this.getContainingPile();
            contPile.removeSpecificCard(this);
            System.out.println("Elements of contpile now: " + contPile.getCards() + " contpile name: " + contPile.getName());
        }else {
            this.getContainingPile().getCards().remove(this);
        }
        destPile.addCard(this);
    }

    public void flip() {
        faceDown = !faceDown;
        setImage(faceDown ? backFace : frontFace);
    }

    @Override
    public String toString() {
        return "The " + "Rank" + rank + " of " + "Suit" + suit;
    }

    public static boolean isOppositeColor(Card card1, Card card2) {
        if ((card1.suit == 1 || card1.suit == 2) && (card2.suit == 3 | card2.suit == 4)) {
            return true;
        }
        return (card1.suit == 3 || card1.suit == 4) && (card2.suit == 1 | card2.suit == 2);
    }

    public static boolean isSameSuit(Card card1, Card card2) {
        return card1.getSuit() == card2.getSuit();
    }

    public static List<Card> createNewDeck() {
        List<Card> result = new ArrayList<>();
        for (int suit = 1; suit < 5; suit++) {
            for (int rank = 1; rank < 14; rank++) {
                result.add(new Card(suit, rank, true));
            }
        }
        return result;
    }

    public static void loadCardImages() {
        cardBackImage = new Image("card_images/card_back2.png", 150, 215, false, false);
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                String cardName = suit.toString() + rank;
                String cardId = "S" + suit.getId() + "R" + rank;
                String imageFileName = "card_images/" + cardName + ".png";
                cardFaceImages.put(cardId, new Image(imageFileName));
            }
        }
    }

    public static List<Image> getAllBackImages() {
        List<Image> backImages = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            backImages.add(new Image("card_images/card_back" + (i + 1) + ".png", 100, 140, false, false));
        }
        return backImages;
    }

    public enum Suit{
        HEARTS ("hearts", "1"),
        DIAMONDS ("diamonds","2"),
        SPADES ("spades","3"),
        CLUBS ("clubs","4");

        private final String suit;
        private final String id;
        Suit(String suit, String id){
            this.suit = suit;
            this.id = id;
        }

        public String toString(){
            return suit;
        }

        public String getId(){
            return id;
        }

    }

    public enum Rank{

        TWO ("2"),
        THREE ("3"),
        FOUR ("4"),
        FIVE ("5"),
        SIX ("6"),
        SEVEN ("7"),
        EIGHT ("8"),
        NINE ("9"),
        TEN ("10"),
        JACK ("11"),
        QUEEN ("12"),
        KING ("13"),
        ACE ("1");

        private final String rank;

        Rank(String rank){
            this.rank = rank;
        }

        public String toString(){
            return rank;
        }

    }



}
