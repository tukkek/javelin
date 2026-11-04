package javelin.controller.content.fight.mutator;

import javelin.Debug;
import javelin.controller.content.fight.Fight;

/// Used to {@link Debug}.
public class Freeze extends Mutator{
  @Override
  public void draw(Fight f){
    super.draw(f);
    var state=Fight.state;
    for(var c:state.getcombatants())
      if(c.automatic||state.redteam.contains(c)) c.ap=9_000;
  }
}
