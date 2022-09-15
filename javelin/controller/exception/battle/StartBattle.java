package javelin.controller.exception.battle;

import java.util.ArrayList;

import javelin.Debug;
import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.content.fight.Fight;
import javelin.controller.content.fight.setup.BattleSetup;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Squad;
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
    var foes=fight.setup();
    if(fight.avoid(foes)) return;
    preparebattle(foes);
    fight.setup.setup();
    Fight.state.next();
    for(var m:fight.mutators) m.ready(fight);
    final var elred=ChallengeCalculator.calculateel(Fight.state.redteam);
    final var elblue=ChallengeCalculator.calculateel(Fight.state.blueteam);
    var diffifculty=elred-elblue;
    if(!Squad.active.skipcombat(diffifculty)){
      BattlePanel.current=Fight.state.next;
      var screen=new BattleScreen(true,true);
      for(var m:fight.mutators) m.draw(fight);
      if(Javelin.DEBUG) Debug.onbattlestart();
      screen.mainloop();
    }else quickbattle(diffifculty);
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
    var resourcesused=ChallengeCalculator.useresources(difficulty);
    var report="Battle report:\n\n";
    var blueteam=new ArrayList<>(Squad.active.members);
    var damage=damage(blueteam,resourcesused);
    for(var i=0;i<blueteam.size();i++)
      report+=strategicdamage(blueteam.get(i),damage.get(i))+"\n\n";
    if(Squad.active.equipment.count()==0)
      report+=Squad.active.wastegold(resourcesused);
    var s=new InfoScreen("");
    s.print(report+"Press ENTER or s to continue...");
    var feedback=s.getinput();
    while(feedback!='\n'&&feedback!='s') continue;
    BattleScreen.active.center();
    Squad.active.gold-=Squad.active.gold*(resourcesused/10f);
    if(Squad.active.members.isEmpty()){
      Javelin.message("Battle report: Squad lost in combat!",false);
      Squad.active.disband();
    }else{
      Fight.victory=true;
      fight.onend();
    }
    Fight.current=null;
  }

  private ArrayList<Float> damage(ArrayList<Combatant> blueteam,
      float resourcesused){
    var damage=new ArrayList<Float>(blueteam.size());
    while(damage.size()<blueteam.size()) damage.add(0f);
    var total=resourcesused*blueteam.size();
    var dealt=0F;
    var step=resourcesused/2f;
    while(dealt<total){
      var i=RPG.r(0,blueteam.size()-1);
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
    var killed=c.hp<=Combatant.DEADATHP|| //
        c.hp<=0&&RPG.random()<Math.abs(c.hp/Float.valueOf(Combatant.DEADATHP));
    var report="";
    var bag=Squad.active.equipment.get(c);
    for(Item i:new ArrayList<>(bag)){
      var used="";
      if(i.waste){
        var wasted=i.waste(resourcesused,c,bag);
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
    Fight.state.redteam=opponents;
    for(var m:fight.mutators) m.prepare(fight);
    var blue=Fight.state.blueteam;
    Fight.originalblueteam=new Combatants(blue);
    Fight.originalredteam=new Combatants(Fight.state.redteam);
    for(var i=0;i<blue.size();i++){
      var c=blue.get(i);
      blue.set(i,c.clone().clonesource());
    }
    Fight.state.next();
  }

  static ArrayList<Combatant> cloneteam(ArrayList<Combatant> team){
    var clone=new ArrayList<Combatant>(team.size());
    for(Combatant c:team) clone.add(c.clone());
    return clone;
  }
}
