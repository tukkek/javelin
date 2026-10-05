package javelin.model.unit.skill;

import javelin.controller.content.action.world.meta.help.Guide;
import javelin.model.unit.Combatant;

/**
 * @see Guide#SKILLS
 * @author alex
 */
public class UseMagicDevice extends Skill{
  /** Constructor. */
  public UseMagicDevice(){
    super("Use magic device",Ability.CHARISMA);
    usedincombat=true;
    intelligent=true;
  }

  /** @return Emulates an ability score. */
  public static int getability(Combatant c){
    return c.taketen(Skill.USEMAGICDEVICE)-15;
  }
}
