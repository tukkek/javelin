package javelin.model.item.potion;

import java.security.InvalidParameterException;

import javelin.Javelin;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.Spell;
import javelin.view.screen.BattleScreen;

/**
 * Represents a consumable potion to be used in-battle. Any {@link Monster} can
 * use a potion. Any self-affecting, benefitial {@link Spell} can be a Potion.
 *
 * @see Flask
 * @author alex
 */
public class Potion extends Item{
	Spell spell;

	/** Subclass constructor. */
	protected Potion(String name,Spell s,double price,boolean register){
		super(name+" of "+s.name.toLowerCase(),price,register);
		if(Javelin.DEBUG&&!s.ispotion) throw new InvalidParameterException();
		usedinbattle=s.castinbattle;
		usedoutofbattle=s.castoutofbattle;
		spell=s;
	}

	/** Constructor. */
	public Potion(Spell s){
		this("Potion",s,s.level*s.casterlevel*50,true);
	}

	@Override
	public boolean use(Combatant user){
		var text=spell.cast(user,user,false,null,null);
		Javelin.redraw();
		BattleScreen.active.center();
		Javelin.message(text,false);
		return true;
	}

	@Override
	public boolean usepeacefully(Combatant user){
		spell.castpeacefully(user,user,null);
		return true;
	}
}
