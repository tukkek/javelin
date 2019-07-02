package javelin.model.unit.abilities.spell.conjuration.teleportation;

import java.util.List;

import javelin.Javelin;
import javelin.controller.action.world.UseItems;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.exception.RepeatTurn;
import javelin.model.item.Item;
import javelin.model.item.consumable.Scroll;
import javelin.model.transport.Transport;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.world.Actor;
import javelin.model.world.location.dungeon.Dungeon;

/**
 * Brings you back to last visited town. See the d20 SRD for more info. Assumes
 * caster level 20 to be able to transport as many creatures as willed.
 *
 * Destination for this spell is set when the spell is prepared, not when it's
 * cast, so it makes some sense that that a {@link Scroll} could also be
 * prepared before being cast. It's a bit of a stretch but doing anything else
 * would be very complicated considering the numberr of different ways an
 * {@link Item} can be generated in Javelin.
 *
 * http://paizo.com/pathfinderRPG/prd/coreRulebook/spells/wordOfRecall.html
 */
public class WordOfRecall extends Spell{
	/** Constructor. */
	public WordOfRecall(){
		super("Word of recall",6,ChallengeCalculator.ratespell(6,20));
		casterlevel=20;
		castoutofbattle=true;
		isritual=false;
	}

	@Override
	public boolean validate(Combatant caster,Combatant target){
		if(Squad.active.lasttown==null) return false;
		UseItems.skiperror=true;
		String message="Do you want to recall to "+Squad.active.lasttown
				+"?\nPress ENTER to confirm or any other key to abort...";
		return Javelin.prompt(message,true)=='\n';
	}

	@Override
	public String castpeacefully(Combatant caster,Combatant combatant,
			List<Combatant> squad){
		Actor town=Squad.active.lasttown;
		teleport(town.x,town.y);
		return null;
	}

	static public void teleport(int x,int y){
		Squad s=Squad.active;
		Transport t=s.transport;
		if(Dungeon.active!=null){
			Dungeon.active.leave();
			s.transport=null;
		}else if(t!=null) try{
			t.park();
		}catch(RepeatTurn e){
			s.transport=null;
		}
		s.x=x;
		s.y=y;
		s.displace();
		s.place();
	}
}
