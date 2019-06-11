package javelin;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.action.Help;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.db.Preferences;
import javelin.controller.event.urban.UrbanEvent;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.Fight;
import javelin.controller.generator.NpcGenerator;
import javelin.controller.generator.WorldGenerator;
import javelin.controller.kit.Kit;
import javelin.controller.map.Map;
import javelin.controller.scenario.Scenario;
import javelin.model.Realm;
import javelin.model.diplomacy.Diplomacy;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.town.Town;
import javelin.view.TextWindow;
import javelin.view.frame.DiplomacyScreen;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.WorldScreen;

/**
 * A collection of methods that can be altered to facilitate testing the game.
 * Entry-points start with "on", such as {@link #oncampaignstart()} and should
 * never be called from other parts of the game unless {@link Javelin#DEBUG} is
 * <code>true</code>. Other methods are helpers to be used within the class,
 * such as {@link #additems(Item[])}.
 *
 * Ideally changes to this class should never be commited unless when expanding
 * debug functionalities (such adding new entry or helper methods).
 *
 * @author alex
 */
public class Debug{
	static class Helpers{
		static void healteam(){
			for(Combatant c:Squad.active.members){
				c.hp=c.maxhp;
				c.detox(c.source.poison);
			}
			if(Fight.state==null) return;
			for(Combatant c:Fight.state.blueTeam){
				c.hp=c.maxhp;
				c.detox(c.source.poison);
			}
		}

		static void healopponenets(){
			if(Fight.state==null) return;
			for(Combatant c:Fight.state.redTeam)
				c.hp=c.maxhp;
		}

		static void additems(Item[] items){
			for(Item i:items)
				Squad.active.receiveitem(i);
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
			Fight maptest=new Fight(){
				@Override
				public ArrayList<Combatant> getfoes(Integer teamel){
					ArrayList<Combatant> monsters=new ArrayList<>(1);
					Combatant c=new Combatant(Javelin.getmonster("Orc"),true);
					c.source.initiative=-20;
					monsters.add(c);
					return monsters;
				}

				@Override
				public boolean win(){
					return false;
				}
			};
			maptest.map=m;
			maptest.bribe=false;
			maptest.hide=false;
			throw new StartBattle(maptest);
		}

		static void freezeopponents(){
			for(Combatant c:Fight.state.redTeam)
				c.ap=Float.MAX_VALUE;
		}

		static void generateincursion(Location l,Realm r,List<Combatant> members){
			for(Actor a:Incursion.getall())
				a.remove();
			Incursion i=new Incursion(l.x,l.y,members,r);
			i.displace();
			i.place();
		}

		static void teleport(Class<? extends Actor> type){
			if(Dungeon.active!=null) return;
			Actor to=null;
			for(Actor a:World.getactors())
				if(type.isInstance(a)){
					to=a;
					break;
				}
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

		static void doevent(Class<? extends UrbanEvent> type){
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

		static void dodiplomacy(){
			var d=Diplomacy.instance;
			d.reputation=Diplomacy.TRIGGER;
			d.hand.clear();
			d.draw();
			DiplomacyScreen.open();
		}

		static void testkit(Kit kit){
			kit.validate();
			Squad.active.members.clear();
			var human=Javelin.getmonster("human");
			for(var level:new int[]{3,8,13,18}){
				var npc=NpcGenerator.generatenpc(human,kit,level);
				if(npc!=null) Squad.active.members.add(npc);
			}
			if(BattleScreen.active.getClass()==WorldScreen.class){
				var guild=kit.createguild();
				guild.x=Squad.active.x;
				guild.y=Squad.active.y;
				guild.place();
				Squad.active.displace();
			}
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
	public static boolean unlcoktemples;
	/** @see Preferences */
	public static boolean bypassdoors;

	/** @see StartBattle */
	public static void onbattlestart(){

	}

	/** Called only once when a {@link Scenario} is initialized. */
	public static void oncampaignstart(){

	}

	/**
	 * Called every time a game starts (roughly the first time the
	 * {@link WorldScreen} is shown.
	 */
	public static void oninit(){

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
