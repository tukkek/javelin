package javelin.controller.content.terrain.hazard;

import javelin.controller.Weather;
import javelin.controller.content.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.Season;
import javelin.old.RPG;

/**
 * Can happen during any storm or in spring time if it's raining.
 *
 * TODO would be cool to have the party divide in 2 when a storm hits
 *
 * @author alex
 */
public class Flood extends Hazard{
	static final int DC=(15+20)/2;

	@Override
	public void hazard(int hoursellapsed){
		for(Combatant c:Squad.active.members)
			if(RPG.r(1,20)+Monster.getbonus(c.source.dexterity)<DC){
				GettingLost.getlost("Squad is taken by a flash flood!",0);
				return;
			}
	}

	@Override
	public boolean validate(){
		if(Squad.active.fly()) return false;
		int survival=Squad.active.getbest(Skill.SURVIVAL).roll(Skill.SURVIVAL);
		survival+=Terrain.get(Squad.active.x,Squad.active.y).survivalbonus;
		if(survival>=DC) return false;
		return Weather.current==Weather.STORM
				||Weather.current==Weather.RAIN&&Season.current==Season.SPRING;
	}
}
