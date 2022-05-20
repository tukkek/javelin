package javelin.model.world.location.town.labor.military;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Calendar;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.content.upgrade.FeatUpgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.discipline.Discipline;
import javelin.model.unit.feat.Feat;
import javelin.model.unit.feat.MartialTraining;
import javelin.model.world.location.academy.Academy;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.Labor;
import javelin.model.world.location.town.labor.Trait;
import javelin.model.world.location.unique.MercenariesGuild;
import javelin.old.RPG;
import javelin.view.screen.Option;
import javelin.view.screen.SquadScreen;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.town.option.ScreenOption;
import javelin.view.screen.upgrading.AcademyScreen;

/**
 * TODO How to offer a defined upgrade path so that it doesn't become to
 * burdersome for players to learn? Also for upgrading NPCs? Here it should be
 * easy enough to have a {@link ScreenOption} that takes care of that but what
 * for NPCs?
 *
 * @author alex
 */
public class DisciplineAcademy extends Academy{
  static final int LEVERSTUDENT=9;
  static final int LEVELTEACHER=12;
  static final int LEVERMASTER=16;

  public final static ArrayList<Monster> CANDIDATES=new ArrayList<>();

  static{
    for(Monster sensei:SquadScreen.CANDIDATES)
      if(sensei.think(0)) CANDIDATES.add(sensei);
  }

  /**
   * TODO use one per {@link Discipline} so the {@link Labor}s don't have to be
   * all at one {@link Trait}.
   */
  public static class BuildDisciplineAcademy extends BuildAcademies{
    Discipline d;

    /** @param d Discipline taught. */
    public BuildDisciplineAcademy(Discipline d){
      super(Rank.VILLAGE);
      this.d=d;
    }

    @Override
    protected Academy generateacademy(){
      return d.generateacademy();
    }
  }

  /** Hires an upgraded mercenary. */
  public class HireOption extends Option{
    Combatant c;

    /** @param c Mercenary. */
    public HireOption(Combatant c){
      super("Hire "+c+" ($"+Javelin.format(c.pay())+"/day)",0);
      this.c=c;
    }

    @Override
    public double sort(){
      return c.source.cr;
    }
  }

  /** User interface for the discipline academies. */
  public class DisciplineAcademyScreen extends AcademyScreen{
    /** @param academy Instance to represent. */
    public DisciplineAcademyScreen(Academy academy){
      super(academy,null);
    }

    @Override
    public List<Option> getoptions(){
      var options=super.getoptions();
      for(Combatant c:new Combatant[]{student,teacher,master})
        if(c!=null) options.add(new HireOption(c));
      return options;
    }

    @Override
    public boolean select(Option op){
      var hire=op instanceof HireOption?(HireOption)op:null;
      if(hire!=null){
        if(!MercenariesGuild.recruit(hire.c,false)){
          final var error="You don't have enough money to pay today's advance!\n"
              +"Press any key to continue...";
          printmessage(error);
          return false;
        }
        if(hire.c==student) student=null;
        else if(hire.c==teacher) teacher=null;
        else if(hire.c==master) master=null;
        return true;
      }
      return super.select(op);
    }

    @Override
    protected boolean upgrade(UpgradeOption o,Combatant c){
      var mt=getmartialtrainingfeat(o);
      if(mt!=null){
        var cr=ChallengeCalculator.calculaterawcr(c.source)[1];
        train(c,c.xp.floatValue(),cr);
        var training=(MartialTraining)c.source.getfeat(d.trainingupgrade.feat);
        if(training==null||training.slots==0) return false;
        return ChallengeCalculator.calculaterawcr(c.source)[1]>cr;
      }
      return super.upgrade(o,c);
    }

    MartialTraining getmartialtrainingfeat(UpgradeOption o){
      if(!(o.u instanceof FeatUpgrade fu)) return null;
      return fu.feat instanceof MartialTraining?(MartialTraining)fu.feat:null;
    }
  }

  /** CR 5 mercenary. */
  Combatant student=null;
  /** CR 10 mercenary. */
  Combatant teacher=null;
  /** CR 15 mercenary. */
  Combatant master=null;
  Discipline d;

  /** @param d Discipline taught. */
  public DisciplineAcademy(Discipline d){
    super(d.name+" academy",null,5,15,Collections.EMPTY_SET,d.ability,
        d.classlevel);
    this.d=d;
    descriptionunknown=descriptionknown;
    upgrades.add(d.skillupgrade.getupgrade());
    upgrades.add(d.knowledgeupgrade.getupgrade());
    upgrades.add(d.trainingupgrade);
    student=train(student,LEVERSTUDENT,1);
    teacher=train(teacher,LEVELTEACHER,2);
    master=train(master,LEVERMASTER,4);
  }

  @Override
  public List<Combatant> getcombatants(){
    var combatants=super.getcombatants();
    for(Combatant c:new Combatant[]{student,teacher,master})
      if(c!=null) combatants.add(c);
    return combatants;
  }

  @Override
  public void turn(long time,WorldScreen world){
    super.turn(time,world);
    student=train(student,LEVERSTUDENT,Calendar.MONTH);
    teacher=train(teacher,LEVELTEACHER,Calendar.SEASON);
    master=train(master,LEVERMASTER,Calendar.YEAR);
  }

  Combatant train(Combatant c,int level,int period){
    if(c!=null||!RPG.chancein(period)) return c;
    c=new Combatant(RPG.pick(CANDIDATES),true);
    c.setmercenary(true);
    train(c,level,ChallengeCalculator.calculaterawcr(c.source)[1]);
    c.postupgradeautomatic();
    name(c);
    return c;
  }

  void name(Combatant c){
    c.source.customName=d.name+" initiate";
    for(Feat f:c.source.feats) if(f instanceof MartialTraining mt){
      c.source.customName=d.name+" "+mt.getrank().toLowerCase();
      return;
    }
  }

  /**
   * @param c Train a combatant...
   * @param xp ... up to this amount of XP (not removed form
   *   {@link Combatant#xp}) ...
   * @param cr ... and this amount of raw CR (with golden rule applied)...
   * @param applied Used for recursion, pass as <code>null</code> initially.
   * @param kits in these Upgrades...
   * @return All applied upgrades.
   * @see ChallengeCalculator#calculaterawcr(javelin.model.unit.Monster)
   */
  public void train(Combatant c,float xp,float cr){
    var upgrades=List.of(d.trainingupgrade,d.knowledgeupgrade.getupgrade(),
        d.skillupgrade.getupgrade(),d.classlevel,d.ability);
    for(var u:upgrades){
      var c2=c.clone().clonesource();
      if(!u.upgrade(c2)) continue;
      final var newcr=ChallengeCalculator.calculaterawcr(c2.source)[1];
      if(newcr-cr>xp) continue;
      c.source=c.source.clone();
      u.upgrade(c);
      train(c,xp,cr);
      return;
    }
  }

  @Override
  public int getlabor(){
    return 10;
  }

  @Override
  protected AcademyScreen getscreen(){
    return new DisciplineAcademyScreen(this);
  }

  @Override
  public String getimagename(){
    return "disciplineacademy";
  }
}
