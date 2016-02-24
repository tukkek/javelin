// package javelin.controller.tournament;
//
// import java.util.ArrayList;
// import java.util.Comparator;
// import java.util.HashMap;
// import java.util.List;
//
// import javelin.Javelin;
// import javelin.controller.challenge.ChallengeRatingCalculator;
// import javelin.controller.exception.UnbalancedTeamsException;
// import javelin.controller.exception.battle.StartBattle;
// import javelin.controller.fight.ExhibitionFight;
// import javelin.controller.upgrade.DamageReduction;
// import javelin.controller.upgrade.EnergyResistance;
// import javelin.controller.upgrade.FastHealing;
// import javelin.controller.upgrade.Flying;
// import javelin.controller.upgrade.NaturalArmor;
// import javelin.controller.upgrade.SpellResistance;
// import javelin.controller.upgrade.Swimming;
// import javelin.controller.upgrade.Upgrade;
// import javelin.controller.upgrade.Vision;
// import javelin.controller.upgrade.WalkingSpeed;
// import javelin.controller.upgrade.ability.RaiseConsitution;
// import javelin.controller.upgrade.ability.RaiseDexterity;
// import javelin.controller.upgrade.ability.RaiseWisdom;
// import javelin.controller.upgrade.classes.Aristocrat;
// import javelin.controller.upgrade.classes.Commoner;
// import javelin.controller.upgrade.classes.Expert;
// import javelin.controller.upgrade.classes.Warrior;
// import javelin.controller.upgrade.damage.MeleeDamage;
// import javelin.controller.upgrade.damage.RangedDamage;
// import javelin.controller.upgrade.feat.BullRushUpgrade;
// import javelin.controller.upgrade.feat.CleaveUpgrade;
// import javelin.controller.upgrade.feat.GreatCleaveUpgrade;
// import javelin.controller.upgrade.feat.GreatFortitude;
// import javelin.controller.upgrade.feat.ImprovedInititative;
// import javelin.controller.upgrade.feat.ImprovedPreciseShot;
// import javelin.controller.upgrade.feat.IronWill;
// import javelin.controller.upgrade.feat.LightningReflexes;
// import javelin.controller.upgrade.feat.MeleeFocus;
// import javelin.controller.upgrade.feat.PointBlankShot;
// import javelin.controller.upgrade.feat.PowerAttackUpgrade;
// import javelin.controller.upgrade.feat.PreciseShot;
// import javelin.controller.upgrade.feat.RangedFocus;
// import javelin.controller.upgrade.feat.RapidShot;
// import javelin.controller.upgrade.feat.Toughness;
// import javelin.model.spell.DayLight;
// import javelin.model.spell.DeeperDarkness;
// import javelin.model.spell.DominateMonster;
// import javelin.model.spell.Heroism;
// import javelin.model.spell.HoldMonster;
// import javelin.model.spell.SlayLiving;
// import javelin.model.spell.totem.BearsEndurance;
// import javelin.model.spell.totem.BullsStrength;
// import javelin.model.spell.totem.CatsGrace;
// import javelin.model.spell.totem.OwlsWisdom;
// import javelin.model.spell.wounds.CureCriticalWounds;
// import javelin.model.spell.wounds.CureLightWounds;
// import javelin.model.spell.wounds.CureModerateWounds;
// import javelin.model.spell.wounds.CureSeriousWounds;
// import javelin.model.spell.wounds.InflictCriticalWounds;
// import javelin.model.spell.wounds.InflictLightWounds;
// import javelin.model.spell.wounds.InflictModerateWounds;
// import javelin.model.spell.wounds.InflictSeriousWounds;
// import javelin.model.unit.Combatant;
// import javelin.model.unit.Monster;
// import tyrant.mikera.engine.RPG;
//
/// **
// * {@link Upgrade}s a set of monsters, one of each of the {@link #ROLES}.
// *
// * TODO this currently needs to be manually updated as new {@link Upgrade}s
// make
// * it into the game.
// *
// * @author alex
// */
// public class SquadExhibition extends Exhibition {
// enum Role {
// STRONG, FAST, HOLY, TOUCHED,
// }
//
// public static final ArrayList<Upgrade> pathstrong =
// new ArrayList<Upgrade>();
// public static final ArrayList<Upgrade> pathfast = new ArrayList<Upgrade>();
// public static final ArrayList<Upgrade> pathholy = new ArrayList<Upgrade>();
// public static final ArrayList<Upgrade> pathtouched =
// new ArrayList<Upgrade>();
//
// static {
// pathstrong.add(new DamageReduction());
// pathstrong.add(new RaiseConsitution());
// pathstrong.add(new Warrior(""));
// pathstrong.add(new MeleeDamage(""));
// pathstrong.add(new BullRushUpgrade());
// pathstrong.add(new CleaveUpgrade());
// pathstrong.add(new GreatCleaveUpgrade());
// pathstrong.add(new GreatFortitude());
// pathstrong.add(new MeleeFocus(""));
// pathstrong.add(new PowerAttackUpgrade());
// pathstrong.add(new Toughness());
// pathstrong.add(new NaturalArmor("", 2));
// pathstrong.add(new NaturalArmor("", 4));
// pathstrong.add(new NaturalArmor("", 6));
// pathstrong.add(new NaturalArmor("", 8));
// pathstrong.add(new Vision("", 1));
// pathfast.add(new Expert(""));
// pathfast.add(new RaiseDexterity());
// pathfast.add(new RangedDamage(""));
// pathfast.add(new ImprovedPreciseShot());
// pathfast.add(new ImprovedInititative());
// pathfast.add(new LightningReflexes());
// pathfast.add(new PointBlankShot());
// pathfast.add(new PreciseShot());
// pathfast.add(new RangedFocus(""));
// pathfast.add(new RapidShot());
// pathfast.add(new Swimming("", 40));
// pathfast.add(new Vision("", 1));
// pathfast.add(new Vision("", 2));
// pathfast.add(new WalkingSpeed("", 50));
// pathholy.add(new EnergyResistance());
// pathholy.add(new RaiseWisdom());
// pathholy.add(new Commoner(""));
// pathholy.add(new MeleeDamage(""));
// pathholy.add(new IronWill());
// pathholy.add(new Vision("Low-light vision", 1));
// pathholy.add(new CureLightWounds());
// pathholy.add(new CureModerateWounds());
// pathholy.add(new CureSeriousWounds());
// pathholy.add(new CureCriticalWounds());
// pathholy.add(new BearsEndurance());
// pathholy.add(new BullsStrength());
// pathholy.add(new CatsGrace());
// pathholy.add(new Heroism());
// pathholy.add(new OwlsWisdom());
// pathtouched.add(new SpellResistance());
// pathtouched.add(new Aristocrat(""));
// pathtouched.add(new RangedDamage(""));
// pathtouched.add(new FastHealing());
// pathtouched.add(new Flying("", 40));
// pathtouched.add(new Vision("Darkvision", 2));
// pathtouched.add(new InflictLightWounds());
// pathtouched.add(new InflictModerateWounds());
// pathtouched.add(new InflictSeriousWounds());
// pathtouched.add(new InflictCriticalWounds());
// pathtouched.add(new DeeperDarkness());
// pathtouched.add(new DominateMonster());
// pathtouched.add(new HoldMonster());
// pathtouched.add(new SlayLiving());
// pathtouched.add(new DayLight());
// }
//
// public static final Role[] ROLES = Role.values();
//
// private class SquadFight extends ExhibitionFight {
// HashMap<Combatant, Role> roles = new HashMap<Combatant, Role>();
//
//
// }
//
// public SquadExhibition() {
// super("Squad");
// }
//
// @Override
// public void start() {
// throw new StartBattle(new SquadFight());
// }
// }
