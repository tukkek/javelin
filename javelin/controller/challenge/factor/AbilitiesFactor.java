/**
 * Alex Henry on 13/10/2010
 */
package javelin.controller.challenge.factor;

import javelin.controller.content.quality.resistance.MindImmunity;
import javelin.model.unit.Monster;

/**
 * Calculates ability score challenge rating, and unrated attributes (value
 * equal to zero).
 *
 * @see MindImmunity
 * @see CrFactor
 */
public class AbilitiesFactor extends CrFactor{
	/** General cost for 1 ability score above or below 10. */
	public static final float COST=.1f;

	@Override
	public float calculate(final Monster m){
		final int[] abilites=new int[]{m.strength,m.dexterity,
				m.constitution+m.poison,m.wisdom,m.intelligence,m.charisma};
		float sum=0;
		for(final int a:abilites)
			if(a!=0) sum+=a-10.5;
		sum=sum*COST;
		if(m.constitution<=0){
			sum+=1;// immune to fortitude saves
			sum-=.2;// destroyed at 0hp
			if(m.isalive()) // no hd bonus
				sum-=0.1*m.hd.count();
		}
		return sum;
	}
}