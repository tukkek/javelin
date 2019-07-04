package javelin.model.item.focus;

import java.security.InvalidParameterException;
import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.action.CastSpell;
import javelin.controller.exception.RepeatTurn;
import javelin.model.item.artifact.Artifact;
import javelin.model.item.artifact.Slot;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.Touch;

/**
 * A much larger, heavier {@link Wand} carried on one's hand that shoots
 * non-{@link Touch} {@link Spell}s that can target a single enemy (even if it
 * causes an area of effect, such as Fireball).
 *
 * As much as benefitial spells could be Rods too, its unlimited use would just
 * incentivize endless micro-management on part of the player (such as optimally
 * buffing allies before entering a dungeon, etc).
 *
 * @see Spell#isrod
 * @author alex
 */
public class Rod extends Artifact{
	Spell spell;

	/**
	 * Subclass constructor.
	 *
	 * @param register
	 */
	protected Rod(String name,int price,Spell s,boolean register){
		super(Wand.name(name,s),price,Slot.HAND,register);
		if(Javelin.DEBUG&&!s.iswand) throw new InvalidParameterException();
		spell=s.clone();
		spell.provokeaoo=false;
		provokesaoo=false;
		apcost=s.apcost;
		usedinbattle=true;
	}

	/** Constructor. */
	public Rod(Spell s){
		this("Rod",s.level*s.casterlevel*2000+s.components*100,s,true);
	}

	@Override
	public boolean equip(Combatant c){
		if(c.equipped.contains(this)) return super.equip(c);
		for(var e:new ArrayList<>(c.equipped))
			if(e instanceof Rod) c.unequip(e);
		return super.equip(c);
	}

	@Override
	protected void apply(Combatant c){
		// does nothing by itself
	}

	@Override
	protected void negate(Combatant c){
		// does nothing by itself
	}

	@Override
	public boolean use(Combatant user){
		if(!user.equipped.contains(this)){
			Javelin.message("This item need to be equipped before being used!",false);
			throw new RepeatTurn();
		}
		return CastSpell.SINGLETON.cast(spell,user);
	}
}
