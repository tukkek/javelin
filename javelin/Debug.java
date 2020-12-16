package javelin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javelin.controller.action.Help;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.collection.CountingSet;
import javelin.controller.db.Preferences;
import javelin.controller.event.EventCard;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.exception.battle.EndBattle;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.Fight;
import javelin.controller.fight.mutator.Mutator;
import javelin.controller.fight.setup.BattleSetup;
import javelin.controller.generator.NpcGenerator;
import javelin.controller.generator.WorldGenerator;
import javelin.controller.kit.Kit;
import javelin.controller.map.Map;
import javelin.controller.scenario.Scenario;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.Period;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.town.Town;
import javelin.view.Images;
import javelin.view.TextWindow;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.WorldScreen;

/**
 * A collection of methods that can be altered to facilitate testing the game.
 * Entry-points start with "on", such as {@link #oncampaignstart()} and should
 * never be called from other parts of the game unless {@link Javelin#DEBUG} is
 * <code>true</code>. Other methods are helpers to be used within the class,
 * such as {@link #grab(Item[])}.
 *
 * Ideally changes to this class should never be commited unless when expanding
 * debug functionalities (such adding new entry or helper methods).
 *
 * @author alex
 */
public class Debug{
	static class DebugFight extends Fight{
		Combatants foes;
		Boolean avoid=null;
		Boolean win=null;

		public DebugFight(Combatants foes){
			this.foes=foes;
			mutators.add(new Mutator(){
				@Override
				public void ready(Fight f){
					super.ready(f);
					for(var c:state.redteam)
						c.ap=1000;
				}
			});
		}

		@Override
		public ArrayList<Combatant> getfoes(Integer teamel){
			return foes;
		}

		@Override
		public boolean avoid(List<Combatant> foes){
			return avoid==null?super.avoid(foes):avoid;
		}

		@Override
		public boolean win(){
			return win==null?super.win():win;
		}
	}

	static class Helpers{
		static void healteam(){
			for(Combatant c:Squad.active.members){
				c.hp=c.maxhp;
				c.detox(c.source.poison);
			}
			if(Fight.state==null) return;
			for(Combatant c:Fight.state.blueteam){
				c.hp=c.maxhp;
				c.detox(c.source.poison);
			}
		}

		static void healopponenets(){
			if(Fight.state==null) return;
			for(Combatant c:Fight.state.redteam)
				c.hp=c.maxhp;
		}

		static void grab(Item[] items){
			for(Item i:items)
				Squad.active.equipment.add(i);
		}

		static String printtowninfo(){
			String s="";
			for(Town t:Town.gettowns()){
				String el=t.ishostile()
						?", EL "+ChallengeCalculator.calculateel(t.garrison)
						:"";
				s+=t+" ("+t.getrank().title+el+")\n";
			}
			return s;
		}

		static void fight(Map m){
			var c=new Combatant(Monster.get("orc"),false);
			c.ap=1000;
			var f=new DebugFight(new Combatants(List.of(c)));
			f.win=false;
			f.map=m;
			f.bribe=false;
			f.hide=false;
			f.period=Period.AFTERNOON;
			throw new StartBattle(f);
		}

		static void freezeopponents(){
			var m=new Mutator(){
				@Override
				public void endturn(Fight fight){
					super.endturn(fight);
					for(var c:Fight.state.redteam)
						c.ap=1000;
				}
			};
			m.endturn(null);
			Fight.current.mutators.add(m);
		}

		static void teleport(Class<? extends Actor> type){
			if(Dungeon.active!=null) return;
			var to=World.getactors().stream().filter(a->type.isInstance(a)).findAny()
					.orElseThrow();
			Squad.active.remove();
			Squad.active.setlocation(to.x,to.y);
			Squad.active.displace();
			Squad.active.place();
		}

		static void printworldresets(){
			String text="";
			for(String reset:WorldGenerator.RESETS.getinvertedelements()){
				text+="Count: "+WorldGenerator.RESETS.getcount(reset)+"\n";
				text+=reset+"\n\n";
			}
			new TextWindow("World generation resets",text).show();
		}

		static void happen(Class<? extends EventCard> type){
			try{
				var e=type.getConstructor(Town.class).newInstance(gettown());
				var s=Squad.active;
				var el=s.getel();
				if(!e.validate(s,el)){
					Javelin.message("Invalid event: "+type.getSimpleName()+"...",false);
					throw new RepeatTurn();
				}
				e.define(s,el);
				e.happen(s);
			}catch(ReflectiveOperationException exception){
				Javelin.message("Error: "+exception,false);
				throw new RepeatTurn();
			}
		}

		static Town gettown(){
			var d=Squad.active.getdistrict();
			if(d!=null) return d.town;
			Javelin.message("Not in town...",false);
			throw new RepeatTurn();
		}

		static void test(Kit kit){
			var s=Squad.active;
			s.members.clear();
			var human=Monster.get("human");
			for(var level:new int[]{3,8,13,18}){
				var npc=NpcGenerator.generatenpc(human,kit,level);
				if(npc!=null) s.members.add(npc);
			}
			if(BattleScreen.active.getClass()==WorldScreen.class){
				kit.createguild().place(s.getlocation());
				s.displace();
			}
		}

		static void reloadimages(){
			Images.clearcache();
		}

		/** Put Fight.withdrawall(false) on {@link Debug#onbattlestart()}. */
		static void place(Integer times,List<? extends Class<? extends Map>> maps){
			if(maps==null) maps=Map.getall().stream().map(m->m.getClass())
					.collect(Collectors.toList());
			if(times==null) times=60;
			try{
				EndBattle.skipresultmessage=true;
				var measures=new ArrayList<Long>(times*maps.size());
				var passes=new CountingSet();
				var opponents=makearmy(100);
				for(var map:maps)
					for(var i=1;i<=times;i++){
						System.out.println(map.getCanonicalName()+" "+i+"/"+times);
						var f=new DebugFight(opponents);
						f.avoid=false;
						f.map=map.getConstructor().newInstance();
						Fight.current=f;
						var clock=System.currentTimeMillis();
						try{
							new StartBattle(f).battle();
						}catch(EndBattle e){
							EndBattle.end();
						}
						measures.add(System.currentTimeMillis()-clock);
						passes.add(BattleSetup.pass);
					}
				measures.sort(null);
				System.out.println("Passes: "+passes+"\nMedian time: "
						+measures.get(measures.size()/2)+"ms");
			}catch(ReflectiveOperationException e){
				throw new RuntimeException(e);
			}finally{
				EndBattle.skipresultmessage=false;
			}
		}

		static Combatants makearmy(int opponents){
			var monsters=new Combatants(opponents);
			while(monsters.size()<opponents)
				monsters.add(new Combatant(Monster.get("Orc"),true));
			return monsters;
		}
	}

	/** @see Preferences */
	public static boolean disablecombat;
	/** @see Preferences */
	public static boolean showmap;
	/** @see Preferences */
	public static Integer xp;
	/** @see Preferences */
	public static Integer gold;
	/** @see Preferences */
	public static boolean labor;
	/** @see Preferences */
	public static String period;
	/** @see Preferences */
	public static String weather;
	/** @see Preferences */
	public static String season;
	/** @see Preferences */
	public static boolean unlocktemples;
	/** @see Preferences */
	public static boolean bypassdoors;

	/**
	 * Java doesn't have programatic breakpoints but sometimes it's useful to
	 * emulate them by setting a breakpoint here instead of directly into code.
	 * One example is using <code>if(something) Debug.breakpoint()</code>, which
	 * is a one-line hack and much faster than conditional breakpoints in Eclipse.
	 */
	public static void breakpoint(){
		return;
	}

	/** Called every time a game starts (before player interaction). */
	public static void oninit(){

	}

	/** @see StartBattle */
	public static void onbattlestart(){

	}

	/** Called only once when a {@link Scenario} is initialized. */
	public static void oncampaignstart(){

	}

	/**
	 * Similar to {@link #onworldhelp()} but called from the {@link BattleScreen}.
	 */
	public static String onbattlehelp(){
		throw new RepeatTurn();
	}

	/**
	 * Called from {@link Help}. Useful for making changes during the course of a
	 * game or testing sequence, since Javelin doesn't have a developer console
	 * for debugging purposes.
	 *
	 * @return Any text will be printed below the usual help output.
	 */
	public static String onworldhelp(){
		throw new RepeatTurn();
	}
}
