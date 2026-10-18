package javelin.controller.content.action.maneuver;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.ai.ChanceNode;
import javelin.controller.content.action.Action;
import javelin.controller.content.action.ai.AiAction;
import javelin.controller.exception.RepeatTurn;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.discipline.Maneuver;
import javelin.model.unit.abilities.discipline.Maneuvers;
import javelin.old.messagepanel.MessagePanel;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.Screen;
import javelin.view.screen.town.SelectScreen;

public class ExecuteManeuver extends Action implements AiAction{
  public static final ExecuteManeuver INSTANCE=new ExecuteManeuver();

  private ExecuteManeuver(){
    super("Execute maneuvers","m");
  }

  @Override
  public List<List<ChanceNode>> getoutcomes(Combatant c,BattleState s){
    final var outcomes=new ArrayList<List<ChanceNode>>();
    final var disciplines=c.disciplines;
    disciplines.sort();
    for(String discipline:disciplines.keySet())
      for(Maneuver m:disciplines.get(discipline)){
        var s2=s.clone();
        var c2=s2.clone(c);
        var m2=c2.disciplines.find(m);
        if(m2.spent){
          var chance=new ArrayList<ChanceNode>();
          c2.ready(m2);
          final var feedback=c2+" readies "+m2.name.toLowerCase()+"!";
          final var node=new ChanceNode(s2,1,feedback,
              Javelin.Delay.BLOCK);
          chance.add(node);
          outcomes.add(chance);
        }else{
          m2.spend();
          outcomes.addAll(m2.getoutcomes(c2,s2));
        }
      }
    return outcomes;
  }

  @Override
  public boolean perform(Combatant c){
    final var disciplines=c.disciplines;
    if(disciplines.isEmpty()){
      Javelin.message("No known manuevers...",Javelin.Delay.WAIT);
      BattleScreen.active.block();
      throw new RepeatTurn();
    }
    var maneuvers=new Maneuvers();
    var prompt="Choose a manuever to execute (or ready), or press any other key to exit...\n";
    for(String disciplinet:disciplines.keySet()){
      prompt+="\n"+disciplinet+":\n";
      var learned=disciplines.get(disciplinet);
      for(var i=0;i<learned.size();i++){
        var m=learned.get(i);
        final var spent=m.spent?" (not ready)":"";
        prompt+="  "+SelectScreen.getkey(i)+" - "+m.name+spent+"\n";
        maneuvers.add(m);
      }
    }
    var m=choose(maneuvers,prompt);
    if(m.spent){
      var name=m.name.toLowerCase();
      var confirm=Javelin.prompt(
          "Do you want to ready "+name+"?\nPress ENTER or m to confirm...");
      if(confirm!='m'&&confirm!='\n') throw new RepeatTurn();
      c.ready(m);
      MessagePanel.active.clear();
      Javelin.message(c+" readies "+name+"...",Javelin.Delay.WAIT);
      return true;
    }
    try{
      m.spend();
      return m.perform(c);
    }catch(RepeatTurn e){
      m.spent=false;
      throw e;
    }
  }

  Maneuver choose(Maneuvers maneuvers,String prompt){
    if(maneuvers.size()==1){
      var m=maneuvers.get(0);
      if(!m.instant) return m;
    }
    var choice=SelectScreen.convertkeytoindex(Javelin.promptscreen(prompt));
    Screen.reopen();
    if(choice==-1&&choice<maneuvers.size()) throw new RepeatTurn();
    return maneuvers.get(choice);
  }
}
