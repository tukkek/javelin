package javelin.model.world.location.dungeon.feature;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javelin.Javelin;
import javelin.controller.comparator.ItemsByName;
import javelin.model.item.Item;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.town.labor.religious.Shrine;
import javelin.model.world.location.town.labor.religious.Shrine.Ritual;
import javelin.old.RPG;

/**
 * Sacrifice an {@link Item} for a chance of casting a {@link Ritual}.
 *
 * @see Spell#isritual
 * @author alex
 */
public class Altar extends Feature{
  static final Set<Spell> BLACKLIST=Set.of();
  static final List<Ritual> BLESSINGS=new ArrayList<>(
      Shrine.RITUALS.stream().filter(r->!BLACKLIST.contains(r.spell)).toList());
  static final String PROMPT="""
      There is an altar here. Would you like to sacrifice an item?

      Press ENTER to sacrifice or any other key to ignore...
      """.trim();

  /** Constructor. */
  @SuppressWarnings("unused")
  public Altar(DungeonFloor f){
    super("An altar","altar");
    remove=false;
  }

  boolean sacrifice(double gold){
    var blessings=BLESSINGS.stream().filter(b->b.price>=gold&&b.validate())
        .toList();
    if(blessings.isEmpty()){
      Javelin.message("The gods have no blessing worthy of this tribute...",
          false);
      return false;
    }
    blessings=RPG.shuffle(blessings,true);
    blessings.sort(Comparator.comparing(b->b.price));
    var b=blessings.get(0);
    if(RPG.random()>gold/b.price){
      Javelin.message("The humor of the gods does not favor you...",true);
      return true;
    }
    var result=b.perform();
    if(result==null){
      var n=b.spell.name.toLowerCase();
      result="The gods grant a blessing: %s!".formatted(n);
    }
    Javelin.message(result,true);
    return true;
  }

  @Override
  public boolean activate(){
    if(Javelin.prompt(PROMPT)!='\n') return false;
    var members=Squad.active.members;
    var c=Javelin.choose("Who will sacrifice an item?",members,true,false);
    if(c<0) return false;
    var m=members.get(c);
    var prompt="%s will sacrifice which item?".formatted(m);
    var bag=Squad.active.equipment.get(m);
    var choices=bag.stream().filter(Item::sell).sorted(ItemsByName.SINGLETON)
        .toList();
    if(choices.isEmpty()){
      var message="%s doesn't have anything to sacrifice...".formatted(m);
      Javelin.message(message,false);
      return false;
    }
    c=Javelin.choose(prompt,choices,true,false);
    if(c<0) return false;
    var i=choices.get(c);
    if(sacrifice(i.price*i.sellvalue)) bag.remove(bag.indexOf(i));
    return true;
  }
}
