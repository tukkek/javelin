package javelin.model.item.gear;

import java.security.InvalidParameterException;
import java.util.List;

import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.model.unit.Slot;
import javelin.model.unit.abilities.spell.Spell;

/**
 * Provides a {@link Combatant} with a certain number of daily {@link #uses} of
 * a {@link Spell}.
 *
 * It requires a rest period before conceding actual uses and will remove a
 * daily number of uses when unequipped (in order to prevend players from
 * removing and putting the item again in order to cheat their way into "free"
 * uses of the spell). TODO this can re resolved with {@link #refresh(int)}
 *
 * {@link CasterRing}s are catch-all use-activated item type for
 * non-{@link #isrod} {@link Spell}s.
 *
 * @author alex
 */
public class CasterRing extends Gear{
	/**
	 * Available number of {@link #uses} tiers in the game. Higher than 5 is
	 * discouraged since 5 equals to "at will" in the Challenging Challenge
	 * Ratings document.
	 *
	 * TODO this is currently artifically limited but could be converted to [1,5]
	 * at most at any point in time. The reason for this is we don't want 95% of
	 * the {@link Gear}s in the game to be Caster Rings. Right now, 60% of the
	 * Artifacts are CasterRings, which is much more acceptable, with around 50%
	 * or less being the current goal.
	 */
	public static List<Integer> VARIATIONS=List.of(4);

	int uses;
	Spell spell;

	/** Constructor. */
	public CasterRing(Spell s,int uses){
		super(name(s,uses),s.casterlevel*s.level*400*uses,Slot.RING);
		if(Javelin.DEBUG&&!s.isring) throw new InvalidParameterException();
		spell=s;
		this.uses=uses;
		waste=false; // wasted as Spell
	}

	static String name(Spell s,int uses){
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
		if(VARIATIONS.size()==1) prefix="Caster ring";
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
