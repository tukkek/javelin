package javelin.model.world.location.town.labor.cultural;

import java.util.ArrayList;
import java.util.stream.Collectors;

import javelin.controller.content.kit.Kit;
import javelin.controller.content.kit.wizard.Wizard;
import javelin.controller.content.upgrade.Upgrade;
import javelin.controller.content.upgrade.ability.RaiseCharisma;
import javelin.controller.content.upgrade.ability.RaiseWisdom;
import javelin.controller.content.upgrade.classes.Aristocrat;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.world.location.academy.Academy;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Labor;
import javelin.old.RPG;

/**
 * Teaches spells from a certain school of magic or specialization.
 *
 * @author alex
 */
public class MagesGuild extends Academy{
  /**
   * {@link Town} {@link Labor}.
   *
   * @author alex
   */
  public static class BuildMagesGuild extends BuildAcademies{
    /** Constructor. */
    public BuildMagesGuild(){
      super(Rank.VILLAGE);
    }

    @Override
    public Academy generateacademy(){
      var guilds=Kit.KITS.stream().filter(k->k instanceof Wizard)
          .collect(Collectors.toList());
      return new MagesGuild((Wizard)RPG.pick(guilds));
    }
  }

  static class HedgeWizard extends Wizard{
    protected HedgeWizard(){
      super("Hedge wizard",RaiseWisdom.SINGLETON);
    }

    @Override
    protected void define(){
      basic.add(RaiseCharisma.SINGLETON);
      basic.addAll(Spell.SPELLS.stream().filter(s->s.level<=1).toList());
    }

    @Override
    protected void extend(){
      //truly "basic", let extensions be for proper mages only
    }
  }

  /**
   * @see Academy#Academy(String, String, int, int, java.util.Set,
   *   javelin.controller.content.upgrade.ability.RaiseAbility,
   *   javelin.controller.content.upgrade.classes.ClassLevelUpgrade)
   */
  public MagesGuild(Wizard w){
    super(w.name+"s guild","Mages guild",0,0,w.getupgrades(),w.ability,
        Aristocrat.SINGLETON);
    while(upgrades.size()>20){
      var u=RPG.pick(upgrades);
      if(u instanceof Spell) upgrades.remove(u);
    }
    var ascending=new ArrayList<Spell>(upgrades.size());
    for(Upgrade u:upgrades) if(u instanceof Spell) ascending.add((Spell)u);
    ascending.sort((o1,o2)->o1.casterlevel-o2.casterlevel);
    minlevel=ascending.get(0).casterlevel;
    maxlevel=ascending.get(ascending.size()-1).casterlevel;
    if(maxlevel>10) maxlevel=10;
  }

  @Override
  public String getimagename(){
    return "magesguild";
  }

  /** @see #makebasic() */
  static public MagesGuild makebasicmage(){
    return new MagesGuild(new HedgeWizard());
  }
}
