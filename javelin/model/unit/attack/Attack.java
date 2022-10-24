package javelin.model.unit.attack;

import java.io.Serializable;
import java.util.Random;

import javelin.controller.ai.AiThread;
import javelin.model.Cloneable;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.Spell;

/**
 * A single attack in an {@link AttackSequence}.
 *
 * @author alex
 */
public class Attack implements Serializable,Cloneable{
  /** Attack description. */
  public final String name;
  /** Attack bonus as it appears on the XML data/stat block. */
  public int bonus;
  /** Format by index: 0d1+2. */
  public int[] damage=null;
  /** Critical hit range (usually only on a natural 20). */
  public int threat=-1;
  /** Critical hit {@link #damage} multiplier. */
  public int multiplier=-1;
  /**
   * If changed to <code>true</code> will use energy resistance instead of
   * damage reducton to absorb attack.
   *
   * @see Monster#energyresistance
   */
  public boolean energy=false;
  /**
   * Allols for overriding {@link #effect} without having to deal with complex
   * clean-up actions. Just set back to <code>null</code> once the new effect is
   * over.
   */
  public Spell temporaryeffect=null;
  /** If <code>true</code> ignores armor class. */
  public boolean touch=false;

  /** This spell will be cast upon hitting with attack. */
  Spell effect=null;

  /** Constructor. */
  public Attack(final String name,final int bonusp,boolean touch){
    this.name=name;
    bonus=bonusp;
    this.touch=touch;
  }

  @Override
  public String toString(){
    var chance=(bonus>=0?"+":"")+bonus;
    return name+" ("+chance+", "+formatDamage()+")";
  }

  public String formatDamage(){
    if(damage==null) return "null";
    final var dice=damage[0]+"d"+damage[1];
    final var bonus=(damage[2]>=0?"+":"")+damage[2];
    var output=dice+bonus;
    if(threat!=20||multiplier!=2){
      final var t=threat==20?"20":threat+"-20";
      output+=", "+t+"/x"+multiplier;
    }
    if(energy) output+=" energy";
    var effect=geteffect();
    if(effect!=null) output+=", "+effect.name.toLowerCase();
    return output;
  }

  /**
   * @return Average damage with elemental but no bonus.
   * @see #damage
   */
  public float getAverageDamageNoBonus(){
    return damage[0]*damage[1]/2f;
  }

  /**
   * @return Non-elemental damage.
   */
  public int getaveragedamage(){
    return damage[0]*damage[1]/2+damage[2];
  }

  @Override
  public Attack clone(){
    try{
      final var clone=(Attack)super.clone();
      clone.damage=damage.clone();
      return clone;
    }catch(CloneNotSupportedException e){
      throw new RuntimeException(e);
    }
  }

  public Spell geteffect(){
    return temporaryeffect!=null?temporaryeffect:effect;
  }

  public void seteffect(Spell effect){
    this.effect=effect.clone();
  }

  /**
   * @return Minimum possible rolled damage (never less than zero).
   */
  public int getminimumdamage(){
    return Math.max(0,damage[0]+damage[2]);
  }

  /** @return Attack {@link #bonus} against this enemy. */
  public int getbonus(Combatant target){
    var b=bonus;
    if(touch) b+=target.source.armor;
    return b;
  }

  /** TODO workaround: can't really use {@link AiThread#getrandom()}. */
  public int rolldamage(Random r){
    var result=damage[2];
    for(var i=0;i<damage[0];i++) result+=r.nextInt(damage[1])+1;
    return result;
  }
}
