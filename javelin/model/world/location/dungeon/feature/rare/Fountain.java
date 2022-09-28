package javelin.model.world.location.dungeon.feature.rare;

import java.awt.Image;
import java.util.List;

import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.DungeonImages;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.town.labor.religious.Shrine;
import javelin.view.Images;

/**
 * Heals hit points and spell uses.
 *
 * TODO could also use a {@link Shrine} dungeon features. Spell#isritual should
 * make it simple enough.
 *
 * @author alex
 */
public class Fountain extends Feature{
  static final String PROMPT="Do you want to drink from the fountain?\n"
      +"Press ENTER to drink or any other key to cancel...";

  /** Constructor. */
  public Fountain(DungeonFloor f){
    super("fountain");
  }

  @Override
  public boolean activate(){
    if(Javelin.prompt(PROMPT)!='\n') return false;
    for(Combatant c:Squad.active.members) heal(c);
    Squad.active.equipment.refresh(24);
    Javelin.message("Party fully recovered!",false);
    return true;
  }

  public static void heal(Combatant c){
    c.detox(c.source.poison);
    c.heal(c.maxhp,true);
    for(var s:c.spells) s.used=0;
    if(Squad.active.members.contains(c))
      for(var e:Squad.active.equipment.get(c)) e.refresh(24);
  }

  @Override
  public Image getimage(){
    var i=Dungeon.active.dungeon.images.get(DungeonImages.FOUNTAIN);
    return Images.get(List.of("dungeon","fountain",i));
  }
}
