package javelin.model.world.location.dungeon.feature;

import java.awt.Image;
import java.io.Serializable;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.table.Table;
import javelin.controller.table.dungeon.feature.CommonFeatureTable;
import javelin.controller.table.dungeon.feature.FeatureModifierTable;
import javelin.controller.table.dungeon.feature.RareFeatureTable;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Perception;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.Features;
import javelin.view.Images;
import javelin.view.mappanel.dungeon.DungeonTile;

/**
 * An interactive dungeon tile.
 *
 * TODO there could be a "lore" feature that is simply a point of interest with
 * a short description of something interesting. there are thousands of random
 * {@link Table}s out there that could be used for that. Decorations should
 * probably come through first though, especially because they can conceal
 * hidden {@link Features}. A cool way to implement those would be to have sets
 * of decorations (images) per theme - each dungeon would have 1 theme (+1 per
 * 50% chance, cumulative, maybe max of tierIndex-1) in order to allow themes to
 * mix-and-match and tickela player's imagination of what this place must be.
 *
 * @author alex
 */
public abstract class Feature implements Serializable{
  /** X coordinate. */
  public int x=-1;
  /** Y coordinate. */
  public int y=-1;
  public String avatarfile;
  /**
   * If <code>true</code> will {@link #remove()} this if {@link #activate()}
   * return <code>true</code>.
   */
  public boolean remove=true;
  /** <code>false</code> if this should be hidden from the player. */
  public boolean draw=true;
  /**
   * If <code>true</code> lets the {@link Squad} stay in the same
   * {@link DungeonTile} as a feature.
   */
  public boolean enter=true;
  /** Human-friendly name. */
  public String description;

  /** Constructor. */
  public Feature(String description,String avatar){
    this.description=description;
    avatarfile=avatar;
  }

  /** Constructor, using trimmed {@link #description} as {@link #avatarfile}. */
  public Feature(String description){
    this(description,description.replace(" ",""));
  }

  /**
   * Called when a {@link Squad} reaches this location.
   *
   * @return <code>true</code> if the Squad activates this feature or
   *   <code>false</code> if it is ignored.
   */
  abstract public boolean activate();

  /**
   * Called when the {@link Squad} is passing nearby this feature. If it's
   * hidden, might have a chance of revealing it.
   *
   * @param searching The unit that is actively looking around (usually the one
   *   with highest perception).
   * @param searchroll A {@link Perception} roll (usually a take-10, to prevent
   *   scumming). If you want to have an automatic success, inform a high number
   *   like 9000, because if you use {@link Integer#MAX_VALUE}, it may overflow
   *   after internal bonuses being applied.
   * @return <code>true</code> if this feature has been seen. Does not result in
   *   side effects other than for {@link Decoration#hidden}.
   *
   * @see #draw
   */
  public boolean discover(Combatant searching,int searchroll){
    return true;
  }

  /** TODO should probably be in {@link DungeonFloor} */
  public void place(DungeonFloor d,Point p){
    x=p.x;
    y=p.y;
    d.features.add(this);
  }

  public void remove(){
    Dungeon.active.features.remove(this);
  }

  /** @return A point representing this feature. */
  public Point getlocation(){
    return new Point(x,y);
  }

  /**
   * Called once per feature after all {@link DungeonFloor} floors are
   * generated. Called after {@link #validate(DungeonFloor)}.
   */
  public void define(DungeonFloor current,List<DungeonFloor> floors){
    // nothing by default
  }

  /**
   * @param f TODO
   * @return If <code>false</code>, generate a new random Feature instead.
   * @see CommonFeatureTable
   * @see RareFeatureTable
   */
  public boolean validate(DungeonFloor f){
    return true;
  }

  @Override
  public String toString(){
    return Javelin.capitalize(description);
  }

  /** TODO model should not handle view */
  public Image getimage(){
    return Images.get(List.of("dungeon",avatarfile));
  }

  /**
   * Called when a feature hidden in {@link Decoration} is found.
   *
   * @param found <code>true</code> if the party has beated
   *   {@link #getsearchdc()}.
   * @return <code>true</code> for the {@link Decoration} be replaced with this,
   *   <code>false</code> to continue hidden. By the default returns
   *   <code>true</code> if the {@link Squad} passed the search test.
   */
  public boolean reveal(boolean found){
    return found;
  }

  /**
   * @return A level-appropriate {@link Skill} roll Difficulty Class, modified
   *   by {@link FeatureModifierTable}.
   */
  public static int getdc(DungeonFloor f){
    return 10+f.level+f.gettable(FeatureModifierTable.class).roll();
  }

  /** Called before {@link #validate(DungeonFloor)}. */
  @SuppressWarnings("unused")
  public void define(DungeonFloor df){
    //nothing by default
  }
}
