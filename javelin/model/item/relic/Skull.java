package javelin.model.item.relic;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.fight.Fight;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.old.RPG;

/**
 * Damages all good creatures in battle (1-99% will save).
 *
 * @author alex
 */
public class Skull extends Relic{
	/** Constructor. */
	public Skull(Integer level){
		super("Skull of Pain",level);
		usedinbattle=true;
		usedoutofbattle=false;
	}

	@Override
	protected boolean activate(Combatant user){
		ArrayList<Combatant> good=new ArrayList<>();
		for(Combatant c:Fight.state.getcombatants()){
			Monster m=c.source;
			if(m.alignment.isgood()) good.add(c);
		}
		if(good.isEmpty()){
			Javelin.message("Nothing seems to happen...",Javelin.Delay.BLOCK);
			return true;
		}
		float dc=0;
		for(Combatant c:good){
			int wisdom=Monster.getbonus(c.source.wisdom);
			if(wisdom!=Integer.MAX_VALUE) dc=Math.max(dc,20+wisdom);
		}
		for(Combatant c:good){
			int wisdom=Monster.getbonus(c.source.wisdom);
			c.hp-=c.maxhp*((RPG.r(1,20)+wisdom)/dc);
			if(c.hp<1)
				c.hp=1;
			else if(c.hp==c.maxhp) c.hp-=1;
		}
		Javelin.message("Good creatures convulse in agony!",Javelin.Delay.BLOCK);
		return true;
	}
}
