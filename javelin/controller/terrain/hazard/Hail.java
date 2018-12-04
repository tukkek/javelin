package javelin.controller.terrain.hazard;

import javelin.Javelin;
import javelin.controller.terrain.Terrain;
import javelin.model.transport.Transport;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.Season;
import javelin.old.RPG;

/**
 * If cold, strong hail may damage the party.
 *
 * @author alex
 */
public class Hail extends Hazard{
	@Override
	public boolean validate(){
		if(Season.current==Season.SUMMER) return false;
		if(Season.current==Season.WINTER) return true;
		var s=Squad.active;
		if(Terrain.get(s.x,s.y).equals(Terrain.DESERT)
				||Transport.SHIP.equals(s.transport)
				||Transport.AIRSHIP.equals(s.transport))
			return false;
		String period=Javelin.getperiod();
		return period==Javelin.PERIODEVENING||period==Javelin.PERIODNIGHT;
	}

	@Override
	public void hazard(int hoursellapsed){
		var s=Squad.active;
		var survival=s.getbest(Skill.SURVIVAL).roll(Skill.SURVIVAL);
		survival+=Terrain.get(Squad.active.x,Squad.active.y).survivalbonus;
		var shelter=survival>=Skill.DifficultyClass.DCAVERAGE;
		var message="Very strong hail starts to fall.";
		if(shelter) message+="\nThankfully you are able to find shelter quickly.";
		for(Combatant c:s){
			if(c.source.dr>0) continue;
			var damage=RPG.rolldice(1,4);
			while(RPG.chancein(2))
				damage+=RPG.rolldice(1,4);
			if(shelter) damage=damage/2;
			c.damage(damage,0);
		}
		Javelin.message(message,true);
	}
}
