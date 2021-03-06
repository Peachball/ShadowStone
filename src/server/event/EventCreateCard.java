package server.event;

import java.util.LinkedList;
import java.util.StringTokenizer;

import client.VisualBoard;
import server.Board;
import server.Player;
import server.card.BoardObject;
import server.card.Card;
import server.card.CardStatus;
import server.card.Deck;
import server.card.Hand;
import server.card.Minion;
import server.card.unleashpower.UnleashPower;

public class EventCreateCard extends Event {
	public static final int ID = 2;
	Card c;
	Board b;
	CardStatus status;
	int team, cardpos;

	public EventCreateCard(Board b, Card c, int team, CardStatus status, int cardpos) {
		super(ID);
		this.c = c;
		this.b = b;
		this.team = team;
		this.status = status;
		this.cardpos = cardpos;
	}

	public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		this.c.team = this.team;
		switch (this.status) {
		case HAND:
			Hand relevantHand = this.b.getPlayer(this.team).hand;
			if (relevantHand.cards.size() >= relevantHand.maxsize) {
				eventlist.add(new EventDestroy(c));
			} else {
				int temppos = this.cardpos == -1 ? (int) relevantHand.cards.size() : this.cardpos;
				temppos = Math.min(this.cardpos, relevantHand.cards.size());
				relevantHand.cards.add(temppos, this.c);
				relevantHand.updatePositions();
			}
			break;
		case BOARD:
			if (this.c instanceof BoardObject) {
				this.b.addBoardObject((BoardObject) this.c, this.team,
						this.cardpos == -1 ? this.c.board.getBoardObjects(this.team).size() : this.cardpos);
				if (c instanceof Minion) {
					((Minion) c).summoningSickness = true;
				}
				if (!loopprotection) {
					eventlist.add(new EventEnterPlay(c));
				}
			}
			break;
		case DECK:
			Deck relevantDeck = this.b.getPlayer(this.team).deck;
			int temppos = this.cardpos == -1 ? (int) relevantDeck.cards.size() : this.cardpos;
			temppos = Math.min(this.cardpos, relevantDeck.cards.size());
			relevantDeck.cards.add(temppos, this.c);
			relevantDeck.updatePositions();
			break;
		case UNLEASHPOWER:
			this.b.getPlayer(this.team).unleashPower = (UnleashPower) this.c;
			((UnleashPower) this.c).p = this.b.getPlayer(this.team);
		default:
			break;
		}
		this.c.status = this.status;
		if (this.b.isClient) {
			this.b.cardsCreated.add(this.c);
		}
	}

	public String toString() {
		return this.id + " " + this.c.toConstructorString() + this.team + " " + this.status.toString() + " "
				+ this.cardpos + "\n";
	}

	public static EventCreateCard fromString(Board b, StringTokenizer st) {
		Card c = Card.createFromConstructorString(b, st);
		if (b instanceof VisualBoard) {
			c.realCard = ((VisualBoard) b).realBoard.cardsCreated.removeFirst();
		}
		int team = Integer.parseInt(st.nextToken());
		String sStatus = st.nextToken();
		CardStatus csStatus = null;
		for (CardStatus cs : CardStatus.values()) {
			if (cs.toString().equals(sStatus)) {
				csStatus = cs;
			}
		}
		int cardpos = Integer.parseInt(st.nextToken());
		return new EventCreateCard(b, c, team, csStatus, cardpos);
	}

	public boolean conditions() {
		return true;
	}
}
