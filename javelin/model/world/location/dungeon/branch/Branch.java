package javelin.model.world.location.dungeon.branch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javelin.controller.fight.Fight;
import javelin.controller.fight.RandomDungeonEncounter;
import javelin.controller.generator.dungeon.template.MapTemplate;
import javelin.controller.table.dungeon.feature.CommonFeatureTable;
import javelin.controller.template.Template;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.DungeonImages;
import javelin.model.world.location.dungeon.feature.Campfire;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.chest.Chest;
import javelin.model.world.location.dungeon.feature.chest.SpecialChest;

/**
 * See https://github.com/tukkek/javelin/issues/254 .
 *
 * TODO add {@link Fight} mechanics
 *
 * TODO add {@link MapTemplate}s
 *
 * @author alex
 */
public class Branch implements Serializable{
	/** Will be added very favorably to {@link CommonFeatureTable}. */
	public List<Class<? extends Feature>> features=new ArrayList<>(
			List.of(Campfire.class));
	/** Theme-appropriate {@link Dungeon} mechanic. */
	public DungeonHazard hazard=null;
	/** Will optimially be applied to all {@link RandomDungeonEncounter}s. */
	public List<Template> templates=new ArrayList<>(0);
	/** @see DungeonImages#FLOOR */
	public String floor=null;
	/** @see DungeonImages#WALL */
	public String wall=null;
	/** @see Dungeon#doorbackground */
	public boolean doorbackground=false;
	/** Favored treasure types. */
	public List<Class<? extends Chest>> treasure=new ArrayList<>(1);
	/** To be used as {@link Monster} pools. */
	public Set<Terrain> terrains=new HashSet<>(1);

	/** Constructor. */
	protected Branch(String floor,String wall){
		this.floor=floor;
		this.wall=wall;
	}

	/** @return If not-<code>null</code>, one replaces {@link SpecialChest}. */
	public Feature generatespecialchest(DungeonFloor f){
		return null;
	}

	/**
	 * @return <code>false</code> if any units aren't theme- or
	 *         gameplay-appropriate. <code>true</code> by default.
	 */
	public boolean validate(List<Combatant> foes){
		return true;
	}

	/** Allows custimization of battles. */
	public void fight(Fight f){
		return;
	}

	/** Allows customization of dungeons. */
	public void define(Dungeon d){
		return;
	}
}