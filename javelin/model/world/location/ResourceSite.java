package javelin.model.world.location;

import java.awt.Image;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javelin.Javelin;
import javelin.controller.terrain.Terrain;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * A resource lifecycle is composed of three parts:
 *
 * 1. The {@link Resource} itself.
 *
 * 2. A {@link ResourceSite}, which there can be multiple of for the same
 * {@link Resource}.
 *
 * 3. A {@link ResourceLink}, which there can be multiple of for a given
 * {@link Resource} site. However, as soon as a link is used to establish a
 * connection to a {@link Town}, the site and all other links are invalidated.
 *
 * @see Squad#resources
 * @author alex
 */
public class ResourceSite extends Location{
	/**
	 * A type of natural resource.
	 *
	 * @author alex
	 */
	public static class Resource implements Serializable{
		public String name;
		public Terrain terrain;

		/**
		 * @param name Description.
		 * @param action Harvesting description.
		 * @param t Terrain resource is found on.
		 */
		public Resource(String name,Terrain t){
			this.name=name;
			terrain=t;
		}

		@Override
		public boolean equals(Object obj){
			Resource r=obj instanceof Resource?(Resource)obj:null;
			return r!=null&&r.name.equals(name);
		}

		@Override
		public int hashCode(){
			return name.hashCode();
		}

		@Override
		public String toString(){
			return name;
		}
	}

	/** All existing resources. */
	public static final HashMap<Terrain,Resource> RESOURCES=new HashMap<>();
	static final Resource CRYSTAL=new Resource("Crystal",Terrain.MOUNTAINS);
	static final Resource FISH=new Resource("Fish",Terrain.WATER);
	static final Resource FRUIT=new Resource("Fruits",Terrain.FOREST);
	static final Resource GEMS=new Resource("Gems",Terrain.DESERT);
	static final Resource GRAIN=new Resource("Grains",Terrain.PLAIN);
	static final Resource MERCURY=new Resource("Mercury",Terrain.MARSH);
	static final Resource STONE=new Resource("Stone",Terrain.HILL);

	static{
		for(Resource t:new Resource[]{CRYSTAL,FISH,FRUIT,GEMS,GRAIN,MERCURY,STONE})
			RESOURCES.put(t.terrain,t);
	}

	static class ResourceLink extends Item{
		ResourceSite site;

		public ResourceLink(ResourceSite site){
			super("Map to "+site.type.name.toLowerCase()+" site",0,null);
			this.site=site;
			usedinbattle=false;
			usedoutofbattle=true;
			targeted=false;
			consumable=false;
		}

		@Override
		public int hashCode(){
			return (site.type.toString()+site.x+":"+site.y).hashCode();
		}

		@Override
		public boolean equals(Object obj){
			if(!(obj instanceof ResourceLink)) return false;
			return hashCode()==obj.hashCode();
		}

		@Override
		public boolean usepeacefully(Combatant user){
			if(site.claimed!=null){
				expend();
				var claimed="This resource site has already been claimed by "
						+site.claimed+"...";
				Javelin.message(claimed,true);
				return true;
			}
			var d=Squad.active.getdistrict();
			if(d==null||d.town.ishostile()){
				var friendly="Resources can only be linked to friendly towns...";
				Javelin.message(friendly,true);
				return true;
			}
			var t=d.town;
			var name=site.type.name.toLowerCase();
			if(t.resources.contains(site.type)){
				var duplicate=t+" is already linked to a source of "+name+"...";
				Javelin.message(duplicate,true);
				return true;
			}
			t.resources.add(site.type);
			site.claimed=t;
			site.remove();
			expend();
			Javelin.message("You connect a "+name+" site to "+t+"!",true);
			return true;
		}
	}

	Resource type=RPG.pick(new ArrayList<>(RESOURCES.values()));
	/** Used to prevent a site from being claimed more than once. */
	Town claimed=null;

	/** Constructor. */
	public ResourceSite(){
		super(null);
		vision=0;
		link=false;
		allowedinscenario=false;
		discard=false;
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
	public List<Combatant> getcombatants(){
		return null;
	}

	@Override
	public Image getimage(){
		return Images.get("locationresource"+type.name.toLowerCase());
	}

	@Override
	public boolean interact(){
		var link=new ResourceLink(this);
		var existing=Squad.active.equipment.get(link);
		if(existing!=null){
			var duplicate="You already have a link to this "+type.name.toLowerCase()
					+" site...";
			Javelin.message(duplicate,false);
			return false;
		}
		Javelin.message("You come across a "+type.name.toLowerCase()+" site!\n"
				+"Bring this information to a friendly town to help boost its economy!",
				true);
		link.grab(Squad.active);
		return true;
	}
}
