package server.playeraction;

import java.lang.reflect.InvocationTargetException;
import java.util.StringTokenizer;

import server.*;

public abstract class PlayerAction {
	int id = 0; // literally just copying off of event

	public PlayerAction(int id) {
		this.id = id;
	}

	public boolean perform(Board b) {
		return true;
	}

	public String toString() {
		return this.id + "\n";
	}

	public static PlayerAction createFromString(Board b, StringTokenizer st) {
		int id = Integer.parseInt(st.nextToken());
		Class c = ActionIDLinker.getClass(id);
		PlayerAction e = null;
		try {
			e = (PlayerAction) c.getMethod("fromString", Board.class, StringTokenizer.class).invoke(null, b, st);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return e;
	}
}
