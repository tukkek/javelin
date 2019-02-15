package javelin.controller.scenario.artofwar;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javelin.controller.InfiniteList;
import javelin.controller.fight.minigame.battlefield.Reinforcement;
import javelin.controller.terrain.Terrain;
import javelin.model.item.Tier;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.world.location.fortification.Fortification;
import javelin.old.RPG;
import javelin.view.Images;
import javelin.view.screen.InfoScreen;

public class WarLocation extends Fortification{
	static final String DESCRIPTION="A battlefield";

	/**
	 * If <code>true</code>, this is the hardest location in this {@link ArtOfWar}
	 * game and as such the player wins when it is conquered.
	 */
	public boolean win=false;
	float el;
	List<Combatants> hires=new ArrayList<>();

	public WarLocation(float el){
		super(DESCRIPTION,DESCRIPTION,0,0);
		this.el=el;
	}

	@Override
	public List<Combatant> getcombatants(){
		return garrison;
	}

	@Override
	protected void generategarrison(int minlevel,int maxlevel){
		List<Float> squads=new ArrayList<>();
		squads.add(el);
		int tier=Tier.get(Math.round(el)).ordinal()+1;
		while(squads.size()<tier*1.5||RPG.r(1,tier)!=1){
			Float squad=RPG.pick(squads);
			if(squad<=2) break;
			squads.remove(squad);
			squad-=1.6f;
			squads.add(squad);
			squads.add(squad);
		}
		InfiniteList<Integer> ranksi=new InfiniteList<>(List.of(0,1,2),true);
		for(float squad:squads){
			Reinforcement r=new Reinforcement(squad,List.of(Terrain.get(x,y)));
			List<List<Combatant>> ranks=List.of(r.commander,r.elites,r.footsoldiers);
			List<Combatant> reinforcement=ranks.get(ranksi.pop());
			garrison.addAll(reinforcement);
			hires.add(new Combatants(reinforcement));
		}
	}

	@Override
	public Image getimage(){
		return Images.get(win?"flagpolered":"flagpoleblue");
	}

	@Override
	public boolean interact(){
		if(!super.interact()) return false;
		hires.sort((a,b)->a.toString().compareTo(b.toString()));
		var s=new InfoScreen("");
		var message="This is a previously conquered garisson.\n"
				+"It may provide access to the following reinforcements after a battle:\n\n";
		s.print(message
				+hires.stream().map(h->"- "+h).collect(Collectors.joining("\n")));
		s.getInput();
		return true;
	}
}
