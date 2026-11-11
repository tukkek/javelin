package javelin.controller.content.action;

import javelin.Javelin;
import javelin.controller.comparator.ItemsByName;
import javelin.controller.content.fight.Fight;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;

/// Activates an {@link Item} in battle.
public class UseItem extends Action{
  /// Singleton.
  public static Action INSTANCE=new UseItem();

  UseItem(){
    super("Use item","i");
  }

  @Override
  public String getDescriptiveName(){
    return "Use battle items";
  }

  @Override
  public boolean perform(Combatant c){
    var item=query(c,true);
    if(item==null) return false;
    c.ap+=item.apcost;
    c.source=c.source.clone();
    if(item.use(c)&&item.consumable) item.expend();
    return true;
  }

  /// Asks player to choose an item.
  ///
  /// @param c Active unit.
  /// @param validate `true` to disregard items that can not be used now.
  ///
  /// @return Selected item or `null` if the player has no items; can not use
  ///   any; cancelled...
  public static Item query(Combatant c,boolean validate){
    var f=Fight.current;
    var items=f.getbag(c).stream().sorted(ItemsByName.SINGLETON).toList();
    if(validate) items=items.stream()
        .filter(i->i.usedinbattle&&i.canuse(c)==null).toList();
    if(items.isEmpty()){
      var text="%s does not have any battling items...";
      Javelin.message(text.formatted(c),false);
      return null;
    }
    var engaged=Fight.state.isengaged(c);
    if(engaged) items=items.stream().filter(i->!i.provokesaoo).toList();
    if(items.isEmpty()){
      Javelin.message("Can not use any of these items while engaged...",false);
      return null;
    }
    var text="Which item?";
    var full=items.size()>3;
    var index=Javelin.choose(text,items,full,false);
    return index<0?null:items.get(index);
  }
}
