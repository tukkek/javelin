package javelin.model.world.location.dungeon.branch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javelin.controller.content.ContentSummary;
import javelin.controller.content.fight.Fight;
import javelin.controller.content.fight.RandomDungeonEncounter;
import javelin.controller.content.fight.mutator.Mutator;
import javelin.controller.content.template.Template;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.db.EncounterIndex;
import javelin.controller.db.reader.fields.Organization;
import javelin.controller.generator.dungeon.template.FloorTile;
import javelin.controller.generator.encounter.Encounter;
import javelin.controller.table.dungeon.feature.CommonFeatureTable;
import javelin.model.item.artifact.Artifact;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonImages;
import javelin.model.world.location.dungeon.feature.BranchPortal;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.chest.Chest;
import javelin.model.world.location.dungeon.feature.common.Campfire;
import javelin.model.world.location.unique.WarlocksTower;

/**
 * Branches are as much content (both pre- and
 * post-<a href='https://github.com/tukkek/javelin/issues/269'>endgame</a> - see
 * {@link WarlocksTower} and {@link BranchPortal}s as examples) as they are a
 * development methodology for Javelin.
 *
 * A good approach when creating new Branches is to aim for 50% of fields filled
 * overall, many of which are optional. As such, new interesting piece-meal
 * content can be developed post-facto and backported into any suitable open
 * slots (the more of which will exist as more branches are introduced).
 *
 * Trying to develop 100% complete branches would be a nightmare on all fronts:
 * thematically, effort-wise and time-wise (more branches with optional slots
 * unfulfilled is more interesting than less branches with all boxes ticked) -
 * not to mention it would make it awkward to be able to think of a cool
 * {@link Artifact} or {@link Mutator} and not be able to backport it and
 * isntead own the burden of creatining a whole new full branch around it.
 *
 * When creating a new Branch and selecting which 50% of fields to fulfill,
 * first add as many as are thematically suitable and then fallback to the
 * {@link ContentSummary} log as needed as a guide on what to prioritize.
 *
 * TODO incorporate https://github.com/tukkek/javelin/issues/254 into javadoc
 *
 * @author alex
 */
public class Branch implements Serializable{
  /** Will be added very favorably to {@link CommonFeatureTable}. */
  public List<Class<? extends Feature>> features=new ArrayList<>(
      List.of(Campfire.class));
  /** Theme-appropriate {@link Dungeon} mechanic. */
  public List<DungeonHazard> hazards=new ArrayList<>(1);
  /** Will optimially be applied to all {@link RandomDungeonEncounter}s. */
  public List<Template> templates=new ArrayList<>(0);
  /** Favored treasure types. */
  public List<Class<? extends Chest>> treasure=new ArrayList<>(1);
  /** To be used as {@link Monster} pools. */
  public Set<Terrain> terrains=new HashSet<>(1);
  /** Map templates that give visual identity to this branch. */
  public List<FloorTile> tiles=new ArrayList<>(0);
  /** Special {@link Fight} mechanics. */
  public List<Mutator> mutators=new ArrayList<>(1);
  /** @see DungeonImages#FLOOR */
  public String floor=null;
  /** @see DungeonImages#WALL */
  public String wall=null;
  /** @see Dungeon#doorbackground */
  public boolean doorbackground=false;
  /** Name prefix ("burning"). */
  public String prefix;
  /** Name prefix ("of fire"). */
  public String suffix;

  /** Constructor. */
  protected Branch(String prefix,String suffix,String floor,String wall){
    this.prefix=prefix;
    this.suffix=suffix;
    this.floor=floor;
    this.wall=wall;
  }

  /**
   * @return <code>false</code> if any units aren't theme- or
   *   gameplay-appropriate. <code>true</code> by default.
   */
  public boolean validate(List<Combatant> foes){
    return true;
  }

  /** Allows customization of dungeons. */
  public void define(Dungeon d){}

  @Override
  public boolean equals(Object o){
    return getClass().equals(o.getClass());
  }

  @Override
  public int hashCode(){
    return getClass().hashCode();
  }

  @Override
  public String toString(){
    return prefix;
  }

  /**
   * A utility function to transform {@link #terrains} into {@link Encounter}s
   * but which also allows custom Encounters and {@link Monster}s to be added or
   * replaced instead.
   */
  public EncounterIndex getencounters(){
    var i=new EncounterIndex();
    for(var t:terrains)
      i.merge(Organization.ENCOUNTERSBYTERRAIN.get(t.toString()));
    return i;
  }
}
