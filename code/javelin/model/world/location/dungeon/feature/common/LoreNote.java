/**
 *
 */
package javelin.model.world.location.dungeon.feature.common;

import javelin.Javelin;
import javelin.controller.table.dungeon.feature.FeatureModifierTable;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonEntrance;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.Lore;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.old.RPG;

/**
 * Discovers {@link Lore} about another {@link DungeonFloor}. Generated
 * on-demand and not while generating {@link DungeonFloor} so we can have access
 * to all {@link DungeonFloor#lore}.
 *
 * @author alex
 */
public class LoreNote extends Feature{
  static final String SUCCESS="""
      In this book you find the following information about %s:
      - %s\n
      (You can view all your lore notes at any time through the Known Lore screen).""";
  static final String FAILURE="%s could not extract any useful information from this text...";

  int dc;

  /** Constructor. */
  public LoreNote(DungeonFloor f){
    super("lore note");
    dc=10+f.level+f.gettable(FeatureModifierTable.class).roll();
  }

  boolean validate(Dungeon d){
    var active=Dungeon.active;
    if(d==active.dungeon) return false;
    var tier=active.gettier().tier;
    var level=d.level;
    return tier.minlevel<=level&&level<=tier.maxlevel+5;
  }

  @Override
  public boolean activate(){
    var reader=Squad.active.getbest(Skill.KNOWLEDGE);
    if(reader.taketen(Skill.KNOWLEDGE)<dc){
      Javelin.message(String.format(FAILURE,reader),false);
      return false;
    }
    remove();
    var entrances=DungeonEntrance.getdungeonsandtemples().stream()
        .filter(e->validate(e.dungeon)).toList();
    var entrance=RPG.pick(entrances);
    entrance.reveal();
    var dungeon=entrance.dungeon;
    var lore=RPG.pick(dungeon.lore);
    lore.discovered=true;
    Javelin.message(SUCCESS.formatted(dungeon,lore),true);
    return false;
  }
}
