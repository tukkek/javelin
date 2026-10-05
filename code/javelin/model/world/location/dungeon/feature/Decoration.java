package javelin.model.world.location.dungeon.feature;

import java.awt.Image;
import java.util.Calendar;
import java.util.List;

import javelin.Javelin;
import javelin.controller.content.action.world.WorldMove;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.feature.chest.Chest;
import javelin.model.world.location.dungeon.feature.trap.Trap;
import javelin.view.Images;

/**
 * Mostly cosmetic {@link DungeonFloor} items. Can contain {@link #hidden}
 * features.
 *
 * TODO in the future, could hide hidden {@link Trap}, {@link Chest}, stc. The
 * easiest way to achieve this would simply be to make any appropriate Search
 * checks then replace this with the actual feature.
 *
 * @see Trap
 * @author alex
 */
public class Decoration extends Feature{
  static final String FOUND="You have found a hidden %s!";

  static Image easteregg=null;
  /** Custom {@link #hidden} reveal message. */
  public static String revealmessage;
  /** Callback for {@link #hidden} features. */
  public static Runnable onreveal;

  static{
    var today=Calendar.getInstance();
    var d=today.get(Calendar.DAY_OF_MONTH);
    var m=today.get(Calendar.MONTH);
    if(d==31&&m==10)
      easteregg=Images.get(List.of("dungeon","decoration","halloween"));
    else if(d==25&&m==12)
      easteregg=Images.get(List.of("dungeon","decoration","christmas"));
  }

  Feature hidden;

  /** Constructor. */
  public Decoration(String avatar,DungeonFloor f){
    super(avatar);
  }

  void reveal(){
    remove();
    remove=false;
    hidden.draw=true;
    hidden.place(Dungeon.active,getlocation());
    Javelin.redraw();
  }

  @Override
  public boolean discover(Combatant searching,int searchroll){
    if(hidden!=null&&hidden.discover(searching,searchroll)) reveal();
    return true;
  }

  @Override
  public boolean activate(){
    if(hidden==null) return false;
    var c=Squad.active.getbest(Skill.PERCEPTION);
    if(!hidden.reveal(hidden.discover(c,c.taketen(Skill.PERCEPTION))))
      return false;
    reveal();
    var message=revealmessage;
    if(message==null)
      message=String.format(FOUND,hidden.description.toLowerCase());
    else revealmessage=null;
    Javelin.message(message,true);
    WorldMove.abort=!(hidden instanceof Trap);
    if(onreveal!=null) onreveal.run();
    onreveal=null;
    return true;
  }

  @Override
  public Image getimage(){
    if(easteregg!=null) return easteregg;
    return Images.get(List.of("dungeon","decoration",avatarfile));
  }

  public void hide(Feature f){
    hidden=f;
  }
}
