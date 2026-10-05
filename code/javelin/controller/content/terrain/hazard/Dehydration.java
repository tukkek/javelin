package javelin.controller.content.terrain.hazard;

import javelin.controller.content.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.condition.Fatigued;
import javelin.model.unit.skill.Skill;
import javelin.old.RPG;

/**
 * Damages characters if Squad has no resource reserves (gold) left.
 *
 * @author alex
 */
public class Dehydration extends PartyHazard{
	@Override
	protected String affect(Combatant c,int hoursellapsed){
		c.damage(RPG.r(1,6),0);
		c.addcondition(new Fatigued(null,8));
		return c+" is dehydratading";
	}

	@Override
	protected boolean save(int hoursellapsed,Combatant c){
		return c.hp==1||c.source.save(c.source.getfortitude(),10+hoursellapsed/2);
	}

	@Override
	public boolean validate(){
		if(Squad.active.gold>0) return false;
		int survival=Squad.active.getbest(Skill.SURVIVAL).roll(Skill.SURVIVAL);
		survival+=Terrain.get(Squad.active.x,Squad.active.y).survivalbonus;
		return survival<25;
	}
}