package javelin.model.world.location.town.diplomacy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.diplomacy.mandate.Mandate;
import javelin.model.world.location.town.diplomacy.quest.Quest;
import javelin.old.RPG;

/**
 * Offers long-term card-game-like actions to pursue towards {@link Town}s. Each
 * {@link Town} is treated as an independent entity from the player, regardless
 * of it being hostile or not. A town must be discovered, however, before
 * diplomatic actions are allowed on it.
 *
 * Reputation is accrued by completing or failing {@link Quest}s and once
 * {@link #getstatus()} reaches 100%, a card can be chosen and paid for
 * (-100%)..New cards are drawn and old ones discarded periodically. Invalid
 * cards will be removed on demand but not replenished immediately to prevent
 * scumming.
 *
 * TODO with 2.0, when we have html documentation, include overview and card
 * documentation
 *
 * @author alex
 * @see Town#ishostile()
 * @see Town#generatereputation()
 * @see Mandate#validate(Diplomacy)
 */
public class Diplomacy implements Serializable{
  /**
   * Currency for acquiring{@link Mandate} rewards once it reaches a value equal
   * or greater than {@link Town#population}.
   */
  public int reputation=0;
  /** Possible diplomatic actions. */
  public TreeSet<Mandate> treaties=new TreeSet<>();
  /** Town these rewards are for. */
  public Town town;
  /** Active quests. Updated daily. */
  public List<Quest> quests=new ArrayList<>(1);

  /** Generates a fresh set of relationships, when a campaign starts. */
  public Diplomacy(Town t){
    town=t;
  }

  /** Ticks a day off active quests and generates new ones. */
  public void updatequests(){
    for(var q:new ArrayList<>(quests)) q.update(true);
    if(quests.size()<town.getrank().rank&&RPG.chancein(7)){
      var q=Quest.generate(town);
      if(q!=null){
        quests.add(q);
        town.events.add("New quest available: "+q+".");
      }
    }
  }

  void updatemandates(){
    if(!treaties.isEmpty()&&RPG.chancein(30)){
      var m=RPG.pick(treaties);
      treaties.remove(m);
      town.events.add("Treaty opportunity expired: "+m+".");
    }
    if(treaties.size()<town.getrank().rank&&RPG.chancein(7)){
      var m=Mandate.generate(this);
      if(m!=null) town.events.add("New treaty available: "+m+".");
    }
  }

  /** To be called once per day per instance. */
  public void turn(){
    validate();
    if(town.ishostile()) return;
    updatequests();
    updatemandates();
  }

  /**
   * Removes invalid entries from {@link #treaties}.
   *
   * @see Mandate#validate()
   */
  public void validate(){
    for(var card:new ArrayList<>(treaties)) if(!card.validate()){
      treaties.remove(card);
      town.events.add("Treaty no longer eligible: "+card+".");
    }
  }

  /** @return A {@link #reputation} percentage of {@link Town#population}. */
  public float getstatus(){
    return reputation/(float)town.population;
  }

  /** @return A human description of {@link #getstatus()}. */
  public String describestatus(){
    var s=getstatus();
    if(s<=-1) return "Hostile";
    if(s<=0) return "Cautious";
    if(s<=.3) return "Neutral";
    if(s<=.7) return "Content";
    if(s<1) return "Happy";
    return "Loyal";
  }

  /** @return <code>true</code> if can claim a {@link Mandate}. */
  public boolean claim(){
    return getstatus()>=1&&!treaties.isEmpty();
  }

  /** @param m Pays for, plays and discards this. */
  public void enact(Mandate m){
    reputation-=town.population;
    m.act();
    treaties.remove(m);
  }

  /** Removes all active {@link Quest}s and {@link Mandate}s. */
  public void clear(){
    quests.clear();
    treaties.clear();
  }
}
