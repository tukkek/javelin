package javelin.model.item.potion;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.ContentSummary;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.view.screen.BattleScreen;

/**
 * Represents a consumable potion to be used in-battle. Any monster can use a
 * potion. Any self-affecting, benefitial {@link Spell} can be a potion.
 *
 * TODO if it's ever necessary to increase the number of potions compared to
 * other items, Flasks can be added, which refresh their content every 24 hours.
 * Same can be repeated with Big flasks (2x/day), ad aeternum. See
 * {@link ContentSummary}.
 *
 * @author alex
 */
public class Potion extends Item{
	javelin.model.unit.abilities.spell.Spell spell;

	/** Constructor. */
	public Potion(Spell s){
		this("Potion",s,s.level*s.casterlevel*50,true);
	}

	/** Subclass constructor. */
	protected Potion(String name,Spell s,int price,boolean register){
		super(name+" of "+s.name.toLowerCase(),price,register);
		if(Javelin.DEBUG&&!s.ispotion) throw new InvalidParameterException();
		usedinbattle=s.castinbattle;
		usedoutofbattle=s.castoutofbattle;
		spell=s;
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

	/** @return All potion types in the game. */
	public static List<Potion> getpotions(){
		return ITEMS.stream().filter(i->i instanceof Potion).map(i->(Potion)i)
				.collect(Collectors.toList());
	}
}
