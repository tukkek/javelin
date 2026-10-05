package javelin.controller.content.fight.mutator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.content.fight.Fight;
import javelin.controller.content.map.Map;
import javelin.model.unit.Combatant;

/**
 * Friendly {@link Fight}s are not to-the-death but rather to an arbitrary
 * {@link Combatant#getstatus()}. If an unit dies, the death occrus as normal
 * but they are generally removed from the {@link Map} before that occurs. As
 * such, it's very unlikely to lose the game during a friendly battle.
 *
 * @author alex
 */
public class Friendly extends Mutator{
  static final String REMOVED="%s is removed from the battlefield!";

  /** @see Combatant#getnumericstatus() */
  public int removeat;

  /** Constructor. */
  public Friendly(int removeat){
    this.removeat=removeat;
  }

  @Override
  public void endturn(Fight f){
    super.endturn(f);
    var s=Fight.state;
    for(var t:List.of(s.blueteam,s.redteam)) for(var c:new ArrayList<>(t)){
      if(c.getnumericstatus()>removeat) continue;
      t.remove(c);
      if(t==s.blueteam) s.fleeing.add(c);
      else if(t==s.redteam) s.dead.add(c);
      if(s.next==c) s.next();
      Javelin.redraw();
      Javelin.message(String.format(REMOVED,c),true);
    }
  }

  @Override
  public void end(Fight f){
    super.end(f);
    var s=Fight.state;
    var survivors=s.dead.stream().filter(d->d.hp>Combatant.DEADATHP)
        .collect(Collectors.toList());
    s.dead.removeAll(survivors);
    s.blueteam.addAll(survivors);
  }
}
