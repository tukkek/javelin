package javelin.controller.fight.minigame.arena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.challenge.TensionDirector;
import javelin.controller.challenge.TensionDirector.TensionAction;
import javelin.controller.exception.battle.EndBattle;
import javelin.controller.fight.minigame.Minigame;
import javelin.controller.fight.minigame.arena.building.ArenaFlagpole;
import javelin.controller.fight.minigame.arena.building.ArenaFountain;
import javelin.controller.fight.minigame.arena.building.ArenaTown;
import javelin.controller.map.Stadium;
import javelin.controller.scenario.Campaign;
import javelin.model.item.Item;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.old.RPG;
import javelin.old.messagepanel.MessagePanel;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.SquadScreen;

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
 * @see Stadium
 *
 * @author alex
 */
public class Arena extends Minigame{
	static final double GLADIATORMAXCR=SquadScreen.SELECTABLE[SquadScreen.SELECTABLE.length
			-1];
	static final float SQUADEL=Campaign.INITIALEL;
	public static final float BOOST=4; // 3,5 maybe?
	static final boolean SPAWN=true;

	/** {@link Item} bag for {@link #gladiators}. */
	public HashMap<Integer,ArrayList<Item>> items=new HashMap<>();
	public int gold=0;

	TensionDirector director=new TensionDirector(Difficulty.EASY,
			Difficulty.DIFFICULT,true);
	ArrayList<List<Combatant>> foes=new ArrayList<>();
	/**
	 * Non-mercenary units, live and dead.
	 *
	 * @see #getgladiators()
	 */
	Combatants gladiators;

	/** Constructor. */
	public Arena(){
		weather=Weather.DRY;
		period=Javelin.PERIODNOON;
		setup=new ArenaSetup(this);
		meld=false;
		canflee=false;
		endless=true;
	}

	Combatants choosegladiators(){
		List<Monster> candidates=Javelin.ALLMONSTERS.stream()
				.filter(m->!m.internal&&m.cr<=GLADIATORMAXCR)
				.collect(Collectors.toList());
		Squad s=new SquadScreen(candidates){
			@Override
			protected boolean checkifsquadfull(){
				return ChallengeCalculator.calculateel(squad.members)>=SQUADEL;
			}
		}.open();
		return new Combatants(s.members);
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
	public List<Item> getbag(Combatant c){
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
		for(List<Combatant> group:new ArrayList<>(foes))
			rewardxp(group);
		for(Combatant c:new ArrayList<>(state.dead))
			if(gladiators.contains(c)) awaken(c);
		if(!state.blueTeam.contains(state.next)) return;
		List<Combatant> opponents=getopponents();
		if(opponents.isEmpty()) rewardgold();
		TensionAction tension=director.check(getallies(),opponents,acting.ap);
		if(tension==TensionAction.KEEP) return;
		rewardgold();
		spawn(tension);
		refillfountains();
	}

	void spawn(TensionAction tension){
		if(tension==TensionAction.RAISE){
			List<ArenaFlagpole> flags=ArenaFlagpole.getflags();
			if(!flags.isEmpty())
				enter(director.monsters,state.redTeam,RPG.pick(flags).getlocation());
		}else{
			ArenaTown town=ArenaTown.get();
			director.monsters.forEach(c->c.setmercenary(true));
			if(town!=null) enter(director.monsters,state.blueTeam,town.getlocation());
		}
	}

	public List<Combatant> getopponents(){
		return state.redTeam.stream().filter(c->!c.source.passive)
				.collect(Collectors.toList());
	}

	void rewardgold(){
		int gold=0;
		ArrayList<Combatant> dead=state.dead;
		for(Combatant c:new ArrayList<>(dead)){
			if(c.mercenary||c.summoned||c.source.passive){
				dead.remove(c);
				continue;
			}
			if(gladiators.contains(c)) continue;
			dead.remove(c);
			Float cr=c.source.cr;
			gold+=RewardCalculator.getgold(cr)*BOOST;
		}
		if(gold==0) return;
		gold=Javelin.round(gold);
		this.gold+=gold;
		ArenaTown town=ArenaTown.get();
		String project=town==null?""
				:"\nNext project is $"+town.getprojectprice()+".";
		Javelin.message("You have earned $"+Javelin.format(gold)+". You now have $"
				+Javelin.format(this.gold)+"."+project,false);
		BattleScreen.active.messagepanel.clear();
	}

	void rewardxp(List<Combatant> group){
		for(Combatant foe:group)
			if(state.redTeam.contains(foe)) return;
		List<Combatant> allies=getallies();
		RewardCalculator.rewardxp(allies,group,BOOST);
		foes.remove(group);
		String xps=allies.stream().filter(c->!c.mercenary)
				.map(c->c+" has "+c.gethumanxp()).collect(Collectors.joining(", "));
		if(xps.isEmpty()) return;
		Javelin.message("You gain experience!\n"+xps,false);
		BattleScreen.active.messagepanel.clear();
	}

	/**
	 * @return Live gladiators.
	 *
	 * @see BattleState#dead
	 */
	public List<Combatant> getgladiators(){
		return state.blueTeam.stream().filter(c->gladiators.contains(c))
				.collect(Collectors.toList());
	}

	void awaken(Combatant c){
		if(c.getnumericstatus()==Combatant.STATUSDEAD){
			state.dead.remove(c);
			gladiators.remove(c);
			return;
		}
		if(c.ap>=state.next.ap) return;
		c.ap+=1;
		Point p=c.getlocation();
		if(state.getcombatant(p.x,p.y)!=null||state.getmeld(p.x,p.y)!=null) return;
		if(RPG.r(1,10)<=Math.abs(c.hp)) return;
		c.hp=1;
		state.dead.remove(c);
		state.blueTeam.add(c);
		notify(c+" awakens!",c.getlocation());
	}

	void refillfountains(){
		ArrayList<ArenaFountain> fountains=ArenaFountain.get();
		if(fountains.isEmpty()) return;
		List<String> messages=new ArrayList<>(0);
		Point p=null;
		for(ArenaFountain f:fountains){
			if(!f.spent) continue;
			if(RPG.random()<f.refillchance){
				f.setspent(false);
				messages.add("A "+f.toString().toLowerCase()+" is replenished!");
				p=f.getlocation();
			}
		}
		if(!messages.isEmpty()) notify(String.join("\n",messages),p);
	}

	@Override
	public void enter(List<Combatant> entering,List<Combatant> team,Point entry){
		if(team==state.redTeam){
			if(Javelin.DEBUG&&!SPAWN) return;
			foes.add(entering);
		}
		super.enter(entering,team,entry);
		if(entering.get(0).equals(gladiators.get(0))) return;
		String title=team==state.redTeam?"Enemies":"Allies";
		String msg=title+" enter the arena:\n"+Combatant.group(entering)+"!";
		notify(msg,entering.get(0).getlocation());
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

	public static Arena get(){
		return (Arena)Javelin.app.fight;
	}

	public List<Combatant> getallies(){
		return state.blueTeam.stream().filter(c->!c.source.passive)
				.collect(Collectors.toList());
	}

	@Override
	public boolean start(){
		gladiators=choosegladiators();
		return super.start();
	}
}
