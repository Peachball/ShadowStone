package server.card.effect;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.StringTokenizer;

import client.Game;
import server.Board;
import server.card.*;
import server.event.Event;
import server.event.EventDraw;

public class Effect {
	public int id = 0, pos = 0;
	public Card owner;
	public String description;
	public boolean basic = false, mute = false;

	public EffectStats set = new EffectStats(), change = new EffectStats();
	public LinkedList<Target> battlecryTargets = new LinkedList<Target>(), unleashTargets = new LinkedList<Target>();

	public Effect(Card owner, int id, String description) {
		this.owner = owner;
		this.description = description;
	}

	public Effect(Card owner, int id, String description, int cost) {
		this(owner, id, description);
		this.set.setStat(EffectStats.COST, cost);
	}

	public Effect(Card owner, int id, String description, int cost, int attack, int magic, int health) {
		this(owner, id, description, cost);
		this.set.setStat(EffectStats.ATTACK, attack);
		this.set.setStat(EffectStats.MAGIC, magic);
		this.set.setStat(EffectStats.HEALTH, health);
	}

	public Effect(Card owner, int id, String description, int cost, int attack, int magic, int health,
			int attacksperturn, boolean storm, boolean rush, boolean ward) {
		this(owner, id, description, cost, attack, magic, health);
		this.set.setStat(EffectStats.ATTACKS_PER_TURN, attacksperturn);
		this.set.setStat(EffectStats.STORM, storm ? 1 : 0);
		this.set.setStat(EffectStats.RUSH, rush ? 1 : 0);
		this.set.setStat(EffectStats.WARD, ward ? 1 : 0);
	}

	public Effect applyEffectStats(Effect e) {
		for (int i = 0; i < e.set.stats.length; i++) {
			if (e.set.use[i]) {
				this.set.setStat(i, e.set.stats[i]);
				this.change.resetStat(i);
			}
		}
		for (int i = 0; i < e.change.stats.length; i++) {
			if (e.change.use[i]) {
				this.change.changeStat(i, e.change.stats[i]);
			}
		}
		return this;
	}

	public int getStat(int index) {
		return this.set.stats[index] + this.change.stats[index];
	}

	public String statsToString() {
		return "(" + this.getStat(EffectStats.COST) + " " + this.getStat(EffectStats.ATTACK) + " "
				+ this.getStat(EffectStats.MAGIC) + " " + this.getStat(EffectStats.HEALTH) + ")";
	}

	public LinkedList<Event> battlecry() {
		return new LinkedList<Event>();
	}

	public void setBattlecryTargets(LinkedList<Target> targets) {
		this.battlecryTargets = targets;
	}

	public void resetBattlecryTargets() {
		for (Target t : this.battlecryTargets) {
			t.reset();
		}
	}

	public Target getNextNeededBattlecryTarget() {
		for (Target t : this.battlecryTargets) {
			if (!t.ready()) {
				return t;
			}
		}
		return null;
	}

	public String battlecryTargetsToString() {
		String ret = "btargets " + this.battlecryTargets.size();
		for (Target t : this.battlecryTargets) {
			ret += t.toString() + " ";
		}
		return ret;
	}

	public LinkedList<Event> unleash() {
		return new LinkedList<Event>();
	}

	public void setUnleashTargets(LinkedList<Target> targets) {
		this.unleashTargets = targets;
	}

	public void resetUnleashTargets() {
		for (Target t : this.unleashTargets) {
			t.reset();
		}
	}

	public Target getNextNeededUnleashTarget() {
		for (Target t : this.unleashTargets) {
			if (!t.ready()) {
				return t;
			}
		}
		return null;
	}

	public String unleashTargetsToString() {
		String ret = "utargets " + this.unleashTargets.size() + " ";
		for (Target t : this.unleashTargets) {
			ret += t.toString() + " ";
		}
		return ret;
	}

	public LinkedList<Event> onAttack(Minion target) {
		return new LinkedList<Event>();
	}

	public LinkedList<Event> onAttacked(Minion target) {
		return new LinkedList<Event>();
	}

	public LinkedList<Event> clash(Minion target) {
		return new LinkedList<Event>();
	}

	public LinkedList<Event> onDamaged(int damage) {
		return new LinkedList<Event>();
	}

	public LinkedList<Event> onTurnStart() {
		return new LinkedList<Event>();
	}

	public LinkedList<Event> onTurnEnd() {
		return new LinkedList<Event>();
	}

	public LinkedList<Event> lastWords() {
		return new LinkedList<Event>();
	}

	public String toString() {
		return this.id + " " + this.owner.toReference() + this.description + Game.STRING_END + this.set.toString()
				+ this.change.toString();
	}

	public static Effect fromString(Board b, StringTokenizer st) {
		int id = Integer.parseInt(st.nextToken());
		if (id == 0) {
			Card owner = Card.fromReference(b, st);
			String description = st.nextToken("" + Game.STRING_END);
			Effect ef = new Effect(owner, 0, description);
			ef.set = EffectStats.fromString(st);
			ef.change = EffectStats.fromString(st);
			return ef;
		} else {
			Class c = EffectIDLinker.getClass(id);
			Effect ef = null;
			try {
				ef = (Effect) c.getMethod("fromString", null).invoke(st);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // AAAAAAAAAa
			return ef;
		}
	}

	public String toReference() {
		return this.owner.toReference() + this.basic + " " + this.pos;
	}

	public static Effect fromReference(Board b, StringTokenizer st) {
		Card c = Card.fromReference(b, st);
		boolean basic = Boolean.parseBoolean(st.nextToken());
		int pos = Integer.parseInt(st.nextToken());
		if (basic) {
			return c.getBasicEffects().get(pos);
		} else {
			return c.getAdditionalEffects().get(pos);
		}
	}

}