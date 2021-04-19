package javelin.controller.content.fight.mutator.mode;

import java.util.Set;

import javelin.Javelin;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.content.fight.Fight;
import javelin.controller.exception.GaveUp;
import javelin.controller.generator.encounter.EncounterGenerator.MonsterPool;
import javelin.model.unit.Combatants;
import javelin.model.unit.Squad;

/**
 * An infinitely-spawning series of combats that favor large numbers of enemies.
 * Players have the option to stop between each wave.
 *
 * The goal is to beat three rounds.
 *
 * @author alex
 */
public class Horde extends FightMode{
	static final String REINFORCEMENTS="Reinforcements are fast approaching!\n"
			+"You see: %s...\n\n"
			+"Press r to retreat to safety or c to continue fighting (fallen allies will also be rescued)...";

	boolean fled=false;
	MonsterPool pool;
	int waves=2;
	/**
	 * For two rounds, this would be EL-2 but since the player has the option to
	 * quit midway through, -1 is fine.
	 */
	int el;

	/** Constructor. */
	public Horde(int elp,MonsterPool p){
		el=elp-1;
		pool=p;
	}

	@Override
	public Combatants generate(Fight f) throws GaveUp{
		int el=this.el-3;//further -3 for 3 packs
		var c=new Combatants(pool.generate(el));
		c.addAll(pool.generate(el));
		c.addAll(pool.generate(el));
		return c;
	}

	@Override
	public void checkend(Fight f){
		super.checkend(f);
		try{
			var s=Fight.state;
			if(!s.redteam.isEmpty()) return;
			waves-=1;
			if(waves<=0) return;
			var horde=generate(f);
			var prompt=String.format(REINFORCEMENTS,Squad.active.scout(horde,null));
			if(Javelin.prompt(prompt,Set.of('c','r'))=='r'){
				fled=true;
				f.flee(true);
			}else
				f.add(horde,s.redteam);
		}catch(GaveUp e){
			return;
		}
	}

	@Override
	public void end(Fight f){
		super.end(f);
		if(fled){
			Fight.victory=false;
			var vanquished=new Combatants(Fight.state.dead);
			vanquished.retainAll(Fight.originalredteam);
			var xpwon=RewardCalculator.rewardxp(Squad.active.members,vanquished,1);
			Javelin.message(xpwon,true);
		}
	}
}
