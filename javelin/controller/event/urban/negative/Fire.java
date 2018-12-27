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
 * A fire destroys one or more {@link District} {@link Location}s, requiring
 * them to be repaired. {@link Trait#NATURAL} {@link Town}s are savvy enough to
 * prevent fires.
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
				.filter(l->!(l instanceof Town)).collect(Collectors.toList());
	}

	@Override
	public boolean validate(Squad s,int squadel){
		return !targets.isEmpty()&&Weather.current==Weather.CLEAR
				&&!town.traits.contains(Trait.NATURAL)&&super.validate(s,squadel);
	}

	@Override
	public void happen(Squad s){
		var rank=town.getrank().rank;
		var targets=RPG.shuffle(this.targets).subList(0,
				Math.min(this.targets.size(),rank+RPG.randomize(rank)));
		var damage=Math.max(1,town.population/targets.size());
		for(var t:targets){
			t.remove();
			if(t instanceof ConstructionSite) continue;
			var name="Repair: "+t.toString().toLowerCase();
			var labor=damage+RPG.randomize(damage);
			var r=new Repair(name,labor,t);
			r.generate(town).start();
		}
		if(notify){
			var affected=targets.stream().map(t->t.toString())
					.collect(Collectors.joining(", "));
			Javelin.message("A fire rages across "+town
					+"! The following locations were damaged:\n"+affected+".",true);
		}
	}
}
