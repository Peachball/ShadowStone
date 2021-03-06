package server.card;

import java.util.ArrayList;

import server.Board;
import server.card.cardpack.basic.*;

public class Deck {
	Board board;
	public ArrayList<Card> cards;
	public int team;

	public Deck(Board board, int team) {
		this.board = board;
		this.team = team;
		this.cards = new ArrayList<Card>();

		this.shuffle();
	}

	public void shuffle() {
		ArrayList<Card> newcards = new ArrayList<Card>();
		while (!this.cards.isEmpty()) {
			int randomindex = (int) (Math.random() * this.cards.size());
			newcards.add(this.cards.remove(randomindex));
		}
		this.cards = newcards;
	}

	public void updatePositions() {
		for (int i = 0; i < this.cards.size(); i++) {
			this.cards.get(i).cardpos = i;
		}
	}
}
