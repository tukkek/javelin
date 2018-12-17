package javelin.model.world;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.Javelin;
import javelin.controller.terrain.Terrain;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.item.artifact.Artifact;
import javelin.model.item.key.TempleKey;
import javelin.model.unit.Combatant;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.basic.Growth;
import javelin.old.RPG;
import javelin.old.messagepanel.MessagePanel;
import javelin.view.Images;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.shopping.ShoppingScreen;
import javelin.view.screen.town.PurchaseOption;

/**
 * A figure that travels from one city to a human (neutral) city. It can be
 * visited to buy {@link Item}s. If it reaches a human {@link Town} it grows by
 * 1 {@link Town#population} - this is an incentive for the player to protect
 * merchants.
 *
 * Unlike {@link Town} {@link Item}s, these are not crafted but sold as-is, and
 * as such are removed after purchase.
 *
 * @author alex
 */
public class Caravan extends Actor{
	public static final boolean ALLOW=true;

	static final int NUMBEROFITEMS=6;
	static final int MINARTIFACTS=1;
	static final int MAXARTIFACTS=3;

	class CaravanScreen extends ShoppingScreen{
		/** Constructor. */
		public CaravanScreen(){
			super("You reach a trading caravan:",null);
		}

		@Override
		protected void afterpurchase(PurchaseOption o){
			inventory.remove(o.i);
			o.i.clone().grab();
		}

		@Override
		protected ItemSelection getitems(){
			return inventory;
		}
	}

	/** Selection of {@link Item}s available for purchase. */
	public ItemSelection inventory=new ItemSelection();

	int tox=-1;
	int toy=-1;
	/** Merchants are slow, act once very other turn. */
	boolean ignoreturn=true;
	int el;

	/** Creates a merchant in the world map but doesn't {@link #place()} it. */
	public Caravan(int x,int y,int el){
		allowedinscenario=false;
		this.x=x;
		this.y=y;
		this.el=el;
		displace();
		determinedestination(Town.gettowns());
		while(inventory.size()<NUMBEROFITEMS){
			Item i=RPG.pick(Item.ALL);
			if(!(i instanceof Artifact)){
				i=i.clone();
				inventory.add(i);
			}
		}
		int withartifacts=inventory.size()+RPG.r(MINARTIFACTS,MAXARTIFACTS);
		while(inventory.size()<withartifacts){
			Item i=RPG.pick(Item.ARTIFACT);
			inventory.add(i);
		}
		if(World.scenario.lockedtemples){
			TempleKey k=TempleKey.generate();
			if(k.price>0) inventory.add(k);
		}
	}

	/** Creates a caravan near a {@link Town}. */
	public Caravan(Town t){
		this(t.x,t.y,t.population);
	}

	/** Reflection-friendly constructor. */
	public Caravan(){
		this(RPG.pick(Town.gettowns()));
	}

	void determinedestination(ArrayList<Town> towns){
		if(towns!=null){
			Collections.shuffle(towns);
			for(var t:towns){
				if(t.x==x&&t.y==y) continue;
				if(t.garrison.isEmpty()&&!Incursion.crosseswater(this,t.x,t.y)){
					tox=t.x;
					toy=t.y;
					return;
				}
			}
		}
		while(tox==-1||Incursion.crosseswater(this,tox,toy)){
			tox=RPG.r(0,World.scenario.size-1);
			toy=RPG.r(0,World.scenario.size-1);
		}
	}

	@Override
	public void turn(long time,WorldScreen world){
		if(ignoreturn){
			ignoreturn=false;
			return;
		}
		ignoreturn=true;
		int x=this.x+calculatedelta(this.x,tox);
		int y=this.y+calculatedelta(this.y,toy);
		if(Terrain.get(x,y).equals(Terrain.WATER)){
			tox=-1;
			determinedestination(null);
			return;
		}
		Actor here=World.get(x,y);
		this.x=x;
		this.y=y;
		place();
		if(x==tox&&y==toy){
			if(here instanceof Town){
				Town town=(Town)here;
				if(town.garrison.isEmpty()&&town.population<Growth.MAXPOPULATION){
					town.population+=1;
					announce(town);
				}
			}
			remove();
		}else if(here!=null){
			ignoreturn=false;
			turn(0,null);// jump over other Actors
		}
	}

	void announce(Town town){
		if(Javelin.DEBUG) return;
		MessagePanel.active.clear();
		Javelin.message("A merchant arrives at "+town
				+", city grows! Press ENTER to continue...",Javelin.Delay.NONE);
		while(Javelin.input().getKeyChar()!='\n'){
			// wait for ENTER
		}
		MessagePanel.active.clear();
	}

	int calculatedelta(int from,int to){
		if(to>from) return +1;
		if(to<from) return -1;
		return 0;
	}

	@Override
	public Boolean destroy(Incursion attacker){
		return true;
	}

	@Override
	public boolean interact(){
		new CaravanScreen().show();
		return true;
	}

	@Override
	public Image getimage(){
		return Images.get("caravan");
	}

	@Override
	public List<Combatant> getcombatants(){
		return null;
	}

	@Override
	public String describe(){
		return "Caravan.";
	}

	@Override
	public Integer getel(Integer attackerel){
		return el;
	}
}
