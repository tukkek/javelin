package javelin.model.world.location.unique;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Tier;
import javelin.controller.content.kit.Kit;
import javelin.controller.content.upgrade.Upgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.old.RPG;
import javelin.view.screen.InfoScreen;

/** Starting location to Apply class {@link Kit}s to low-level player units. */
public class AdventurersGuild extends UniqueLocation{
  static final List<String> KEYS=Arrays
      .asList("1234567890abcdefghijklnoprsuvxyz/*-+.?!@#$%&()_=[]{}<>;:\"\\|"
          .split(""));
  static final String TITLE="Adventurers guild";
  static final float MAX=Tier.LOW.maxlevel;
  static final String PROMPT="""
      You enter a tall building where people are training and studying in several large rooms.

      %s

      Press the respective number to select kits.
      Training time: 1 week, fees: $%s. You have $%s.

      t - begin training
      q - quit
      """
      .trim();

  Map<Integer,Kit> kits=new HashMap<>();

  /** Constructor. */
  public AdventurersGuild(){
    super(TITLE,TITLE,1,1);
    vision=2;
  }

  @Override
  protected void generategarrison(int minlevel,int maxlevel){
    // clear
  }

  @Override
  public boolean interact(){
    var s=Squad.active.members;
    for(var member:s){
      var i=member.id;
      if(!kits.containsKey(i))
        kits.put(i,RPG.pick(Kit.getpreferred(member.source,false)));
    }
    var screen=new InfoScreen("");
    Character input='a';
    while(input!='q'){
      screen.print(show());
      input=screen.getinput();
      var index=KEYS.indexOf(input.toString());
      if(0<=index&&index<s.size()){
        change(s.get(index));
        continue;
      }
      if(input=='t'&&train()) return true;
    }
    return true;
  }

  void change(Combatant c){
    if(validate(c)!=null) return;
    var preferred=Kit.getpreferred(c.source,false);
    var current=kits.get(c.id);
    var to=0;
    if(current!=null){
      to=preferred.indexOf(current)+1;
      if(to>=preferred.size()) to=0;
    }
    kits.put(c.id,preferred.get(to));
  }

  String validate(Combatant c){
    if(ChallengeCalculator.calculatecr(c.source)>=MAX)
      return "has learned all they can here";
    if(c.xp.floatValue()<1) return "needs at least 100XP";
    return null;
  }

  long price(){
    var s=Squad.active.members;
    return 50*s.stream().filter(m->validate(m)==null).count();
  }

  String show(){
    var trainees=new ArrayList<String>();
    var s=Squad.active.members;
    for(var i=0;i<s.size();i++){
      var member=s.get(i);
      var message=validate(member);
      if(message==null) message=kits.get(member.id).toString().toLowerCase();
      trainees.add("[%s] %s - %s".formatted(KEYS.get(i),member,message));
    }
    var p=Javelin.format(price());
    var g=Javelin.format(Squad.active.gold);
    return PROMPT.formatted(String.join("\n",trainees),p,g);
  }

  boolean train(){
    var p=price();
    if(p==0) return false;
    if(!Squad.active.pay(Math.round(p))){
      Javelin.promptscreen("Not enough gold...");
      return false;
    }
    Squad.active.delay(24*7);
    for(var m:Squad.active.members) if(validate(m)==null){
      var upgrades=new ArrayList<>(kits.get(m.id).getupgrades());
      train(m,RPG.shuffle(upgrades),1);
    }
    return true;
  }

  /**
   * Will also show a summary screen after done.
   *
   * @param upgrades Randomly applies these upgrades to the given
   *   {@link Combatant}, until all possibilities are exhausted (if any).
   * @param xp How much experience to spend at most. Will also be subtracted
   *   from {@link Combatant#xp}.
   * @return <code>true</code> if at least one {@link Upgrade} has been applied.
   */
  static public boolean train(Combatant student,Collection<Upgrade> upgrades,
      float xp){
    var learned=new ArrayList<Upgrade>();
    while(xp>0){
      Upgrade applied=null;
      Float cost=null;
      for(var upgrade:RPG.shuffle(new ArrayList<>(upgrades))){
        cost=upgrade.getcost(student);
        if(cost!=null&&0<cost&&cost<=xp){
          applied=upgrade;
          break;
        }
      }
      if(applied==null||cost==null) break;
      applied.upgrade(student);
      learned.add(applied);
      student.xp=student.xp.subtract(new BigDecimal(cost));
      xp-=cost;
    }
    var haslearned=!learned.isEmpty();
    if(haslearned){
      student.postupgrade();
      ChallengeCalculator.calculatecr(student.source);
    }
    printresult(student,learned);
    return haslearned;
  }

  static void printresult(Combatant student,ArrayList<Upgrade> learned){
    String training;
    if(learned.isEmpty())
      training=student+" was unable to learn anything at this time...\n";
    else{
      training=student+" learns:\n\n";
      learned.sort(Comparator.comparing(Upgrade::getname));
      for(Upgrade u:learned) training+=u.getname()+"\n";
    }
    training+="\nPress ENTER to continue...";
    var screen=new InfoScreen(training);
    Javelin.app.switchScreen(screen);
    while(screen.getinput()!='\n'){}
  }

  @Override
  public List<Combatant> getcombatants(){
    return null;
  }
}
