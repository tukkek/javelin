/**
 *
 */
package javelin.model.world.location.dungeon.feature;

import javelin.Javelin;
import javelin.controller.content.action.world.WorldMove;
import javelin.controller.generator.WorldGenerator;
import javelin.controller.table.dungeon.BranchTable;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.Portal;
import javelin.model.world.location.dungeon.branch.Branch;
import javelin.old.RPG;

/** Portal to a {@link Branch}ed {@link Dungeon}. */
public class BranchPortal extends Feature{
  static final String PROMPT="Do you want to enter this portal to: %s?\n"
      +"Press ENTER to confirm or any other key to cancel...";
  static final String UNKNOWN="an unknown branch";
  /**
   * Ideally would be 2 but that's too costly for {@link WorldGenerator} for
   * development purposes. 3 is MUCH faster and not a terrible compromise.
   */
  static final int NEWFLOORCHANCE=3;

  /** A {@link Branch}ed dungeon. */
  public Dungeon destination;

  /** Constructor. */
  public BranchPortal(DungeonFloor f){
    super("portal");
    remove=false;
    var maxfloors=f.dungeon.floors.size()-1;
    if(maxfloors<1) maxfloors=1;
    var floors=1;
    while(floors<maxfloors&&RPG.chancein(NEWFLOORCHANCE)) floors+=1;
    var level=f.level;
    while(RPG.chancein(2)) level+=1;
    destination=new Dungeon("Branch",level,floors);
    destination.branches.addAll(f.gettable(BranchTable.class).rollaffixes());
    destination.exit=f;
  }

  @Override
  public boolean activate(){
    var dc=10+destination.level;
    var p=String.format(PROMPT,Portal.discover(dc)?destination:UNKNOWN);
    if(Javelin.prompt(p)!='\n') return false;
    destination.floors.get(0).enter();
    WorldMove.abort=true;
    return true;
  }
}
