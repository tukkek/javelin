/**
 *
 */
package javelin.controller.content.fight.mutator.mode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.content.fight.Fight;
import javelin.controller.exception.GaveUp;
import javelin.controller.generator.encounter.EncounterGenerator.MonsterPool;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.item.Tier;
import javelin.model.unit.Combatants;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.old.RPG;

/**
 * A series of battles, which the player can continue or abort after each round,
 * for more rewards.
 *
 * TODO can also be used as a {@link Location} or {@link Feature}, only needs an
 * Image (that works for both, ideally).
 *
 * @author alex
 */
public class Gauntlet extends FightMode{
	static final String PROMPT="A mysterious, %s voice asks:\n"
			+"\"Can you withstand a tougher challenge or do you concede, mortal?\"\n"
			+"Your current rewards are: %s.\n"
			+"Press a to advance to the next round or c to concede...";

	List<Item> rewards=new ArrayList<>(0);
	MonsterPool monsters;
	ItemSelection items;
	String voice;
	Tier tier;
	int level;

	/**
	 * Constructor.
	 *
	 * @param el Encounter level. May be modifeid to start at an easier level.
	 */
	public Gauntlet(int el,MonsterPool m,ItemSelection i,String v){
		monsters=m;
		items=i;
		voice=v;
		tier=Tier.get(el);
		level=Math.max(tier.minlevel,el-4);
	}

	@Override
	public void setup(Fight f){
		super.setup(f);
		f.rewardgold=false;
	}

	@Override
	public Combatants generate(Fight f) throws GaveUp{
		return monsters.generate(level);
	}

	void reward(){
		var gold=RewardCalculator.getgold(ChallengeCalculator.eltocr(level));
		if(RPG.chancein(2)){
			for(var r:rewards)
				gold+=r.price;
			rewards.clear();
		}
		var loot=RewardCalculator.generateloot(gold,1,items);
		for(var l:loot)
			l.identified=true;
		rewards.addAll(loot);
	}

	@Override
	public void checkend(Fight f){
		super.checkend(f);
		try{
			var s=Fight.state;
			if(!s.redteam.isEmpty()) return;
			reward();
			if(level==tier.maxlevel) return;
			level+=1;
			var enemies=generate(f);
			var rewards=Javelin.group(this.rewards).toLowerCase();
			var prompt=String.format(PROMPT,voice,rewards);
			if(Character.toLowerCase(Javelin.prompt(prompt,Set.of('a','c')))=='a')
				f.add(enemies,s.redteam);
		}catch(GaveUp e){
			return;
		}
	}

	@Override
	public void end(Fight f){
		super.end(f);
		if(Fight.victory) for(var r:rewards)
			r.grab();
	}
}
