/**
 * Alex Henry on 13/10/2010
 */
package javelin.controller.challenge.factor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.AttackSequence;

/**
 * I do have a question about CRs though if you can remember the answer at all.
 * The full attack factor uses average damage to calculate part of the monster's
 * CR. But what about monsters who have multiple attack sequences? Let's say my
 * Huge Tentacled Nerd monster has 2 full-attack sequences:
 *
 * 1. Keyboard +3 (2d4-1), tablet +2 (1d8) and assorted wires +0 (1d4); and
 *
 * 2. Tentacles +9/+3 (1d6+2 and poison)
 *
 * The Challenging CRs appendix isn't clear what I should do here. Do I factor
 * in just the best full-attack sequence? Do I count both sequences for CR
 * purposes?
 *
 * UPPER KRUST'S ANSWER: In this situation you need to factor both.
 *
 * See {@link CrFactor}.
 *
 * @author alex
 */
public class FullAttackFactor extends CrFactor{
	@Override
	public float calculate(final Monster monster){
		final List<Attack> attks=new ArrayList<>();
		final ArrayList<List<AttackSequence>> attacktypes=new ArrayList<>();
		attacktypes.add(monster.melee);
		attacktypes.add(monster.ranged);
		for(final List<AttackSequence> attacktype:attacktypes)
			for(final AttackSequence a:attacktype)
				if(!a.rapid&&!a.powerful) attks.addAll(a);
		if(attks.size()==0) return -1;
		final HashSet<String> effects=new HashSet<>();
		float sum=0;
		Attack last=null;
		for(final Attack a:attks){
			sum+=a.getAverageDamageNoBonus()
					*(last!=null&&last.name.equals(a.name)?.05f:.1f);
			last=a;
			Spell effect=a.geteffect();
			if(effect!=null&&effects.add(effect.name)) sum+=.1*effect.level;
		}
		return sum;
	}
}