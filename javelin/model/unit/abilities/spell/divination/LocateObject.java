package javelin.model.unit.abilities.spell.divination;

import java.util.List;

import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.walker.Walker;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.Chest;
import javelin.model.world.location.dungeon.feature.Feature;

/**
 * Allows player to find nearest treasure chest in a {@link Dungeon}.
 */
public class LocateObject extends Spell{
	/** Constructor. */
	public LocateObject(){
		super("Locate object",2,ChallengeCalculator.ratespelllikeability(2),
				Realm.MAGIC);
		castinbattle=false;
		castoutofbattle=true;
		isscroll=true;
	}

	@Override
	public boolean validate(Combatant caster,Combatant target){
		return Dungeon.active!=null;
	}

	@Override
	public String castpeacefully(Combatant caster,Combatant target,
			List<Combatant> squad){
		Feature closest=findtreasure();
		if(closest==null) return "No treasure left.";
		Dungeon.active.setvisible(closest.x,closest.y);
		return null;
	}

	/**
	 * @return Closest treasure chest.
	 */
	public static Chest findtreasure(){
		Chest closest=null;
		Point hero=JavelinApp.context.getherolocation();
		for(Feature f:Dungeon.active.features)
			if(f instanceof Chest){
				Chest t=(Chest)f;
				if(closest==null)
					closest=t;
				else if(Walker.distance(hero.x,hero.y,t.x,t.y)<Walker.distance(hero.x,
						hero.y,closest.x,closest.y)){}

			}
		return closest;
	}
}
