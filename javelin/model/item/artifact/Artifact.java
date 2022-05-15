package javelin.model.item.artifact;

import javelin.Javelin;
import javelin.controller.challenge.Tier;
import javelin.model.item.Item;
import javelin.model.item.gear.rune.RuneGear;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.world.Period;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.WorldScreen;

/**
 * Extrenely powerful, intentionally balance-defying rare Challenge rewards.
 *
 * <a href='http://crawl.chaosforge.org/Randart'>Randarts</a> are popular
 * features in other roguelikes but Javelin approaches those as exhautively
 * procedurally-generated {@link Item}s instead (such as {@link RuneGear} and
 * most other {@link Spell}-based item types). This leaves plenty of room for
 * {@link Tier#EPIC} (and beyond) Items to be created through universal methods,
 * leaving artifacts on their own category as hand-crafted, rare god-like items.
 *
 * TODO endgame https://github.com/tukkek/javelin/issues/269
 *
 * @author alex
 */
public abstract class Artifact extends Item{
  static int RECHARGEPERIOD=24*7;
  long lastused=-RECHARGEPERIOD;

  /** Constructor. */
  public Artifact(String name){
    super(name,0,false);
    consumable=false;
    waste=false;
    provokesaoo=false;
  }

  @Override
  public boolean use(Combatant user){
    if(!usedinbattle) super.use(user);
    userelic(user);
    return false;
  }

  @Override
  public boolean usepeacefully(Combatant user){
    if(!usedoutofbattle) super.use(user);
    return userelic(user);
  }

  boolean userelic(Combatant user){
    if(charge()){
      var text="The "+name+" is recharging and can't be used right now...";
      if(BattleScreen.active instanceof WorldScreen){
        Javelin.app.switchScreen(BattleScreen.active);
        Javelin.message(text,false);
      }else Javelin.message(text,Javelin.Delay.BLOCK);
      return true;
    }
    if(!activate(user)) return false;
    lastused=Period.gettime();
    return true;
  }

  boolean charge(){
    return Period.gettime()-lastused<RECHARGEPERIOD;
  }

  /**
   * Invoke the relics's power.
   *
   * @param user Unit handling the relic.
   * @return <code>false</code> if the use is cancelled by the player.
   */
  protected abstract boolean activate(Combatant user);

  @Override
  public String canuse(Combatant c){
    return charge()?"recharging":null;
  }
}
