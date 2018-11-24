package javelin.model.world.location.dungeon.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.action.world.WorldMove;
import javelin.controller.challenge.Difficulty;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.scenario.dungeondelve.DungeonDelve;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.unique.AdventurersGuild;
import javelin.old.RPG;

/**
 * Allows a {@link Combatant} to learn a set of {@link Upgrade}s.
 *
 * Initially selected {@link Upgrade}s on-the-fly to ensure at least one was
 * available but that turned out poorly, especially for {@link DungeonDelve}
 * where the player depends on Stones to advance units. Now randomly adds
 * upgrades from one of the predefined sets in {@link UpgradeHandler}, making it
 * a strategic gamble as well.
 *
 * TODO take the (now inert) stone for its {@link #value} in gold
 *
 * @author alex
 */
public class LearningStone extends Feature{
	static final String CONFIRM="Will you use this learning stone?\n"
			+"Press ENTER to confirm or any other key to cancel...";

	static{
		UpgradeHandler.singleton.gather();
	}

	/** {@link Difficulty#MODERATE} value in gold. */
	int value=RewardCalculator.getgold(Math.max(1,Dungeon.active.level-4));
	List<Upgrade> upgrades;

	/** Constructor. */
	public LearningStone(){
		super(-1,-1,"dungeonlearningstone");
		remove=false;
		upgrades.addAll(RPG.pick(
				new ArrayList<>(UpgradeHandler.singleton.getall(false).values())));
		upgrades.sort((a,b)->a.getname().compareTo(b.getname()));
	}

	@Override
	public boolean activate(){
		if(Javelin.prompt(CONFIRM)!='\n') return false;
		WorldMove.abort=true;
		var options=Squad.active.members.stream().map(c->c+" ("+c.gethumanxp()+")")
				.collect(Collectors.toList());
		var prompt="This is a x learning stone. Who will touch from it?";
		var choice=Javelin.choose(prompt,options,true,false);
		if(choice<0) return true;
		var student=Squad.active.members.get(choice);
		AdventurersGuild.train(student,upgrades,student.xp.floatValue());
		return true;
	}

	static boolean accept(Upgrade u){
		for(var c:Squad.active.members)
			if(u.validate(c)) return true;
		return false;
	}
}
