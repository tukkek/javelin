package javelin.model.unit;

import java.io.Serializable;
import java.util.Arrays;

import javelin.old.RPG;

/**
 * Lawful-chaotic and good-evil alignment data, as per the SRD.
 *
 * @author alex
 * @see Monster
 */
public class Alignment implements Serializable{
	/** Lawful, neutral or chaotic. */
	public enum Ethics{
		/** Alignment of order. */
		LAWFUL,
		/** In-between {@link #LAWFUL} and {@link #CHAOTIC}. */
		NEUTRAL,
		/** Random, instinctual, baser. */
		CHAOTIC
	}

	/** Good, neutral or evil. */
	public enum Morality{
		/** Collective-oriented, pious. */
		GOOD,
		/** Neither {@link #GOOD} nor {@link #EVIL}. */
		NEUTRAL,
		/** Antisocial, harmful, cruel. */
		EVIL
	}

	/** @see Ethics */
	public Ethics ethics;
	/** @see Morality */
	public Morality morals;

	/** Creates a custom instance. */
	public Alignment(Ethics ethicsp,Morality moralsp){
		super();
		ethics=ethicsp;
		morals=moralsp;
	}

	/** Creates a true neutral instance. */
	public Alignment(){
		this(Ethics.NEUTRAL,Morality.NEUTRAL);
	}

	@Override
	public String toString(){
		String alignment;
		if(ethics==Ethics.NEUTRAL){
			if(morals==Morality.NEUTRAL) return "True neutral";
			alignment="Neutral";
		}else if(ethics==Ethics.LAWFUL)
			alignment="Lawful";
		else
			alignment="Chaotic";
		if(morals==Morality.NEUTRAL)
			return alignment+=" neutral";
		else if(morals==Morality.GOOD)
			return alignment+" good";
		else
			return alignment+" evil";
	}

	/** @return <code>true</code> if {@link Ethics#LAWFUL}. */
	public boolean islawful(){
		return ethics.equals(Ethics.LAWFUL);
	}

	/** @return <code>true</code> if {@link Ethics#CHAOTIC}. */
	public boolean ischaotic(){
		return ethics.equals(Ethics.CHAOTIC);
	}

	/** @return <code>true</code> if {@link Morality#GOOD}. */
	public boolean isgood(){
		return morals.equals(Morality.GOOD);
	}

	/** @return <code>true</code> if {@link Morality#EVIL}. */
	public boolean isevil(){
		return morals.equals(Morality.EVIL);
	}

	/** @return A randomly assigned alignment. */
	public static Alignment random(){
		var ethics=RPG.pick(Arrays.asList(Ethics.values()));
		var morals=RPG.pick(Arrays.asList(Morality.values()));
		return new Alignment(ethics,morals);
	}
}
