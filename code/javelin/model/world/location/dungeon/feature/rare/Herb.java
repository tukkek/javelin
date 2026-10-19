package javelin.model.world.location.dungeon.feature.rare;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Debug;
import javelin.Javelin;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.comparator.ItemsByPrice;
import javelin.controller.content.fight.RandomDungeonEncounter;
import javelin.controller.exception.battle.StartBattle;
import javelin.model.item.Item;
import javelin.model.item.consumable.potion.Potion;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.unit.skill.Survival;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.chest.Chest;
import javelin.old.RPG;

/**
 * Similar to a {@link Chest} but requires someone with enough {@link Survival}
 * and results in a higher-than average {@link Potion} treasure.
 *
 * TODO brew bombs?
 *
 * @see RewardCalculator
 * @author alex
 */
public class Herb extends Feature{
  /**
   * Unfortunately {@link Potion}s are on the lower end of magic {@link Item}s,
   * so it's hard to scale this {@link DungeonFloor} feature indefinitely.
   * Instead, set this max allowed level as per {@link DungeonFloor#level}.
   *
   * Note that this feature will generate one or multiple instances of the same
   * potion type.
   */
  public static final int MAXLEVEL;

  static final List<Potion> POTIONS=Item.ITEMS.stream()
      .filter(i->i.getClass().equals(Potion.class)).map(i->(Potion)i)
      .collect(Collectors.toList());
  static final int MAXCOPIES=2;

  static{
    if(Javelin.DEBUG&&POTIONS.isEmpty())
      throw new RuntimeException("Could not find potions!");
    var ceiling=POTIONS.get(POTIONS.size()-1).price;
    ceiling*=MAXCOPIES;
    var level=1;
    while(RewardCalculator.getgold(level+1)<=ceiling) level+=1;
    MAXLEVEL=level;
  }

  record Test(Combatant combatant,int roll){}

  int dc;
  List<Potion> loot;

  /** Constructor. */
  public Herb(DungeonFloor f){
    super("herb");
    remove=false;
    dc=getdc(f);
    loot=generate(f);
  }

  Test test(Skill s){
    var combatant=Squad.active.getbest(s);
    return new Test(combatant,combatant.taketen(s));
  }

  @Override
  public boolean activate(){
    var description=describe(loot);
    var squad=Squad.active;
    var survival=test(Skill.SURVIVAL);
    var survivalist=survival.combatant;
    if(survival.roll<dc){
      var wisdom=test(Skill.KNOWLEDGE).roll;
      var text=wisdom>=dc
          ?"A survivalist could turn these to %s...".formatted(description)
          :"%s is is unfamiliar with this plant...".formatted(survivalist);
      Javelin.message(text,false);
      return true;
    }
    var lines=List.of("Should %s extract these herbs to a %s?",
        "Press ENTER to continue; any other key to cancel...");
    var text=String.join("\n",lines).formatted(survivalist,description);
    if(Javelin.prompt(text)!='\n') return false;
    if(RPG.chancein(2)&&!Debug.disablecombat){
      Javelin.message("You are interrupted!",false);
      throw new StartBattle(new RandomDungeonEncounter(Dungeon.active));
    }
    squad.delay(1);
    text="You extract a %s from the herbs!".formatted(description);
    Javelin.message(text,false);
    for(var potion:loot) potion.grab();
    remove();
    return false;
  }

  /**
   * @param level Approximate reward level.
   * @return Generates a set of potions.
   * @see RewardCalculator
   * @see #MAXLEVEL
   */
  List<Potion> generate(DungeonFloor f){
    if(!validate(f)) return Collections.EMPTY_LIST;
    var all=new ArrayList<>(POTIONS); //avoid concurrency issues
    RPG.shuffle(all).sort(ItemsByPrice.SINGLETON);
    var potions=new ArrayList<Potion>(MAXCOPIES);
    var reward=RewardCalculator.getgold(f.level);
    for(var p:POTIONS){
      potions.clear();
      var pool=reward;
      for(var i=0;i<MAXCOPIES&&pool>0;i++){
        var c=(Potion)p.clone();
        c.identified=true;
        potions.add(c);
        pool-=p.price;
      }
      if(-reward*10<=pool&&pool<=0) return potions;
    }
    return potions;
  }

  /**
   * @param potions A number of the same type of item.
   * @return "a potion of cure light wounds", "4x potion of barkskin"...
   */
  static String describe(List<? extends Item> potions){
    var amount=potions.size();
    var description=potions.get(0).toString().toLowerCase();
    return (amount==1?"a":amount+"x")+" "+description;
  }

  @Override
  public boolean validate(DungeonFloor f){
    return super.validate(f)&&f.level<=MAXLEVEL;
  }

  @Override
  public String toString(){
    return "Herb ("+loot.get(0).name.toLowerCase()+")";
  }
}
