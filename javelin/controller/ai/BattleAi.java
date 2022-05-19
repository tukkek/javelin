package javelin.controller.ai;

import java.util.List;

import javelin.controller.ai.valueselector.ValueSelector;
import javelin.controller.content.action.ai.AiMovement;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.condition.Defending;

/**
 * Javelin's implementation of {@link AlphaBetaSearch}.
 *
 * @author alex
 */
public class BattleAi extends AlphaBetaSearch{
  /**
   * Ideally should use something that will never be reached by the
   * {@link #ratechallenge(List)} but not any higher.
   */
  private static final float LIMIT=Float.MAX_VALUE;

  /** Constructor. */
  public BattleAi(final int aiDepth){
    super(aiDepth);
  }

  @Override
  protected Node catchMemoryIssue(final Error e){
    throw e;
  }

  @Override
  public float utility(final Node node){
    final var state=(BattleState)node;
    final var redTeam=BattleAi.ratechallenge(state.getredteam());
    if(redTeam==0f) return LIMIT;
    final var blueTeam=BattleAi.ratechallenge(state.getblueteam());
    if(blueTeam==0f) return -LIMIT;
    return redTeam-measuredistances(state.redteam,state.blueteam)
        -state.meld.size()-defending(state)
        -(blueTeam-measuredistances(state.blueteam,state.redteam));
  }

  static float defending(BattleState state){
    var ndefending=0;
    for(Combatant c:state.redteam)
      if(c.hascondition(Defending.class)!=null) ndefending+=1;
    return ndefending;
  }

  static private float ratechallenge(final List<Combatant> team){
    var challenge=0f;
    for(final Combatant c:team) challenge+=c.source.cr*(1+c.hp/(float)c.maxhp);
    return challenge;
  }

  static float measuredistances(List<Combatant> usp,List<Combatant> themp){
    var us=usp.stream().map(Combatant::getlocation).toList();
    var them=themp.stream().map(Combatant::getlocation).toList();
    return AiMovement.score(us,them)/125f;
  }

  @Override
  public boolean terminalTest(final Node node){
    final var state=(BattleState)node;
    return state.redteam.isEmpty()||state.blueteam.isEmpty();
  }

  @Override
  public ValueSelector getplayer(Node node){
    var s=(BattleState)node;
    return s.blueteam.contains(s.next)?minValueSelector:maxValueSelector;
  }
}
