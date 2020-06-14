package javelin.controller.exception.battle;

import java.util.ArrayList;

import javelin.Debug;
import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.fight.Fight;
import javelin.controller.fight.minigame.Minigame;
import javelin.controller.fight.setup.BattleSetup;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.old.RPG;
import javelin.view.mappanel.battle.BattlePanel;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.InfoScreen;

/**
 * A {@link Fight} has started.
 *
 * @see BattleSetup
 * @author alex
 */
public class StartBattle extends BattleEvent{
	/** Controller for the battle. */
	public final Fight fight;

	/** Constructor. */
	public StartBattle(final Fight d){
		fight=d;
	}

	/** Prepares and switches to a {@link BattleScreen}. */
	public void battle(){
		ArrayList<Combatant> foes=fight.setup();
		if(World.scenario!=null)
			World.scenario.start(fight,Fight.originalblueteam,Fight.originalredteam);
		if(fight.avoid(foes)) return;
		preparebattle(foes);
		fight.setup.setup();
		Fight.state.next();
		fight.ready();
		final int elred=ChallengeCalculator.calculateel(Fight.state.redTeam);
		final int elblue=ChallengeCalculator.calculateel(Fight.state.blueTeam);
		int diffifculty=elred-elblue;
		if(fight instanceof Minigame||!Squad.active.skipcombat(diffifculty)){
			BattlePanel.current=Fight.state.next;
			BattleScreen screen=new BattleScreen(true,true);
			fight.draw();
			if(Javelin.DEBUG) Debug.onbattlestart();
			screen.mainloop();
		}else
			quickbattle(diffifculty);
	}

	/**
	 * Runs a strategic combat instead of opening a {@link BattleScreen}. The
	 * problem with this is that, being more predictable, makes it easier for a
	 * human player to just safely farm gold and XP on easy regions without much
	 * chance of death (even at low HP) - so an extra level of fair difficulty
	 * randomization is added here, if only to prevent players from farming in
	 * strategic mode without ever resting.
	 *
	 * @param difficulty
	 */
	public void quickbattle(int difficulty){
		difficulty+=RPG.randomize(2);
		float resourcesused=ChallengeCalculator.useresources(difficulty);
		String report="Battle report:\n\n";
		ArrayList<Combatant> blueteam=new ArrayList<>(Squad.active.members);
		ArrayList<Float> damage=damage(blueteam,resourcesused);
		for(int i=0;i<blueteam.size();i++)
			report+=strategicdamage(blueteam.get(i),damage.get(i))+"\n\n";
		if(Squad.active.equipment.count()==0)
			report+=Squad.active.wastegold(resourcesused);
		InfoScreen s=new InfoScreen("");
		s.print(report+"Press ENTER or s to continue...");
		Character feedback=s.getInput();
		while(feedback!='\n'&&feedback!='s')
			continue;
		BattleScreen.active.center();
		Squad.active.gold-=Squad.active.gold*(resourcesused/10f);
		if(Squad.active.members.isEmpty()){
			Javelin.message("Battle report: Squad lost in combat!",false);
			Squad.active.disband();
		}else{
			Fight.victory=true;
			fight.onend();
		}
		Javelin.app.fight=null;
	}

	private ArrayList<Float> damage(ArrayList<Combatant> blueteam,
			float resourcesused){
		ArrayList<Float> damage=new ArrayList<>(blueteam.size());
		while(damage.size()<blueteam.size())
			damage.add(0f);
		float total=resourcesused*blueteam.size();
		float dealt=0;
		float step=resourcesused/2f;
		while(dealt<total){
			int i=RPG.r(0,blueteam.size()-1);
			if(damage.get(i)<=1){
				damage.set(i,damage.get(i)+step);
				dealt+=step;
			}
		}
		return damage;
	}

	/**
	 * TODO this needs to be enhanced because currently fighting with full health
	 * in a EL-1 battle will result in everyone surviving with 1% health or
	 * something, making this very easy to abuse. A better option might be to
	 * introduce some randomness on the difficulty used to calculate this or think
	 * of a new system where the damage can be distributed randomly between party
	 * members (instead of uniformly) or even "cancel" units of same CR before
	 * doing calculations.
	 *
	 * @return
	 */
	static String strategicdamage(Combatant c,float resourcesused){
		c.hp-=c.maxhp*resourcesused;
		boolean killed=c.hp<=Combatant.DEADATHP|| //
				c.hp<=0&&RPG.random()<Math.abs(c.hp/new Float(Combatant.DEADATHP));
		String report="";
		ArrayList<Item> bag=Squad.active.equipment.get(c);
		for(Item i:new ArrayList<>(bag)){
			String used="";
			if(i.waste){
				String wasted=i.waste(resourcesused,c,bag);
				if(wasted!=null) used+=wasted+", ";
			}
			if(!used.isEmpty())
				report+=" Used: "+used.substring(0,used.length()-2)+".";
		}
		if(killed){
			Squad.active.remove(c);
			c.hp=Integer.MIN_VALUE;
		}else{
			if(c.hp<=0) c.hp=1;
			report+=c.wastespells(resourcesused);
		}
		return c+" is "+c.getstatus()+"."+report;
	}

	/** TODO deduplicate originals */
	public void preparebattle(ArrayList<Combatant> opponents){
		Fight.state.redTeam=opponents;
		var blue=Fight.state.blueTeam;
		Fight.originalblueteam=new Combatants(blue);
		Fight.originalredteam=new Combatants(Fight.state.redTeam);
		for(int i=0;i<blue.size();i++){
			var c=blue.get(i);
			blue.set(i,c.clone().clonesource());
		}
		for(var p:fight.onprepare)
			p.run();
		Fight.state.next();
	}

	static ArrayList<Combatant> cloneteam(ArrayList<Combatant> team){
		ArrayList<Combatant> clone=new ArrayList<>(team.size());
		for(Combatant c:team)
			clone.add(c.clone());
		return clone;
	}
}
