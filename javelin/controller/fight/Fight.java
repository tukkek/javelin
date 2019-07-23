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
import javelin.controller.action.world.WorldMove;
import javelin.controller.ai.BattleAi;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.exception.GaveUp;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.exception.battle.EndBattle;
import javelin.controller.fight.minigame.Minigame;
import javelin.controller.fight.setup.BattleSetup;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.map.Map;
import javelin.controller.scenario.Scenario;
import javelin.controller.terrain.Terrain;
import javelin.controller.terrain.Underground;
import javelin.controller.terrain.Water;
import javelin.model.item.Item;
import javelin.model.state.BattleState;
import javelin.model.state.Meld;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.enchantment.compulsion.DominateMonster.Dominated;
import javelin.model.unit.skill.Diplomacy;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.old.RPG;
import javelin.old.messagepanel.MessagePanel;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.InfoScreen;

/**
 * A battle scenario.
 */
public abstract class Fight{
	/** Global fight state. */
	public static BattleState state=null;
	/** See {@link #win(BattleScreen)}. */
	public static Boolean victory;
	/**
	 * @return <code>true</code> if {@link Meld} should be generated.
	 */
	public boolean meld=false;
	/**
	 * Map this battle is to happen on or <code>null</code> for one to be
	 * generated according to current tile's terrain.
	 */
	public Map map=null;
	/**
	 * If <code>true</code> will remove opponents at first sign of blood instead
	 * of at negative hit points.
	 */
	public boolean friendly=false;
	/**
	 * If <code>false</code> will not reward experience points after victory.
	 */
	public boolean rewardxp=true;
	/**
	 * If <code>false</code> will not reward gold after victory.
	 */
	public boolean rewardgold=true;
	/**
	 * If <code>true</code> will reward reputation according to
	 * {@link Difficulty}.
	 */
	public boolean rewardreputation=false;
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
	/** If not <code>null</code> will use this terrain when generating a map. */
	public Terrain terrain=null;
	/**
	 * If not <code>null</code> will override any other flooding level.
	 *
	 * TODO should set from {@link Weather#current} and then allow it to be
	 * overriden.
	 *
	 * @see Weather#current
	 * @see Map#maxflooding
	 */
	public Integer weather=Weather.current;
	/**
	 * Since {@link Squad#time} is always ticking and needs to be updated even
	 * when fights do happen this by default holds the period at the moment of
	 * instantiation, so we can be more faithful to what appears on screen instead
	 * of the period after the {@link WorldMove} or similar has been completed.
	 *
	 * @see Javelin#getperiod()
	 */
	public String period=Javelin.getperiod();
	/** Status to remove {@link Combatant} from a {@link #friendly} battle. */
	public int friendlylevel=Combatant.STATUSWOUNDED;
	/** Delegates some setup details.TODO */
	public BattleSetup setup=new BattleSetup();
	public boolean denydarkvision=false;
	public boolean canflee=true;
	public boolean endless=false;
	/** Red team at the moment the {@link Fight} begins. */
	public static Combatants originalredteam;
	/** Blue team at the moment the {@link Fight} begins. */
	public static Combatants originalblueteam;
	/**
	 * These callbacks are called at the last opportunity for changing this fight
	 * before actual battle begins. At this point the entire stack should be
	 * setup.
	 *
	 * @see BattleSetup
	 */
	public List<Runnable> onready=new ArrayList<>(0);
	/**
	 * Called after {@value #originalblueteam} and {@value #originalredteam} team
	 * are set but before they are placed, allowing for temporary combatants to be
	 * included.
	 */
	public List<Runnable> onprepare=new ArrayList<>(0);

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
	 * @param teamel usually comes from {@link #getel(int)}, and so might be
	 *          <code>null</code>.
	 *
	 * @return The list of monsters that are going to be featured in this fight.
	 *         If <code>null</code>, will then use
	 *         {@link #getel(JavelinApp, int)}.
	 */
	public abstract ArrayList<Combatant> getfoes(Integer teamel);

	/**
	 * Called in case of a successful bribe.
	 */
	public void bribe(){
		if(Javelin.DEBUG&&!bribe)
			throw new RuntimeException("Cannot bribe this fight! "+getClass());
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
		final int gold=RewardCalculator.receivegold(defeated);
		final float food=Squad.active.eat()/2;
		/* should at least serve as food for 1 day */
		final int bonus=Javelin.round(Math.round(Math.max(food,gold)));
		String rewards="Congratulations! ";
		if(rewardxp)
			rewards+=RewardCalculator.rewardxp(Fight.originalblueteam,defeated,1);
		if(rewardgold){
			Squad.active.gold+=bonus;
			rewards+=" Party receives $"+Javelin.format(bonus)+"!";
		}
		var d=javelin.model.diplomacy.Diplomacy.instance;
		if(rewardreputation&&d!=null){
			var blueel=ChallengeCalculator.calculateel(Fight.originalblueteam);
			var redel=ChallengeCalculator.calculateel(Fight.originalredteam);
			var fightel=getel(blueel);
			if(fightel!=null&&fightel>redel) redel=fightel;
			var reputation=redel-blueel-Difficulty.MODERATE;
			if(reputation>0){
				d.reputation+=reputation+RPG.randomize(reputation);
				rewards+=" You gain "+reputation+" reputation!";
			}
		}
		return rewards+"\n";
	}

	/**
	 * Called when a battle ends but before {@link EndBattle} clean-ups.
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
		for(Combatant c:state.getfleeing(Fight.originalblueteam))
			state.blueTeam.add(c);
		EndBattle.showcombatresult();
		return true;
	}

	/**
	 * @param screen Active screen.
	 * @throws EndBattle If this battle is over.
	 */
	public void checkend(){
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
	 * @param el Target encounter level for the fight. Taken as a guideline
	 *          because given {@link Terrain} and such a fight cannot be generated
	 *          for this exact level.
	 * @param terrain Terrain this fight takes place on.
	 * @return The resulting opponents.
	 */
	public ArrayList<Combatant> generate(){
		Integer blueel=getel(ChallengeCalculator.calculateel(Fight.state.blueTeam));
		ArrayList<Terrain> terrains=getterrains();
		ArrayList<Combatant> foes=getfoes(blueel);
		if(foes==null) foes=generate(blueel,terrains);
		enhance(foes);
		return foes;
	}

	/**
	 * @param el Encounter level.
	 * @param terrains Possible {@link Monster} terrains.
	 * @return A group of enemies that closely match the given EL, as far as
	 *         possible.
	 */
	static public ArrayList<Combatant> generate(final int el,
			ArrayList<Terrain> terrains){
		int delta=0;
		ArrayList<Combatant> generated=null;
		while(generated==null){
			generated=chooseopponents(el-delta,terrains);
			if(generated!=null) break;
			if(delta!=0) generated=chooseopponents(el+delta,terrains);
			delta+=1;
		}
		return generated;
	}

	static ArrayList<Combatant> chooseopponents(final int el,
			ArrayList<Terrain> terrains){
		try{
			return EncounterGenerator.generate(el,terrains);
		}catch(final GaveUp e){
			return null;
		}
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
	 * @param town Terrain hint. Usually {@link Terrain#current()}.
	 * @return A list of {@link Terrain}s which foes in this fight can inhabit.
	 */
	public ArrayList<Terrain> getterrains(){
		return getdefaultterrains(Terrain.current(),flood());
	}

	/**
	 * Default implementation of {@link #getterrains(Terrain)}.
	 *
	 * @param t Will return this (alongside {@link Water} if enough flood
	 *          level)...
	 * @return or {@link Underground} if there is inside a {@link Dungeon}.
	 *
	 * @see Weather#flood(BattleMap, int)
	 * @see Map#maxflooding
	 * @see Dungeon#active
	 */
	public static ArrayList<Terrain> getdefaultterrains(Terrain t,int floodlevel){
		ArrayList<Terrain> terrains=new ArrayList<>();
		if(Dungeon.active!=null){
			terrains.add(Terrain.UNDERGROUND);
			return terrains;
		}
		terrains.add(t);
		if(floodlevel==Weather.STORM) terrains.add(Terrain.WATER);
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
		for(var r:onready)
			r.run();
	}

	/**
	 * Setups {@link #state} and {@link BattleState#blueTeam}.
	 *
	 * @return Opponent units.
	 */
	public ArrayList<Combatant> setup(){
		if(Debug.period!=null) period=Debug.period;
		Fight.state=new BattleState(this);
		Fight.state.blueTeam=getblueteam();
		return generate();
	}

	/**
	 * Called after a unit completes an {@link Action}.
	 */
	public void endturn(){
		if(friendly){
			BattleState s=Fight.state;
			int ncombatants=s.blueTeam.size()+s.redTeam.size();
			cleanwounded(s.blueTeam,s);
			cleanwounded(s.redTeam,s);
			if(s.blueTeam.size()+s.redTeam.size()<ncombatants){
				Fight.state=s;
				Javelin.redraw();
			}
		}
	}

	void cleanwounded(ArrayList<Combatant> team,BattleState s){
		for(Combatant c:(List<Combatant>)team.clone()){
			if(c.getnumericstatus()>friendlylevel) continue;
			if(team==s.blueTeam) s.fleeing.add(c);
			team.remove(c);
			if(s.next==c) s.next();
			addmeld(c.location[0],c.location[1],c,s);
			Javelin.message(
					c+" is removed from the battlefield!\n"+"Press ENTER to continue...",
					Javelin.Delay.NONE);
			while(Javelin.input().getKeyChar()!='\n'){
				// wait for enter
			}
			MessagePanel.active.clear();
		}
	}

	/**
	 * Called when an unit reaches {@link Meld}. Note that only human units use
	 * this, computer units use {@link Combatant#meld()} directly.
	 *
	 * @param hero Meld collector.
	 * @param meld2
	 */
	public void meld(Combatant hero,Meld m){
		Javelin.message(hero+" powers up!",Javelin.Delay.BLOCK);
		hero.meld();
		Fight.state.meld.remove(m);
	}

	/**
	 * @param x Meld location.
	 * @param y Meld location.
	 * @param dead Unit that died, triggering {@link Meld} creation.
	 * @param s Current battle state.
	 * @return Created meld.
	 */
	public Meld addmeld(int x,int y,Combatant dead,BattleState s){
		if(dead.summoned||dead.getnumericstatus()!=Combatant.STATUSDEAD
				||!dead.source.isalive())
			return null;
		Meld m=new Meld(x,y,s.next.ap+1,dead);
		s.meld.add(m);
		return m;
	}

	/**
	 * Called after painting the {@link BattleScreen} for the first time.
	 */
	public void draw(){
		// nothing by default
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
		if(Fight.state.isengaged(combatant)){
			Javelin.message("Disengage first!",Javelin.Delay.BLOCK);
			InfoScreen.feedback();
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
		if(meld||Meld.DEBUG) addmeld(c.location[0],c.location[1],c,s);
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
}
