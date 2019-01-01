package javelin.controller.event.urban.positive;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.event.urban.UrbanEvent;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Labor;
import javelin.old.RPG;

/**
 * A {@link Town} asks a {@link Squad} for help with their {@link Labor}s.
 *
 * @author alex
 */
public class CollectiveEffort extends UrbanEvent{
	ArrayList<Labor> projects=town.getgovernor().getprojects();

	/** Reflection constructor. */
	public CollectiveEffort(Town t){
		super(t,null,Rank.HAMLET);
	}

	@Override
	public boolean validate(Squad s,int squadel){
		return s!=null&&!projects.isEmpty()&&town.strike==0
				&&town.describehappiness()!=Town.REVOLTING&&super.validate(s,squadel);
	}

	@Override
	public void happen(Squad s){
		var labors=projects.stream().map(p->p.toString().toLowerCase())
				.collect(Collectors.joining(", "));
		var input=Javelin.prompt("The citizens of "+town
				+" ask you for help witht their labors. Do you want to spend the day helping?\n"
				+"They are working on "+labors+".\n"
				+"Press h to help or i to ignore their request...",Set.of('h','i'));
		if(input!='h') return;
		Squad.active.hourselapsed+=24;
		var total=0;
		var r=town.getrank().rank;
		for(var c:s){
			var labor=1;
			var con=Monster.getbonus(c.source.constitution);
			while(RPG.r(1,20)+con>=town.population&&labor<r)
				labor+=1;
			total+=labor;
		}
		town.getgovernor().work(total*Town.DAILYLABOR,town.getdistrict());
	}
}
