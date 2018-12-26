package javelin.controller.event.urban.negative;

import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.event.urban.UrbanEvent;
import javelin.model.unit.Squad;
import javelin.model.world.location.ConstructionSite;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Build;
import javelin.model.world.location.town.labor.Trait;
import javelin.old.RPG;

/**
 * A fire destroys a {@link District} {@link Location}. {@link Trait#NATURAL}
 * {@link Town}s are savvy enough to prevent fires.
 *
 * @author alex
 */
public class Fire extends UrbanEvent{
	/**
	 * Repairs a damaged {@link District} {@link Location}.
	 *
	 * @author alex
	 */
	static class Repair extends Build{
		Location damaged;

		Repair(String name,int labor,Location damaged){
			super(name,labor,Rank.HAMLET,null);
			this.damaged=damaged;
		}

		@Override
		public Location getgoal(){
			return damaged;
		}

		@Override
		protected Point getsitelocation(){
			return damaged.getlocation();
		}
	}

	List<Location> targets;

	/** Reflection constructor. */
	public Fire(Town t){
		super(t,null,Rank.HAMLET);
		targets=t.getdistrict().getlocations().stream()
				.filter(l->!(l instanceof Town)&&!(l instanceof ConstructionSite))
				.collect(Collectors.toList());
	}

	@Override
	public boolean validate(Squad s,int squadel){
		return !targets.isEmpty()&&Weather.current==Weather.CLEAR
				&&!town.traits.contains(Trait.NATURAL)&&super.validate(s,squadel);
	}

	@Override
	public void happen(Squad s){
		var target=RPG.pick(targets);
		String name=target.toString().toLowerCase();
		if(notify)
			Javelin.message("The "+name+" in "+town+" is ruined in a fire!",true);
		target.remove();
		var labor=town.population+RPG.randomize(town.population);
		var r=new Repair("Repair: "+name,labor,target);
		r.generate(town).start();
	}
}
