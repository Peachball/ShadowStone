package client.states;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import client.Game;
import client.ui.GenericButton;
import client.ui.UI;
import client.ui.UIEventListener;
import client.ui.UIMouseListenerWrapper;
import client.ui.menu.DeckSelectPanel;
import client.ui.menu.PlayButton;

public class StateMenu extends BasicGameState {
	UI ui;
	UIMouseListenerWrapper listener;
	PlayButton playButton;

	@Override
	public void init(GameContainer arg0, StateBasedGame arg1) throws SlickException {
		// TODO Auto-generated method stub

	}

	@Override
	public void enter(GameContainer gameContainer, StateBasedGame stateBasedGame) throws SlickException {
		this.ui = new UI();
		this.listener = new UIMouseListenerWrapper(this.ui);
		gameContainer.getInput().addMouseListener(listener);
		gameContainer.getInput().addKeyListener(this.ui);

		this.ui.addListener(new UIEventListener() {
			@Override
			public void onAlert(String strarg, int... intarg) {
				switch (strarg) {
				case DeckSelectPanel.DECK_CONFIRM:
					if (playButton.deckspanel.selectedDeckUnit != null) {
						StateGame.tempdeck = playButton.deckspanel.selectedDeckUnit.deck;
						stateBasedGame.enterState(Game.STATE_GAME);
					}

					break;
				case "deckbuild":
					stateBasedGame.enterState(Game.STATE_DECKBUILD);
					break;
				default:
					break;
				}
			}
		});
		GenericButton deckbuildbutton = new GenericButton(this.ui, new Vector2f(Game.WINDOW_WIDTH / 2, 800),
				new Vector2f(120, 80), "Manage Decks", 0) {
			@Override
			public void mouseClicked(int button, int x, int y, int clickCount) {
				this.alert("deckbuild");
			}
		};
		this.ui.addUIElementParent(deckbuildbutton);
		this.playButton = new PlayButton(ui);
		this.ui.addUIElementParent(this.playButton);
	}

	@Override
	public void leave(GameContainer gameContainer, StateBasedGame stateBasedGame) throws SlickException {
		gameContainer.getInput().removeMouseListener(listener);
	}

	@Override
	public void render(GameContainer arg0, StateBasedGame arg1, Graphics graphics) throws SlickException {
		this.ui.draw(graphics);
	}

	@Override
	public void update(GameContainer arg0, StateBasedGame arg1, int deltaMillis) throws SlickException {
		this.ui.update(deltaMillis / 1000.);
	}

	@Override
	public int getID() {
		return Game.STATE_MENU;
	}

}
