package javelin.controller.exception.battle;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.ai.ThreadManager;
import javelin.controller.ai.cache.AiCache;
import javelin.controller.fight.Fight;
import javelin.controller.terrain.Terrain;
import javelin.controller.wish.Ressurect;
import javelin.model.item.consumable.Scroll;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.conjuration.healing.RaiseDead;
import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.old.messagepanel.MessagePanel;
import javelin.view.screen.BattleScreen;

/**
 * A victory or defeat condition has been achieved.
 *
 * @author alex
 */
public class EndBattle extends BattleEvent{
	/** For debugging purposes. Reset manually. */
	public static boolean skipresultmessage=false;

	/** Start after-{@link Fight} cleanup. */
	public static void end(){
		Fight.victory=Javelin.app.fight.win();
		terminateconditions(Fight.state,BattleScreen.active);
		if(Javelin.app.fight.onend()){
			var s=Squad.active;
			if(s!=null){
				while(World.get(s.x,s.y,Incursion.class)!=null){
					s.displace();
					s.place();
				}
				end(Fight.originalblueteam);
				if(Dungeon.active!=null) Dungeon.active.activate(false);
			}
		}
		AiCache.reset();
		if(World.scenario!=null)
			World.scenario.end(Javelin.app.fight,Fight.victory);
		Javelin.app.fight=null;
		Fight.state=null;
	}

	static void terminateconditions(BattleState s,BattleScreen screen){
		screen.block();
		for(Combatant c:Fight.state.getcombatants())
			c.finishconditions(s,screen);
	}

	/**
	 * Prints combat info (rewards, etc).
	 *
	 * @param prefix
	 */
	public static void showcombatresult(){
		MessagePanel.active.clear();
		String combatresult;
		if(Fight.victory)
			combatresult=Javelin.app.fight.reward();
		else if(Fight.state.getfleeing(Fight.originalblueteam).isEmpty()){
			Squad.active.disband();
			combatresult="You lost!";
		}else if(Javelin.app.fight.friendly)
			combatresult="You lost!";
		else{
			combatresult="Fled from combat. No awards received.";
			if(!Fight.victory
					&&Fight.state.fleeing.size()!=Fight.originalblueteam.size()){
				combatresult+="\nFallen allies left behind are lost!";
				for(Combatant abandoned:Fight.state.dead)
					abandoned.hp=Combatant.DEADATHP;
			}
			if(Squad.active.transport!=null&&Dungeon.active==null
					&&!Terrain.current().equals(Terrain.WATER)){
				combatresult+=" Vehicle lost!";
				Squad.active.transport=null;
				Squad.active.updateavatar();
			}
		}
		if(combatresult==null|skipresultmessage) return;
		Javelin.message(combatresult+"\nPress any key to continue...",
				Javelin.Delay.BLOCK);
		BattleScreen.active.getUserInput();
	}

	static void updateoriginal(List<Combatant> originalteam){
		var update=new ArrayList<>(Fight.state.blueTeam);
		for(var d:Fight.state.dead)
			if(d.hp>Combatant.DEADATHP) update.add(d);
		for(var inbattle:update){
			int originali=originalteam.indexOf(inbattle);
			if(originali>=0){
				inbattle.terminateconditions(0);
				inbattle.xp=originalteam.get(originali).xp;
				originalteam.set(originali,inbattle);
			}
		}
	}

	static void copyspells(final Combatant from,final Combatant to){
		for(var spell:from.spells){
			var original=to.spells.get(spell.getClass());
			if(original!=null) original.used=spell.used;
		}
	}

	/**
	 * Tries to {@link #revive(Combatant)} the combatant. If can't, remove him
	 * from the game.
	 *
	 * TODO isn't updating {@link Ressurect#dead} when the entire Squad dies! this
	 * probably isn't being called
	 */
	static void bury(List<Combatant> originalteam){
		for(Combatant c:Fight.state.dead){
			if(!originalteam.contains(c)) continue;
			if(c.hp>Combatant.DEADATHP&&c.source.constitution>0)
				c.hp=1;
			else if(!Fight.victory||!revive(c,originalteam)){
				originalteam.remove(c);
				c.bury();
			}
		}
		Fight.state.dead.clear();
	}

	/**
	 * TODO this doesnt let you select between spell or scroll, between which
	 * instance of nay of those nor between which characters to use it with. In
	 * short, we need a screen for all that.
	 */
	static boolean revive(Combatant dead,List<Combatant> originalteam){
		var alive=new ArrayList<>(originalteam);
		alive.removeAll(Fight.state.dead);
		var spell=castrevive(alive);
		var scroll=findressurectscroll(alive);
		if(scroll!=null) spell=scroll.spell;
		if(spell==null||!spell.validate(null,dead)) return false;
		spell.castpeacefully(null,dead,originalteam);
		if(scroll==null)
			spell.used+=1;
		else
			Squad.active.equipment.remove(scroll);
		return true;
	}

	static Scroll findressurectscroll(List<Combatant> alive){
		List<Scroll> ressurectscrolls=new ArrayList<>();
		for(Scroll s:Squad.active.equipment.getall(Scroll.class))
			if(s.spell instanceof RaiseDead) ressurectscrolls.add(s);
		if(ressurectscrolls.isEmpty()) return null;
		for(Combatant c:alive)
			for(Scroll s:ressurectscrolls)
				if(s.canuse(c)==null) return s;
		return null;
	}

	static Spell castrevive(List<Combatant> alive){
		for(Combatant c:alive)
			for(Spell s:c.spells)
				if(s instanceof RaiseDead&&!s.exhausted()) return s;
		return null;
	}

	static void end(Combatants originalteam){
		for(Combatant c:Fight.state.getcombatants())
			if(c.summoned){
				Fight.state.blueTeam.remove(c);
				Fight.state.redTeam.remove(c);
			}
		updateoriginal(originalteam);
		bury(originalteam);
		Squad.active.members=originalteam;
		ThreadManager.printbattlerecord();
	}
}
