package javelin.controller.terrain.hazard;

import javelin.controller.Weather;
import javelin.model.unit.Combatant;
import javelin.model.unit.condition.Fatigued;
import javelin.model.world.Period;
import javelin.model.world.Season;
import javelin.old.RPG;

/**
 * Frostbite.
 *
 * @author alex
 */
public class Cold extends PartyHazard{
	@Override
	protected boolean save(int hoursellapsed,Combatant c){
		return c.hp==1||c.source.save(c.source.getfortitude(),15+hoursellapsed/2);
	}

	@Override
	protected String affect(Combatant c,int hoursellapsed){
		for(int i=0;i<hoursellapsed;i++)
			c.damage(RPG.r(1,6),0);
		c.addcondition(new Fatigued(null,8));
		return c+" is suffering from frostbite";
	}

	@Override
	public boolean validate(){
		int level=0;
		if(Season.current==Season.WINTER)
			level+=2;
		else if(Season.current==Season.SUMMER) level-=2;
		if(Weather.current!=Weather.CLEAR) level+=1;
		if(Period.NIGHT.is())
			level+=1;
		else if(Period.AFTERNOON.is()) level-=1;
		return level>=2;
	}

}
