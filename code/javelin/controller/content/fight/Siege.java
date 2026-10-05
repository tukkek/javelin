package javelin.controller.content.fight;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.content.fight.mutator.Meld;
import javelin.controller.content.terrain.Terrain;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.Town;
import javelin.view.screen.BattleScreen;

/**
 * Battle when a player invades a hostile town.
 *
 * @see Town#ishostile()
 *
 * @author alex
 */
public class Siege extends Fight{
  public Location location;
  /**
   * If <code>false</code> will skip
   * {@link #onEnd(BattleScreen, ArrayList, BattleState)} but still call
   * {@link Fight#onEnd(BattleScreen, ArrayList, BattleState)}. This allows
   * subclasses to take control of after-fight consequences.
   */
  protected boolean cleargarrison=true;

  /**
   * @param l Where this fight is occurring at.
   */
  public Siege(Location l){
    location=l;
    hide=false;
    mutators.add(new Meld());
    terrains=List.of(Terrain.get(l.x,l.y));
    var d=l.getdistrict();
    if(d!=null) map=d.town.getmap();
  }

  @Override
  public ArrayList<Combatant> getfoes(Integer teamel){
    var clones=new ArrayList<Combatant>(location.garrison);
    for(var i=0;i<clones.size();i++)
      clones.set(i,clones.get(i).clone().clonesource());
    return clones;
  }

  @Override
  public void bribe(){
    afterwin();
    location.realm=null;
  }

  @Override
  public boolean onend(){
    if(cleargarrison){
      if(Fight.victory) afterwin();
      else afterlose();
      /* TODO this should probably be inside afterwin() */
      if(location.garrison.isEmpty()) location.capture();
    }
    return super.onend();
  }

  /**
   * TODO ideally would have a d10 check to give a chance for negative
   * {@link Combatant}s in {@link BattleState#dead} to recover: for example, a
   * character that ends with -5hp should have a 50% of immediately recovering
   * to full HP instead of being removed from {@link Location#garrison}. 0hp
   * would mean 100% recovery chance while -10 or less would be guaranteed
   * removal.
   */
  protected void afterlose(){
    location.garrison.removeAll(state.dead);
  }

  protected void afterwin(){
    location.garrison.clear();
  }
}
