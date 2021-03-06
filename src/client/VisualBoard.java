package client;

import java.util.*;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.MouseListener;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.geom.Vector2f;

import client.ui.Text;
import client.ui.UI;
import client.ui.game.CardSelectPanel;
import client.ui.game.EndTurnButton;
import client.ui.game.UnleashButton;
import server.Board;
import server.card.BoardObject;
import server.card.Card;
import server.card.CardStatus;
import server.card.Leader;
import server.card.Minion;
import server.card.Spell;
import server.card.Target;
import server.card.effect.EffectStats;
import server.event.*;
import utils.DefaultMouseListener;

public class VisualBoard extends Board implements DefaultMouseListener {
	public static final int BO_SPACING = 190;
	// distance mouse can move between mouse down and mouse up for it to count
	// as a click
	public static final double CLICK_DISTANCE_THRESHOLD = 5;
	public static final double CARD_SCALE_DEFAULT = 1, CARD_SCALE_HAND = 0.75, CARD_SCALE_HAND_EXPAND = 1.2,
			CARD_SCALE_BOARD = 1, CARD_SCALE_ABILITY = 1.5, CARD_SCALE_TARGET = 1.2, CARD_SCALE_ATTACK = 1.3,
			CARDS_SCALE_PLAY = 2.5;
	public UI ui;
	public Board realBoard;
	Vector2f mouseDownPos = new Vector2f();
	public Card preSelectedCard, selectedCard, draggingCard, playingCard, visualPlayingCard;
	ArrayList<Card> targetedCards = new ArrayList<Card>();
	int playingX;
	public Minion attackingMinion, unleashingMinion;
	CardSelectPanel cardSelectPanel;
	EndTurnButton endTurnButton;
	Text targetText;
	double animationtimer = 0;
	LinkedList<String> inputeventliststrings = new LinkedList<String>();
	Event currentEvent;

	public boolean disableInput = false;
	boolean expandHand = false;
	boolean draggingUnleash = false;

	public VisualBoard() {
		super();
		this.ui = new UI();
		this.isClient = true;
		this.isServer = false;
		this.realBoard = new Board();
		this.realBoard.isClient = true;
		this.player1.realPlayer = this.realBoard.player1;
		this.player2.realPlayer = this.realBoard.player2;
		this.cardSelectPanel = new CardSelectPanel(this.ui, this);
		this.ui.addUIElementParent(this.cardSelectPanel);
		this.cardSelectPanel.setHide(true);
		this.endTurnButton = new EndTurnButton(this.ui, this);
		this.ui.addUIElementParent(this.endTurnButton);
		this.targetText = new Text(ui, new Vector2f(), "Target", 400, 24, "Verdana", 30, 0, -1);
		this.ui.addUIElementParent(this.targetText);
		// this.cardSelectPanel.draggable = true;
	}

	public void update(double frametime) {
		this.updateEventAnimation(frametime);
		ui.update(frametime);
		player1.update(frametime);
		player2.update(frametime);
		for (BoardObject bo : player1side) {
			bo.update(frametime);
		}
		for (BoardObject bo : player2side) {
			bo.update(frametime);
		}
		String eventstring = this.realBoard.retrieveEventString();
		if (!eventstring.isEmpty()) {
			this.parseEventString(eventstring);
		}
	}

	public void draw(Graphics g) {
		for (BoardObject bo : this.getBoardObjects(this.realBoard.localteam, true, false, false)) {
			bo.targetpos.set(boardPosToX(bo.cardpos, this.localteam), 680);
			bo.draw(g);
		}
		for (BoardObject bo : this.getBoardObjects(this.localteam * -1, true, false, false)) {
			bo.targetpos.set(boardPosToX((bo.cardpos), this.localteam * -1), 420);
			bo.draw(g);
		}
		if (!this.getBoardObjects(this.localteam, false, false, false).isEmpty()) {
			BoardObject localleader = this.getBoardObjects(this.localteam, false, false, false).get(0);
			localleader.targetpos.set(960, 950);
			localleader.draw(g);
			String manastring = this.getPlayer(this.realBoard.localteam).mana + "/"
					+ this.getPlayer(this.realBoard.localteam).maxmana;
			UnicodeFont font = Game.getFont("Verdana", 24, true, false);
			font.drawString(localleader.pos.x - font.getWidth(manastring) / 2, localleader.pos.y - 100, manastring);
		}
		if (!this.getBoardObjects(this.realBoard.localteam * -1, false, false, false).isEmpty()) {
			BoardObject otherleader = this.getBoardObjects(this.realBoard.localteam * -1, false, false, false).get(0);
			otherleader.targetpos.set(960, 100);
			otherleader.draw(g);
			String manastring = this.getPlayer(this.realBoard.localteam * -1).mana + "/"
					+ this.getPlayer(this.realBoard.localteam * -1).maxmana;
			UnicodeFont font = Game.getFont("Verdana", 24, true, false);
			font.drawString(otherleader.pos.x - font.getWidth(manastring) / 2, otherleader.pos.y - 100, manastring);
		}
		if (this.getPlayer(this.realBoard.localteam).unleashPower != null) {
			this.getPlayer(this.realBoard.localteam).unleashPower.targetpos.set(1100, 850);
		}
		if (this.getPlayer(this.realBoard.localteam * -1).unleashPower != null) {
			this.getPlayer(this.realBoard.localteam * -1).unleashPower.targetpos.set(1100, 200);
		}
		this.player1.draw(g);
		this.player2.draw(g);
		for (int i = 0; i < this.getPlayer(this.realBoard.localteam).hand.cards.size(); i++) {
			Card c = this.getPlayer(this.realBoard.localteam).hand.cards.get(i);
			if (c != this.playingCard && c != this.draggingCard && c != this.visualPlayingCard) {
				c.targetpos.set(
						(int) (((i) - (this.getPlayer(this.realBoard.localteam).hand.cards.size()) / 2.)
								* (this.expandHand ? (700 + this.getPlayer(this.localteam).hand.cards.size() * 40)
										: 450)
								/ this.getPlayer(this.realBoard.localteam).hand.cards.size()
								+ (this.expandHand
										? (1300 - this.getPlayer(this.realBoard.localteam).hand.cards.size() * 20)
										: 1400)),
						(this.expandHand ? 900 : 950));
				c.scale = this.expandHand ? CARD_SCALE_HAND_EXPAND : CARD_SCALE_HAND;
			}

			c.draw(g);
		}
		for (int i = 0; i < getPlayer(this.realBoard.localteam * -1).hand.cards.size(); i++) {
			Card c = getPlayer(this.realBoard.localteam * -1).hand.cards.get(i);
			if (c != this.visualPlayingCard) {
				c.targetpos.set((int) (((i) - (getPlayer(this.realBoard.localteam * -1).hand.cards.size()) / 2.) * 500
						/ getPlayer(this.realBoard.localteam * -1).hand.cards.size() + 1500), 100);
				c.scale = CARD_SCALE_HAND;

			}
			c.draw(g);
		}
		for (Card c : this.targetedCards) {
			g.setColor(org.newdawn.slick.Color.red);
			g.drawRect((float) (c.pos.x - Card.CARD_DIMENSIONS.x * c.scale / 2 * 0.9),
					(float) (c.pos.y - Card.CARD_DIMENSIONS.y * c.scale / 2 * 0.9),
					(float) (Card.CARD_DIMENSIONS.x * c.scale * 0.9), (float) (Card.CARD_DIMENSIONS.y * c.scale * 0.9));
			g.setColor(org.newdawn.slick.Color.white);
		}
		this.targetText.setHide(false);
		if (this.playingCard != null && this.playingCard.getNextNeededBattlecryTarget() != null) {
			this.targetText.setText(this.playingCard.getNextNeededBattlecryTarget().description);
			this.targetText.setPos(new Vector2f(this.playingCard.pos.x, this.playingCard.pos.y + 100), 1);
		} else if (this.unleashingMinion != null && this.unleashingMinion.getNextNeededUnleashTarget() != null) {
			this.targetText.setText(this.unleashingMinion.getNextNeededUnleashTarget().description);
			this.targetText.setPos(new Vector2f(this.unleashingMinion.pos.x, this.unleashingMinion.pos.y + 100), 1);
		} else {
			this.targetText.setHide(true);
		}
		ui.draw(g);
		if (this.draggingCard != null && this.draggingCard instanceof BoardObject && this.ui.lastmousepos.y < 750) {
			g.drawLine(this.playBoardPosToX(this.XToPlayBoardPos(this.ui.lastmousepos.x, 1), this.localteam), 600,
					this.playBoardPosToX(this.XToPlayBoardPos(this.ui.lastmousepos.x, 1), this.localteam), 800);
		}
		this.drawEventAnimation(g);
	}

	// auxiliary function for position on board
	private int boardPosToX(int i, int team) {
		if (team == 1) {
			return (int) ((i - 1 - (player1side.size() - 2) / 2.) * BO_SPACING + Game.WINDOW_WIDTH / 2);
		}
		return (int) ((i - 1 - (player2side.size() - 2) / 2.) * BO_SPACING + Game.WINDOW_WIDTH / 2);
	}

	private int XToBoardPos(double x, int team) {
		int pos = 0;
		if (team == 1) {
			pos = (int) (((x - Game.WINDOW_WIDTH / 2) / BO_SPACING) + ((player1side.size() - 2) / 2.) + 0.5) + 1;
			if (pos >= player1side.size()) {
				pos = player1side.size() - 1;
			}
			if (pos < 1) {
				pos = 1;
			}
		} else {
			pos = (int) (((x - Game.WINDOW_WIDTH / 2) / BO_SPACING) + ((player2side.size() - 2) / 2.) + 0.5) + 1;
			if (pos >= player2side.size()) {
				pos = player2side.size() - 1;
			}
			if (pos < 1) {
				pos = 1;
			}
		}
		return pos;
	}

	private int playBoardPosToX(int i, int team) {
		if (team == 1) {
			return (int) ((i - 1.5 - (player1side.size() - 2) / 2.) * BO_SPACING + Game.WINDOW_WIDTH / 2);
		}
		return (int) ((i - 1.5 - (player2side.size() - 2) / 2.) * BO_SPACING + Game.WINDOW_WIDTH / 2);
	}

	private int XToPlayBoardPos(double x, int team) {
		int pos = 0;
		if (team == 1) {
			pos = (int) (((x - Game.WINDOW_WIDTH / 2) / BO_SPACING) + ((player1side.size() - 2) / 2.) + 1) + 1;
			if (pos > player1side.size()) {
				pos = player1side.size();
			}
			if (pos < 1) {
				pos = 1;
			}
		} else {
			pos = (int) (((x - Game.WINDOW_WIDTH / 2) / BO_SPACING) + ((player2side.size() - 2) / 2.) + 1) + 1;
			if (pos > player2side.size()) {
				pos = player2side.size();
			}
			if (pos < 1) {
				pos = 1;
			}
		}
		return pos;
	}

	@Override
	public LinkedList<Event> resolveAll(LinkedList<Event> eventlist, boolean loopprotection) {

		new Exception("this shouldn't happen lmao").printStackTrace();
		return null;

	}

	@Override
	public void parseEventString(String s) {
		if (!this.realBoard.isServer) {
			this.realBoard.parseEventString(s);
		}
		StringTokenizer st = new StringTokenizer(s, "\n");
		System.out.println("EVENTSTRING:");
		System.out.println(s);

		while (st.hasMoreTokens()) {
			String event = st.nextToken();
			this.inputeventliststrings.add(event);
		}

	}

	public void updateEventAnimation(double frametime) {
		if (this.animationtimer > 0) {
			this.animationtimer -= frametime;
		}
		if (this.animationtimer <= 0) {
			if (!this.inputeventliststrings.isEmpty()) {
				StringTokenizer st = new StringTokenizer(this.inputeventliststrings.remove(0));
				this.currentEvent = Event.createFromString(this, st);
				if (this.currentEvent != null && this.currentEvent.conditions()) {
					LinkedList<Event> lmao = new LinkedList<Event>();
					this.currentEvent.resolve(lmao, false);

					if (this.currentEvent instanceof EventMinionAttack) {
						this.animationtimer = 0.2;
					} else if (this.currentEvent instanceof EventMinionAttackDamage) {
						this.animationtimer = 0.5;
					} else if (this.currentEvent instanceof EventDamage) {
						this.animationtimer = 0.5;
					} else if (this.currentEvent instanceof EventRestore) {
						this.animationtimer = 0.5;
					} else if (this.currentEvent instanceof EventMinionDamage) {
						this.animationtimer = 0.25;
					} else if (this.currentEvent instanceof EventUnleash) {
						this.animationtimer = 1;
					} else if (this.currentEvent instanceof EventTurnStart) {
						this.animationtimer = 1;
						if (((EventTurnStart) this.currentEvent).p.team != this.realBoard.localteam) {
							this.realBoard.AIThink();
						} else {
							this.disableInput = false;
						}
					} else if (this.currentEvent instanceof EventPlayCard) {
						this.visualPlayingCard = ((EventPlayCard) this.currentEvent).c;
						this.visualPlayingCard.scale = CARDS_SCALE_PLAY;
						this.animationtimer = 0.7;
					} else if (this.currentEvent instanceof EventPutCard) {
						EventPutCard e = (EventPutCard) this.currentEvent;
						for (Card c : e.c) {
							if (e.status.equals(CardStatus.BOARD)) {
								c.scale = CARD_SCALE_BOARD;
							} else if (e.status.equals(CardStatus.HAND)) {
								c.scale = CARD_SCALE_HAND;
							}
						}
						this.animationtimer = 0.5;
					} else if (this.currentEvent instanceof EventBattlecry) {
						EventBattlecry e = (EventBattlecry) this.currentEvent;
						if (!(e.effect.owner instanceof Spell)) {
							this.animationtimer = 0.7;
						} else {
							this.currentEvent = null;
						}

					} else if (this.currentEvent instanceof EventLastWords) {
						this.animationtimer = 0.4;
					} else if (this.currentEvent instanceof EventFlag || this.currentEvent instanceof EventClash
							|| this.currentEvent instanceof EventOnAttack
							|| this.currentEvent instanceof EventOnAttacked) {
						this.animationtimer = 0.6;
					} else if (this.currentEvent instanceof EventAddEffect) {
						EventAddEffect e = (EventAddEffect) this.currentEvent;
						if (!e.c.isEmpty()) {
							this.animationtimer = 0.3;
						}
					} else if (this.currentEvent instanceof EventRemoveEffect) {
						EventRemoveEffect e = (EventRemoveEffect) this.currentEvent;
						if (e.c != null) {
							this.animationtimer = 0.3;
						}
					} else if (!(this.currentEvent instanceof EventCreateCard)) {
						this.animationtimer = 0.2;
					}

				} else {
					this.currentEvent = null;
				}
			} else {
				this.currentEvent = null;
			}
		}
	}

	public void drawEventAnimation(Graphics g) {
		if (this.currentEvent != null) {
			UnicodeFont font = Game.getFont("Verdana", 60, true, false);
			g.setFont(font);
			String debugevent = this.currentEvent.getClass().getName();
			g.drawString(debugevent, 20, 20);
		}
		if (this.currentEvent != null) {
			if (this.currentEvent instanceof EventMinionAttack) {
				EventMinionAttack e = (EventMinionAttack) this.currentEvent;
				Vector2f pos = e.m1.pos.copy().sub(e.m2.pos).scale((float) (this.animationtimer / 0.2)).add(e.m2.pos);
				g.fillOval(pos.x - 5, pos.y - 5, 10, 10);
			} else if (this.currentEvent instanceof EventMinionAttackDamage) {
				EventMinionAttackDamage e = (EventMinionAttackDamage) this.currentEvent;
				Vector2f pos = e.m1.pos.copy().sub(e.m2.pos).scale((float) (this.animationtimer / 0.5)).add(e.m2.pos);
				Vector2f pos2 = e.m1.pos.copy().sub(e.m2.pos).scale(1 - (float) (this.animationtimer / 0.5))
						.add(e.m2.pos);
				g.fillOval(pos.x - 20, pos.y - 20, 40, 40);
				g.fillOval(pos2.x - 20, pos2.y - 20, 40, 40);
			} else if (this.currentEvent instanceof EventDamage) {
				EventDamage e = (EventDamage) this.currentEvent;
				g.setColor(Color.red);
				UnicodeFont font = Game.getFont("Verdana", 80, true, false);
				g.setFont(font);
				float yoff = (float) (Math.pow(this.animationtimer / 0.5 - 0.5, 2) * 300) - 37.5f;
				for (int i = 0; i < e.m.size(); i++) {
					String dstring = e.damage.get(i) + "";
					g.drawString(dstring, e.m.get(i).pos.x - font.getWidth(dstring) / 2,
							e.m.get(i).pos.y - font.getHeight(dstring) + yoff);
				}
				g.setColor(Color.white);
			} else if (this.currentEvent instanceof EventRestore) {
				EventRestore e = (EventRestore) this.currentEvent;
				g.setColor(Color.green);
				UnicodeFont font = Game.getFont("Verdana", 80, true, false);
				g.setFont(font);
				float yoff = (float) (Math.pow(this.animationtimer / 0.5, 2) * 100) - 12.5f;
				for (int i = 0; i < e.m.size(); i++) {
					String dstring = e.actualHeal.get(i) + "";
					g.drawString(dstring, e.m.get(i).pos.x - font.getWidth(dstring) / 2,
							e.m.get(i).pos.y - font.getHeight(dstring) + yoff);
				}
				g.setColor(Color.white);
			} else if (this.currentEvent instanceof EventMinionDamage) {
				g.setColor(Color.red);
				EventMinionDamage e = (EventMinionDamage) this.currentEvent;
				for (int i = 0; i < e.m2.size(); i++) {
					Vector2f pos = e.m1.pos.copy().sub(e.m2.get(i).pos).scale((float) (this.animationtimer / 0.25))
							.add(e.m2.get(i).pos);
					g.fillOval(pos.x - 20, pos.y - 20, 40, 40);
				}
				g.setColor(Color.white);
			} else if (this.currentEvent instanceof EventUnleash) {
				EventUnleash e = (EventUnleash) this.currentEvent;
				Vector2f pos = e.source.pos.copy().sub(e.m.pos).scale((float) (this.animationtimer / 1)).add(e.m.pos);
				g.setColor(Color.yellow);
				g.fillOval(pos.x - 40, pos.y - 40, 80, 80);
				g.setColor(Color.white);
			} else if (this.currentEvent instanceof EventTurnStart) {
				EventTurnStart e = (EventTurnStart) this.currentEvent;
				UnicodeFont font = Game.getFont("Verdana", 80, true, false);
				String dstring = "TURN START";
				switch (e.p.team * this.realBoard.localteam) { // ez hack
				case 1:
					g.setColor(Color.cyan);
					dstring = "YOUR TURN";
					break;
				case -1:
					g.setColor(Color.red);
					dstring = "OPPONENT'S TURN";
					break;
				}
				g.setFont(font);
				g.drawString(dstring, Game.WINDOW_WIDTH / 2 - font.getWidth(dstring) / 2,
						Game.WINDOW_HEIGHT / 2 - font.getHeight(dstring));
				g.setColor(Color.white);
			} else if (this.currentEvent instanceof EventPlayCard) {
				EventPlayCard e = (EventPlayCard) this.currentEvent;
				e.c.targetpos = new Vector2f(Game.WINDOW_WIDTH / 2, Game.WINDOW_HEIGHT / 2);
			} else if (this.currentEvent instanceof EventPutCard) {

			} else if (this.currentEvent instanceof EventBattlecry) {
				EventBattlecry e = (EventBattlecry) this.currentEvent;
				for (int i = 0; i < 4; i++) {
					Image img = Game.getImage("res/game/battlecry.png");
					float xoffset = ((38f * i) % 64) - 32;
					float yoffset = (((32 * i) + (float) this.animationtimer * 700) % 128) - 64;
					g.drawImage(img, e.effect.owner.pos.x - img.getWidth() / 2 + xoffset,
							e.effect.owner.pos.y - img.getHeight() / 2 + yoffset);
				}
			} else if (this.currentEvent instanceof EventLastWords) {
				EventLastWords e = (EventLastWords) this.currentEvent;
				Image img = Game.getImage("res/game/lastwords.png");
				float yoffset = (float) (this.animationtimer * 250) - 64;
				g.drawImage(img, e.effect.owner.pos.x - img.getWidth() / 2,
						e.effect.owner.pos.y - img.getHeight() / 2 + yoffset);
			} else if (this.currentEvent instanceof EventFlag) {
				EventFlag e = (EventFlag) this.currentEvent;
				Image img = Game.getImage("res/game/flag.png");
				float yoffset = (float) (this.animationtimer * this.animationtimer * 300) - 30;
				g.drawImage(img, e.effect.owner.pos.x - img.getWidth() / 2,
						e.effect.owner.pos.y - img.getHeight() / 2 + yoffset);
			} else if (this.currentEvent instanceof EventClash) {
				EventClash e = (EventClash) this.currentEvent;
				Image img = Game.getImage("res/game/clash.png");
				float yoffset = (float) (this.animationtimer * this.animationtimer * 300) - 30;
				g.drawImage(img, e.effect.owner.pos.x - img.getWidth() / 2,
						e.effect.owner.pos.y - img.getHeight() / 2 + yoffset);
			} else if (this.currentEvent instanceof EventOnAttack) {
				EventOnAttack e = (EventOnAttack) this.currentEvent;
				Image img = Game.getImage("res/game/attack.png");
				float yoffset = (float) (this.animationtimer * this.animationtimer * 300) - 30;
				g.drawImage(img, e.effect.owner.pos.x - img.getWidth() / 2,
						e.effect.owner.pos.y - img.getHeight() / 2 + yoffset);
			} else if (this.currentEvent instanceof EventOnAttacked) {
				EventOnAttacked e = (EventOnAttacked) this.currentEvent;
				Image img = Game.getImage("res/game/defend.png");
				float yoffset = (float) (this.animationtimer * this.animationtimer * 300) - 30;
				g.drawImage(img, e.effect.owner.pos.x - img.getWidth() / 2,
						e.effect.owner.pos.y - img.getHeight() / 2 + yoffset);
			} else if (this.currentEvent instanceof EventAddEffect) {
				EventAddEffect e = (EventAddEffect) this.currentEvent;
				for (Card c : e.c) {
					for (int i = 0; i < 4; i++) {
						Image img = Game.getImage("res/game/battlecry.png");
						float xoffset = (float) Math.random() * 150 - 75;
						float yoffset = (float) (this.animationtimer * 650) - 80 + (float) Math.random() * 150 - 75;
						g.drawImage(img, c.pos.x - img.getWidth() / 2 + xoffset,
								c.pos.y - img.getHeight() / 2 + yoffset);
					}
				}
			} else if (this.currentEvent instanceof EventRemoveEffect) {
				EventRemoveEffect e = (EventRemoveEffect) this.currentEvent;
				for (int i = 0; i < 4; i++) {
					Image img = Game.getImage("res/game/battlecry.png");
					float xoffset = (float) Math.random() * 150 - 75;
					float yoffset = (float) (this.animationtimer * -650) + 80 + (float) Math.random() * 150 - 75;
					g.drawImage(img, e.c.pos.x - img.getWidth() / 2 + xoffset,
							e.c.pos.y - img.getHeight() / 2 + yoffset);
				}
			}

		}

	}

	public BoardObject BOAtPos(Vector2f pos) {
		for (BoardObject bo : player1side) {
			if (bo.isInside(pos)) {
				return bo;
			}
		}
		for (BoardObject bo : player2side) {
			if (bo.isInside(pos)) {
				return bo;
			}
		}
		return null;
	}

	public Card cardInHandAtPos(Vector2f pos) {
		for (int i = getPlayer(this.realBoard.localteam).hand.cards.size() - 1; i >= 0; i--) {
			if (getPlayer(this.realBoard.localteam).hand.cards.get(i).isInside(pos)) {
				return getPlayer(this.realBoard.localteam).hand.cards.get(i);
			}
		}
		return null;
	}

	@Override
	public void mouseClicked(int button, int x, int y, int clickCount) {
		this.ui.mouseClicked(button, x, y, clickCount);
	}

	@Override
	public void mousePressed(int button, int x, int y) {
		// TODO Auto-generated method stub
		this.mouseDownPos.set(x, y);
		if (!this.ui.mousePressed(button, x, y)) { // if we didn't click on
													// anything in the ui
			this.selectedCard = null;
			this.preSelectedCard = null;
			Card c = cardInHandAtPos(new Vector2f(x, y));
			if (c != null) {
				if (!this.handleTargeting(c)) {
					if (this.realBoard.getPlayer(this.realBoard.localteam).canPlayCard(c.realCard) && !this.disableInput
							&& this.expandHand) {
						this.draggingCard = c;
					}
					this.preSelectedCard = c;
					this.expandHand = true;
				}
			} else if (this.getPlayer(this.realBoard.localteam).unleashPower != null
					&& this.getPlayer(this.realBoard.localteam).unleashPower.isInside(new Vector2f(x, y))) {
				this.handleTargeting(null);
				this.preSelectedCard = this.getPlayer(this.realBoard.localteam).unleashPower;
				if (this.getPlayer(this.realBoard.localteam).canUnleash() && !this.disableInput) {
					this.getPlayer(this.realBoard.localteam).unleashPower.scale = CARD_SCALE_ABILITY;
					this.draggingUnleash = true;
					for (BoardObject b : this.getBoardObjects(this.localteam)) {
						if (b instanceof Minion && this.getPlayer(this.realBoard.localteam).canUnleashCard(b)) {
							b.scale = CARD_SCALE_TARGET;
						}
					}
				}
			} else if (this.getPlayer(this.realBoard.localteam * -1).unleashPower != null
					&& this.getPlayer(this.realBoard.localteam * -1).unleashPower.isInside(new Vector2f(x, y))) {
				this.handleTargeting(null);
				this.preSelectedCard = this.getPlayer(this.realBoard.localteam * -1).unleashPower;
			} else {
				this.expandHand = x > 1000 && y > 850;
				BoardObject bo = BOAtPos(new Vector2f(x, y));
				if (!this.handleTargeting(bo)) {
					if (bo != null && bo instanceof Minion && bo.realCard.team == this.realBoard.localteam
							&& ((Minion) bo.realCard).canAttack() && !this.disableInput) {
						this.attackingMinion = (Minion) bo;
						for (BoardObject b : this.getBoardObjects(this.realBoard.localteam * -1)) {
							if (b instanceof Minion && ((Minion) this.attackingMinion.realCard).getAttackableTargets()
									.contains((Minion) b.realCard)) {
								b.scale = CARD_SCALE_TARGET;
							}
						}
						bo.scale = CARD_SCALE_ATTACK;
					}
					this.preSelectedCard = bo;
				}
			}
		}
		// System.out.println("REAL BOARD:");
		// System.out.println(this.realBoard.stateToString());
		// this.realBoard.player1.printHand();
		// this.realBoard.player2.printHand();
		// System.out.println("VISUAL BOARD");
		// this.player1.printHand();
		// this.player2.printHand();
		// System.out.println(this.stateToString());

	}

	@Override
	public void mouseReleased(int button, int x, int y) {
		// TODO Auto-generated method stub
		this.ui.mouseReleased(button, x, y);
		if (this.mouseDownPos.distance(new Vector2f(x, y)) <= CLICK_DISTANCE_THRESHOLD) {
			this.selectedCard = this.preSelectedCard;
		}
		if (this.attackingMinion != null) {
			BoardObject target = BOAtPos(new Vector2f(x, y));
			if (target != null && (target instanceof Minion) && target.team != this.realBoard.localteam
					&& ((Minion) this.attackingMinion.realCard).getAttackableTargets().contains(target.realCard)) {
				this.realBoard.playerOrderAttack((Minion) this.attackingMinion.realCard, (Minion) target.realCard);
				target.scale = CARD_SCALE_BOARD;
			}
			for (BoardObject b : this.getBoardObjects(this.localteam * -1)) {
				if (b instanceof Minion && ((Minion) this.attackingMinion.realCard).getAttackableTargets()
						.contains((Minion) b.realCard)) {
					b.scale = CARD_SCALE_BOARD;
				}
			}
			this.attackingMinion.scale = CARD_SCALE_BOARD;
			this.attackingMinion = null;
		} else if (this.draggingUnleash) {
			this.getPlayer(this.realBoard.localteam).unleashPower.scale = CARD_SCALE_DEFAULT;
			for (BoardObject b : this.getBoardObjects(this.realBoard.localteam)) {
				if (b instanceof Minion) {
					b.scale = CARD_SCALE_BOARD;
				}
			}
			this.draggingUnleash = false;
			BoardObject target = this.BOAtPos(new Vector2f(x, y));
			if (target != null && target instanceof Minion && target.team == this.realBoard.localteam
					&& this.getPlayer(this.realBoard.localteam).canUnleashCard(target)) {
				this.selectUnleashingMinion((Minion) target);
			}
		} else if (this.draggingCard != null) {
			if (y < 750 && this.getPlayer(this.realBoard.localteam).canPlayCard(this.draggingCard)) {
				this.playingCard = this.draggingCard;
				this.selectedCard = null;
				this.resolveNoBattlecryTarget();
				this.animateBattlecryTargets(true);
				this.playingX = x;
			}
			this.draggingCard = null;
		}

		if (this.playingCard != null) {
			Target t = this.playingCard.getNextNeededBattlecryTarget();
			if (t == null) {
				// convert visual card's targets to real card's targets
				this.playingCard.realCard.resetBattlecryTargets();
				LinkedList<Target> visualbt = this.playingCard.getBattlecryTargets();
				LinkedList<Target> realbt = this.playingCard.realCard.getBattlecryTargets();
				for (int i = 0; i < visualbt.size(); i++) {
					for (Card c : visualbt.get(i).getTargets()) {
						realbt.get(i).setTarget(c.realCard);
					}
				}
				this.realBoard.playerPlayCard(this.realBoard.getPlayer(this.realBoard.localteam),
						this.playingCard.realCard, XToPlayBoardPos(this.playingX, this.localteam));
				this.playingCard = null;
			} else {
				this.playingCard.targetpos = new Vector2f(200, 300);
				this.playingCard.scale = CARD_SCALE_ABILITY * CARD_SCALE_HAND;
			}
		} else if (this.unleashingMinion != null) {
			Target t = this.unleashingMinion.getNextNeededUnleashTarget();
			if (t == null) {
				// convert visual card's targets to real card's targets
				((Minion) this.unleashingMinion.realCard).resetUnleashTargets();
				LinkedList<Target> visualut = this.unleashingMinion.getUnleashTargets();
				LinkedList<Target> realut = ((Minion) this.unleashingMinion.realCard).getUnleashTargets();
				for (int i = 0; i < visualut.size(); i++) {
					for (Card c : visualut.get(i).getTargets()) {
						realut.get(i).setTarget(c.realCard);
					}
				}
				this.realBoard.playerUnleashMinion(this.realBoard.getPlayer(this.realBoard.localteam),
						(Minion) this.unleashingMinion.realCard);
				this.unleashingMinion.scale = CARD_SCALE_BOARD;
				this.unleashingMinion = null;
			} else {
				this.unleashingMinion.scale = CARD_SCALE_ABILITY;
			}
		}
	}

	@Override
	public void mouseMoved(int oldx, int oldy, int newx, int newy) {
		// TODO Auto-generated method stub
		this.ui.mouseMoved(oldx, oldy, newx, newy);
	}

	@Override
	public void mouseDragged(int oldx, int oldy, int newx, int newy) {
		// TODO Auto-generated method stub
		this.ui.mouseDragged(oldx, oldy, newx, newy);
		if (this.draggingCard != null) {
			this.draggingCard.targetpos.add(new Vector2f(newx, newy).sub(new Vector2f(oldx, oldy)));
		}
	}

	@Override
	public void mouseWheelMoved(int change) {
		this.ui.mouseWheelMoved(change);
	}

	public boolean handleTargeting(Card c) {
		if (this.disableInput) {
			return false;
		}
		if (this.playingCard != null && this.playingCard.getNextNeededBattlecryTarget() != null) {
			if (c != null && c.realCard.alive
					&& this.playingCard.realCard.getNextNeededBattlecryTarget().canTarget(c.realCard)) {
				if (this.targetedCards.contains(c)) {
					this.targetedCards.remove(c);
				} else {
					this.targetedCards.add(c);
					// whether max targets have been selected or all selectable
					// targets have been selected
					if (this.targetedCards.size() >= this.playingCard.getNextNeededBattlecryTarget().maxtargets
							|| this.targetedCards.size() == this
									.getTargetableCards(this.playingCard.getNextNeededBattlecryTarget()).size()) {
						this.animateBattlecryTargets(false);
						this.playingCard.getNextNeededBattlecryTarget().setTargets(this.targetedCards);
						this.targetedCards.clear();
						this.resolveNoBattlecryTarget();
						this.animateBattlecryTargets(true);
					}
				}

				return true;
			} else {
				this.animateBattlecryTargets(false);
				this.playingCard.scale = this.expandHand ? CARD_SCALE_HAND_EXPAND : CARD_SCALE_HAND;
				this.playingCard.resetBattlecryTargets();
				this.targetedCards.clear();
				this.playingCard = null;
			}
		} else if (this.unleashingMinion != null && this.unleashingMinion.getNextNeededUnleashTarget() != null) {
			if (c != null && c.realCard.alive
					&& ((Minion) this.unleashingMinion.realCard).getNextNeededUnleashTarget().canTarget(c.realCard)) {
				if (this.targetedCards.contains(c)) {
					this.targetedCards.remove(c);
				} else {
					this.targetedCards.add(c);
					// whether max targets have been selected or all selectable
					// targets have been selected
					if (this.targetedCards.size() >= this.unleashingMinion.getNextNeededUnleashTarget().maxtargets
							|| this.targetedCards.size() == this
									.getTargetableCards(this.unleashingMinion.getNextNeededUnleashTarget()).size()) {
						this.animateUnleashTargets(false);
						this.unleashingMinion.getNextNeededUnleashTarget().setTarget(c);
						this.targetedCards.clear();
						this.resolveNoUnleashTarget();
						this.animateUnleashTargets(true);
					}
				}
				return true;
			} else {
				this.animateUnleashTargets(false);
				this.unleashingMinion.scale = CARD_SCALE_BOARD;
				this.unleashingMinion.resetUnleashTargets();
				this.targetedCards.clear();
				this.unleashingMinion = null;
			}
		}
		return false;
	}

	public void selectUnleashingMinion(Minion m) {
		m.resetUnleashTargets();
		((Minion) m.realCard).resetUnleashTargets();
		this.unleashingMinion = m;
		this.resolveNoUnleashTarget();
		this.animateUnleashTargets(true);
	}

	@Override
	public LinkedList<Card> getTargetableCards(Target t) {
		LinkedList<Card> list = new LinkedList<Card>();
		if (t == null) {
			return list;
		}
		for (Card c : this.getTargetableCards()) {
			if (t.canTarget(c.realCard)) {
				list.add(c);
			}
		}
		return list;
	}

	public void animateBattlecryTargets(boolean activate) {
		LinkedList<Card> tc = this.getTargetableCards(this.playingCard.getNextNeededBattlecryTarget());
		for (Card c : tc) {
			c.scale = activate ? CARD_SCALE_TARGET
					: (c.status.equals(CardStatus.HAND) ? CARD_SCALE_HAND : CARD_SCALE_BOARD);

		}
	}

	public void animateUnleashTargets(boolean activate) {
		LinkedList<Card> tc = this.getTargetableCards(this.unleashingMinion.getNextNeededUnleashTarget());
		for (Card c : tc) {
			c.scale = activate ? CARD_SCALE_TARGET
					: (c.status.equals(CardStatus.HAND) ? CARD_SCALE_HAND : CARD_SCALE_BOARD);
		}
	}

	public void resolveNoBattlecryTarget() {
		LinkedList<Card> tc = this.getTargetableCards(this.playingCard.getNextNeededBattlecryTarget());
		while (tc.isEmpty() && this.playingCard.getNextNeededBattlecryTarget() != null) {
			this.playingCard.getNextNeededBattlecryTarget().setTarget(null);
			tc = this.getTargetableCards(this.playingCard.getNextNeededBattlecryTarget());
		}
	}

	public void resolveNoUnleashTarget() {
		LinkedList<Card> tc = this.getTargetableCards(this.unleashingMinion.getNextNeededUnleashTarget());

		while (tc.isEmpty() && this.unleashingMinion.getNextNeededUnleashTarget() != null) {
			this.unleashingMinion.getNextNeededUnleashTarget().setTarget(null);
			tc = this.getTargetableCards(this.unleashingMinion.getNextNeededUnleashTarget());
		}
	}
}
