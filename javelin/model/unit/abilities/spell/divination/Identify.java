package javelin.model.unit.abilities.spell.divination;

import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.exception.RepeatTurn;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;

/**
 * @see Item#identified
 * @author alex
 */
public class Identify extends Spell{
	/** Constructor. */
	public Identify(){
		super("Identify",1,ChallengeCalculator.ratespell(1));
		castinbattle=false;
		castonallies=false;
		castoutofbattle=true;
		components=100;
		isritual=true;
		isscroll=true;
	}

	@Override
	public String castpeacefully(Combatant caster,Combatant target){
		var targets=Squad.active.equipment.getall().stream()
				.filter(i->!i.identified).collect(Collectors.toList());
		if(targets.isEmpty()) return "You have no unidentified items...";
		var choice=Javelin.choose("Identify which item?",targets,true,false);
		if(choice<0) throw new RepeatTurn();
		var i=targets.get(choice);
		i.identified=true;
		return "You have identified the "+i.toString().toLowerCase()+"!";
	}
}
