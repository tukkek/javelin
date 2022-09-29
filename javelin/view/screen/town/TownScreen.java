package javelin.view.screen.town;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.challenge.RewardCalculator;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.diplomacy.mandate.Mandate;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.Option;
import javelin.view.screen.town.option.ScreenOption;
import javelin.view.screen.town.option.TournamentScreenOption;

/**
 * Shown when a {@link Squad} enters a {@link Town}.
 *
 * @author alex
 */
public class TownScreen extends PurchaseScreen{
  static final boolean DEBUGMANAGEMENT=false;
  static final Option RENAME=new Option("Rename town",0,'r');
  static final Option TREATISE=new Option("Claim treaty",0,'t');
  static final Option DONATE=new Option("Donate",0,'t');
  static final String REPUTATION="Reputation: %s.";

  class Manage extends ScreenOption{
    public Manage(Town town){
      super("Manage town",town,'m');
    }

    @Override
    public SelectScreen getscreen(){
      return new GovernorScreen(t);
    }
  }

  /** Constructor. */
  public TownScreen(final Town t){
    super(title(t),t);
  }

  static String title(final Actor t){
    return "Welcome to "+t+"!";
  }

  class TreatyOption extends Option{
    Mandate m;

    public TreatyOption(Mandate m,char key){
      super(m.name,0,key);
      this.m=m;
    }
  }

  class SelectTreaty extends SelectScreen{
    List<Option> options;

    public SelectTreaty(){
      super("Select a treaty to claim:",TownScreen.this.town);
      stayopen=false;
      var treaties=town.diplomacy.treaties;
      options=new ArrayList<>(treaties.size());
      var i=0;
      for(var t:treaties){
        options.add(new TreatyOption(t,KEYS[i]));
        i++;
      }
    }

    @Override
    public String getCurrency(){
      return "";
    }

    @Override
    public String printinfo(){
      return "";
    }

    @Override
    public String printpriceinfo(Option o){
      return "";
    }

    @Override
    public List<Option> getoptions(){
      return options;
    }

    @Override
    public boolean select(Option o){
      town.diplomacy.enact(((TreatyOption)o).m);
      return true;
    }
  }

  int donate(){
    var r=town.diplomacy.reputation;
    if(r==0) r=1;
    var p=town.population;
    return Javelin.round(RewardCalculator.getgold(p)*p/r);
  }

  @Override
  public boolean select(final Option o){
    if(o instanceof ScreenOption){
      var screen=((ScreenOption)o).getscreen();
      screen.show();
      if(screen.forceclose) stayopen=false;
      return true;
    }
    if(!super.select(o)) return false;
    if(o==RENAME){
      town.rename();
      title=title(town)+"\n\n";
      return true;
    }
    if(o==DONATE){
      if(Squad.active.pay(donate())){
        var d=town.diplomacy;
        d.reputation=town.population;
        var m="%s becomes %s!".formatted(town,d.describestatus());
        Javelin.message(m,true);
      }else Javelin.message("You don't have that much gold!",false);
    }else if(o==TREATISE) new SelectTreaty().show();
    stayopen=false;
    return true;
  }

  @Override
  public List<Option> getoptions(){
    final var list=new ArrayList<Option>();
    list.add(new Manage(town));
    list.add(RENAME);
    var d=town.diplomacy;
    if(d.getstatus()<1){
      DONATE.name="Donate ($%s)".formatted(Javelin.format(donate()));
      list.add(DONATE);
    }else if(!d.treaties.isEmpty()) list.add(TREATISE);
    if(town.ishosting())
      list.add(new TournamentScreenOption("Enter tournament",town,'e'));
    return list;
  }

  @Override
  public String printpriceinfo(Option o){
    return o.price>0?super.printpriceinfo(o):"";
  }

  @Override
  public void onexit(){
    Javelin.app.switchScreen(BattleScreen.active);
  }

  @Override
  public String printinfo(){
    var info=new ArrayList<String>(0);
    var d=town.diplomacy;
    if(!d.quests.isEmpty()){
      var active=new ArrayList<String>(2);
      active.add("Active quests:");
      for(var q:d.quests){
        var r=q.describereward().toLowerCase();
        if(!r.isEmpty()) r=", reward: "+r;
        active.add("- "+q+r);
      }
      info.add(String.join("\n",active));
    }
    if(!d.treaties.isEmpty()){
      var treaties="Available treaties:\n";
      treaties+=d.treaties.parallelStream().map(m->"- "+m)
          .collect(Collectors.joining("\n"));
      info.add(treaties);
    }
    var reputation=d.getstatus();
    if(reputation<0) reputation=0;
    else if(reputation>1) reputation=1;
    info.add(String.format(REPUTATION,d.describestatus().toLowerCase()));
    return String.join("\n\n",info);
  }

  @Override
  protected Comparator<Option> sort(){
    return Comparator.comparing(o1->o1.key);
  }
}
