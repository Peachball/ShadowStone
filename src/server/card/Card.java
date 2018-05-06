package server.card;

import java.awt.Font;
import java.util.LinkedList;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.geom.Vector2f;

import client.Game;
import server.Board;
import server.event.Event;

public class Card {
	public static final Vector2f CARD_DIMENSIONS = new Vector2f(150, 200);
	public static final double EPSILON = 0.0001;
	public static final double NAME_FONT_SIZE = 24;
	public Board board;
	public int id, cost, handpos, team;
	public String name, text, imagepath;
	public Vector2f targetpos, pos;
	public double scale;
	double speed;
	Image image;
	public CardStatus status;

	public LinkedList<Target> targets;

	public Card() {
		this.cost = 1;
		this.status = CardStatus.BOARD;
		this.targets = new LinkedList<Target>();
		this.team = 0;
	}

	public Card(Board board, CardStatus status, int cost, String name, String text, String imagepath, int team,
			int id) {
		this.board = board;
		this.cost = cost;
		this.name = name;
		this.text = text;
		if (imagepath != null) {
			this.image = Game.getImage(imagepath).getScaledCopy((int) CARD_DIMENSIONS.x, (int) CARD_DIMENSIONS.y);
		}
		this.imagepath = imagepath;
		this.targetpos = new Vector2f();
		this.pos = new Vector2f();
		this.speed = 0.999;
		this.scale = 1;
		this.id = id;
		this.status = status;
		this.targets = new LinkedList<Target>();
		this.team = team;
	}

	public void update(double frametime) {
		Vector2f delta = this.targetpos.copy().sub(this.pos);
		if (delta.length() > EPSILON) {
			float ratio = 1 - (float) Math.pow(1 - this.speed, frametime);
			this.pos.add(delta.scale(ratio));
		}
	}

	public void draw(Graphics g) {
		Image scaledCopy = this.image.getScaledCopy((float) this.scale);
		g.drawImage(scaledCopy, (int) (this.pos.x - CARD_DIMENSIONS.x * this.scale / 2),
				(int) (this.pos.y - CARD_DIMENSIONS.y * this.scale / 2));
		switch (this.status) {
		case BOARD:
			this.drawOnBoard(g);
			break;
		case HAND:
			UnicodeFont font = Game.getFont("Verdana", (NAME_FONT_SIZE * this.scale), true, false);
			font.drawString(this.pos.x - font.getWidth(this.name) / 2,
					this.pos.y - CARD_DIMENSIONS.y * (float) this.scale / 2, this.name);
			this.drawInHand(g);
			break;
		default:
			break;
		}
	}

	public void drawOnBoard(Graphics g) {

	}

	public void drawInHand(Graphics g) {

	}

	public LinkedList<Event> battlecry() {
		return new LinkedList<Event>();
	}

	public boolean conditions() { // to be able to even begin to play
		return true;
	}

	public void resetTargets() {
		for (Target t : this.targets) {
			t.reset();
		}
	}

	public Target getNextNeededTarget() {
		for (Target t : this.targets) {
			if (!t.ready()) {
				return t;
			}
		}
		return null;
	}

	public boolean isInside(Vector2f p) {
		return p.x >= this.pos.x - this.image.getWidth() / 2 * this.scale
				&& p.y >= this.pos.y - this.image.getHeight() / 2 * this.scale
				&& p.x <= this.pos.x + this.image.getWidth() / 2 * this.scale
				&& p.y <= this.pos.y + this.image.getHeight() / 2 * this.scale;
	}

	public String targetsToString() {
		String st = "targets ";
		for (Target t : this.targets) {
			if (t.target != null) {
				st += t.target.toString() + " ";
			}
		}
		return st;
	}

	public String posToString() {
		switch (this.status) {
		case HAND:
			return "hand " + this.handpos;
		case BOARD:
			return "board";
		case DECK:
			return "deck";
		default:
			return "";
		}
	}

	public String toString() {
		return "card " + this.id + " " + this.posToString() + " " + this.targetsToString();
	}
}
