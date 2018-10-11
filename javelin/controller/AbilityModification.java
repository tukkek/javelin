package javelin.controller;

import java.util.ArrayList;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.skill.Skill;

/**
 * Handles ability score modification for {@link Monster}s.
 *
 * TODO currently handler zero abilities (no changes) but does not handle scores
 * being sent to zero.
 *
 * TODO turning ability scores and {@link Skill} into enum-based structures
 * would make this easier, instead of spamming {@link Combatant} with 2 methods
 * (changescore and changemodifier) per per-ability.
 *
 * @author alex
 */
public class AbilityModification{
	/**
	 * Will contain zero or more +1 or -1 elements, one per change in ability
	 * modifier (not ability score). Useful for iterating over modification
	 * increments.
	 */
	public ArrayList<Integer> modifications=new ArrayList<>();
	/**
	 * Will be the final score value of the given ability, to be assigned.
	 */
	public int newscore;
	/**
	 * Alternative to using {@link #modifications}. Has the total different in the
	 * ability modifier (for example: +2 or -4).
	 */
	public int modifierchange;
	public int scorechange=0;

	/**
	 * @param currentscore Current ability score. If equal to zero,
	 *          {@link #modify(int)} will have no effect and {@link #newscore}
	 *          will return this exact value so that it can be assined without
	 *          changing anything.
	 */
	private AbilityModification(int currentscore){
		newscore=currentscore;
	}

	/**
	 * Generates proper content for {@link #newscore} and {@link #modifications}.
	 *
	 * @param change The amount ability score change to apply (for example +2 ou
	 *          -1).
	 */
	public void modify(int change){
		if(newscore==0||change==0) return;
		int direction=change>0?+1:-1;
		change=Math.abs(change);
		for(int i=0;i<change;i++){
			if(direction==-1&&newscore==1) break;
			newscore+=direction;
			scorechange+=direction;
			boolean even=newscore%2==0;
			if(direction==+1&&even||direction==-1&&!even){
				modifications.add(direction);
				modifierchange+=direction;
			}
		}
	}

	public static AbilityModification modify(int current,int scorechange){
		AbilityModification m=new AbilityModification(current);
		m.modify(scorechange);
		return m;
	}
}