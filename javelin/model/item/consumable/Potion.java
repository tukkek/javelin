package javelin.model.item.consumable;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.ContentSummary;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.view.screen.BattleScreen;

/**
 * Represent a consumable potion to be used in-battle. Any monster can use a
 * potion.
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

	/**
	 * @param s One-use spell effect of drinking this potion.
	 */
	public Potion(Spell s){
		super("Potion of "+s.name.toLowerCase(),s.level*s.casterlevel*50,true);
		if(Javelin.DEBUG) assert s.ispotion;
		usedinbattle=s.castinbattle;
		usedoutofbattle=s.castoutofbattle;
		spell=s;
	}

	@Override
	public boolean use(Combatant user){
		String text=spell.cast(user,user,false,null,null);
		Javelin.redraw();
		/* TODO should be less awkward once Context are implemented (2.0) */
		if(BattleScreen.active.getClass().equals(BattleScreen.class))
			BattleScreen.active.center(user.location[0],user.location[1]);
		Javelin.message(text,false);
		return true;
	}

	@Override
	public boolean usepeacefully(Combatant user){
		spell.castpeacefully(user,user,null);
		return true;
	}

	public static List<Potion> getpotions(){
		ArrayList<Potion> potions=new ArrayList<>();
		for(Item i:ITEMS)
			if(i instanceof Potion) potions.add((Potion)i);
		return potions;
	}
}
