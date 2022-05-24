package javelin.model.unit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

import javelin.Javelin;
import javelin.controller.AbilityModification;
import javelin.controller.Weather;
import javelin.controller.ai.BattleAi;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.factor.SpellsFactor;
import javelin.controller.comparator.FeatByNameComparator;
import javelin.controller.content.action.Breathe;
import javelin.controller.content.map.Map;
import javelin.controller.content.quality.subtype.Construct;
import javelin.controller.content.quality.subtype.Undead;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.content.upgrade.classes.ClassLevelUpgrade;
import javelin.controller.db.reader.MonsterReader;
import javelin.controller.db.reader.fields.Skills;
import javelin.controller.generator.SpellbookGenerator;
import javelin.model.Cloneable;
import javelin.model.unit.abilities.BreathWeapon;
import javelin.model.unit.abilities.Constrict;
import javelin.model.unit.abilities.TouchAttack;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.Spells;
import javelin.model.unit.abilities.spell.enchantment.compulsion.HoldMonster;
import javelin.model.unit.abilities.spell.necromancy.Poison;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.AttackSequence;
import javelin.model.unit.feat.Feat;
import javelin.model.unit.feat.attack.focus.WeaponFocus;
import javelin.model.unit.feat.internal.WeaponFinesse;
import javelin.model.unit.skill.Skill;
import javelin.model.world.Period;
import javelin.model.world.location.academy.Academy;
import javelin.model.world.location.unique.MercenariesGuild;
import javelin.old.RPG;

/**
 * A Monster represents a stat-block (monster information) from the monster
 * manual. Different in-game battle units can be linked to the same stat block.
 * A player character creates it's own stat-block which is updated as he levels
 * up.
 *
 * For performance, Monsters are not to be cloned during BattleNode replication.
 * A monster should only be cloned and modified during AI thinking in the case a
 * spell or ability changes that specific unit's stats (cat's grace or level
 * drain, for example).
 *
 * @author Brian Voon Yee Yap
 * @author alex
 */
public class Monster implements Cloneable,Serializable{
  /**
   * Monster descriptions, separate from {@link Monster} data to avoid
   * duplication in memory when using {@link Monster#clone()}.
   *
   * @see Combatant#clonedeeply()
   */
  public static final TreeMap<String,String> DESCRIPTIONS=new TreeMap<>();
  /** All loaded XML {@link Monster}s. See {@link MonsterReader}. */
  public static final List<Monster> ALL=new ArrayList<>();
  /** All loaded monster mapped by challenge rating. */
  public static final TreeMap<Float,List<Monster>> BYCR=new TreeMap<>();
  /** TODO make enumeration */
  public static final int VISION_LOWLIGHT=1;
  /** TODO make enumeration */
  public static final int VISION_DARK=2;

  /**
   * Monster types as per the SRD.
   *
   * @author alex
   */
  @SuppressWarnings("javadoc")
  public enum MonsterType{
    ABERRATION,ANIMAL,CONSTRUCT,DRAGON,ELEMENTAL,FEY,GIANT,HUMANOID,MAGICALBEAST,MONSTROUSHUMANOID,OOZE,OUTSIDER,PLANT,SHAPECHANGER,UNDEAD,VERMIN;
  }

  /**
   * Map of {@link Terrain} types, mapped by {@link Monster#name}.
   *
   * @deprecated
   * @see #getterrains()
   */
  @Deprecated
  public static final HashMap<String,List<String>> TERRAINDATA=new HashMap<>();
  public int strength=-1;
  public int dexterity=-1;
  /** May be 0. */
  public int constitution=-1;
  /** Maybe be 0. */
  public int intelligence=-1;
  public int wisdom=-1;
  public int charisma=-1;

  /**
   * See {@link #getfortitude()}
   */
  @Deprecated
  public int fort;
  public int ref;
  /**
   * @deprecated See {@link #getwill()})
   */
  @Deprecated
  public int will;

  /**
   * 5 units = 1 square. A unit is able to move this number of squares as a
   * move-equivalent action (.5 action points).
   *
   * @see #fly
   */
  public int walk=0;
  /**
   * Flying allows an unit to ignore water and obstacles. A flying unit should
   * have {@link #walk} 0.
   *
   * {@link Map#flying} might prevent units from flying over obstacles (like
   * undergound, where there's no "above" to fly over) - but it still allows for
   * other benefits, such as ignoring water.
   *
   * TODO also offer perfect flight, which could at least charge through
   * obstacles.
   *
   * @see #walk
   */
  public int fly=0;
  /**
   * Burrow allows a creature to submerge into the earth. Normally they would be
   * able to descend to any depth but due to AI and complexity concerns
   * burrowing right is more restrict. Read "combatmodifiers.txt".
   *
   * Burrowing and surfacing cost the equivalent of a single square movement
   * unless engaged.
   */
  public int burrow=0;

  /**
   * A swimming creature is able to ignore water penalties and charge through
   * flooded squares.
   *
   * @see #walk
   */
  public int swim=0;

  /** The portion of {@link #ac} due to armor protection. */
  public int armor;

  public ArrayList<AttackSequence> melee=new ArrayList<>();
  public ArrayList<AttackSequence> ranged=new ArrayList<>();

  public Feats feats=new Feats();
  public HD hd=new HD();
  /** @see Breathe */
  public CloneableList<BreathWeapon> breaths=new CloneableList<>();
  /**
   * This is the spells that this source has by default. Learned spells go to
   * {@link Combatant#spells}.
   *
   * TOOD this could be removed and better designed.
   *
   * @see Monster#spellcr
   * @see Spell
   */
  public Spells spells=new Spells();
  /** @see TouchAttack */
  public TouchAttack touch=null;

  public int initiative=Integer.MIN_VALUE;
  /** Monster name as found in the d20 SRD. */
  public String name=null;
  /**
   * Index for {@link #SIZES}.
   *
   * @see #size()
   */
  public int size=-1;
  /** Group description, as found in "Name (group)". */
  public String group;
  /** Challenge rating (cache for {@link ChallengeCalculator}). */
  public Float cr=null;
  /** @see MonsterType */
  public MonsterType type;
  /** TODO should probably be a Combatant#name */
  public String customName=null;
  public int meleedamageupgrades=0;
  public int fasthealing=0;
  public int rangeddamageupgrades=0;
  public int warrior=0;
  public int aristocrat=0;
  public int expert=0;
  public int commoner=0;
  public String avatarfile=null;
  /**
   * What type of vision perception the monster has.
   *
   * @see #VISION_LOWLIGHT
   * @see #VISION_DARK
   */
  public int vision=0;
  public float originalhd;
  /**
   * Used to distribute random spells to a new {@link Combatant}.
   *
   * TODO {@link ChallengeCalculator} is using this for {@link SpellsFactor}
   * instead of taking the {@link Combatant} into consideration. Maintain?
   *
   * @see SpellbookGenerator
   *
   */
  public float spellcr=0;
  /**
   * Damage reduction.
   */
  public int dr=0;
  /**
   * Energy resistance. Currently applied to everything that is not a physical
   * attacks like melee, ranged and charge. A value of {@link Integer#MAX_VALUE}
   * means energy immunity.
   */
  public int energyresistance=0;
  /**
   * Spell resistance.
   *
   * "To affect a creature that has spell resistance, a spellcaster must make a
   * caster level check (1d20 + caster level) at least equal to the creature’s
   * spell resistance."
   */
  public int sr=0;

  /** Available {@link Slot}s. Will not be cloned! */
  public Body body=null;
  /** Creatures that should only be spawned at night or underground. */
  public boolean nightonly=false;
  /** Immunity to critical hits. */
  public boolean immunitytocritical=false;
  /** Immune to the effects of {@link Poison}. */
  public boolean immunitytopoison=false;
  /** If <code>true</code> cannot be affected by {@link HoldMonster}. */
  public boolean immunitytoparalysis=false;
  /** Immune to mind-affecting effects. */
  public boolean immunitytomind=false;

  /**
   * Temporary {@link Monster#constitution} damage. 1 poison = -1 temporary
   * constitution score.
   */
  public int poison=0;
  /** Percent chance of an attack missing (1 = 100%). */
  public float misschance=0;
  /**
   * Since it's likely for units to advance in differnent classes instead of in
   * a single one due to having {@link Academy} locations and only usually one
   * class option at each, Base Attack Bonus is stored as a fraction if a
   * {@link ClassLevelUpgrade} isn't enough to bring it to a new digit.
   */
  public float babpartial;
  /**
   * A passive combatant represents something other than a {@link Monster}. They
   * usually don't act (at least in the normal sense) and have zero Challenge
   * Rating.
   */
  public boolean passive=false;
  /**
   * <code>true</code> if this stat block shoudl'nt be used normally but is
   * required for some internal game feature (examples being Settlers and Arena
   * buildings).
   */
  public boolean internal=false;
  /**
   * <code>true</code> for most monster. {@link Undead} and {@link Construct}
   * types don't heal naturally though.
   */
  public boolean heal=true;
  public Constrict constrict=null;
  /**
   * Counts skill ranks both from {@link #trained} and untrained {@link Skill}s.
   * Untrained skill usually come from the base monster stats.
   */
  public HashMap<String,Integer> ranks=new HashMap<>();
  /**
   * Trained {@link Skill}s are maximized every time a unit levels up.
   *
   * @see Skill#maximize(Monster)
   * @see ClassLevelUpgrade
   * @see #ranks
   */
  public HashSet<String> trained=new HashSet<>(0);
  public boolean elite=false;
  int ac;
  /** Lawful/chaotic and good/evil axis. */
  public Alignment alignment=new Alignment();
  /** Lowercase subtypes (eg. aquatic, reptilian, goblinoid...). */
  public List<String> subtypes=new ArrayList<>(1);

  @Override
  public Monster clone(){
    try{
      final var m=(Monster)super.clone();
      m.melee=copyattacks(melee);
      m.ranged=copyattacks(ranged);
      m.feats=m.feats.clone();
      m.hd=hd.clone();
      m.breaths=breaths.clone();
      m.ranks=(HashMap<String,Integer>)ranks.clone();
      m.trained=(HashSet<String>)trained.clone();
      if(touch!=null) m.touch=touch.clone();
      if(constrict!=null) m.constrict=constrict.clone();
      return m;
    }catch(final CloneNotSupportedException e){
      throw new RuntimeException(e);
    }
  }

  static private ArrayList<AttackSequence> copyattacks(
      final List<AttackSequence> original){
    final var copy=new ArrayList<AttackSequence>(original.size());
    for(final AttackSequence sequence:original) copy.add(sequence.clone());
    return copy;
  }

  public void addfeat(final Feat feat){
    if(feat.stack||!hasfeat(feat)){
      feats.add(feat);
      feats.sort(FeatByNameComparator.INSTANCE);
    }
  }

  public int countfeat(final Feat f){
    var i=0;
    for(final Feat existing:feats) if(existing.name.equals(f.name)) i+=1;
    return i;
  }

  public boolean hasfeat(final Feat f){
    for(final Feat existing:feats) if(existing.equals(f)) return true;
    return false;
  }

  /**
   * Rolls a d20 to determine a saving throw result. Note that for being random
   * this can only be used outside of {@link BattleAi} thinking.
   *
   * @param bonus {@link #getfortitude()}, {@link #getwill()} or {@link #ref}
   *   bonus. A value of {@link Integer#MAX_VALUE} represents automatic success.
   * @param dc Difficulty class target for the saving roll.
   * @return <code>true</code> on save success.
   */
  public boolean save(int bonus,int dc){
    if(bonus==Integer.MAX_VALUE) return true;
    var roll=RPG.r(1,20);
    if(roll==1) return false;
    if(roll==20) return true;
    return roll+bonus>=dc;
  }

  /**
   * @return Base attack bonus (0 or higher value).
   */
  public int getbab(){
    var classesbab=0;
    for(ClassLevelUpgrade classdata:ClassLevelUpgrade.classes){
      var level=classdata.getlevel(this);
      for(var i=1;i<=level;i++) classesbab+=classdata.advancebab(i);
    }
    var bab=originalhd*WeaponFocus.BAB.get(type);
    return classesbab+Long.valueOf(Math.round(bab)).intValue();
  }

  /**
   * @param modifierchange Raises {@link #strength} bonus by this many steps (+1
   *   bonus = +2 score points)
   */
  public void changestrengthmodifier(int modifierchange){
    changestrengthscore(modifierchange*2);
  }

  /**
   * Same as {@link #changestrengthmodifier(int)} but receives a score change
   * instead.
   */
  public void changestrengthscore(int scorechange){
    var m=AbilityModification.modify(strength,scorechange);
    strength=m.newscore;
    if(m.modifierchange==0) return;
    ArrayList<WeaponFinesse> finesses=getfeats(WeaponFinesse.SINGLETON);
    for(AttackSequence sequence:melee) attacks:for(Attack a:sequence){
      for(WeaponFinesse wf:finesses) if(wf.affects(a)) continue attacks;
      a.bonus+=m.modifierchange;
      a.damage[2]+=m.modifierchange;
    }
  }

  /**
   * @param modifierchange Raises {@link #dexterity} bonus by +1 this many times
   *   (+1 bonus = +2 ability score points).
   */
  public void changedexteritymodifier(int modifierchange){
    changedexterityscore(modifierchange*2);
  }

  /**
   * Same as {@link #changedexteritymodifier(int)} but receives an ability score
   * change instead.
   *
   * Assumes that the maximum dexterity bonus for the given armor is equal to
   * (8-{@link #armor}). This doesn't affect anything else besides allowing
   * dexterity to improve AC or not, internally.
   */
  public void changedexterityscore(int scorechange){
    var m=AbilityModification.modify(dexterity,scorechange);
    dexterity=m.newscore;
    var modifierchange=m.modifierchange;
    if(m.modifierchange==0) return;
    if(getbonus(dexterity)<=armor) ac+=modifierchange;
    ref+=modifierchange;
    var attacks=new ArrayList<Attack>();
    for(AttackSequence sequence:ranged) attacks.addAll(sequence);
    for(WeaponFinesse wf:getfeats(WeaponFinesse.SINGLETON))
      attacks.addAll(wf.getallaffected(this));
    for(Attack a:attacks) a.bonus+=modifierchange;
    initiative+=modifierchange;
  }

  /**
   * @param c Raises {@link #constitution} by +2...
   * @param modifierchange multiplied by this magnitude. Negative values
   *   allowed.
   * @return
   * @throws Death In case of zero or negative constitution.
   * @return <code>false</code> if this combatant has been killed during this
   *   operation.
   */
  public void changeconstitutionmodifier(Combatant c,int modifierchange){
    changeconstitutionscore(c,modifierchange*2);
  }

  /**
   * Same as {@link #changeconstitutionmodifier(Combatant, int)} but receives
   * score point changes isntead of modifier changes (2 score points = 1
   * modifier change).
   *
   * it's tricky to come back to the original state from <1hp/hd so just avoid
   * it for now. maybe it'll be easier when abiblities and skills are enums and
   * we can just have a simple AbilityDamage field or something like that TODO
   */
  public void changeconstitutionscore(Combatant c,int scorechange){
    var m=AbilityModification.modify(constitution,scorechange);
    if(constitution==m.newscore||m.modifierchange==0) return;
    final var hds=hd.count();
    var bonushp=hds*m.modifierchange;
    if(c.maxhp+bonushp<hds+hd.extrahp||c.hp+bonushp<1) return;
    constitution=m.newscore;
    fort+=m.modifierchange;
    hd.extrahp+=bonushp;
    c.hp+=bonushp;
    c.maxhp+=bonushp;
    if(c.hp>c.maxhp) c.hp=c.maxhp;
    for(BreathWeapon breath:breaths) breath.savedc+=m.modifierchange;
  }

  /**
   * Same as {@link #changeintelligencescore(int)} but receives a score change
   * instead (2 score points = 1 modifier point).
   */
  public void changeintelligencemodifier(int modifierchange){
    changeintelligencescore(modifierchange*2);
  }

  /**
   * Should give players more points for {@link Skills} but instead just lower
   * {@link #cr} considering how far behind the unit is form where it should be
   * - preventing all sorts of complications.
   *
   * @param newscore Raises {@link #intelligence} by this many ability score
   *   points (+2 point = +1 bonus modifier).
   */
  public void changeintelligencescore(int scorechange){
    intelligence=AbilityModification.modify(intelligence,scorechange).newscore;
  }

  /**
   * Same as {@link #changewisdomscore(int)} but receives a modifier instead (1
   * positive or negative modifier change = 2 score change).
   */
  public void changewisdommodifier(int modifierchange){
    changewisdomscore(modifierchange*2);
  }

  /**
   * TODO implement 0 == helpless (or something to that effect).
   *
   * @param scorechange Raises {@link #wisdom} by this many ability score points
   *   (+2 points = +1 bonus modifier).
   */
  public void changewisdomscore(int scorechange){
    var m=AbilityModification.modify(wisdom,scorechange);
    wisdom=m.newscore;
    if(m.modifierchange!=0) will+=m.modifierchange;
  }

  /**
   * @param modifierchange Raises {@link #charisma} modifer ( +1 bonus modifier
   *   == +2 score)
   */
  public void changecharismamodifier(int modifierchange){
    changecharismascore(modifierchange*2);
  }

  /**
   * Same as {@link #changecharismamodifier(int)} but receives a score change
   * instead.
   */
  public void changecharismascore(int scorechange){
    charisma=AbilityModification.modify(charisma,scorechange).newscore;
  }

  /**
   * @return All {@link Feat}s that are {@link Feat#equals(Object)} to the given
   *   parameter.
   * @see #getfeat(Feat)
   */
  public <K extends Feat> ArrayList<K> getfeats(K search){
    var found=new ArrayList<K>(0);
    for(Feat f:feats) if(f.equals(search)) found.add((K)f);
    return found;
  }

  /**
   * @return The first {@link Feat} that is {@link Feat#equals(Object)} to the
   *   given parameter.
   * @see #getfeats(Feat)
   */
  public <K extends Feat> K getfeat(K search){
    for(Feat f:feats) if(f.equals(search)) return (K)f;
    return null;
  }

  /**
   * @param ability Ability score.
   * @return Ability bonus.
   */
  static public int getbonus(int ability){
    return Long.valueOf(Math.round(Math.floor(ability/2.0-5.0))).intValue();
  }

  @Override
  public String toString(){
    return customName==null?name:customName;
  }

  /**
   * @return {@link Integer#MAX_VALUE} if immune, will saving throw bonus
   *   otherwise.
   */
  public int getwill(){
    return immunitytomind?Integer.MAX_VALUE:will;
  }

  public static String getsignedbonus(int score){
    var bonus=Monster.getbonus(score);
    return bonus>=0?"+"+bonus:Integer.toString(bonus);
  }

  public void setwill(int willp){
    will=willp;
  }

  /**
   * @param delta Adds this bonus to willpower saving throws, even if
   *   {@link #getwill()} returns {@link Integer#MAX_VALUE}.
   */
  public void addwill(int delta){
    will+=delta;
  }

  @Override
  public boolean equals(Object obj){
    var m=(Monster)obj;
    return name.equals(m.name);
  }

  @Override
  public int hashCode(){
    return name.hashCode();
  }

  /**
   * TODO could probably be turned into a map
   *
   * @return A more computational-friendly version of {@link #size} where small
   *   is 1/2, medium is 1, large is 2...
   * @see Squad#eat()
   */
  public float size(){
    switch(size){
      case Size.FINE:
        return 1/16f;
      case Size.DIMINUTIVE:
        return 1/8f;
      case Size.TINY:
        return 1/4f;
      case Size.SMALL:
        return 1/2f;
      case Size.MEDIUM:
        return 1;
      case Size.LARGE:
        return 2;
      case Size.HUGE:
        return 4;
      case Size.GARGANTUAN:
        return 8;
      case Size.COLOSSAL:
        return 16;
      default:
        throw new RuntimeException("Unknown size "+size);
    }
  }

  /**
   * Monsters such as {@link Construct} and {@link Undead} with 0
   * {@link #constitution} always pass fortitude checks.
   *
   * @return The fortitude saving throw bonus of {@link Integer#MAX_VALUE} if
   *   immune.
   */
  public int getfortitude(){
    return constitution==0?Integer.MAX_VALUE:fort;
  }

  /**
   * @param minimum Minimum intelligence bonus required. -1 or higher is
   *   primitive intelligence.
   * @return <code>true</code> if able to communicate and think reasonably well.
   *   Being able to understand and follow simple, general tasks should do
   *   (which is not the case for animals that can only follow a few specific,
   *   well-trained tasks).
   */
  public boolean think(int minimum){
    return Monster.getbonus(intelligence)>=minimum;
  }

  /**
   * @return a -0, -2 or -4 skill check penalty depending on time of day and
   *   {@link Monster} vision.
   *
   * @see Javelin#getperiod()
   * @see #vision
   * @see Combatant#perceive(String)
   * @see Combatant#view(String)
   * @see Skills#perceive(Monster, boolean)
   */
  public int see(){
    if(vision==VISION_DARK) return 0;
    var verydark=Period.NIGHT.is()||Weather.current==Weather.STORM;
    if(vision==VISION_LOWLIGHT){
      if(verydark) return -2;
    }else{ // normal vision
      if(Period.EVENING.is()||Weather.current==Weather.RAIN) return -2;
      if(verydark) return -4;
    }
    return 0;
  }

  /**
   * @return All terrain types this monster can be found in. May be empty if
   *   this is an internal Javelin monster.
   * @see Terrain#toString()
   */
  public List<String> getterrains(){
    return TERRAINDATA.get(name);
  }

  /**
   * @return 0 if cannot swim or fly over water.
   * @see #swim
   * @see #fly
   */
  public int swim(){
    return Math.max(swim,fly);
  }

  /**
   * @return All attacks in every {@link AttackSequence} in {@link #melee} and
   *   {@link #ranged}.
   */
  public ArrayList<Attack> getattacks(){
    var attacks=new ArrayList<Attack>(melee.size()+ranged.size());
    for(var sequence:melee) attacks.addAll(sequence);
    for(var sequence:ranged) attacks.addAll(sequence);
    return attacks;
  }

  /**
   * Represents a general daily upkeep for this unit, not considering a
   * emrcenary's fee.
   *
   * @return How much food this units eats in gold pieces per day.
   * @see MercenariesGuild#getfee(Monster)
   */
  public float eat(){
    return size()/2f;
  }

  public boolean isalive(){
    return type!=MonsterType.UNDEAD&&type!=MonsterType.CONSTRUCT;
  }

  public boolean isaquatic(){
    var terrains=getterrains();
    return terrains.size()==1
        &&terrains.get(0).equalsIgnoreCase(Terrain.WATER.name);
  }

  /**
   * @return Unmodified Armor Class.
   * @see Combatant#getac()
   */
  public int getrawac(){
    return ac;
  }

  /**
   * @param ac Sets this as the unmodified Armor Class.
   */
  public void setrawac(int ac){
    this.ac=ac;
  }

  /**
   * @param name Monster type. Example: orc, kobold, young white dragon... Case
   *   insensitive.
   * @return A clone.
   * @see Monster#clone()
   */
  public static Monster get(String name){
    Monster monster=null;
    for(Monster m:Monster.ALL) if(m.name.equalsIgnoreCase(name)){
      monster=m.clone();
      break;
    }
    if(monster==null) return null;
    ChallengeCalculator.calculatecr(monster);
    return monster;
  }

  /**
   * TODO a collection would make more sense
   */
  public static List<Monster> get(MonsterType type){
    var monsters=new ArrayList<Monster>();
    for(Monster m:Monster.ALL) if(m.type==type) monsters.add(m);
    return monsters;
  }
}
