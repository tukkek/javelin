package javelin.controller.challenge;

import java.util.List;

import javelin.old.RPG;

/**
 * TODO in the distant future, can expand this to Paragon (up to 40), Demigod
 * (up to 50), God (up to 55) and Deity (up to 60). Actually Paragon should be
 * what is now considered Epic and vice-versa.
 *
 * @author alex
 */
public class Tier{
  /** Already heroes but largely unknown. */
  public static final Tier LOW=new Tier("Low",1,5);
  /** Well-known local figures. */
  public static final Tier MID=new Tier("Mid",6,10);
  /** World-class adventurers, instantly recognized. */
  public static final Tier HIGH=new Tier("High",11,15);
  /** Legendary figures who will be immortalized in history. */
  public static final Tier EPIC=new Tier("Epic",16,20);

  /** All intended gameplay tiers from lowest to highest. */
  public static List<Tier> TIERS=List.of(LOW,MID,HIGH,EPIC);

  String name;
  /** Lowest tier level. */
  public int minlevel;
  /** Highest tier level. */
  public int maxlevel;

  /** Constructor. */
  public Tier(String name,int minlevel,int maxlevel){
    this.name=name;
    this.minlevel=minlevel;
    this.maxlevel=maxlevel;
  }

  /** @return The relevant tier, given a character level. */
  public static Tier get(double level){
    if(level<LOW.minlevel) return LOW;
    if(level>EPIC.maxlevel) return EPIC;
    for(var t:TIERS) if(t.minlevel<=level&&level<=t.maxlevel) return t;
    throw new RuntimeException("Error determining tier for level "+level);
  }

  @Override
  public String toString(){
    return name;
  }

  /** @return 0 for {@link #LOW}, 1 for {@link #MID}... */
  public int getordinal(){
    return TIERS.indexOf(this);
  }

  /**
   * @param randomdifficulty If <code>true</code>, will add a
   *   {@link Difficulty#get()} result.
   * @return An encounter rolled between {@link #minlevel} and
   *   {@link #maxlevel}.
   */
  public Integer getrandomel(boolean randomdifficulty){
    var el=RPG.r(minlevel,maxlevel);
    if(randomdifficulty) el+=Difficulty.get();
    return el;
  }
}
