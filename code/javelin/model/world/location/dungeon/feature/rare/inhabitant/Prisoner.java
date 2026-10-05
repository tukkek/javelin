package javelin.model.world.location.dungeon.feature.rare.inhabitant;

import javelin.Javelin;
import javelin.controller.challenge.Difficulty;
import javelin.controller.content.fight.RandomDungeonEncounter;
import javelin.controller.exception.battle.StartBattle;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.DisableDevice;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.old.RPG;

/** Joins {@link Squad} when freed with {@link DisableDevice}. */
public class Prisoner extends Inhabitant{
  static final String RELEASE="""
      You find a %s prisoner here.
      The lock here looks %s for %s to open. Would you like to try?
      Press ENTER to attempt to unlock or any other key to ignore...""";

  int unlockdc;

  /** Constructor. */
  public Prisoner(DungeonFloor f){
    super(f.level+Difficulty.VERYEASY+1,f.level+Difficulty.EASY,"prisoner",f);
    unlockdc=getdc(f);
  }

  boolean free(Combatant locksmith){
    if(locksmith.taketen(Skill.DISABLEDEVICE)>=unlockdc) return true;
    if(RPG.chancein(2)){
      Javelin.message("You are interrupted!",false);
      throw new StartBattle(new RandomDungeonEncounter(Dungeon.active));
    }
    if(20+Skill.DISABLEDEVICE.getbonus(locksmith)<unlockdc){
      Javelin.message("%s cannot pick this lock...",false);
      return false;
    }
    return true;
  }

  @Override
  public boolean activate(){
    var locksmith=Squad.active.getbest(Skill.DISABLEDEVICE);
    var difficulty=Skill.DISABLEDEVICE.describe(unlockdc,locksmith);
    var text=RELEASE.formatted(inhabitant,difficulty,locksmith);
    if(Javelin.prompt(text)!='\n') return false;
    if(!free(locksmith)) return true;
    Javelin.message("%s joins your party!".formatted(inhabitant),true);
    var prisoner=Squad.active.recruit(inhabitant.source);
    prisoner.hp=prisoner.maxhp
        *RPG.r(Combatant.STATUSWOUNDED,Combatant.STATUSSCRATCHED)
        /Combatant.STATUSUNHARMED;
    if(prisoner.hp<1) prisoner.hp=1;
    Dungeon.active.features.remove(this);
    return true;
  }
}
