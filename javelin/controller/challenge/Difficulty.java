package javelin.controller.challenge;

import java.util.ArrayList;
import java.util.List;

import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.old.RPG;

/**
 * Describes an Encounter Level difference. For example: an encounter is
 * irrelevant if it's of {@value #IRRELEVANT} of lower.
 */
public class Difficulty{
	public static final int IRRELEVANT=-13;
	public static final int VERYEASY=-9;
	public static final int EASY=-5;
	public static final int MODERATE=-4;
	public static final int DIFFICULT=0;
	public static final int DEADLY=+4;

	static final ArrayList<Integer> DISTRIBUTION=new ArrayList<>(17);

	static{
		DISTRIBUTION.add(-6);
		DISTRIBUTION.add(-5);
		for(int i=0;i<10;i++)
			DISTRIBUTION.add(-4);
		DISTRIBUTION.add(-3);
		DISTRIBUTION.add(-2);
		DISTRIBUTION.add(-1);
		DISTRIBUTION.add(+0);
		DISTRIBUTION.add(+1);
	}

	public static String describe(int delta){
		if(delta<=IRRELEVANT) return "irrelevant";
		if(delta<=VERYEASY) return "very easy";
		if(delta<=EASY) return "easy";
		if(delta==MODERATE) return "moderate";
		if(delta<=DIFFICULT) return "difficult";
		if(delta<=DEADLY) return "deadly";
		return "impossible";
	}

	/**
	 * 2 chances of an easy encounter, 10 chances of a moderate encounter, 4
	 * chances of a difficult encounter and 1 chance of an overwhelming encounter
	 *
	 * @return The EL modifier (-6 to +1).
	 */
	public static int get(){
		return RPG.pick(DISTRIBUTION);
	}

	/**
	 * @return Same as {@link describe} except compares given {@link Combatant}s
	 *         to {@link Squad#active}.
	 */
	public static String describe(List<Combatant> opponents){
		return describe(calculate(Squad.active.members,opponents));
	}

	/**
	 * @return A {@link Difficulty}.
	 */
	public static int calculate(List<Combatant> squad,List<Combatant> opponents){
		return ChallengeCalculator.calculateel(opponents)
				-ChallengeCalculator.calculateel(squad);
	}
}