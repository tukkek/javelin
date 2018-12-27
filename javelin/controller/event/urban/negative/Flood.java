package javelin.controller.event.urban.negative;

import javelin.controller.Weather;
import javelin.controller.event.urban.UrbanEvent;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.condition.Sickened;
import javelin.model.world.World;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Trait;
import javelin.old.RPG;

/**
 * A flood damages a {@link Town} near to water. Natural towns are immune to it.
 *
 * @author alex
 * @see Trait#NATURAL
 */
public class Flood extends UrbanEvent{
	boolean sickened=false;

	/** Reflection constructor. */
	public Flood(Town t){
		super(t,null,Rank.HAMLET);
	}

	@Override
	public boolean validate(Squad s,int squadel){
		return Weather.current==Weather.STORM
				&&Terrain.search(town.getlocation(),Terrain.WATER,
						town.getdistrict().getradius()+1,World.seed)>0
				&&!town.traits.contains(Trait.NATURAL)&&super.validate(s,squadel);
	}

	@Override
	public void happen(Squad s){
		var message=town
				+" is caught in a massive flood, causing major fatalities!\n";
		if(s!=null){
			var damaged=false;
			var toxic=town.getrank().rank>=Rank.TOWN.rank;
			for(var squad:town.getdistrict().getsquads())
				for(var member:squad)
					if(affect(member,toxic)) damaged=true;
			if(damaged){
				message+="One or more squads are caught in it!";
				if(sickened)
					message+=" One or more members have also been exposed to disease in the water!";
			}
		}
		if(town.population>1) town.population-=1;
		notify(message);
	}

	boolean affect(Combatant member,boolean toxic){
		if(member.source.swim()!=0) return false;
		member.damage(member.hp/2,0);
		if(!toxic) return true;
		int fortitude=member.source.getfortitude();
		if(fortitude==Integer.MAX_VALUE||RPG.r(1,20)+fortitude>=town.population)
			return true;
		member.addcondition(new Sickened(Float.MAX_VALUE,member,24*7));
		sickened=true;
		return true;
	}
}
