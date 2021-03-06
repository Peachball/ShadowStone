package server.event;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.StringTokenizer;

import server.Board;
import server.Player;
import server.card.Card;
import server.card.Minion;
import server.card.Target;
import server.card.effect.EffectStats;

public class EventRestore extends Event {
	public static final int ID = 13;
	// actualheal is (kind of) for display only
	public ArrayList<Integer> heal, actualHeal;
	public ArrayList<Minion> m;

	public EventRestore(ArrayList<Minion> m, ArrayList<Integer> heal) {
		super(ID);
		this.m = new ArrayList<Minion>();
		this.m.addAll(m);
		this.heal = new ArrayList<Integer>();
		this.heal.addAll(heal);
	}

	public EventRestore(Target t, int heal) {
		super(ID);
		this.m = new ArrayList<Minion>();
		this.heal = new ArrayList<Integer>();
		for (Card c : t.getTargets()) {
			if (c instanceof Minion) {
				Minion m = (Minion) c;
				this.m.add(m);
				this.heal.add(heal);
			}
		}
	}

	public EventRestore(Minion m, int heal) {
		super(ID);
		this.m = new ArrayList<Minion>();
		this.m.add(m);
		this.heal = new ArrayList<Integer>();
		this.heal.add(heal);
	}

	@Override
	public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		this.actualHeal = new ArrayList<Integer>();
		for (int i = 0; i < this.m.size(); i++) { // whatever
			Minion minion = this.m.get(i);
			minion.health += this.heal.get(i);
			int healAmount = this.heal.get(i);
			if (minion.health > minion.finalStatEffects.getStat(EffectStats.HEALTH)) {
				healAmount -= minion.health - minion.finalStatEffects.getStat(EffectStats.HEALTH);
				minion.health = minion.finalStatEffects.getStat(EffectStats.HEALTH);
			}
			this.actualHeal.add(healAmount);
		}

		// TODO on healed
	}

	@Override
	public String toString() {
		String ret = this.id + " " + this.m.size() + " ";
		for (int i = 0; i < this.m.size(); i++) {
			ret += this.m.get(i).toReference() + this.heal.get(i) + " ";
		}
		return ret + "\n";
	}

	public static EventRestore fromString(Board b, StringTokenizer st) {
		int size = Integer.parseInt(st.nextToken());
		ArrayList<Minion> m = new ArrayList<Minion>(size);
		ArrayList<Integer> heal = new ArrayList<Integer>(size);
		for (int i = 0; i < size; i++) {
			Minion minion = (Minion) Card.fromReference(b, st);
			int h = Integer.parseInt(st.nextToken());
			m.add(minion);
			heal.add(h);
		}
		return new EventRestore(m, heal);
	}

	@Override
	public boolean conditions() {
		return !this.m.isEmpty();
	}
}
