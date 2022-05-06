package javelin.model.world.location.town.diplomacy.quest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.challenge.Difficulty;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.comparator.ItemsByPrice;
import javelin.controller.content.ContentSummary;
import javelin.controller.content.terrain.Terrain;
import javelin.model.item.Item;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.diplomacy.Diplomacy;
import javelin.model.world.location.town.diplomacy.quest.fetch.FetchArt;
import javelin.model.world.location.town.diplomacy.quest.fetch.FetchGem;
import javelin.model.world.location.town.diplomacy.quest.find.Connect;
import javelin.model.world.location.town.diplomacy.quest.find.Discover;
import javelin.model.world.location.town.diplomacy.quest.kill.Raid;
import javelin.model.world.location.town.labor.Trait;
import javelin.old.RPG;

/**
 * A task that can be completed for rewards and {@link Diplomacy#reputation}.
 * Non-hostile towns will have an active number of them equal to their
 * {@link Rank}.
 *
 * Design-wise, quests have a few gameplay objectives:
 *
 * 1. Provide lightweight but still unique gameplay not found elsewhere.
 *
 * 2. Nudge the player towards less obvious tasks they may overlook, with the
 * allure of a reward - thus promoting variety.
 *
 * 3. An alternate way to level up characters - not fully pacifist or otherwise
 * different to that degree but still...
 *
 * 4. Make the world feel alive, with things happening and opportunities coming
 * and going regardless of what the player is choosing to do. Since strategy is
 * continuous planning under evolcing circumstances, this is key to strategy.
 *
 * @author alex
 */
public abstract class Quest implements Serializable{
  /** All available quest templates. */
  public final static Map<String,List<Class<? extends Quest>>> QUESTS=new HashMap<>();
  /** For {@link World} quests. */
  public static final int SHORT=7;
  /** Default {@link #duration}. For {@link Dungeon} quests. */
  public static final int LONG=30;

  static final String COMPLETE="""
      You have completed a quest (%s)!
      %s
      %s
      Mood in %s is now: %s.""";
  static final List<Class<? extends Quest>> ALL=new ArrayList<>(8);
  static final Class<? extends Quest> DEBUG=Raid.class;

  static{
    //TODO QUESTS.put(Trait.CRIMINAL,List.of(Hit.class));
    QUESTS.put(Trait.EXPANSIVE,List.of(Discover.class));
    //QUESTS.put(Trait.MAGICAL,List.of(FetchRecipe.class));
    QUESTS.put(Trait.MERCANTILE,List.of(Connect.class));
    //TODO QUESTS.put(Trait.MILITARY,List.of(Raid.class));
    QUESTS.put(Trait.NATURAL,List.of(FetchGem.class));
    QUESTS.put(Trait.RELIGIOUS,List.of(FetchArt.class));
    for(var quests:QUESTS.values()) ALL.addAll(quests);
  }

  /** Town this quest was generated for. */
  public Town town;
  /**
   * Name of the quest. Used as a locally-exclusive identifier per {@link Town}.
   */
  public String name;
  /**
   * Amount of gold to be awarded upon completion. Subclasses can modify it
   * before {@link #reward()} is called, usually in {@link #define(Town)}..
   *
   * @see #reward()
   */
  public int gold=0;
  /**
   * Item reward.
   *
   * @see #reward()
   */
  public Item item;
  /** When <code>true</code> will not expire the quest until redeemed. */
  public boolean completed=false;
  /** Encounter level, between 1 and {@link Town#population}. */
  public int el;
  /**
   * Random daily chance a quest will expire.
   *
   * @see RPG#chancein(int)
   */
  protected int duration=LONG;

  /** @return If <code>false</code>, cancel or skip this quest type. */
  public boolean validate(){
    return name!=null;
  }

  /** TODO would be cool to have trait-based rewards */
  void reward(){
    var min=RewardCalculator.getgold(el-1);
    var max=RewardCalculator.getgold(el+1);
    gold+=Javelin.round(RPG.r(min,max));
    var items=RewardCalculator.generateloot(gold,1,Item.ITEMS);
    if(!items.isEmpty()){
      gold=0;
      items.sort(ItemsByPrice.SINGLETON.reversed());
      item=items.get(0);
      item.identified=true;
    }
  }

  /** Main method for generating quest details. */
  protected void define(Town t){
    town=t;
    el=RPG.high(1,t.population);
  }

  /**
   * @return If <code>true</code>, the quest is considered completed and a
   *   {@link Squad} may claim the reward.
   */
  protected abstract boolean complete();

  @Override
  public boolean equals(Object o){
    var q=o instanceof Quest?(Quest)o:null;
    return q!=null&&q.name.equals(name);
  }

  @Override
  public int hashCode(){
    return name.hashCode();
  }

  @Override
  public String toString(){
    return name;
  }

  /**
   * @return A brand-new, valid quest or <code>null</code> if couldn't generate
   *   any.
   */
  public static Quest generate(Town t){
    var quests=new ArrayList<Class<? extends Quest>>();
    if(Javelin.DEBUG&&DEBUG!=null) quests.add(DEBUG);
    else{
      for(var trait:t.traits) if(QUESTS.get(trait)!=null) //TODO only needed until redesign is done
        quests.addAll(QUESTS.get(trait));
      RPG.shuffle(quests);
      quests.addAll(RPG.shuffle(ALL));
    }
    try{
      for(var quest:quests){
        var q=quest.getConstructor().newInstance();
        q.define(t);
        q.reward();
        if(q.validate()&&!t.diplomacy.quests.contains(q)) return q;
      }
    }catch(ReflectiveOperationException e){
      if(Javelin.DEBUG)
        throw new RuntimeException("Cannot generate Town quest.",e);
    }
    return null;
  }

  /** @see ContentSummary */
  public static String printsummary(){
    var total=QUESTS.values().stream()
        .collect(Collectors.summingInt(List::size));
    var traits=QUESTS.keySet().stream().map(t->QUESTS.get(t).size()+" "+t)
        .collect(Collectors.joining(", "));
    return total+" town quests ("+traits+")";
  }

  /** @return Description of {@link #item} or {@link #gold}. */
  public String describereward(){
    if(item!=null) return item.toString();
    if(gold==0) return "";
    return "$"+Javelin.format(gold);
  }

  /**
   * Removes this from {@link Diplomacy#quests} and lowers
   * {@link Diplomacy#reputation}.
   */
  public void cancel(){
    town.diplomacy.quests.remove(this);
    town.diplomacy.reputation-=1;
    town.events.add("Quest expired: "+name+".");
  }

  /**
   * Updates the quest state, possibly making it {@link #completed} or
   * {@link #cancel()}.
   *
   * @param expire If <code>true</code>, will also roll a daily chance to
   *   expire.
   * @see #duration
   */
  public void update(boolean expire){
    if(completed) return;
    if(complete()) completed=true;
    else if(!validate()||expire&&RPG.chancein(duration)) cancel();
  }

  /** @return Message show to player during successful {@link #claim()}. */
  protected String message(){
    var xp=RewardCalculator.rewardxp(Squad.active.members,el,1);
    var reward=describereward();
    if(!reward.isEmpty())
      reward="You are rewarded for your efforts with: "+reward+"!\n";
    var reputation=town.diplomacy.describestatus().toLowerCase();
    return COMPLETE.formatted(name,xp,reward,town,reputation);
  }

  /** Completes the quest succesfully. */
  public void claim(){
    update(false);
    if(!completed) return;
    var p=town.population;
    town.diplomacy.reputation+=RPG.randomize(p/town.getrank().rank,0,p);
    Javelin.message(message(),true);
    if(gold>0) Squad.active.gold+=gold;
    if(item!=null) item.grab();
    town.diplomacy.quests.remove(this);
  }

  /** @return <code>true</code> if this is a good challenge for this quest. */
  protected boolean challenge(int el){
    return this.el+Difficulty.EASY<=el&&el<=this.el+Difficulty.DIFFICULT;
  }

  /**
   * This is important for quests so as to not always deterministically return
   * the same goal ({@link Location}, {@link Item})... when a new quest is
   * generated - while still allowing for probably selecting the closest goal.
   *
   * TODO make sure all quests are using
   *
   * @return 50% chance of first item, then 50% of second...
   */
  public static <K extends Object> K select(List<K> items){
    for(var i:items) if(RPG.chancein(2)) return i;
    return items.get(0);
  }

  /**
   * TODO make sure all quests are using
   *
   * @return Terrain-based {@link #name} suffix to help players estimate the
   *   goal's location ("in the forest").
   */
  public static String locate(Actor a){
    var l=a.getlocation();
    return "in the "+Terrain.get(l.x,l.y).description;
  }

}
