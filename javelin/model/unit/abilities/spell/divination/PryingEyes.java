package javelin.model.unit.abilities.spell.divination;

import java.util.List;

import javelin.controller.DungeonMapCrawler;
import javelin.controller.Point;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.world.location.Outpost;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.door.Door;

/**
 * Reveals {@link DungeonFloor} map or nearby area on the WorldScreen.
 *
 * @author alex
 */
public class PryingEyes extends Spell{
	/** Constructor. */
	public PryingEyes(){
		super("Prying eyes",5,ChallengeCalculator.ratespell(5));
		castoutofbattle=true;
	}

	@Override
	public String castpeacefully(Combatant caster,Combatant target,
			List<Combatant> squad){
		var dungeon=Dungeon.active;
		if(dungeon==null)
			Outpost.discover(Squad.active.x,Squad.active.y,Outpost.VISIONRANGE);
		else{
			var crawler=new DungeonMapCrawler(dungeon.squadlocation,9000,dungeon){
				@Override
				protected boolean validate(Feature f){
					return !(f instanceof Door);
				}
			};
			for(Point p:crawler.crawl())
				dungeon.setvisible(p.x,p.y);
		}
		return null;
	}
}
