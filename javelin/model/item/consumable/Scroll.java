package javelin.model.item.consumable;

import java.security.InvalidParameterException;
import java.util.HashSet;

import javelin.Javelin;
import javelin.controller.content.action.CastSpell;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.skill.Skill;

/**
 * A catch-all spell-completion item type. Reading scrolls provoke attacks of
 * opportunity and as such are prohibited while engaged.
 *
 * In Javelin, for all intents and purposes {@link #identify(Combatant)} and
 * "decipher" are the same thing (it's not that different in the OGL rules too,
 * except it's on a personal basis with minor quirks). The canonical rules
 * regarding magic-item-activation-requirements are very convoluted and this
 * greatly simplifies this aspect, while also hitting basically every gameplay
 * and aesthetic note. Impact on balance is minimal, with a roughly equal amount
 * of very minor pluses and minuses to account for (especially considering this
 * is only the first step to actually activating a scroll successfully).
 *
 * @author alex
 */
public class Scroll extends Item{
  /** Contains one instance of each type of spell. */
  public static final HashSet<Scroll> SCROLLS=new HashSet<>();
  /** Spell this scroll can cast once. */
  public final Spell spell;

  /**
   * @param s The Spell this scroll casts.
   * @see Item#Item(String, int, ItemSelection)
   */
  public Scroll(final Spell s){
    super("Scroll of "+s.name.toLowerCase(),
        s.level*s.casterlevel*50+s.components,true);
    if(Javelin.DEBUG&&!s.isscroll&&!s.provokeaoo)
      throw new InvalidParameterException();
    spell=s.clone();
    usedinbattle=s.castinbattle;
    usedoutofbattle=s.castoutofbattle;
    apcost=0;
    identified=false;
    SCROLLS.add(this);
  }

  @Override
  public boolean use(Combatant user){
    CastSpell.SINGLETON.cast(spell,user);
    return true;
  }

  @Override
  public String canuse(Combatant c){
    if(!identified) return "can't read";
    if(Spell.enable(spell,c)) return null;
    var umd=c.taketen(Skill.USEMAGICDEVICE);
    if(umd>=20+spell.casterlevel) return null;
    return "can't activate";
  }

  @Override
  public boolean usepeacefully(Combatant c){
    failure=null;
    var use=canuse(c);
    if(use!=null){
      failure=use;
      return false;
    }
    if(!spell.validate(c,null)) return false;
    spell.castpeacefully(c);
    return true;
  }

  @Override
  public boolean equals(Object obj){
    return super.equals(obj)&&name.equals(((Scroll)obj).name);
  }

  @Override
  public String describefailure(){
    return failure==null?super.describefailure():failure;
  }

  @Override
  public boolean identify(Combatant c){
    if(super.identify(c)) return true;
    if(!c.source.think(-2)) return false;
    var s=20+Skill.SPELLCRAFT.getbonus(c);
    var dc=20+spell.level;
    if(s>=dc) return true;
    var umd=20+Skill.USEMAGICDEVICE.getbonus(c);
    if(umd>=dc+5) return true;
    return false;
  }
}
