package javelin.controller.content.action.ai;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javelin.Javelin;
import javelin.Javelin.Delay;
import javelin.controller.Audio;
import javelin.controller.Point;
import javelin.controller.ai.AiThread;
import javelin.controller.ai.BattleAi;
import javelin.controller.ai.ChanceNode;
import javelin.controller.content.action.Action;
import javelin.controller.content.action.Movement;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.view.Images;
import javelin.view.mappanel.battle.overlay.AiOverlay;
import javelin.view.mappanel.battle.overlay.BattleWalker;

/**
 * Handles {@link BattleAi} {@link Movement}.
 *
 * TODO would be cool to take {@link Combatant#calculatevision(BattleState)}
 * into consideration but that is tricky given that it can return the actual
 * entire Map and even in almost all cases more than an unit can actually walk.
 *
 * @author alex
 */
public class AiMovement extends Action implements AiAction{
  /** Image overlay representing movement. */
  public static final Image MOVEOVERLAY=Images.get(List.of("overlay","move"));
  /** Unique instace of this class. */
  public static final AiMovement SINGLETON=new AiMovement();

  static final int CULL=2;

  class Moved extends ChanceNode{
    Combatant moving;
    Path path;

    Moved(Combatant c,Path p,Delay d,BattleState s){
      super(s,1,p.message.formatted(c),d);
      moving=c;
      path=p;
      audio=new Audio("move",moving.source);
      var o=new AiOverlay(p.steps);
      o.image=AiMovement.MOVEOVERLAY;
      overlay=o;
    }

    float score(){
      var s=(BattleState)n;
      return BattleAi.measuredistances(s.getteam(moving),
          s.getopponents(moving));
    }
  }

  class Path{
    String message="%s moves...";
    List<Point> steps;
    float ap;

    Path(Combatant c){
      steps=List.of(c.getlocation());
      ap=0;
    }

    Path(Path p){
      steps=new ArrayList<>(p.steps.size()+1);
      steps.addAll(p.steps);
      ap=p.ap;
    }

    Point getlast(){
      return steps.get(steps.size()-1);
    }

    boolean stop(Combatant c,BattleState s){
      var l=getlast();
      return s.getmeld(l.x,l.y)!=null||BattleWalker.engage(c,l,s);
    }

    Path step(Combatant c,Point to,BattleState s){
      if(BattleWalker.block(c,to,s)) return null;
      var m=s.getmeld(to.x,to.y);
      if(m!=null&&c.ap+ap<m.meldsat) return null;
      var p=new Path(this);
      p.steps.add(to);
      p.ap+=BattleWalker.getcost(c,to.x,to.y,s);
      return p;
    }
  }

  class Search{
    List<Path> moves=new ArrayList<>();
    Set<Point> skip=new HashSet<>();
    Combatant combatant;
    BattleState state;

    Search(Combatant c,BattleState s){
      combatant=c;
      state=s;
      var combatants=s.getcombatants();
      combatants.remove(c);
      skip.addAll(combatants.stream().map(Combatant::getlocation).toList());
    }

    void move(Path p){
      AiThread.checkinterrupted();
      var width=state.map.length;
      var height=state.map[0].length;
      for(var step:p.getlast().getadjacent()){
        if(!step.validate(width,height)||skip.contains(step)) continue;
        var next=p.step(combatant,step,state);
        if(next!=null) moves.add(next);
      }
    }

    boolean engage(){
      if(!state.isengaged(combatant)) return false;
      var ap=Movement.disengage(combatant);
      for(var m:moves){
        m.ap=ap;
        m.message="%s disengages...";
      }
      return true;
    }

    List<Path> search(){
      move(new Path(combatant));
      if(engage()||moves.isEmpty()) return moves;
      var next=moves.get(0);
      while(next.ap<.5){
        next=moves.stream().filter(m->!skip.contains(m.getlast()))
            .min(Comparator.comparing(m->m.ap)).orElse(null);
        if(next==null) break;
        skip.add(next.getlast());
        if(!next.stop(combatant,state)) move(next);
      }
      return moves;
    }
  }

  AiMovement(){
    super("Long move");
    allowburrowed=true;
  }

  @Override
  public boolean perform(Combatant active){
    throw new UnsupportedOperationException();
  }

  Path maxap=null;//TODO

  Moved act(Combatant c,Path p,BattleState s){
    s=s.clone();
    c=s.clone(c);
    var to=p.getlast();
    c.setlocation(to);
    //    if(maxap==null||p.ap>maxap.ap) maxap=p;//TODO
    System.out.println(List.of(p.ap,p.steps.size()));//TODO
    c.ap+=p.ap;
    var m=s.getmeld(to.x,to.y);
    var d=Javelin.Delay.WAIT;
    if(m!=null){
      c.meld();
      s.meld.remove(m);
      p.message="%s powers up!";
      d=Javelin.Delay.BLOCK;
    }
    return new Moved(c,p,d,s);
  }

  //TODO would be neat to perform culling before cloning states in act()
  List<Moved> cull(List<Moved> moves,BattleState s){
    var culled=new ArrayList<Moved>(CULL);
    for(var m:moves){
      var l=m.path.getlast();
      if(s.getmeld(l.x,l.y)!=null) culled.add(m);
    }
    var byscore=moves.stream().filter(m->!culled.contains(m))
        .sorted(Comparator.comparing(Moved::score)).limit(CULL+culled.size())
        .toList();
    culled.addAll(byscore);
    return culled;
  }

  @Override
  public List<List<ChanceNode>> getoutcomes(Combatant c,BattleState s){
    if(c.gettopspeed(s)==0) return Collections.emptyList();
    var moves=new Search(c,s).search().stream().map(path->act(c,path,s));
    var outcomes=new ArrayList<List<ChanceNode>>(CULL);
    for(var m:cull(moves.toList(),s)) outcomes.add(List.of(m));
    return outcomes;
  }
}
