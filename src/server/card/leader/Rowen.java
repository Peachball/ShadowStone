package server.card.leader;

import server.Board;
import server.card.ClassCraft;
import server.card.Leader;

public class Rowen extends Leader {
	public static final int ID = -1;

	public Rowen(Board b, int team) {
		super(b, team, ClassCraft.DRAGONDRUID, ID, "Rowen");
	}
}
