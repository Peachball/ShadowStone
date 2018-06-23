package server.card.effect;

import java.util.StringTokenizer;

import server.card.*;

public class EffectStats { // this is literally just a struct
	public static final int NUM_STATS = 8;
	public static final int COST = 0, ATTACK = 1, MAGIC = 2, HEALTH = 3, ATTACKS_PER_TURN = 4, STORM = 5, RUSH = 6,
			WARD = 7;
	public int[] stats = new int[NUM_STATS];
	public boolean[] use = new boolean[NUM_STATS];

	public EffectStats() {

	}

	public void setStat(int index, int stat) {
		this.stats[index] = stat;
		this.use[index] = true;
	}

	public void changeStat(int index, int stat) {
		this.stats[index] += stat;
		this.use[index] = true;
	}

	public void resetStat(int index) {
		this.stats[index] = 0;
		this.use[index] = false;
	}

	public void applyStats(Stats stats) {
		this.setStat(ATTACK, stats.a);
		this.setStat(MAGIC, stats.m);
		this.setStat(HEALTH, stats.h);
	}

	public String toString() {
		String s = "";
		for (int i = 0; i < NUM_STATS; i++) {
			s += use[i] + " " + stats[i] + " ";
		}
		return s;
	}

	public static EffectStats fromString(StringTokenizer st) {
		EffectStats ret = new EffectStats();
		for (int i = 0; i < NUM_STATS; i++) {
			boolean use = st.nextToken().equals("true");
			if (use) {
				ret.setStat(i, Integer.parseInt(st.nextToken()));
			}
		}
		return ret;
	}
}