package javelin.model.world.location.dungeon.feature;

import java.util.List;

import javelin.Javelin;
import javelin.controller.action.world.WorldMove;
import javelin.controller.collection.InfiniteList;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.old.RPG;

/**
 * TODO would be nice if some of those were secret
 *
 * TODO would be nice to refactor so that {@link StairsUp} and
 * {@link StairsDown} are subclasses?
 *
 * @author alex
 */
public class Passage extends Feature{
	static final InfiniteList<String> DESCRIPTIONS=new InfiniteList<>(
			List.of("tunnel","rope","climb","waterway","well","rail","crawlspace",
					"waterfall","crevasse","stream","corridor","shaft","elevator",
					"stream","ladder","flooded passage"),
			true);

	/** Target. <code>null</code> means outside. */
	public Passage destination=null;
	public DungeonFloor floor;
	public boolean found=false;
	public String description=DESCRIPTIONS.pop();

	transient boolean generated=false;

	public Passage(DungeonFloor f){
		super("passage");
		remove=false;
		floor=f;
	}

	public Passage(Passage entrance,DungeonFloor floor){
		this(floor);
		destination=entrance;
		this.floor=floor;
		description=entrance.description;
		place(floor,floor.getunnocupied());
		generated=true;
	}

	/**
	 * Places the {@link Squad#active} on the other side of {@link #destination}.
	 */
	public void enter(){
		destination.found=true;
		destination.floor.squadlocation=destination.getlocation();
		if(destination.floor!=Dungeon.active) destination.floor.enter();
	}

	@Override
	public boolean activate(){
		String to;
		if(!found)
			to="somewhere unknown";
		else if(destination==null)
			to="outside";
		else if(destination.floor==floor)
			to="to another part of this level";
		else
			to="to level "+destination.floor.getfloor();
		if(Javelin.prompt(
				"Do you want to journey through this "+description+" leading "+to+"?\n"
						+"Press ENTER to confirm or any other key to cancel...")!='\n')
			return false;
		WorldMove.abort=true;
		found=true;
		Squad.active.delay(1);
		if(destination==null)
			floor.leave();
		else
			enter();
		return true;
	}

	@Override
	public void define(DungeonFloor current,List<DungeonFloor> floors){
		if(generated) return;
		super.define(current,floors);
		if(RPG.chancein(3)) return;
		var direction=RPG.chancein(2)?+1:-1;
		var floor=floors.indexOf(current);
		while(RPG.chancein(2))
			floor+=direction;
		if(floor<0) floor=0;
		if(floor>=floors.size()) floor=floors.size()-1;
		destination=new Passage(this,floors.get(floor));
		generated=true;
	}

	@Override
	public String toString(){
		return Javelin.capitalize(description)+" on level "+floor.getfloor();
	}
}
