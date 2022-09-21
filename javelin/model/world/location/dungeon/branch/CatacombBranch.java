package javelin.model.world.location.dungeon.branch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import javelin.Javelin;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.content.fight.Fight;
import javelin.controller.content.fight.RandomDungeonEncounter;
import javelin.controller.content.fight.mutator.Mutator;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.db.EncounterIndex;
import javelin.controller.exception.GaveUp;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.model.item.Item;
import javelin.model.item.consumable.Eidolon;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;
import javelin.model.unit.abilities.spell.conjuration.Summon;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.unique.Catacombs;
import javelin.old.RPG;

/**
 * TODO not a complete branch quite yet?
 *
 * TODO an interesting {@link Mutator} would be spider-webs that temporarily
 * disables units that can't pass a strengh check
 *
 * @see Catacombs
 */
public class CatacombBranch extends Branch{
  /** Singleton. */
  public static final CatacombBranch INSTANCE=new CatacombBranch();
  /** @see MonsterType#UNDEAD */
  public static final EncounterIndex UNDEAD=new EncounterIndex(
      Monster.get(MonsterType.UNDEAD));
  /** @see MonsterType#VERMIN */
  public static final EncounterIndex VERMIN=new EncounterIndex(
      Monster.get(MonsterType.VERMIN));
  /** Branch {@link Monster}s. */
  public static final EncounterIndex MONSTERS=EncounterIndex
      .merge(List.of(UNDEAD,VERMIN));
  /** {@link MonsterType#UNDEAD} {@link Eidolon}s. */
  public static final List<Item> PHYLACTERIES=new ArrayList<>();
  /** {@link MonsterType#VERMIN} {@link Eidolon}s. */
  public static final List<Item> PHEROMONES=new ArrayList<>();
  /** {@link #PHEROMONES} and {@link #PHEROMONES}. */
  public static final List<Item> EIDOLONS=new ArrayList<>();

  static final List<Integer> CHARGES=IntStream.range(0,5+1).boxed().toList();
  static final String ACTIVATE="""
      There is %s here. Do you want to disturb it?
      Press ENTER to confirm or any other key to ignore...
      """.trim();

  static{
    for(var s:Monster.get(MonsterType.VERMIN).stream()
        .map(m->new Summon(m.name,1)).toList())
      for(var c:CHARGES) PHEROMONES.add(new Pheromone(s,c));
    for(var s:Monster.get(MonsterType.UNDEAD).stream()
        .map(m->new Summon(m.name,1)).toList())
      for(var c:CHARGES) PHYLACTERIES.add(new Phylactery(s,c));
    EIDOLONS.addAll(PHEROMONES);
    EIDOLONS.addAll(PHYLACTERIES);
  }

  static class Pheromone extends Eidolon{
    Pheromone(Summon s,int charges){
      super(s,charges,false);
      name="Pheromone";
    }
  }

  static class Phylactery extends Eidolon{
    Phylactery(Summon s,int charges){
      super(s,charges,false);
      name="Phylactery";
    }
  }

  /** {@link MonsterType#UNDEAD} encounter, rewards a {@link Phylactery}. */
  public static class Nest extends Feature{
    Combatants encounter=new Combatants(2);
    Item eidolon;

    /** Inheritance constructor. */
    public Nest(String description,String avatar){
      super(description,avatar);
    }

    /** Reflective constructor. */
    @SuppressWarnings("unused")
    public Nest(DungeonFloor f){
      this("A nest","nest");
    }

    void define(DungeonFloor f,EncounterIndex monsters,List<Item> rewards){
      try{
        monsters=monsters.filter(f.level).limit(9);
        for(var i=0;i<2;i++){
          var e=EncounterGenerator.generate(f.level,monsters).clone();
          encounter.addAll(e);
        }
        var g=RewardCalculator.getgold(encounter);
        eidolon=reward(g,rewards);
      }catch(GaveUp e){
        encounter.clear();
      }
    }

    @Override
    public void define(DungeonFloor f){
      define(f,VERMIN,PHEROMONES);
    }

    @Override
    public boolean validate(DungeonFloor f){
      return !encounter.isEmpty()&&eidolon!=null;
    }

    @Override
    public boolean activate(){
      if(Javelin.prompt(ACTIVATE.formatted(description.toLowerCase()))!='\n')
        return false;
      remove();
      var e=new RandomDungeonEncounter(Dungeon.active);
      e.bribe=false;
      e.hide=false;
      e.encounter=encounter;
      e.rewardgold=false;
      e.mutators.add(new Mutator(){
        @Override
        public void end(Fight f){
          super.end(f);
          if(Fight.victory) eidolon.grab();
        }
      });
      throw new StartBattle(e);
    }
  }

  /** {@link MonsterType#UNDEAD} encounter, rewards a {@link Phylactery}. */
  public static class Sarcophagus extends Nest{
    /** Reflective constructor. */
    @SuppressWarnings("unused")
    public Sarcophagus(DungeonFloor f){
      super("A sarcophagus","sarcophagus");
    }

    @Override
    public void define(DungeonFloor f){
      super.define(f,UNDEAD,PHYLACTERIES);
    }
  }

  /** Constructor. */
  CatacombBranch(){
    super("Catacomb","of the dead","floorcatacombs","wallcatacombs");
    terrains.add(Terrain.UNDERGROUND);
    features.addAll(List.of(Nest.class,Sarcophagus.class));
  }

  @Override
  public EncounterIndex getencounters(){
    return MONSTERS;
  }

  /**
   * @param gold Gold pool to generate item with.
   * @param pool Shuffled list.
   * @return A {@link Phylactery} or {@link Pheromone}.
   */
  public static Item reward(int gold,List<Item> pool){
    return RPG.shuffle(pool,true).stream().filter(p->p.price<=gold)
        .sorted(Collections.reverseOrder(Comparator.comparing(p->p.price)))
        .findFirst().orElse(null);
  }
}
