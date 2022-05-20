package javelin.controller.content.kit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Tier;
import javelin.controller.content.kit.dragoon.BlackDragoon;
import javelin.controller.content.kit.dragoon.BlueDragoon;
import javelin.controller.content.kit.dragoon.GreenDragoon;
import javelin.controller.content.kit.dragoon.RedDragoon;
import javelin.controller.content.kit.dragoon.WhiteDragoon;
import javelin.controller.content.kit.wizard.Abjurer;
import javelin.controller.content.kit.wizard.Conjurer;
import javelin.controller.content.kit.wizard.Diviner;
import javelin.controller.content.kit.wizard.Enchanter;
import javelin.controller.content.kit.wizard.Evoker;
import javelin.controller.content.kit.wizard.Necromancer;
import javelin.controller.content.kit.wizard.Transmuter;
import javelin.controller.content.upgrade.Upgrade;
import javelin.controller.content.upgrade.ability.RaiseAbility;
import javelin.controller.content.upgrade.classes.ClassLevelUpgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.discipline.Discipline;
import javelin.model.unit.abilities.discipline.expertise.CombatExpertiseDiscipline;
import javelin.model.unit.abilities.discipline.serpent.SteelSerpent;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.conjuration.Summon;
import javelin.model.world.location.Location;
import javelin.model.world.location.academy.Academy;
import javelin.model.world.location.academy.Guild;
import javelin.model.world.location.academy.Academy.BuildAcademy;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.Labor;
import javelin.model.world.location.unique.AdventurersGuild;
import javelin.old.RPG;

/**
 * Kits represent sets of {@link Upgrade}s that constitute a role a character
 * may have a play in. As much inspired on AD&D kits as actual character
 * classes, these are used on the {@link AdventurersGuild} and {@link Academy}
 * types as means of upgrading {@link Combatant}s.
 *
 * Kits are usually created by piecing together 3 to 7 lowest-level upgrades.
 *
 * TODO at some point should reference all kits by Class and keep an internal
 * Map - instead, currently when we save and load, new instance are generated
 * needlessly. a good way to enforce that would be to make kits
 * non-serializable.
 *
 * @author alex
 */
public abstract class Kit implements Serializable{
  /**
   * All kits available in game.
   *
   * @see #validate()
   */
  public static final List<Kit> KITS=List.of(Ninja.INSTANCE,Barbarian.INSTANCE,
      Bard.INSTANCE,Cleric.INSTANCE,Druid.INSTANCE,Fighter.INSTANCE,
      Monk.INSTANCE,Paladin.INSTANCE,Ranger.INSTANCE,Rogue.INSTANCE,
      Transmuter.INSTANCE,Enchanter.INSTANCE,Necromancer.INSTANCE,
      Conjurer.INSTANCE,Evoker.INSTANCE,Abjurer.INSTANCE,Diviner.INSTANCE,
      SteelSerpent.INSTANCE,BlackDragoon.INSTANCE,BlueDragoon.INSTANCE,
      GreenDragoon.INSTANCE,RedDragoon.INSTANCE,WhiteDragoon.INSTANCE);

  /**
   * TODO temporaty class to help transtition from {@link UpgradeHandler} to a
   * pure {@link Kit}-based system.
   *
   * @author alex
   */
  protected class BuildSimpleGuild extends BuildAcademy{
    /** Constructor. */
    protected BuildSimpleGuild(Rank minimumrank){
      super(minimumrank);
    }

    @Override
    protected Academy generateacademy(){
      return createguild();
    }
  }

  /**
   * TODO temporaty class to help transtition from {@link UpgradeHandler} to a
   * pure {@link Kit}-based system.
   *
   * @author alex
   */
  protected class SimpleGuild extends Guild{
    /** Constructor. */
    protected SimpleGuild(String string,Kit k){
      super(string,k);
    }

    @Override
    public String getimagename(){
      return "martialacademy";
    }
  }

  /** Name of the kit, also used for {@link #titles}. */
  public String name;
  /** @see #define() */
  public HashSet<Upgrade> basic=new HashSet<>();
  /** @see #extend() */
  public HashSet<Upgrade> extension=new HashSet<>();
  /** Class progression. */
  public ClassLevelUpgrade classlevel;
  /**
   * Primary ability. A secondary is also taken by the constructor and added to
   * {@link #extension} if not <code>null</code>.
   */
  public RaiseAbility ability;

  /**
   * One title per {@link Tier}. A $ should be replaced by the
   * {@link Monster#name}.
   *
   * @see #rename(Monster)
   */
  @Deprecated
  protected String[] titles;

  /**
   * A prestige kit is usually not suitable for starting characters, but only
   * for mid or high level ones.
   */
  public boolean prestige=false;

  /** Constructor. */
  public Kit(String name,ClassLevelUpgrade classlevel,RaiseAbility primary,
      RaiseAbility secondary){
    this.name=name;
    this.classlevel=classlevel;
    basic.add(classlevel);
    ability=primary;
    basic.add(ability);
    if(secondary!=null) extension.add(secondary);
    define();
    extend();
    var lower=name.toLowerCase();
    titles=new String[]{"Inept $ "+lower,"Rookie $ "+lower,"$ "+lower,
        "Veteran $ "+lower};
  }

  /**
   * Used only for "virtual" kits, ones that should never be used as proper
   * kits.
   *
   * @deprecated
   * @see CombatExpertiseDiscipline
   */
  @Deprecated
  public Kit(){}

  /**
   * Registers around 3-5 {@link Upgrade}s that all (or most) members of this
   * Kit should share. Usually for CRs around 1-5.
   */
  abstract protected void define();

  /**
   * Add any other {@link Upgrade}s that extend this Kit into middle, high and
   * epic levels.
   */
  protected abstract void extend();

  /**
   * TODO would be better to have the game concept of Ability as a programming
   * unit.
   *
   * @return The value of the {@link #ability} favored by this class. As such,
   *   it can be measured against the highest ability values of a unit to
   *   determine if that unit would be a good candidate for this kit.
   */
  public int getpreferredability(Monster m){
    return ability.getattribute(m);
  }

  @Override
  public String toString(){
    return name;
  }

  /**
   * @return <code>true</code> if this is a good choice for the given
   *   {@link Monster}. The default implementation just compares the two given
   *   ability scores to this class {@link #getpreferredability(Monster)}.
   */
  public boolean allow(int bestability,int secondbest,Monster m){
    var score=getpreferredability(m);
    return score==bestability||score==secondbest;
  }

  /**
   * @param allowprestige {@link Discipline}s are hardly suited for beginning
   *   characters so in some bases you may want not to include those as part of
   *   the result.
   * @return A list of kits that should be well suited for the given
   *   {@link Monster}. Current Kit selection has been set up so that this
   *   should never be empty.
   */
  public static List<Kit> getpreferred(Monster m,boolean allowprestige){
    if(!m.think(-1)) return List.of(Elite.SINGLETON);
    var attributes=new ArrayList<Integer>(6);
    attributes.add(m.strength);
    attributes.add(m.dexterity);
    attributes.add(m.constitution);
    attributes.add(m.intelligence);
    attributes.add(m.wisdom);
    attributes.add(m.charisma);
    attributes.sort(null);
    int[] best={attributes.get(4),attributes.get(5)};
    var preferred=new ArrayList<Kit>(1);
    for(Kit k:Kit.KITS){
      if(k.prestige&&!allowprestige) continue;
      if(k.allow(best[0],best[1],m)) preferred.add(k);
    }
    return preferred;
  }

  /**
   * @return All upgrades, from {@link #basic} and {@link #extension} on a new
   *   set.
   */
  public HashSet<Upgrade> getupgrades(){
    var upgrades=new HashSet<>(basic);
    upgrades.addAll(extension);
    return upgrades;
  }

  /**
   * Sets {@link Monster#customName} to one of the appropriate {@link #titles}.
   */
  public void rename(Monster m){
    m.customName=m.name+" "+name.toLowerCase();
  }

  /**
   * A {@link District} {@link Location} to learn this kit.
   *
   * TODO the default implemtation is temporary, see {@link BuildSimpleGuild} -
   * move to abstract ASAP
   */
  public Academy createguild(){
    return new SimpleGuild(name+"s guild",this);
  }

  /**
   * @deprecated temporaty class to help transtition from {@link UpgradeHandler}
   *   to a pure {@link Kit}-based system.
   */
  @Deprecated
  public Labor buildguild(){
    return new BuildSimpleGuild(Rank.HAMLET);
  }

  /** Does a few sanity and design checks if in {@link Javelin#DEBUG} mode. */
  public void validate(){
    if(!Javelin.DEBUG) return;
    if(!Kit.KITS.contains(this))
      throw new RuntimeException("Kit not registered: "+name);
    var nupgrades=basic.size();
    if(!(3<=nupgrades&&nupgrades<=7)){
      var error=name+" has "+nupgrades+" basic upgrades!";
      throw new RuntimeException(error);
    }
    for(var u:getupgrades())
      if(u==null) throw new RuntimeException("Null upgrade for Kit "+name);
  }

  /**
   * @return All {@link Spell}s that are part of this kit. May be empty.
   */
  @SuppressWarnings("unchecked")
  public <K extends Upgrade> List<K> filter(Class<K> type){
    return getupgrades().stream().filter(u->type.isInstance(u)).map(u->(K)u)
        .collect(Collectors.toList());
  }

  /**
   * Allows to complete a Kit after any external pendencies are resolved, such
   * as {@link Summon} {@link Spell}s.
   *
   * The default implementation will transfer any Spells with
   * {@link Spell#casterlevel} 1 to {@link #basic} from {@link #basic} to
   * {@link #extension} so it should be invoked at the end of a subclass'
   * implementaion.
   *
   * @see Summon#setupsummons()
   */
  public void finish(){
    var transfer=extension.stream().filter(u->u instanceof Spell)
        .map(u->(Spell)u).filter(s->s.casterlevel==1)
        .collect(Collectors.toList());
    extension.removeAll(transfer);
    basic.addAll(transfer);
    validate();
  }

  public boolean upgrade(Combatant c,boolean spendxp){
    for(var u:RPG.shuffle(new ArrayList<>(getupgrades()))){
      if(!u.upgrade(c,spendxp)) continue;
      c.postupgradeautomatic();
      ChallengeCalculator.calculatecr(c.source);
      c.source.elite=true;
      return true;
    }
    return false;
  }

  public boolean upgrade(Combatant c){
    return upgrade(c,false);
  }

  /**
   * Checks that every {@link Monster} in the game has at least one preferred
   * Kit.
   */
  public static void test(){
    for(var m:Monster.ALL) if(Kit.getpreferred(m,true).isEmpty())
      throw new RuntimeException("No kit for "+m+"!");
  }
}
