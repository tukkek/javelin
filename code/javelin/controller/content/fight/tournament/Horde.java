package javelin.controller.content.fight.tournament;

import java.util.ArrayList;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.content.fight.ExhibitionFight;
import javelin.controller.exception.UnbalancedTeams;
import javelin.controller.exception.battle.StartBattle;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;

/**
 * Exhibition against many opponents.
 *
 * @author alex
 */
public class Horde extends Exhibition{
  /** Constructor. */
  public Horde(){
    super("Horde");
  }

  @Override
  public void start(){
    throw new StartBattle(new ExhibitionFight(){
      @Override
      public ArrayList<Combatant> getfoes(Integer teamel){
        crloop:for(Monster m:new CrIterator(Monster.BYCR.descendingMap())){
          var opponents=new ArrayList<Combatant>();
          var el=0;
          for(var i=0;i<Squad.active.members.size()+5;i++){
            opponents.add(new Combatant(m.clone(),true));
            try{
              el=ChallengeCalculator.calculateelsafe(opponents);
            }catch(UnbalancedTeams e){
              continue crloop;
            }
            if(el>teamel) continue crloop;
            if(el==teamel) return opponents;
          }
        }
        throw new RuntimeException("Couldn't generate Horde exhibition");
      }
    });
  }
}
