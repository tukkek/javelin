package javelin.model.world.location.unique;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.comparator.CombatantByCr;
import javelin.controller.generator.NpcGenerator;
import javelin.model.unit.Body;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.Period;
import javelin.model.world.World;
import javelin.model.world.location.Fortification;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Build;
import javelin.old.RPG;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.WorldScreen;

/**
 * The **Mercenaries guild** allows a player to hire mercenaries, which are paid
 * a certain amount in gold per day.
 *
 * @see Combatant#mercenary
 * @author alex
 */
public class MercenariesGuild extends Fortification{
  static final int STARTINGMERCENARIES=9;
  static final boolean DEBUG=false;

  /**
   * Builds a Mercenary Guild in a {@link Town}.
   *
   * @author alex
   */
  public static class BuildMercenariesGuild extends Build{
    /** Constructor. */
    public BuildMercenariesGuild(){
      super("Build mercenaries guild",15,Rank.TOWN,null);
    }

    @Override
    public Location getgoal(){
      return new MercenariesGuild();
    }
  }

  /** Available mercenaries. */
  public ArrayList<Combatant> mercenaries=new ArrayList<>();
  /** All mercenaries. */
  public ArrayList<Combatant> all=new ArrayList<>();

  /** Constructor. */
  public MercenariesGuild(){
    super("Mercenaries' Guild","Mercenaries' Guild",11,15);
    gossip=true;
    vision=3;
    while(mercenaries.size()<STARTINGMERCENARIES) generatemercenary();
    if(DEBUG) garrison.clear();
  }

  void generatemercenary(){
    var cr=RPG.r(11,20);
    List<Monster> candidates=new ArrayList<>();
    for(Float tier:Monster.BYCR.keySet())
      if(cr/2<=tier&&tier<cr) for(Monster m:Monster.BYCR.get(tier))
        if(m.think(-1)&&m.body.equals(Body.HUMANOID)) candidates.add(m);
    var m=RPG.pick(candidates);
    var c=NpcGenerator.generatenpc(m,cr);
    if(c==null){
      if(Javelin.DEBUG) System.out.println("Couldn't generate mercenary!");
      return;
    }
    c.setmercenary(true);
    mercenaries.add(c);
    all.add(c);
  }

  @Override
  public boolean interact(){
    if(!super.interact()) return false;
    ChallengeCalculator.updatecr(mercenaries);
    mercenaries.sort(Collections.reverseOrder(CombatantByCr.SINGLETON));
    var prices=new ArrayList<String>(mercenaries.size());
    for(Combatant c:mercenaries)
      prices.add(c+" ($"+Javelin.format(c.pay())+")");
    var index=Javelin.choose(
        "\"Welcome to the guild! Do you want to hire one of our mercenaries for a modest daily fee?\"\n\nYou have $"
            +Javelin.format(Squad.active.gold),
        prices,true,false);
    if(index==-1) return true;
    if(!recruit(mercenaries.get(index),true,true)) return false;
    mercenaries.remove(index);
    return true;
  }

  /**
   * Pays for the rest of the day and adds to active {@link Squad}. If cannot
   * pay warn the user.
   *
   * @param pay If <code>false</code> will not deduct {@link Squad#gold}.
   * @param message If <code>true</code> and doesn't have enough money to pay.
   *   will open up a {@link InfoScreen} to let the player know. Use
   *   <code>false</code> to warn in another manner.
   *
   * @return <code>false</code> if doesn't have enough money to pay in advance.
   */
  static public boolean recruit(Combatant c,boolean pay,boolean message){
    var advance=Period.gethour()*c.pay()/24;
    if(advance<1) advance=1;
    if(pay&&!Squad.active.pay(Math.round(advance))){
      if(message){
        var output="You don't have the money to pay today's advancement ($"
            +Javelin.format(advance)+")!";
        Javelin.app.switchScreen(new InfoScreen(output));
        Javelin.input();
      }
      return false;
    }
    c=c.clone().clonesource();
    c.setmercenary(true);
    Squad.active.add(c);
    return true;
  }

  /**
   * @return Daily fee for a mercenary, based on it's CR (single treasure
   *   value).
   */
  public static int getfee(Monster m){
    return Javelin.round(RewardCalculator.getgold(m.cr));
  }

  /**
   * @param returning Returns a mercenary to {@link #mercenaries}.
   */
  public void receive(Combatant returning){
    if(all.contains(returning)) mercenaries.add(returning);
  }

  @Override
  public List<Combatant> getcombatants(){
    var combatants=new ArrayList<>(garrison);
    combatants.addAll(all);
    return combatants;
  }

  /**
   * @return All existings instances of Mercenary Guilds.
   */
  public static List<MercenariesGuild> getguilds(){
    var all=World.getall(MercenariesGuild.class);
    var guilds=new ArrayList<MercenariesGuild>(all.size());
    for(Actor a:all) guilds.add((MercenariesGuild)a);
    return guilds;
  }

  /**
   * @param c Removes this unit from any guilds it might be listed in.
   */
  public static void die(Combatant c){
    for(MercenariesGuild g:getguilds()) g.all.remove(c);
  }

  @Override
  public void turn(long time,WorldScreen world){
    super.turn(time,world);
    if(all.size()<STARTINGMERCENARIES&&RPG.chancein(100)) generatemercenary();
  }

  /**
   * @param c Given a combatant
   * @return its daily fee as in "$40/day".
   */
  public static String getformattedfee(Combatant c){
    return "$"+Javelin.format(c.pay())+"/day";
  }
}
