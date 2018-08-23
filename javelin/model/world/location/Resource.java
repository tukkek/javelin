package javelin.model.world.location;

import java.awt.Image;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javelin.Javelin;
import javelin.controller.generator.feature.FeatureGenerator;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.Season;
import javelin.model.world.location.fortification.Fortification;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * A resource can be extracted by Workers and acts as "mobile labor" which can
 * be returned to {@link Town}s or used to speed {@link Improvement}s.
 *
 * TODO instead of using the traditional {@link FeatureGenerator} maybe have a
 * system for {@link Terrain}s spawning resources based on {@link Season}? This
 * would promote more resoruces without the expense of other {@link Location}s.
 *
 * @see Squad#resources
 * @author alex
 */
public class Resource extends Location{
	static class Type implements Serializable{
		String name;
		int amount;
		String action;
		Terrain terrain;

		public Type(String name,String action,int amount,Terrain t){
			this.name=name;
			this.amount=amount;
			this.action=action;
			terrain=t;
		}
	}

	static final Type CRYSTAL=new Type("Crystal","mining",30,Terrain.MOUNTAINS);
	static final Type FISH=new Type("Fish","fishing",5,Terrain.WATER);
	static final Type FRUIT=new Type("Fruits","picking",5,Terrain.FOREST);
	static final Type GEMS=new Type("Gems","collecting",30,Terrain.DESERT);
	static final Type GRAIN=new Type("Grains","harvesting",15,Terrain.PLAIN);
	static final Type MERCURY=new Type("Mercury","extracting",15,Terrain.MARSH);
	static final Type STONE=new Type("Stone","quarrying",15,Terrain.HILL);
	static final HashMap<Terrain,Type> RESOURCES=new HashMap<>();

	static{
		for(Type t:new Type[]{CRYSTAL,FISH,FRUIT,GEMS,GRAIN,MERCURY,STONE})
			RESOURCES.put(t.terrain,t);
	}

	Type type=RPG.pick(new ArrayList<>(RESOURCES.values()));

	/** Constructor. */
	public Resource(){
		super(null);
		vision=0;
		link=false;
		allowedinscenario=false;
	}

	@Override
	protected void generate(){
		while(x==-1||!type.terrain.equals(Terrain.get(x,y)))
			generate(this,true);
		description=type.name+" (resource)";
		sacrificeable=true;
		allowentry=false;
	}

	@Override
	public Integer getel(int attackerel){
		return Integer.MIN_VALUE;
	}

	@Override
	public List<Combatant> getcombatants(){
		return null;
	}

	@Override
	public Image getimage(){
		return Images.get("locationresource"+type.name.toLowerCase());
	}

	@Override
	public boolean interact(){
		float totalsize=0;
		for(Combatant c:Squad.active.members)
			if(c.source.think(-1)) totalsize+=c.source.size();
		int gold=getspoils();
		if(totalsize==0){
			Character input=Javelin
					.prompt("Your current party members can't harvest this resource.\n"//
							+"Do you want to plunder it instead for $"+Javelin.format(gold)
							+"?\n\n"+"Press p to plunder or any other key to cancel...");
			if(input=='p') plunder(gold);
			return true;
		}
		float time=Math.round(type.amount/totalsize);
		int rounded=Math.round(Math.round(Math.ceil(time)));
		String prompt="With your current party, gathering this resource will take "
				+rounded+" day(s).\n\n" //
				+"Press s to start "+type.action+"\n"//
				+"Press p to immediately plunder resource for $"+Javelin.format(gold)
				+"\n"+ //
				"Press any other key to leave...";
		Character input=Javelin.prompt(prompt);
		if(input=='p')
			plunder(gold);
		else if(input=='s'){
			String result="Your team begins "+type.action+".\n"//
					+"Once done you may bring the resources back to a town.";
			Javelin.message(result,false);
			Squad.active.hourselapsed+=time*24;
			remove();
			Squad.active.resources+=Math.round(type.amount*2*Town.DAILYLABOR);
		}
		return true;
	}

	void plunder(int gold){
		remove();
		Squad.active.gold+=gold;
	}

	int getspoils(){
		int el;
		if(type.amount==5)
			el=1;
		else if(type.amount==15)
			el=2;
		else if(type.amount==30)
			el=3;
		else
			throw new RuntimeException("#unkwnonresourceamount "+type.amount);
		return Fortification.getspoils(el);
	}
}
