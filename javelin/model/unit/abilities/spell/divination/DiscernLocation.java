package javelin.model.unit.abilities.spell.divination;

import java.util.ArrayList;
import java.util.Collections;

import javelin.controller.Point;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.walker.Walker;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.town.Town;
import javelin.view.screen.WorldScreen;

/**
 * Shows closest undiscovered town in the {@link World}.
 *
 * @author alex
 */
public class DiscernLocation extends Spell{
	/** Constructor. */
	public DiscernLocation(){
		super("Discern location",8,ChallengeCalculator.ratespell(8));
		castoutofbattle=true;
		isritual=true;
	}

	@Override
	public boolean validate(Combatant caster,Combatant target){
		return Dungeon.active==null;
	}

	@Override
	public String castpeacefully(Combatant caster,Combatant target){
		ArrayList<Actor> towns=World.getall(Town.class);
		Town closest=null;
		for(Actor p:towns){
			if(WorldScreen.see(new Point(p.x,p.y))) continue;
			if(closest==null
					||Walker.distance(Squad.active.x,Squad.active.y,p.x,p.y)<Walker
							.distance(Squad.active.x,Squad.active.y,closest.x,closest.y))
				closest=(Town)p;
		}
		if(closest==null) return "All towns have been discovered.";
		walktotown(closest);
		return null;
	}

	void walktotown(Town closest){
		int x=Squad.active.x;
		int y=Squad.active.y;
		walk:while(x!=closest.x||y!=closest.y){
			ArrayList<Point> points=new ArrayList<>(4);
			points.add(new Point(x+1,y));
			points.add(new Point(x-1,y));
			points.add(new Point(x,y+1));
			points.add(new Point(x,y-1));
			Collections.shuffle(points);
			for(Point p:points)
				if(Walker.distance(p.x,p.y,closest.x,closest.y)<Walker.distance(x,y,
						closest.x,closest.y)){
					x=p.x;
					y=p.y;
					WorldScreen.discover(x,y);
					continue walk;
				}
			throw new RuntimeException(
					"Should have found path discovery step #shrine");
		}
	}
}
