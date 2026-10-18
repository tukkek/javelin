package javelin.model.item.artifact;

import javelin.Javelin;
import javelin.model.transport.FlyingNimbus;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;

/**
 * Summons a {@link FlyingNimbus}.
 *
 * @author alex
 */
public class Flute extends Artifact{
  /** Constructor. */
  public Flute(){
    super("Flute of Wind");
    usedinbattle=false;
    usedoutofbattle=true;
  }

  @Override
  protected boolean activate(Combatant user){
    var text="You play the flute but nothing happens. Try it outside next time!";
    if(Dungeon.active==null){
      Squad.active.transport=new FlyingNimbus(Squad.active.transport);
      Squad.active.updateavatar();
      text="You are taken by a "+Squad.active.transport+"!";
    }
    Javelin.message(text,false);
    return true;
  }
}
