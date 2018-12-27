package javelin.model.unit;

import java.io.Serializable;
import java.util.Arrays;
import java.util.TreeSet;

import javelin.Debug;
import javelin.Javelin;
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
		CHAOTIC;

		@Override
		public String toString(){
			return tostring(this);
		}
	}

	/** Good, neutral or evil. */
	public enum Morals{
		/** Collective-oriented, pious. */
		GOOD,
		/** Neither {@link #GOOD} nor {@link #EVIL}. */
		NEUTRAL,
		/** Antisocial, harmful, cruel. */
		EVIL;

		@Override
		public String toString(){
			return tostring(this);
		}
	}

	/** @see Ethics */
	public Ethics ethics;
	/** @see Morals */
	public Morals morals;

	/** Creates a custom instance. */
	public Alignment(Ethics ethicsp,Morals moralsp){
		super();
		ethics=ethicsp;
		morals=moralsp;
	}

	/** Creates a true neutral instance. */
	public Alignment(){
		this(Ethics.NEUTRAL,Morals.NEUTRAL);
	}

	@Override
	public String toString(){
		String alignment;
		if(ethics==Ethics.NEUTRAL){
			if(morals==Morals.NEUTRAL) return "True neutral";
			alignment="Neutral";
		}else if(ethics==Ethics.LAWFUL)
			alignment="Lawful";
		else
			alignment="Chaotic";
		if(morals==Morals.NEUTRAL)
			return alignment+=" neutral";
		else if(morals==Morals.GOOD)
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

	/** @return <code>true</code> if {@link Morals#GOOD}. */
	public boolean isgood(){
		return morals.equals(Morals.GOOD);
	}

	/** @return <code>true</code> if {@link Morals#EVIL}. */
	public boolean isevil(){
		return morals.equals(Morals.EVIL);
	}

	/** @return A randomly assigned alignment. */
	public static Alignment random(){
		var ethics=RPG.pick(Arrays.asList(Ethics.values()));
		var morals=RPG.pick(Arrays.asList(Morals.values()));
		return new Alignment(ethics,morals);
	}

	static String tostring(Enum<?> e){
		return Javelin.capitalize(e.name());
	}

	/**
	 * @return A positive number if both alignments are similar, a negative number
	 *         if they are incompatible or zero if neutral to each other.
	 */
	int align(Alignment a){
		var aligned=0;
		if(ethics.equals(a.ethics))
			aligned+=1;
		else if(islawful()&&a.ischaotic())
			aligned-=1;
		else if(ischaotic()&&a.islawful()) aligned-=1;
		if(morals.equals(a.morals))
			aligned+=1;
		else if(isgood()&&a.isevil())
			aligned-=1;
		else if(isevil()&&a.isgood()) aligned-=1;
		return aligned;
	}

	/** @return <code>true</code> if both alignments are mostly compatible. */
	public boolean iscompatible(Alignment a){
		return align(a)>0;
	}

	/** @return <code>true</code> if both alignments are mostly incompatible. */
	public boolean isincompatible(Alignment a){
		return align(a)<0;
	}

	/**
	 * Tests compatiblity methods.
	 *
	 * @see #iscompatible(Alignment)
	 * @see #isincompatible(Alignment)
	 * @see Debug
	 */
	static public String testcompatibility(){
		var result=new TreeSet<String>();
		for(int i=0;i<1000;i++){
			var a=random();
			var b=random();
			String s=a+" vs "+b+":";
			if(a.iscompatible(b))
				s+=" compatible";
			else if(a.isincompatible(b))
				s+=" incompatible";
			else
				s+=" neutral";
			result.add(s);
		}
		return String.join("\n",result);
	}
}
