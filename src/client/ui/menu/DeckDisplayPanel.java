package client.ui.menu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

import org.newdawn.slick.geom.Vector2f;

import client.ui.GenericButton;
import client.ui.ScrollingContext;
import client.ui.Text;
import client.ui.TextField;
import client.ui.UI;
import client.ui.UIBox;
import server.card.cardpack.ConstructedDeck;

public class DeckDisplayPanel extends UIBox {
	public static final String CARD_CLICK = "deckdisplaycardselect";
	public static final String DECK_CONFIRM = "deckdisplaydeckconfirm";
	public static final String BACKGROUND_CLICK = "deckdisplaybackgroundclick";
	public ConstructedDeck deck;
	ScrollingContext scroll;
	ArrayList<CardDisplayUnit> cards = new ArrayList<CardDisplayUnit>();
	GenericButton okbutton;
	TextField textfield;
	Text text;
	boolean edit;

	public DeckDisplayPanel(UI ui, Vector2f pos, boolean edit) {
		super(ui, pos, new Vector2f(1600, 500), "res/ui/uiboxborder.png");
		this.margins.set(10, 10);
		this.edit = edit;
		if (edit) {
			this.textfield = new TextField(ui, new Vector2f(0, -225), new Vector2f(400, 50), "Deck",
					new Text(ui, new Vector2f(0, 0), "Deck", 400, 20, "Verdana", 28, 0, 0));
			this.addChild(this.textfield);
		} else {
			this.text = new Text(ui, new Vector2f(0, -225), "Deck", 300, 20, "Verdana", 34, 0, 0);
			this.addChild(this.text);
		}
		this.scroll = new ScrollingContext(ui, new Vector2f(), new Vector2f((float) this.getWidth(true), 400));
		this.scroll.clip = true;
		this.addChild(this.scroll);
		this.okbutton = new GenericButton(ui, new Vector2f(0, 210), new Vector2f(100, 50), "Ok", 0) {
			@Override
			public void mouseClicked(int button, int x, int y, int clickCount) {
				this.alert(DECK_CONFIRM);
			}
		};
		this.addChild(this.okbutton);
	}

	@Override
	public void onAlert(String strarg, int... intarg) {
		switch (strarg) {
		case CardDisplayUnit.CARD_CLICK:
			this.alert(CARD_CLICK, intarg);
			break;
		case DECK_CONFIRM:
			this.deck.name = this.textfield.getText();
			this.alert(strarg, intarg);
			break;
		case TextField.TEXT_ENTER:
			this.deck.name = this.textfield.getText();
			break;
		default:
			this.alert(strarg, intarg);
			break;
		}
	}

	@Override
	public void mouseClicked(int button, int x, int y, int clickCount) {
		this.alert(BACKGROUND_CLICK);
	}

	public void setDeck(ConstructedDeck deck) {
		if (this.edit) {
			this.textfield.setText(deck.name);
		} else {
			this.text.setText(deck.name);
		}
		for (CardDisplayUnit cdu : this.cards) {
			this.scroll.removeChild(cdu);
		}
		this.cards.clear();
		this.deck = deck;
		if (deck != null) {
			for (Map.Entry<Integer, Integer> entry : deck.idcounts.entrySet()) {
				CardDisplayUnit cdu = new CardDisplayUnit(ui, new Vector2f());
				this.scroll.addChild(cdu);
				this.cards.add(cdu);
				cdu.setCardID(entry.getKey());
				cdu.setCount(entry.getValue());
			}
		}
		this.updateCardPositions();
	}

	public void addCard(int id) {
		boolean newpanel = !this.deck.idcounts.containsKey(id);
		if (this.deck.addCard(id)) {
			if (newpanel) {
				CardDisplayUnit cdu = new CardDisplayUnit(ui, new Vector2f());
				cdu.setCardID(id);
				this.scroll.addChild(cdu);
				this.cards.add(cdu);

			}

			this.getCardDisplayUnit(id).setCount(this.deck.idcounts.get(id));
			this.updateCardPositions();
		}
	}

	public void removeCard(int id) {
		if (this.deck.removeCard(id)) {

			if (!this.deck.idcounts.containsKey(id)) {
				CardDisplayUnit cdu = this.getCardDisplayUnit(id);
				this.scroll.removeChild(cdu);
				this.cards.remove(cdu);
			} else {
				this.getCardDisplayUnit(id).setCount(this.deck.idcounts.get(id));
			}
			this.updateCardPositions();
		}
	}

	public void updateCardPositions() {
		this.cards.sort(new Comparator<CardDisplayUnit>() {
			@Override
			public int compare(CardDisplayUnit a, CardDisplayUnit b) {
				return (a.card.tooltip.cost == b.card.tooltip.cost) ? a.card.tooltip.id - b.card.tooltip.id
						: a.card.tooltip.cost - b.card.tooltip.cost;
			}
		});
		for (int i = 0; i < this.cards.size(); i++) {
			this.cards.get(i).setPos(new Vector2f(i % 8 * 160 - 560, i / 8 * 100 - 70), 0.99);
		}
	}

	private CardDisplayUnit getCardDisplayUnit(int id) {
		for (CardDisplayUnit cdu : this.cards) {
			if (cdu.getCardID() == id) {
				return cdu;
			}
		}
		return null;
	}

}
