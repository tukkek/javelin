package javelin.model;

import java.awt.Color;
import java.io.Serializable;
import java.util.List;

import javelin.controller.content.terrain.Terrain;
import javelin.controller.content.terrain.Underground;
import javelin.model.item.artifact.Amulet;
import javelin.model.item.artifact.Ankh;
import javelin.model.item.artifact.Artifact;
import javelin.model.item.artifact.Candle;
import javelin.model.item.artifact.Crown;
import javelin.model.item.artifact.Flute;
import javelin.model.item.artifact.Map;
import javelin.model.item.artifact.Skull;

/**
 * A realm is an in-game faction, associated with OGL concepts in an arbitrary
 * manner, representing a "generic strategy game lore" of colors/factions/races.
 *
 * TODO this needn't be {@link Serializable} necessarily
 *
 * @author alex
 */
public class Realm implements Serializable{
	/** Realm of the element of air. */
	public static final Realm AIR=new Realm("air",Color.GRAY,Terrain.MOUNTAINS,
			List.of(new Flute()));
	/** Realm of the element of earth. */
	public static final Realm EARTH=new Realm("earth",Color.GREEN.darker(),
			Terrain.FOREST,List.of(new Map()));
	/** Realm of negative energy. */
	public static final Realm EVIL=new Realm("evil",Color.BLACK,Terrain.MARSH,
			List.of(new Skull()));
	/** Realm of the element of fire. */
	public static final Realm FIRE=new Realm("fire",Color.RED,Terrain.DESERT,
			List.of(new Candle()));
	/** Realm of positive energy. */
	public static final Realm GOOD=new Realm("good",Color.WHITE,Terrain.PLAIN,
			List.of(new Ankh()));
	/** Transcendent realm. */
	public static final Realm MAGIC=new Realm("magic",Color.MAGENTA,Terrain.HILL,
			List.of(new Amulet()));
	/** Realm of the element of water. */
	public static final Realm WATER=new Realm("water",Color.BLUE,Terrain.WATER,
			List.of(new Crown()));
	/** Every realm. */
	public static final List<Realm> REALMS=List.of(AIR,EARTH,EVIL,FIRE,GOOD,MAGIC,
			WATER);

	/** Human-friendly name. */
	public String name;
	/** Realm color for view components. */
	public Color color;
	/** Each Terrain corresponds to a Realm (except {@link Underground}). */
	public Terrain terrain;
	/** Each artifact is associated with a Realm. */
	public List<Artifact> artifacts;

	Realm(String name,Color c,Terrain t,List<Artifact> artifacts){
		this.name=name;
		color=c;
		terrain=t;
		this.artifacts=artifacts;
	}

	@Override
	public boolean equals(Object o){
		if(!(o instanceof Realm)) return false;
		var r=(Realm)o;
		return name.equals(r.name);
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