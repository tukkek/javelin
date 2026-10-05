package javelin.view.screen.town;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Debug;
import javelin.controller.content.action.world.meta.help.Guide;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Labor;
import javelin.view.screen.Option;

/** Shows {@link Town} details - including {@link Labor} management. */
public class GovernorScreen extends SelectScreen{
  static final String SCREEN="""
      Town management:

      A town will auto-manage itself but this screen enables you to direct the process.
      Press h to see the description for each project.

      %s

      Ongoing projects:
      %s

      Available projects:

      """;
  static final String STATUS="""
      %s:
      Population: %s (%s).
      Traits: %s.
      Production: %s labor/week (on average).
      Resources: %s.
      Reputation: %s (%s%%).
      """.trim();

  Town town;

  class LaborOption extends Option{
    Labor l;

    public LaborOption(Labor l){
      super(l.name,l.cost);
      this.l=l;
    }
  }

  /** Constructor. */
  public GovernorScreen(Town t){
    super("",t);
    town=t;
  }

  @Override
  public String getCurrency(){
    return null;
  }

  @Override
  public String printpriceinfo(Option o){
    var l=o instanceof LaborOption?(LaborOption)o:null;
    return l==null?"":" ("+l.l.cost+" labor)";
  }

  @Override
  public boolean select(Option o){
    var l=((LaborOption)o).l;
    if(Debug.labor){
      l.start();
      while(town.getgovernor().getprojects().contains(l)) l.work(1);
      return true;
    }
    l.start();
    if(l.closescreen){
      stayopen=false;
      forceclose=true;
    }
    return true;
  }

  @Override
  public List<Option> getoptions(){
    var hand=town.getgovernor().gethand();
    var labors=new ArrayList<Option>();
    if(!hand.isEmpty()) for(var i=0;i<hand.size();i++){
      var l=hand.get(i);
      labors.add(new LaborOption(l));
    }
    return labors;
  }

  String printcurrent(List<Labor> queue){
    if(queue.isEmpty()) return "  (no current projects)";
    return queue.stream()
        .map(q->"  - %s (%s%%)".formatted(q.name,q.getprogress()))
        .collect(joining("\n"));
  }

  String printcityinfo(Town t){
    var name=t.description;
    var p=t.population;
    var rank=t.getrank().title.toLowerCase();
    var traits=t.traits.isEmpty()?"none":String.join(", ",t.traits);
    var production=t.getweeklylabor(false);
    var resources="none";
    if(!town.resources.isEmpty())
      resources=town.resources.stream().map(r->r.name.toLowerCase()).sorted()
          .collect(Collectors.joining(", "));
    var s=t.diplomacy.describestatus().toLowerCase();
    var r=Math.round(100*t.diplomacy.reputation/p);
    return STATUS.formatted(name,p,rank,traits,production,resources,s,r);
  }

  @Override
  public void printoptions(List<Option> options){
    var info=printcityinfo(town);
    var projects=printcurrent(town.getgovernor().getprojects());
    text=String.format(SCREEN,info,projects);
    if(town.getgovernor().gethand().isEmpty())
      text+="  (no labor projects available right now)\n";
    else super.printoptions(options);
  }

  @Override
  protected boolean select(char feedback,List<Option> options){
    if(feedback=='h'){
      Guide.DISTRICT.perform();
      return true;
    }
    return super.select(feedback,options);
  }

  @Override
  public String printinfo(){
    return "";
  }
}
