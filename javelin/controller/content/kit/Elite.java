package javelin.controller.content.kit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javelin.controller.content.upgrade.Upgrade;
import javelin.controller.content.upgrade.classes.ClassLevelUpgrade;
import javelin.controller.content.upgrade.classes.Commoner;
import javelin.controller.content.upgrade.classes.Expert;
import javelin.controller.content.upgrade.classes.Warrior;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.old.RPG;

/**
 * A {@link ClassLevelUpgrade}-only kit for non-thinking {@link Monster}s.
 *
 * TODO this is better than the current methods to generate elites
 *
 * @see Monster#think(int)
 * @author alex
 */
public class Elite extends Kit{
  /** Global instance. */
  public static final Elite SINGLETON=new Elite();

  record SortedClass(int ability,ClassLevelUpgrade upgrade){
    //
  }

  class EliteUpgrade extends Upgrade{
    public EliteUpgrade(){
      super("Elite");
    }

    @Override
    public String inform(Combatant c){
      throw new UnsupportedOperationException();
    }

    @Override
    protected boolean apply(Combatant c){
      var s=c.source;
      var upgrades=List.of(new SortedClass(s.strength,Warrior.SINGLETON),
          new SortedClass(s.dexterity,Expert.SINGLETON),
          new SortedClass(s.constitution,Commoner.SINGLETON));
      upgrades=RPG.shuffle(new ArrayList<>(upgrades)).stream()
          .sorted(Comparator.comparing(u->u.ability)).toList();
      for(var u:upgrades) if(u.upgrade.apply(c)) return true;
      return false;
    }
  }

  Elite(){
    super("Primitive elite",null,null,null);
  }

  @Override
  protected void define(){
    basic.clear();
    basic.add(new EliteUpgrade());
  }

  @Override
  protected void extend(){
    //don't
  }
}
