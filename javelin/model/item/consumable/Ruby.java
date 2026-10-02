package javelin.model.item.consumable;

import javelin.Javelin;
import javelin.controller.content.fight.Fight;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.model.item.CommandItem;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.conjuration.Summon;
import javelin.model.world.location.dungeon.feature.rare.Fountain;
import javelin.view.screen.BattleScreen;

/**
 * TODO deactivate trap (rubies = dungeon level)
 *
 * @author alex
 */
public class Ruby extends CommandItem{
  private static final String PROMPT="Do you want to spend all of your rubies to summon allies?\n"
      +"Press ENTER to confirm or any other key to cancel...";

  public Ruby(){
    super("Wish ruby",0,false);
    consumable=true;
    waste=false;
    usedoutofbattle=true;
    targeted=false;
    sellvalue=0;
  }

  @Override
  public boolean usepeacefully(Combatant user){
    //TODO test and Wish is probably no longer needed
    //TODO new Hire action as third use for rubies? instead of per-screen logic
    for(var member:Squad.active) Fountain.heal(member);
    for(var item:Squad.active.equipment.getall())
      item.refresh(Integer.MAX_VALUE);
    Javelin.message("The ruby brings your party back to full strength!",true);
    /* Squad.active.equipment.clean(); var rubies=0; for(ArrayList<Item>
     * bag:Squad.active.equipment.values()) for(Item i:bag) if(i instanceof
     * Ruby) rubies+=1; new WishScreen(rubies).show();
     * UseItems.skiperror=true; */
    return true;
  }

  @Override
  public boolean use(Combatant user){
    var el=Math.max(1,Math.round(user.source.cr));
    var summoned=EncounterGenerator.generate(el,Terrain.NONWATER);
    for(Combatant c:summoned)
      Summon.place(user,c,Fight.state.blueteam,Fight.state);
    Javelin.redraw();
    BattleScreen.active.center(user.location[0],user.location[1]);
    var feedback="Summoned: "+Javelin.group(summoned).toLowerCase()+"!";
    Javelin.message(feedback,false);
    return true;
  }
}
