package javelin.controller.content.fight;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Debug;
import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.content.action.Withdraw;
import javelin.controller.content.fight.mutator.Friendly;
import javelin.controller.content.fight.mutator.Mutator;
import javelin.controller.content.fight.setup.BattleSetup;
import javelin.controller.content.map.Map;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.exception.battle.EndBattle;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.model.item.Item;
import javelin.model.state.BattleState;
import javelin.model.state.MeldCrystal;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.enchantment.compulsion.DominateMonster.Dominated;
import javelin.model.unit.skill.Diplomacy;
import javelin.model.world.Period;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.old.messagepanel.MessagePanel;
import javelin.view.screen.BattleScreen;

/**
 * A battle scenario.
 *
 * TODO use {@link Combatants}
 */
public abstract class Fight{
  static final String ESCAPE="Are you sure you want to escape?\n"
      +"Press ENTER to confirm or any other key to cancel...";
  /**
   * Average combat duration in rounds. This is a very tricky estimate, based on
   * lack of sources, full reliance on anecdotal evidence and high-level combat
   * being much more volatile (because of "whoever goes first wins" abilities).
   *
   * In my research, the one source I found for d20 proper suggested 4 rounds as
   * median.
   * https://paizo.com/threads/rzs2kgg1?On-average-how-many-rounds-does-combat-last
   *
   * A dozen or two discussions for other D&D editions and Pathfinder 2E boiled
   * down to a median of 4.
   *
   * D&D 5e is designed around 3-round combats, with a couple "multiply damage
   * by 3" guidelines that imply it in the DMG and MM.
   */
  public static final int COMBATLENGTH=4;

  /** Global fight state. */
  public static BattleState state=null;
  /** @see #win() */
  public static Boolean victory;
  /** Red team at the moment the {@link Fight} begins. */
  public static Combatants originalredteam;
  /** Blue team at the moment the {@link Fight} begins. */
  public static Combatants originalblueteam;
  /** Active fight or <code>null</code>. */
  public static Fight current=null;
  /** Last {@link #reward()} gold prize. */
  public static int gold;

  /** Map to generate. If <code>null</code>, choose from {@link #terrains}. */
  public Map map=null;
  /** If <code>false</code> will not reward experience points after victory. */
  public boolean rewardxp=true;
  /** If <code>false</code> will not reward gold after victory. */
  public boolean rewardgold=true;
  /** <code>true</code> to use {@link #avoid(List)}. */
  public boolean hide=true;
  /** <code>true</code> to enable {@link Diplomacy}. */
  public boolean bribe=true;
  /** If not <code>null</code> will override any other {@link Weather} level. */
  public Integer weather=Weather.current;
  /** Time of day / lightning level. */
  public Period period=Period.now();
  /** Delegates some setup details. */
  public BattleSetup setup=new BattleSetup();
  /** Wether {@link Withdraw} is enabled. */
  public boolean canflee=true;
  /** Custom combat rules. */
  public List<Mutator> mutators=new ArrayList<>(0);
  /** {@link #map} fallback. See {@link #getdefaultterrains()}. */
  public List<Terrain> terrains=getdefaultterrains();
  /** Set to <code>true</code> to mute {@link EndBattle#resolve()}. */
  public boolean skipresult=false;

  /** Will multiply gold reward. */
  protected double goldbonus=1;
  /** Will multiply experience reward. */
  protected double xpbonus=1;

  /**
   * @return An encounter level for which an appropriate challenge should be
   *   generated. May return <code>null</code> if the subclass will generate its
   *   own foes manually.
   *
   * @see ChallengeCalculator
   */
  public Integer getel(int teamel){
    return Terrain.current().getel(teamel);
  }

  /**
   * @param el Usually comes from {@link #getel(int)}, and so might be
   *   <code>null</code>.
   * @return The list of monsters that are going to be featured in this fight.
   *   If <code>null</code>, will then use {@link #getel(JavelinApp, int)}.
   */
  public ArrayList<Combatant> getfoes(Integer el){
    return generate(el,terrains);
  }

  /** Called in case of a successful bribe. */
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
    var rewards=new ArrayList<String>(3);
    rewards.add("Congratulations!");
    var r=Fight.originalredteam;
    if(rewardxp)
      rewards.add(RewardCalculator.rewardxp(Fight.originalblueteam,r,xpbonus));
    if(rewardgold){
      gold=RewardCalculator.receivegold(r);
      gold=Javelin.round(Math.max(gold*goldbonus,Squad.active.eat()/2));
      Squad.active.gold+=gold;
      rewards.add("Party receives $%s!".formatted(Javelin.format(gold)));
    }
    return String.join(" ",rewards)+"\n";
  }

  /**
   * Called when a battle ends but before {@link EndBattle} clean-ups.
   * {@link EndBattle#resolve()} is called by the default implementation, which
   * calls {@link #reward()}.
   *
   * @return If <code>true</code> will perform a bunch of post-battle clean-ups,
   *   usually required only for typical {@link Scenario} battles but not for
   *   {@link Minigame}s.
   */
  public boolean onend(){
    state.blueteam.addAll(state.getfleeing(Fight.originalblueteam));
    for(var m:mutators) m.end(this);
    EndBattle.resolve();
    for(var m:mutators) m.after(this);
    return true;
  }

  /** @throws EndBattle If this battle is over. */
  public void checkend(){
    for(var m:mutators) m.checkend(this);
    if(win()||state.blueteam.isEmpty()) throw new EndBattle();
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

  /** @return Opponents. */
  public Combatants generate(ArrayList<Combatant> blueteam){
    var el=getel(ChallengeCalculator.calculateel(blueteam));
    return new Combatants(getfoes(el));
  }

  /** @return Enemies that match the given EL, as much as possible. */
  static public Combatants generate(final int el,List<Terrain> terrains){
    var foes=EncounterGenerator.generate(el,terrains);
    for(var delta=1;foes==null;delta++){
      if(delta==20) throw new RuntimeException("Cannot generate fight!");
      foes=EncounterGenerator.generate(el-delta,terrains);
      if(foes==null) foes=EncounterGenerator.generate(el+delta,terrains);
    }
    return foes;
  }

  /** @return Default value of {@link #terrains}. */
  public ArrayList<Terrain> getdefaultterrains(){
    if(Dungeon.active!=null)
      return new ArrayList<>(List.of(Terrain.UNDERGROUND));
    var terrains=new ArrayList<>(List.of(Terrain.current()));
    if(flood()==Weather.STORM) terrains.add(Terrain.WATER);
    return terrains;
  }

  /** @return <code>true</code> if battle has been won. */
  public boolean win(){
    var r=state.redteam;
    return r.isEmpty()
        ||r.stream().allMatch(c->c.hascondition(Dominated.class)!=null);
  }

  /** @return Final {@link #weather} level for this fight. */
  public int flood(){
    if(weather!=null) return weather;
    if(map==null) return Weather.current;
    return Math.min(Weather.current,map.maxflooding);
  }

  /** @return Units that the player will fight with. */
  public ArrayList<Combatant> getblueteam(){
    return Squad.active.members;
  }

  /** @return Inventory for the given unit.. */
  public List<Item> getbag(Combatant c){
    return Squad.active.equipment.get(c);
  }

  /**
   * Setups {@link #state} and {@link BattleState#blueteam}.
   *
   * @return Opponent units.
   */
  public ArrayList<Combatant> setup(){
    for(var m:mutators) m.setup(this);
    if(Debug.period!=null) period=Period.ALL.stream()
        .filter(p->p.toString().equalsIgnoreCase(Debug.period)).findFirst()
        .orElseThrow();
    state=new BattleState(this);
    state.blueteam=new ArrayList<>(getblueteam());
    return generate(state.blueteam);
  }

  /**
   * Called when an unit reaches a {@link MeldCrystal}. Note that only human
   * units use this, computer units use {@link Combatant#meld()} directly.
   *
   * @param hero Meld collector.
   * @param meld2
   */
  public void meld(Combatant hero,MeldCrystal m){
    Javelin.message(hero+" powers up!",Javelin.Delay.BLOCK);
    hero.meld();
    state.meld.remove(m);
  }

  /** @param c Fleeing unit. */
  public void withdraw(Combatant c){
    if(!canflee){
      Javelin.message("Cannot flee!",Javelin.Delay.BLOCK);
      BattleScreen.active.block();
      throw new RepeatTurn();
    }
    if(Javelin.DEBUG) withdrawall(true);
    if(has(Friendly.class)==null&&state.isengaged(c)){
      Javelin.prompt("Disengage first!");
      throw new RepeatTurn();
    }
    Javelin.message(ESCAPE,Javelin.Delay.NONE);
    if(Javelin.input().getKeyChar()!='\n') throw new RepeatTurn();
    c.escape(state);
    throw state.blueteam.isEmpty()?new EndBattle():new RepeatTurn();
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
    for(var c:new ArrayList<>(state.blueteam)) c.escape(state);
    throw new EndBattle();
  }

  /**
   * Called when an unit becomes {@link Combatant#STATUSDEAD} or
   * {@link Combatant#STATUSUNCONSCIOUS}.
   */
  public void die(Combatant c,BattleState s){
    for(var m:mutators) m.die(c,s,this);
    s.remove(c);
    s.dead.add(c);
  }

  /**
   * @param exclude A list of Combatants to ignore, may be <code>null</code>.
   * @return The average {@link Combatant#ap} for all units in the fight.
   */
  protected float getaverageap(List<Combatant> exclude){
    var combatants=state.getcombatants();
    if(exclude!=null) combatants.removeAll(exclude);
    return combatants.stream().collect(Collectors.averagingDouble(c->c.ap))
        .floatValue();
  }

  /** @return Mutator instance if currently active or <code>null</code>. */
  @SuppressWarnings("unchecked")
  public <K extends Mutator> K has(Class<K> type){
    for(var m:mutators) if(type.isInstance(m)) return (K)m;
    return null;
  }

  /**
   * @param units Add these units...
   * @param team to this team... (if {@link BattleState#redteam} will also add
   *   to {@link #originalredteam so XP is awarded properly}
   * @param spawn and place each of them at one of these points.
   * @param f Current fight.
   */
  public void add(Combatants units,List<Combatant> team,List<Point> spawn){
    state.next();
    for(var c:units) c.rollinitiative(state.next.ap);
    team.addAll(units);
    if(team==state.redteam) originalredteam.addAll(units);
    setup.place(units,spawn);
  }

  /**
   * Same as {@link #add(Combatants, List, List, Fight)} but uses default spawn
   * points.
   *
   * @see javelin.controller.content.map.Map#getspawn(List)
   */
  public void add(Combatants units,List<Combatant> team){
    add(units,team,map.getspawn(team));
  }

  /**
   * Flees with all {@link BattleState#blueteam} {@link Combatant}s and
   * {@link EndBattle}.
   *
   * @param rescue If <code>true</code>, any fallen allies will also be returned
   *   to safety.
   */
  public void flee(boolean rescue){
    if(rescue){
      var rescued=new Combatants(state.dead);
      rescued.retainAll(originalblueteam);
      state.fleeing.addAll(rescued);
    }
    state.fleeing.addAll(state.blueteam);
    state.blueteam.clear();
    victory=false;
    throw new EndBattle();
  }
}
