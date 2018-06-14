package com.codecool.klondike;

import java.util.ArrayList;
import java.util.List;

public class Move {

    private List<Card> cards;
    private Pile destPile;

    public Move(List<Card> cards, Pile destPile){
        this.cards = cards;
        this.destPile = destPile;
    }

    public Pile getDestPile(){
        return destPile;
    }

    public List<Card> getCards(){
        return cards;
    }

    @Override
    public String toString() {
        return "Cards: " + cards.toString() + " DestPile " + destPile.getName();
    }
}
