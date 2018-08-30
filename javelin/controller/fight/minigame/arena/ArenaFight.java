package javelin.controller.fight.minigame.arena;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.exception.GaveUp;
import javelin.controller.exception.battle.EndBattle;
import javelin.controller.fight.minigame.Minigame;
import javelin.controller.fight.minigame.arena.building.ArenaFountain;
import javelin.controller.fight.minigame.arena.building.ArenaGateway;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.map.Arena;
import javelin.controller.terrain.Terrain;
import javelin.model.item.Item;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.old.RPG;
import javelin.old.messagepanel.MessagePanel;
import javelin.view.screen.BattleScreen;

/**
 * TODO would be cool if could generate heroes to fight against at some point
 *
 * TODO would be amazing to have 4 different map types generated (1 per
 * quadrant), also visually, when the engine allows it
 *
 * TODO clicking on a building, even if far away, should tell you your current
 * amount of XP
 *
 * TODO instead of current victory condition, add a Gate building that on raise
 * tension has a (current monster EL/EL goal) chance of activating (minimum
 * always 5%). If it is destroyed, lose the arena. If it is activated, save
 * progress and allow to pick up from there.
 *
 * TODO things to add to make it more strategic: towers, powerup (shrines).
 * Towers could be static spawners, summoning units with a percent chance arch
 * turn or static buildings with a single unlimited-use spell. Shrines would be
 * ArenaBuildings that work like Campaign-mode shrines.
 *
 * TODO having buildings to be attacked would add a lot to gemeplay, instead of
 * being only a defensive game. The goal could then be to killa all buildings
 * instead.
 *
 * TODO it would be cool if when an enemy structure is razed, the player could
 * choose which one of build in its stead (by paying the level 1 building price
 * and waiting for it to be reconstructed). The same could apply for monsters
 * razing player's structures.
 *
 * TODO to make space for new structures, altering fountains to heal everyone in
 * an area instead of just the user would be great.
 *
 * @see Arena
 *
 * @author alex
 */
public class ArenaFight extends Minigame{
	public static final float BOOST=4; // 3,5 talvez?

	static final boolean SPAWN=false;
	static final int TENSIONMIN=-5;
	static final int TENSIONMAX=0;
	static final int ELMIN=-12;
	static final int ELMAX=0;

	/** {@link Item} bag for {@link #gladiators}. */
	public HashMap<Integer,ArrayList<Item>> items=new HashMap<>();
	public int gold=0;

	/**
	 * Non-mercenary units, live and dead.
	 *
	 * @see #getgladiators()
	 */
	Combatants gladiators;
	int tension=RPG.r(TENSIONMIN,TENSIONMAX);
	float check=-Float.MAX_VALUE;
	/**
	 * Ensures that new waves are never becoming less dangerous (pressuring the
	 * player to upgrade and not just sit around).
	 */
	int baseline=Integer.MIN_VALUE;
	ArrayList<ArrayList<Combatant>> foes=new ArrayList<>();
	int goal=6;
	ArenaSetup arenasetup=new ArenaSetup(this);

	/** Constructor. */
	public ArenaFight(Combatants gladiatorsp){
		gladiators=gladiatorsp;
		meld=true;
		weather=Weather.DRY;
		period=Javelin.PERIODNOON;
		setup=arenasetup;
		meld=false;
		canflee=false;
		endless=true;
	}

	@Override
	public ArrayList<Combatant> generate(){
		return new ArrayList<>();
	}

	@Override
	public ArrayList<Combatant> getblueteam(){
		return new ArrayList<>(gladiators);
	}

	@Override
	public ArrayList<Item> getbag(Combatant c){
		ArrayList<Item> bag=items.get(c.id);
		if(bag==null){
			bag=new ArrayList<>();
			items.put(c.id,bag);
		}
		return bag;
	}

	@Override
	public ArrayList<Combatant> getfoes(Integer teamel){
		return null;
	}

	@Override
	public void startturn(Combatant acting){
		super.startturn(acting);
		for(ArrayList<Combatant> group:new ArrayList<>(foes))
			rewardxp(group);
		awaken();
		if(!state.blueTeam.contains(state.next)) return;
		if(acting.ap<check) if(getopponents().isEmpty())
			reward(state.dead);
		else
			return;
		int elblue=ChallengeCalculator.calculateel(getgladiators());
		int elred=ChallengeCalculator.calculateel(getopponents());
		if(getopponents().isEmpty()||elred-elblue<tension){
			raisetension(elblue);
			tension=RPG.r(TENSIONMIN,TENSIONMAX);
			reward(state.dead);
		}
		check=acting.ap+RPG.r(10,40)/10f;
	}

	ArrayList<Combatant> getopponents(){
		ArrayList<Combatant> opponents=new ArrayList<>(state.redTeam);
		opponents.removeAll(getflagpoles());
		return opponents;
	}

	void reward(ArrayList<Combatant> dead){
		ArrayList<Combatant> defeated=new ArrayList<>(dead.size());
		for(Combatant c:new ArrayList<>(dead))
			if(c.mercenary||c.summoned)
				dead.remove(c);
			else if(!gladiators.contains(c)){
				defeated.add(c);
				dead.remove(c);
				if(!c.summoned){
					Float cr=c.source.cr;
					gold+=RewardCalculator.getgold(cr)*BOOST;
				}
			}
	}

	void rewardxp(ArrayList<Combatant> group){
		for(Combatant foe:group)
			if(state.redTeam.contains(foe)) return;
		RewardCalculator.rewardxp(getallies(),group,BOOST);
		foes.remove(group);
	}

	/**
	 * @return Live gladiators.
	 *
	 * @see BattleState#dead
	 */
	public List<Combatant> getgladiators(){
		ArrayList<Combatant> gladiators=new ArrayList<>(this.gladiators.size());
		for(Combatant c:state.blueTeam)
			if(this.gladiators.contains(c)) gladiators.add(c);
		return gladiators;
	}

	void awaken(){
		for(Combatant c:new ArrayList<>(state.dead)){
			if(c.mercenary||!gladiators.contains(c)) continue;
			if(c.getnumericstatus()==Combatant.STATUSDEAD){
				state.dead.remove(c);
				gladiators.remove(c);
				continue;
			}
			if(c.ap>=state.next.ap) continue;
			c.ap+=1;
			if(state.getcombatant(c.location[0],c.location[1])!=null) continue;
			if(RPG.r(1,10)>Math.abs(c.hp)){
				state.dead.remove(c);
				c.hp=1;
				state.blueTeam.add(c);
				notify(c+" awakens!",c.getlocation());
			}
		}
	}

	void raisetension(int elblue){
		if(placefoes(elblue)!=null) refillfountains();
	}

	void refillfountains(){
		ArrayList<ArenaFountain> fountains=ArenaFountain.get();
		if(check==-Float.MAX_VALUE||fountains.isEmpty()) return;
		int refilled=0;
		Point p=null;
		for(ArenaFountain f:fountains)
			if(f.spent&&RPG.random()<f.refillchance){
				f.setspent(false);
				refilled+=1;
				p=f.getlocation();
			}
		if(refilled>0) notify(refilled+" fountain(s) refilled!",p);
	}

	Integer placefoes(int elblue){
		if(getflagpoles().isEmpty()) return null;
		ArrayList<Combatant> last=null;
		int min=Math.max(elblue+ELMIN,baseline);
		baseline=min;
		ArrayList<Combatant> opponents=new ArrayList<>();
		for(Combatant c:getopponents())
			if(!c.summoned) opponents.add(c);
		for(int el=min;el<=elblue+ELMAX;el+=1){
			ArrayList<Combatant> group=generatefoes(el);
			if(group==null) continue;
			ArrayList<Combatant> newteam=new ArrayList<>(opponents);
			newteam.addAll(group);
			int tension=ChallengeCalculator.calculateel(newteam)-elblue;
			if(tension==this.tension){
				enter(group,state.redTeam);
				return el;
			}
			if(tension>this.tension){
				enter(last==null?group:last,state.redTeam);
				return el;
			}
			last=group;
		}
		return null;
	}

	ArrayList<ArenaGateway> getflagpoles(){
		ArrayList<ArenaGateway> flags=new ArrayList<>(4);
		for(Combatant c:state.redTeam)
			if(c instanceof ArenaGateway) flags.add((ArenaGateway)c);
		flags.sort((a,b)->a.level-b.level);
		return flags;
	}

	ArrayList<Combatant> generatefoes(int el){
		try{
			return EncounterGenerator.generate(el,Arrays.asList(Terrain.ALL));
		}catch(GaveUp e){
			return null;
		}
	}

	public void enter(ArrayList<Combatant> group,ArrayList<Combatant> team){
		Point entrance=null;
		ArrayList<ArenaGateway> flags=getflagpoles();
		while(entrance==null||!ArenaSetup.validate(entrance))
			entrance=displace(RPG.pick(flags).getlocation());
		enter(group,team,entrance);
	}

	float getbaseap(){
		float ap=0;
		List<Combatant> gladiators=getgladiators();
		for(Combatant c:gladiators)
			ap+=c.ap;
		return ap/gladiators.size();
	}

	void enter(ArrayList<Combatant> entering,List<Combatant> team,Point entry){
		if(team==state.redTeam){
			if(!SPAWN) return;
			foes.add(entering);
		}
		LinkedList<Combatant> place=new LinkedList<>(entering);
		Collections.shuffle(place);
		Combatant last=place.pop();
		last.setlocation(entry);
		float ap=getbaseap();
		if(!team.contains(last)){
			team.addAll(entering);
			for(Combatant c:entering){
				c.rollinitiative();
				if(check!=-Float.MAX_VALUE){
					c.ap+=ap;
					c.initialap=c.ap;
				}
			}
		}
		while(!place.isEmpty()){
			Point p=displace(last.getlocation());
			last=place.pop();
			last.setlocation(p);
		}
		if(team==state.redTeam){
			String msg="New enemies enter the arena:\n"+Combatant.group(entering)+"!";
			notify(msg,last.getlocation());
		}
	}

	public static Point displace(Point reference){
		Point p=null;
		while(p==null||!ArenaSetup.validate(p)){
			p=new Point(reference);
			p.x+=RPG.r(-1,+1)+RPG.randomize(2);
			p.y+=RPG.r(-1,+1)+RPG.randomize(2);
		}
		return p;
	}

	void notify(String text,Point p){
		BattleScreen.active.center(p.x,p.y);
		Javelin.redraw();
		Javelin.message(text,true);
		MessagePanel.active.clear();
	}

	@Override
	public void checkend(){
		if(getallies().isEmpty()){
			String loss="You've lost this match... better luck next time!";
			Javelin.message(loss,true);
			throw new EndBattle();
		}
		if(state.redTeam.isEmpty()){
			Javelin.message("You have beaten the arena :D",true);
			throw new EndBattle();
		}
	}

	public static ArenaFight get(){
		return (ArenaFight)Javelin.app.fight;
	}

	List<Combatant> getallies(){
		ArrayList<Combatant> allies=new ArrayList<>();
		for(Combatant c:state.blueTeam)
			if(!c.source.passive) allies.add(c);
		return allies;
	}
}
