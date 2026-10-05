package javelin.controller.content.action.world;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.old.messagepanel.MessagePanel;
import javelin.view.screen.WorldScreen;

/**
 * Cast {@link Spell}s out of battle.
 *
 * @see Spell#castoutofbattle
 * @author alex
 */
public class CastSpells extends WorldAction{

  /** Constructor. */
  public CastSpells(){
    super("Cast spells",new int[0],new String[]{"s"});
  }

  Combatant target(Spell s,Combatant caster){
    var targets=new Combatants(Squad.active.members);
    s.filter(caster,targets,null);
    var description=targets.stream()
        .map(t->"%s (%s)".formatted(t,t.printstatus(null))).toList();
    var choice=Javelin.choose("Cast on...",description,targets.size()>4,false);
    return choice>=0?targets.get(choice):null;
  }

  @Override
  public void perform(WorldScreen screen){
    var casters=filtercasters();
    if(casters.isEmpty()){
      MessagePanel.active.clear();
      Javelin.message("No peaceful spells to cast right now...",
          Javelin.Delay.WAIT);
      return;
    }
    var choice=Javelin.choose("Who?",casters,false,false);
    if(choice==-1){
      MessagePanel.active.clear();
      return;
    }
    var caster=casters.get(choice);
    List<Spell> spells=new ArrayList<>(caster.spells);
    var s=selectspell(spells);
    if(s==null){
      MessagePanel.active.clear();
      return;
    }
    Combatant target=null;
    if(s.castonallies){
      target=target(s,caster);
      if(target==null){
        MessagePanel.active.clear();
        return;
      }
    }
    if(!s.validate(caster,target)){
      Javelin.message("Can't cast this spell right now.",Javelin.Delay.BLOCK);
      return;
    }
    var message=s.castpeacefully(caster,target);
    if(message!=null){
      Javelin.app.switchScreen(screen);
      screen.center();
      Javelin.message(message,false);
    }
    s.used+=1;
  }

  private Spell selectspell(List<Spell> spells){
    var spellnames=listspells(spells);
    if(spellnames.size()==0){
      MessagePanel.active.clear();
      Javelin.message("All spells already cast! Rest to regain them.",
          Javelin.Delay.BLOCK);
      return null;
    }
    var input=Javelin.choose("Which spell?",spellnames,false,false);
    if(input==-1) return null;
    var name=spellnames.get(input);
    for(Spell s:spells) if(s.toString().equals(name)) return s;
    throw new RuntimeException("Should have caught spell name");
  }

  ArrayList<Combatant> filtercasters(){
    var casters=new ArrayList<>(Squad.active.members);
    for(Combatant m:new ArrayList<>(casters))
      if(listspells(new ArrayList<>(m.spells)).size()==0) casters.remove(m);
    return casters;
  }

  ArrayList<String> listspells(List<Spell> spells){
    var spellnames=new ArrayList<String>();
    for(Spell s:spells)
      if(!s.exhausted()&&s.castoutofbattle) spellnames.add(s.toString());
    return spellnames;
  }
}
