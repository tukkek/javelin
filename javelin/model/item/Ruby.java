package javelin.model.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javelin.Javelin;
import javelin.controller.action.world.UseItems;
import javelin.controller.exception.GaveUp;
import javelin.controller.fight.Fight;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.terrain.Terrain;
import javelin.controller.wish.Wish.WishScreen;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.conjuration.Summon;
import javelin.view.screen.BattleScreen;

public class Ruby extends Item{
	private static final String PROMPT="Do you want to spend all of your rubies to summon allies?\n"
			+"Press ENTER to confirm or any other key to cancel...";

	public Ruby(){
		super("Wish ruby",0,null);
		consumable=true;
		waste=false;
		usedinbattle=true;
		usedoutofbattle=true;
		targeted=false;
		provokesaoo=true;
	}

	@Override
	public void register(){
		// don't
	}

	@Override
	public void expend(){
		// spent elsewhere
	}

	@Override
	public boolean usepeacefully(Combatant user){
		Squad.active.equipment.clean();
		int rubies=0;
		for(ArrayList<Item> bag:Squad.active.equipment.values())
			for(Item i:bag)
				if(i instanceof Ruby) rubies+=1;
		new WishScreen(rubies).show();
		UseItems.skiperror=true;
		return false;
	}

	@Override
	public boolean use(Combatant user){
		try{
			List<Terrain> terrains=Arrays.asList(Terrain.NONWATER);
			int el=Math.max(1,Math.round(user.source.cr));
			Combatants summoned=EncounterGenerator.generate(el,terrains);
			for(Combatant c:summoned)
				Summon.place(user,c,Fight.state.blueTeam,Fight.state);
			Javelin.redraw();
			BattleScreen.active.center(user.location[0],user.location[1]);
			String feedback="Summoned: "+Javelin.group(summoned).toLowerCase()+"!";
			Javelin.message(feedback,false);
			return true;
		}catch(GaveUp e){
			throw new RuntimeException(e);
		}
	}
}
