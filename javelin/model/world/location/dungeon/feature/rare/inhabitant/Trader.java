package javelin.model.world.location.dungeon.feature.rare.inhabitant;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.Difficulty;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.comparator.ItemsByPrice;
import javelin.controller.content.fight.RandomDungeonEncounter;
import javelin.controller.exception.battle.StartBattle;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.town.labor.basic.Shop;
import javelin.old.RPG;
import javelin.view.screen.BattleScreen;

/** Works like a small {@link Shop} but for {@link Dungeon}s. */
public class Trader extends Inhabitant{
  class TraderFight extends RandomDungeonEncounter{
    TraderFight(){
      super(Dungeon.active);
      bribe=false;
      hide=false;
      rewardgold=false;
    }

    @Override
    public Combatants generate(ArrayList<Combatant> blueteam){
      return new Combatants(List.of(inhabitant));
    }

    @Override
    public boolean onend(){
      if(!super.onend()) return false;
      Dungeon.active.features.remove(Trader.this);
      for(var i:inventory) i.grab();
      return true;
    }
  }

  /** Stock. */
  protected List<Item> inventory;

  /** Reflection-friendly constructor. */
  public Trader(DungeonFloor f){
    super(f.level+Difficulty.DIFFICULT,f.level+Difficulty.DEADLY,"trader",f);
    var pool=RewardCalculator.getgold(inhabitant.source.cr);
    if(pool<50) pool=50;
    inventory=stock(pool);
    sort();
  }

  /** @see List#sort(java.util.Comparator) */
  protected void sort(){
    inventory.sort(ItemsByPrice.SINGLETON);
  }

  /** @return Items to be sold. */
  protected List<Item> stock(int goldpool){
    var nitems=1;
    while(RPG.chancein(2)) nitems+=1;
    return RewardCalculator.generateloot(goldpool,nitems,Item.NONPRECIOUS);
  }

  @Override
  public boolean activate(){
    var name=inhabitant.toString().toLowerCase();
    var prompt="This "+name+" has a few items to offer.\n\n";
    var s=Squad.active;
    prompt+="You have $"+Javelin.format(s.gold)+" gold.";
    var options=new ArrayList<String>(inventory.size());
    for(var item:inventory){
      var detail="$"+Javelin.format(item.price);
      if(!item.canuse(s.members)) detail+=", can't use";
      options.add(item+" ("+detail+")");
    }
    var difficulty=Difficulty.describe(s.members,List.of(inhabitant));
    var attack="Attack %s (%s)".formatted(inhabitant,difficulty);
    options.add(attack);
    var choice=Javelin.choose(prompt,options,true,false);
    if(choice<0) return true;
    if(options.get(choice)==attack) throw new StartBattle(new TraderFight());
    var item=inventory.get(choice);
    if(Squad.active.pay(item.price)) sell(item);
    else{
      Javelin.app.switchScreen(BattleScreen.active);
      Javelin.message("Too expensive...",false);
    }
    return true;
  }

  /** Removes item from stock and gives to {@link Squad}. */
  protected void sell(Item i){
    inventory.remove(i);
    i.grab();
  }
}
