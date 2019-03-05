package javelin.controller.scenario.artofwar;

import java.awt.Image;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javelin.controller.InfiniteList;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.minigame.battlefield.Reinforcement;
import javelin.controller.map.Map;
import javelin.controller.terrain.Terrain;
import javelin.model.item.Tier;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.world.location.fortification.Fortification;
import javelin.model.world.location.haunt.Haunt;
import javelin.old.RPG;
import javelin.view.Images;
import javelin.view.screen.InfoScreen;

public class WarLocation extends Fortification{
	static final String DESCRIPTION="A battlefield";
	float el;
	public List<Combatants> hires=new ArrayList<>();
	public String avatar;
	Class<? extends Map> map;
	public boolean town=false;

	public WarLocation(float el,String avatar){
		super(DESCRIPTION,DESCRIPTION,0,0);
		this.el=el;
		this.avatar=avatar;
	}

	public WarLocation(Haunt h){
		this(h.getel(null),h.getimagename());
		garrison.addAll(h.garrison);
		map=h.getmap().getClass();
	}

	@Override
	public List<Combatant> getcombatants(){
		return garrison;
	}

	@Override
	protected void generategarrison(int minlevel,int maxlevel){
		var pregenerated=!garrison.isEmpty();
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
		var t=terrain==null?Terrain.get(x,y):terrain;
		for(float squad:squads){
			Reinforcement r=new Reinforcement(squad,List.of(t));
			List<List<Combatant>> ranks=List.of(r.commander,r.elites,r.footsoldiers);
			List<Combatant> reinforcement=ranks.get(ranksi.pop());
			hires.add(new Combatants(reinforcement));
			if(!pregenerated) garrison.addAll(reinforcement);
		}
	}

	@Override
	public Image getimage(){
		return Images.get(avatar);
	}

	@Override
	public boolean interact(){
		try{
			if(!super.interact()) return false;
		}catch(StartBattle b){
			if(map!=null) try{
				b.fight.map=map.getConstructor().newInstance();
			}catch(InstantiationException|IllegalAccessException
					|IllegalArgumentException|InvocationTargetException
					|NoSuchMethodException|SecurityException e){
						e.printStackTrace();
					}
			throw b;
		}
		hires.sort((a,b)->a.toString().compareTo(b.toString()));
		var s=new InfoScreen("");
		var message="This is a previously conquered garisson.\n"
				+"It may provide access to the following reinforcements after a battle:\n\n";
		s.print(message
				+hires.stream().map(h->"- "+h).collect(Collectors.joining("\n")));
		s.getInput();
		return true;
	}

	public void setdungeon(){
		terrain=Terrain.UNDERGROUND;
		map=RPG.pick(terrain.getmaps()).getClass();
	}
}
