package javelin.model.item.artifact;

import java.util.List;

import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;

/**
 * Provides a {@link Combatant} with a certain number of daily {@link #uses} of
 * a {@link Spell}. It requires a rest period before conceding actual uses and
 * will remove a daily number of uses when unequipped (in order to prevend
 * players from removing and putting the item again in order to cheat their way
 * into "free" uses of the spell).
 *
 * @author alex
 */
public class CasterRing extends Artifact{
	/**
	 * Available number of {@link #uses} tiers in the game. Higher than 5 is
	 * discouraged since 5 equals to "at will" in the Challenging Challenge
	 * Ratings document.
	 *
	 * TODO this is currently artifically limited but could be converted to [1,5]
	 * at most at any point in time. The reason for this is we don't want 95% of
	 * the {@link Artifact}s in the game to be Caster Rings. Right now, 60% of the
	 * Artifacts are CasterRings, which is much more acceptable, with around 50%
	 * or less being the current goal.
	 */
	public static List<Integer> POWERLEVELS=List.of(2);

	int uses;
	Spell spell;

	/** Constructor. */
	public CasterRing(Spell s,int uses){
		super(getname(s,uses),s.casterlevel*s.level*400*uses,Slot.FINGER);
		if(Javelin.DEBUG) assert s.isring;
		spell=s;
		this.uses=uses;
		waste=false; // wasted as Spell
	}

	static String getname(Spell s,int uses){
		String prefix;
		if(uses==1)
			prefix="Minor ring";
		else if(uses==2)
			prefix="Caster ring";
		else if(uses==3)
			prefix="Major ring";
		else if(uses==4)
			prefix="Greater ring";
		else if(uses==5)
			prefix="Epic ring";
		else
			throw new RuntimeException("Invalid number of uses #casterring");
		return prefix+" ["+s.name.toLowerCase()+"]";
	}

	@Override
	protected void apply(Combatant c){
		Spell s=c.spells.get(spell.getClass());
		if(s==null){
			s=spell.clone();
			s.perday=uses;
			s.used=uses;
			c.spells.add(s);
		}else{
			s.perday+=uses;
			s.used+=uses;
		}
	}

	@Override
	protected void negate(Combatant c){
		Spell s=c.spells.get(spell.getClass());
		s.perday-=uses;
		if(s.perday<=0)
			c.spells.remove(s);
		else if(s.used>s.perday) s.used=s.perday;
	}
}
