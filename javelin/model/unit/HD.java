package javelin.model.unit;

import java.io.Serializable;
import java.util.TreeMap;

import javelin.model.Cloneable;
import tyrant.mikera.engine.RPG;

/**
 * Represent the total hit dice of a {@link Monster}.
 * 
 * @author alex
 */
public class HD implements Serializable, Cloneable {
	private static final String SEPARATOR = " + ";
	/**
	 * List of dice number by dice type (1d8 would hitdice[8]=1).
	 */
	TreeMap<Integer, Float> hitdice = new TreeMap<Integer, Float>();
	public int extrahp = 0;

	public void add(float ndice, int hitdie, int extra) {
		Float hd = hitdice.get(hitdie);
		if (hd == null) {
			hd = 0.0f;
		}
		hd += ndice;
		hitdice.put(hitdie, hd);
		extrahp += extra;
	}

	@Override
	public String toString() {
		String output = "";
		for (int hd : hitdice.keySet()) {
			output += translate(hitdice.get(hd)) + "d" + hd + SEPARATOR;
		}
		output = output.substring(0, output.length() - SEPARATOR.length());
		output += extrahp >= 0 ? " + " : " - ";
		return output + Math.abs(extrahp);
	}

	private String translate(Float hd) {
		return hd >= 1 ? Long.toString(Math.round(hd))
				: "1/" + Math.round(1 / (1 - hd));
	}

	public int roll(Monster m) {
		int hp = extrahp;
		for (int hd : hitdice.keySet()) {
			Float dice = hitdice.get(hd);
			if (dice < 1) {
				hp += Math.max(1, Math.round(RPG.r(1, hd) * dice));
				continue;
			}
			for (int i = 0; i < dice; i++) {
				int roll = RPG.r(1, hd);
				if (m.constitution > 0) {
					roll += Monster.getbonus(m.constitution);
				}
				hp += Math.max(1, roll);
			}
		}
		return Math.max(1, hp);

	}

	public int maximize() {
		int hp = extrahp;
		for (Integer hd : hitdice.keySet()) {
			hp += hd * hitdice.get(hd);
		}
		return hp;
	}

	@Override
	public HD clone() {
		try {
			final HD clone = (HD) super.clone();
			clone.hitdice = (TreeMap<Integer, Float>) hitdice.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public int count() {
		double dice = 0.0;
		for (double ndice : hitdice.values()) {
			dice += ndice;
		}
		return new Long(Math.round(dice)).intValue();
	}

	public int average() {
		float average = extrahp;
		for (Integer hd : hitdice.keySet()) {
			average += hitdice.get(hd) * (1 + hd) / 2f;
		}
		return Math.round(average);
	}
}
