package javelin.model.unit.abilities.spell.conjuration.healing.raise;

import java.util.Set;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;

/** Also features "restoration", implicitly. See the d20 SRD for more info. */
public class RaiseDead extends Spell{
  static final float RESTORATIONCR=ChallengeCalculator.ratespell(4);
  static final String PROMPT="""
      Revive %s?
      Press r to revive or g to let go of this unit...
      """;
  static final Set<Character> KEYS=Set.of('r','g');

  /** Constructor. */
  public RaiseDead(){
    super("Raise dead",5,ChallengeCalculator.ratespell(5)+RESTORATIONCR);
    components=5000;
    castinbattle=false;
  }

  /** Constructor. */
  public RaiseDead(String name,int levelp,float incrementcost){
    super(name,levelp,incrementcost);
  }

  @Override
  public boolean validate(Combatant caster,Combatant target){
    return Javelin.prompt(PROMPT.formatted(target),KEYS)=='r';
  }

  @Override
  public String castpeacefully(Combatant caster,Combatant target){
    target.hp=target.source.hd.count();
    return null;
  }
}
