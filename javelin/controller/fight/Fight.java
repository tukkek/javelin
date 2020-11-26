package javelin.controller.fight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Debug;
import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.action.Action;
import javelin.controller.action.Withdraw;
import javelin.controller.ai.BattleAi;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.exception.battle.EndBattle;
import javelin.controller.fight.mutator.Friendly;
import javelin.controller.fight.mutator.Mutator;
import javelin.controller.fight.setup.BattleSetup;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.map.Map;
import javelin.controller.scenario.Scenario;
import javelin.controller.terrain.Terrain;
import javelin.controller.terrain.Underground;
import javelin.controller.terrain.Water;
import javelin.model.item.Item;
import javelin.model.state.BattleState;
import javelin.model.state.MeldCrystal;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.enchantment.compulsion.DominateMonster.Dominated;
import javelin.model.unit.skill.Diplomacy;
import javelin.model.world.Period;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.old.messagepanel.MessagePanel;
import javelin.view.screen.BattleScreen;

/**
 * A battle scenario.
 */
public abstract class Fight{
	/** Global fight state. */
	public static BattleState state=null;
	/** See {@link #win(BattleScreen)}. */
	public static Boolean victory;
	/** Red team at the moment the {@link Fight} begins. */
	public static Combatants originalredteam;
	/** Blue team at the moment the {@link Fight} begins. */
	public static Combatants originalblueteam;

	/**
	 * Map this battle is to happen on or <code>null</code> for one to be
	 * generated according to current tile's terrain.
	 */
	public Map map=null;
	/**
	 * If <code>false</code> will not reward experience points after victory.
	 */
	public boolean rewardxp=true;
	/**
	 * If <code>false</code> will not reward gold after victory.
	 */
	public boolean rewardgold=true;
	/**
	 * <code>true</code> if there is a chance for the {@link Squad} to hide and
	 * avoid this combat. This doesn't make sense for {@link Siege}s for example
	 * since they are actually engaging the enemy.
	 */
	public boolean hide=true;
	/**
	 * <code>true</code> if this fight is susceptible to {@link Diplomacy}.
	 */
	public boolean bribe=true;
	/** If not <code>null</code> will override any other {@link Weather} level. */
	public Integer weather=Weather.current;
	/** Time of day / lightning level. */
	public Period period=Period.now();
	/** Delegates some setup details.TODO */
	public BattleSetup setup=new BattleSetup();
	/** Wheter {@link Withdraw} is enabled. */
	public boolean canflee=true;
	/** Custom combat rules. */
	public List<Mutator> mutators=new ArrayList<>(0);
	/** If not <code>null</code> will use this when choosing a {@link Map}. */
	public List<Terrain> terrains=getdefaultterrains();

	/**
	 * @return an encounter level for which an appropriate challenge should be
	 *         generated. May return <code>null</code> if the subclass will
	 *         generate its own foes manually.
	 *
	 * @see ChallengeCalculator
	 */
	public Integer getel(int teamel){
		return Terrain.current().getel(teamel);
	}

	/**
	 * @param el usually comes from {@link #getel(int)}, and so might be
	 *          <code>null</code>.
	 * @return The list of monsters that are going to be featured in this fight.
	 *         If <code>null</code>, will then use
	 *         {@link #getel(JavelinApp, int)}.
	 */
	public ArrayList<Combatant> getfoes(Integer el){
		return generate(el,terrains);
	}

	/**
	 * Called in case of a successful bribe.
	 */
	public void bribe(){
		if(Javelin.DEBUG&&!bribe)
			throw new RuntimeException("Cannot bribe this fight! "+getClass());
	}

	/** @return Amount of gold to reward party. */
	protected int getgoldreward(List<Combatant> defeated){
		var gold=RewardCalculator.receivegold(defeated);
		return Javelin.round(Math.max(gold,Squad.active.eat()/2));
	}

	/**
	 * Only called on victory.
	 *
	 * @return Reward description.
	 */
	public String reward(){
		List<Combatant> defeated=new ArrayList<>(Fight.originalredteam);
		defeated.removeAll(Fight.state.fleeing);
		if(defeated.isEmpty()) return "All enemies have fled...";
		/* should at least serve as food for 1 day */
		String rewards="Congratulations! ";
		if(rewardxp)
			rewards+=RewardCalculator.rewardxp(Fight.originalblueteam,defeated,1);
		if(rewardgold){
			var gold=getgoldreward(defeated);
			Squad.active.gold+=gold;
			rewards+=" Party receives $"+Javelin.format(gold)+"!";
		}
		return rewards+"\n";
	}

	/**
	 * Called when a battle ends but before {@link EndBattle} clean-ups.
	 * Fight{@link #reward()} is called by the default implementation.
	 *
	 * @param screen Currently open screen.
	 * @param originalTeam Team state before the battle started.
	 * @param s Final battle state.
	 * @param combatresult Description of rewards and other remarks such as
	 *          vehicle/allies left behind...
	 *
	 * @return If <code>true</code> will perform a bunch of post-battle clean-ups,
	 *         usually required only for typical {@link Scenario} battles but not
	 *         for {@link Minigame}s.
	 */
	public boolean onend(){
		state.blueTeam.addAll(state.getfleeing(Fight.originalblueteam));
		for(var m:mutators)
			m.end(this);
		EndBattle.showcombatresult();
		return true;
	}

	/**
	 * @param screen Active screen.
	 * @throws EndBattle If this battle is over.
	 */
	public void checkend(){
		for(var m:mutators)
			m.checkend(this);
		if(win()||Fight.state.blueTeam.isEmpty()) throw new EndBattle();
	}

	/**
	 * @return <code>true</code> if there are any active enemies here.
	 */
	public boolean checkforenemies(){
		for(Combatant c:Fight.state.redTeam)
			if(c.hascondition(Dominated.class)==null) return true;
		return false;
	}

	/**
	 * @param foes List of enemies.
	 * @return <code>true</code> if this battle has been avoided.
	 */
	public boolean avoid(List<Combatant> foes){
		if(hide&&Squad.active.hide(foes)) return true;
		if(bribe&&Squad.active.bribe(foes)){
			bribe();
			return true;
		}
		return false;
	}

	/**
	 * @param foes Gives an opportunity to alter the generated enemies.
	 */
	public void enhance(List<Combatant> foes){
		// nothing by default
	}

	/**
	 * @param waveel Target encounter level for the fight. Taken as a guideline
	 *          because given {@link Terrain} and such a fight cannot be generated
	 *          for this exact level.
	 * @param terrains Terrain this fight takes place on.
	 * @return The resulting opponents.
	 */
	public ArrayList<Combatant> generate(){
		var el=getel(ChallengeCalculator.calculateel(Fight.state.blueTeam));
		var foes=getfoes(el);
		enhance(foes);
		return foes;
	}

	/**
	 * @param el Encounter level.
	 * @param terrains Possible {@link Monster} terrains.
	 * @return A group of enemies that match the given EL as much as possible.
	 */
	static public Combatants generate(final int el,List<Terrain> terrains){
		var foes=chooseopponents(el,terrains);
		for(var delta=1;foes==null;delta++){
			if(delta==20) throw new RuntimeException("Cannot generate fight!");
			foes=chooseopponents(el-delta,terrains);
			if(foes==null) foes=chooseopponents(el+delta,terrains);
		}
		return foes;
	}

	static Combatants chooseopponents(final int el,List<Terrain> terrains){
		return EncounterGenerator.generate(el,terrains);
	}

	/**
	 * @return <code>false</code> if any of these {@link Combatant}s are not
	 *         supposed to be in this fight.
	 * @see TempleEncounter
	 */
	public boolean validate(ArrayList<Combatant> encounter){
		return true;
	}

	/**
	 * Default implementation of {@link #getterrains(Terrain)}.
	 *
	 * @param t Will return this (alongside {@link Water} if enough flood
	 *          level)...
	 * @return or {@link Underground} if there is inside a {@link DungeonFloor}.
	 *
	 * @see Weather#flood(BattleMap, int)
	 * @see Map#maxflooding
	 * @see Dungeon#active
	 */
	public ArrayList<Terrain> getdefaultterrains(){
		if(Dungeon.active!=null)
			return new ArrayList<>(List.of(Terrain.UNDERGROUND));
		var terrains=new ArrayList<>(List.of(Terrain.current()));
		if(flood()==Weather.STORM) terrains.add(Terrain.WATER);
		return terrains;
	}

	/**
	 * @return <code>true</code> if battle has been won.
	 */
	public boolean win(){
		return Fight.state.redTeam.isEmpty()||!checkforenemies();
	}

	/**
	 * @return The {@link #weather} level for this fight, taking into account all
	 *         factors.
	 */
	public int flood(){
		if(weather!=null) return weather;
		if(map==null) return Weather.current;
		return Math.min(Weather.current,map.maxflooding);
	}

	/**
	 * @return Team that the player will fight with.
	 */
	public ArrayList<Combatant> getblueteam(){
		return Squad.active.members;
	}

	/**
	 * @return Inventory for the given unit..
	 */
	public List<Item> getbag(Combatant combatant){
		return Squad.active.equipment.get(combatant);
	}

	/** @see #onready */
	public final void ready(){
		for(var m:mutators)
			m.ready(this);
	}

	/**
	 * Setups {@link #state} and {@link BattleState#blueTeam}.
	 *
	 * @return Opponent units.
	 */
	public ArrayList<Combatant> setup(){
		if(Debug.period!=null) period=Period.ALL.stream()
				.filter(p->p.toString().equalsIgnoreCase(Debug.period)).findFirst()
				.orElseThrow();
		Fight.state=new BattleState(this);
		Fight.state.blueTeam=new ArrayList<>(getblueteam());
		for(var m:mutators)
			m.setup(this);
		return generate();
	}

	/**
	 * Called after a unit completes an {@link Action}.
	 */
	public void endturn(){
		for(var m:mutators)
			m.endturn(this);
	}

	/**
	 * Called when an unit reaches {@link MeldCrystal}. Note that only human units
	 * use this, computer units use {@link Combatant#meld()} directly.
	 *
	 * @param hero Meld collector.
	 * @param meld2
	 */
	public void meld(Combatant hero,MeldCrystal m){
		Javelin.message(hero+" powers up!",Javelin.Delay.BLOCK);
		hero.meld();
		Fight.state.meld.remove(m);
	}

	/**
	 * TODO probablby better to just have flee=true/false in Fight.
	 *
	 * @param combatant Fleeing unit.
	 * @param screen Active screen.
	 */
	public void withdraw(Combatant combatant,BattleScreen screen){
		if(!canflee){
			Javelin.message("Cannot flee!",Javelin.Delay.BLOCK);
			BattleScreen.active.block();
			throw new RepeatTurn();
		}
		if(Javelin.DEBUG) withdrawall(true);
		if(has(Friendly.class)==null&&Fight.state.isengaged(combatant)){
			Javelin.prompt("Disengage first!");
			throw new RepeatTurn();
		}
		var prompt="Are you sure you want to escape? Press ENTER to confirm...\n";
		Javelin.message(prompt,Javelin.Delay.NONE);
		if(Javelin.input().getKeyChar()!='\n') throw new RepeatTurn();
		combatant.escape(Fight.state);
		throw Fight.state.blueTeam.isEmpty()?new EndBattle():new RepeatTurn();
	}

	/**
	 * Removes all {@link Combatant}s from a {@link Fight}. Intended for
	 * debugging.
	 *
	 * @param prompt If <code>true</code>, will ask for user confirmation.
	 * @throws EndBattle
	 */
	public static void withdrawall(boolean prompt){
		if(prompt){
			var message="Press w to cancel battle... (debug feature)";
			if(Javelin.prompt(message)!='w'){
				MessagePanel.active.clear();
				return;
			}
		}
		for(var c:new ArrayList<>(Fight.state.blueTeam))
			c.escape(Fight.state);
		throw new EndBattle();
	}

	/**
	 * Called before a unit acts, human or computer. Called from controller code,
	 * not from {@link BattleAi} routines.
	 *
	 * @param acting Creature to perform this turn (human or AI).
	 */
	public void startturn(Combatant acting){
		// nothing
	}

	public void die(Combatant c,BattleState s){
		for(var m:mutators)
			m.die(c,s,this);
		s.remove(c);
		s.dead.add(c);
	}

	/**
	 * @param exclude A list of Combatants to ignore, may be <code>null</code>.
	 * @return The average {@link Combatant#ap} for all units in the fight.
	 */
	protected float getaverageap(List<Combatant> exclude){
		List<Combatant> combatants=state.getcombatants();
		if(exclude!=null) combatants.removeAll(exclude);
		return combatants.stream().collect(Collectors.averagingDouble(c->c.ap))
				.floatValue();
	}

	public void enter(List<Combatant> entering,List<Combatant> team,Point entry){
		if(entering.isEmpty()) return;
		var s=Fight.state;
		while(!entry.validate(0,0,s.map.length,s.map.length)
				||s.isblocked(entry.x,entry.y))
			entry=displace(entry);
		LinkedList<Combatant> place=new LinkedList<>(entering);
		Collections.shuffle(place);
		Combatant last=place.pop();
		last.setlocation(entry);
		float ap=getaverageap(null);
		if(!team.contains(last)){
			team.addAll(entering);
			for(Combatant c:entering)
				c.rollinitiative(ap);
		}
		while(!place.isEmpty()){
			Point p=displace(last.getlocation());
			last=place.pop();
			last.setlocation(p);
		}
	}

	static Point displace(Point reference){
		Point p=new Point(reference);
		while(!p.validate(0,0,state.map.length,state.map[0].length)
				||state.isblocked(p.x,p.y))
			p.displace();
		return p;
	}

	/** @return Mutator instance if currenclty active. */
	public Mutator has(Class<? extends Mutator> type){
		for(var m:mutators)
			if(type.isInstance(m)) return m;
		return null;
	}
}
