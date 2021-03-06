package server.card.unleashpower;

import java.util.LinkedList;

import org.newdawn.slick.geom.Vector2f;

import client.tooltip.*;
import server.Board;
import server.card.*;
import server.card.effect.*;
import server.event.*;

public class UnleashEmbraceNature extends UnleashPower {
	public static final int ID = -9;
	public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
	public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower("Embrace Nature",
			"<b> Unleash </b> an allied minion. If it has already attacked this turn, return it to your hand and subtract 1 from its cost.",
			"res/unleashpower/embracenature.png", CRAFT, 2, ID, Tooltip.UNLEASH);

	public UnleashEmbraceNature(Board b) {
		super(b, TOOLTIP, new Vector2f(380, 280), 0.3);
	}

	@Override
	public LinkedList<Event> unleash(Minion m) {
		LinkedList<Event> list = new LinkedList<Event>();
		list.add(new EventUnleash(this, m));
		if (m.attacksThisTurn > 0) {
			list.add(new EventPutCard(this.p, m, CardStatus.HAND, this.p.team, -1));
			EffectStatChange esc = new EffectStatChange("Cost reduced by 1 from <b> Embrace Nature. </b>");
			esc.change.setStat(EffectStats.COST, -1);
			list.add(new EventAddEffect(m, esc));
		}
		return list;
	}
}
