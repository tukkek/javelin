package javelin.model.world.location.dungeon.feature;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.action.world.WorldMove;
import javelin.controller.challenge.Difficulty;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.old.RPG;

/**
 * Allows a {@link Combatant} to learn a set of {@link Upgrade}s. The upgrade is
 * applied immediately, even if results in a negative XP pool for the unit -
 * mostly because it's no fun to find a cool feature and not be able to use it.
 * Any upgrade is eligible as long as 50% fo its experience cost can be paid
 * upfront.
 *
 * In the same spirit, the available {@link #upgrades} are not predetermined but
 * chosen randomly so it will at least be able to be applied to one
 * {@link Squad} member.
 *
 * TODO take the (now inert) stone for its {@link #value} in gold
 *
 * @author alex
 */
public class LearningStone extends Feature{
	static final String CONFIRM="Will you use this learning stone?\n"
			+"Press ENTER to confirm or any other key to cancel...";

	/** {@link Difficulty#MODERATE} value in gold. */
	int value=RewardCalculator.getgold(Math.max(1,Dungeon.active.level-4));
	List<Upgrade> upgrades=new ArrayList<>();

	/** Constructor. */
	public LearningStone(){
		super(-1,-1,"dungeonlearningstone");
		remove=false;
	}

	@Override
	public boolean activate(){
		if(Javelin.prompt(CONFIRM)!='\n') return false;
		WorldMove.abort=true;
		generateupgrades();
		Upgrade u=selectupgrade();
		if(u==null) return false;
		var c=selectmember(u);
		if(c==null) return false;
		var cost=u.getcost(c);
		u.upgrade(c);
		c.xp=c.xp.subtract(new BigDecimal(cost));
		return true;
	}

	void generateupgrades(){
		if(!upgrades.isEmpty()) return;
		UpgradeHandler handler=UpgradeHandler.singleton;
		handler.gather();
		var allupgrades=new LinkedList<>(handler.getalluncategorized());
		Collections.shuffle(allupgrades);
		var target=RPG.r(3,7);
		while(upgrades.size()<target&&!allupgrades.isEmpty()){
			var u=allupgrades.pop();
			if(accept(u)) upgrades.add(u);
		}
		upgrades.sort((a,b)->a.getname().compareTo(b.getname()));
	}

	static boolean accept(Upgrade u){
		for(var c:Squad.active.members)
			if(u.validate(c)) return true;
		return false;
	}

	Upgrade selectupgrade(){
		String prompt="Which will you learn?";
		var names=upgrades.stream().map(u->u.getname())
				.collect(Collectors.toList());
		var choice=Javelin.choose(prompt,names,true,false);
		return choice>=0?upgrades.get(choice):null;
	}

	static Combatant selectmember(Upgrade u){
		Squad.active.sort();
		var members=new ArrayList<>(Squad.active.members);
		members.removeAll(Squad.active.getmercenaries());
		var eligible=new ArrayList<Combatant>(members.size());
		var choices=new ArrayList<String>(members.size());
		for(var c:members){
			final String detail;
			if(!u.validate(c))
				detail="not currently eligible";
			else{
				var cost=u.getcost(c);
				if(c.xp.floatValue()<cost/2)
					detail="not enough XP";
				else{
					detail=u.inform(c);
					eligible.add(c);
				}
			}
			choices.add(c+" ("+detail+")");
		}
		var prompt="Who will learn "+u.getname()+"?";
		var choice=Javelin.choose(prompt,choices,true,false);
		if(choice<0) return null;
		var c=members.get(choice);
		return eligible.contains(c)?c:null;
	}
}
