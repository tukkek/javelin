package javelin.model.world.location.town.labor.basic;

import java.util.ArrayList;
import java.util.Set;

import javelin.controller.challenge.Tier;
import javelin.controller.content.scenario.Campaign;
import javelin.controller.content.upgrade.NaturalArmor;
import javelin.controller.content.upgrade.Upgrade;
import javelin.controller.content.upgrade.ability.RaiseCharisma;
import javelin.controller.content.upgrade.ability.RaiseConstitution;
import javelin.controller.content.upgrade.ability.RaiseDexterity;
import javelin.controller.content.upgrade.ability.RaiseIntelligence;
import javelin.controller.content.upgrade.ability.RaiseStrength;
import javelin.controller.content.upgrade.ability.RaiseWisdom;
import javelin.controller.content.upgrade.classes.Aristocrat;
import javelin.controller.content.upgrade.classes.Commoner;
import javelin.controller.content.upgrade.classes.Expert;
import javelin.controller.content.upgrade.classes.Warrior;
import javelin.controller.content.upgrade.damage.MeleeDamage;
import javelin.controller.content.upgrade.damage.RangedDamage;
import javelin.model.Realm;
import javelin.model.item.Item;
import javelin.model.unit.Squad;
import javelin.model.world.location.Academy;
import javelin.model.world.location.Guild;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Labor;

/**
 * A non-{@link Labor} location that is spawned in the starting area of a
 * {@link Campaign}. The {@link Upgrade}s within are fixed and hand-selected to
 * be of most value for a starting, low-level player. It cannot be further
 * upgraded.
 *
 * The main benefit of a {@link BasicAcademy} over {@link Realm} Academies (or a
 * random {@link Guild}) is that it prevents, to a reasonable extent, players
 * from feeling like they need to start a new game repeatedly until they get the
 * most optimal set of circunstances, thus giving incentives for them to bore
 * themselves.
 *
 * This {@link Location} also tackles the problem of players needing to have
 * {@link Upgrade} options as soon as possible in their games. The initial
 * solution was Realm academies, which are now unfeasible without {@link Realm}s
 * being linked to {@link Item}s or {@link Upgrade}s. The next solution was to
 * just put a random {@link Guild} in each {@link Town} but that would mean only
 * being able to level up a single play style for the player's whole
 * {@link Squad}. This approach allows for players to quickly start working on
 * all their unit's desired builds while still largely relying on the randomness
 * of building more interesting in-depth Guilds to further advance those builds.
 *
 * @see Shop#newbasic()
 * @author alex
 */
public class BasicAcademy extends Academy{
  static final String DESCRIPTIONBASIC="Academy";

  /** Constructor. */
  public BasicAcademy(){
    super(DESCRIPTIONBASIC,DESCRIPTIONBASIC,Tier.LOW.minlevel,Tier.LOW.maxlevel,
        Set.of(),null,null);
    /* TODO not ideal but the starting academy keeps being snatched by enemy
     * incursions, making further progress impossible. The ideal solution would
     * be to make it a higher CR or allow other forms of advacenemnt. */
    impermeable=true;
    /* TODO raise damage isn't "proper" for low-level characters but there's not
     * any other good "generic" alternatives for now. It's not intended that
     * players should return to the basic academy during mid- and late- game to
     * raise damage. */
    upgrades.addAll(Set.of(RaiseStrength.SINGLETON,RaiseDexterity.SINGLETON,
        RaiseConstitution.SINGLETON,RaiseIntelligence.SINGLETON,
        RaiseWisdom.SINGLETON,RaiseCharisma.SINGLETON,Commoner.SINGLETON,
        Warrior.SINGLETON,Expert.SINGLETON,Aristocrat.SINGLETON,
        NaturalArmor.LEATHER,NaturalArmor.SCALES,NaturalArmor.PLATES,
        MeleeDamage.INSTANCE,RangedDamage.INSTANCE));
  }

  @Override
  public ArrayList<Labor> getupgrades(District d){
    return new ArrayList<>(0);
  }
}
