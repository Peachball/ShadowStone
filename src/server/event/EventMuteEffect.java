package server.event;

import java.util.LinkedList;
import java.util.StringTokenizer;

import server.Board;
import server.card.Card;
import server.card.Minion;
import server.card.Target;
import server.card.effect.Effect;
import server.card.effect.EffectStats;

public class EventMuteEffect extends Event {
	public static final int ID = 29;
	public Card c;
	Effect e;
	boolean mute;

	public EventMuteEffect(Card c, Effect e, boolean mute) {
		super(ID);
		this.c = c;
		this.e = e;
		this.mute = mute;
	}

	public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		this.c.muteEffect(this.e, this.mute);
		if (c instanceof Minion) {
			Minion m = ((Minion) c);
			if (c.finalStatEffects.getStat(EffectStats.HEALTH) < m.health) {
				m.health = m.finalStatEffects.getStat(EffectStats.HEALTH);
			}
			if (m.health <= 0) {
				eventlist.add(new EventDestroy(m));
			}
		}
		if (c.finalStatEffects.getUse(EffectStats.COUNTDOWN)
				&& c.finalStatEffects.getStat(EffectStats.COUNTDOWN) <= 0) {
			eventlist.add(new EventDestroy(c));
		}

	}

	public String toString() {
		return this.id + " " + this.c.toReference() + this.e.toReference() + this.mute + "\n";
	}

	public static EventMuteEffect fromString(Board b, StringTokenizer st) {
		Card c = Card.fromReference(b, st);
		Effect e = Effect.fromReference(b, st);
		boolean mute = Boolean.parseBoolean(st.nextToken());
		return new EventMuteEffect(c, e, mute);
	}

	public boolean conditions() {
		return this.c.getAdditionalEffects().contains(this.e);
	}
}
