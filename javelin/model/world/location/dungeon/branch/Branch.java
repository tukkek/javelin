package javelin.model.world.location.dungeon.branch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javelin.controller.fight.Fight;
import javelin.controller.template.Template;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.Campfire;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.chest.Chest;
import javelin.model.world.location.dungeon.feature.chest.SpecialChest;

/**
 * TODO would be better as a look-up instead of serializable?
 *
 * @author alex
 */
public class Branch implements Serializable{
	public List<Class<? extends Feature>> features=new ArrayList<>(
			List.of(Campfire.class));
	public DungeonHazard hazard=null;
	public List<Template> templates=new ArrayList<>(0);

	public String floor=null;
	public String wall=null;
	public boolean doorbackground=false;
	/**
	 * TODO would this be better as an {@link Item} filter?}
	 */
	public Class<? extends Chest> treasure;

	protected Branch(String floor,String wall){
		this.floor=floor;
		this.wall=wall;
	}

	/** @return If not-<code>null</code>, one replaces {@link SpecialChest}. */
	public SpecialChest generatespecialchest(){
		return null;
	}

	/**
	 * @return <code>false</code> if any units aren't theme- or
	 *         gameplay-appropriate. <code>true</code> by default.
	 */
	public boolean validate(List<Combatant> foes){
		return true;
	}

	public void fight(Fight f){
		return;
	}

	public void define(Dungeon d){
		return;
	}
}