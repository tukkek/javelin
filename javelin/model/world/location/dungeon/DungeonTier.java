package javelin.model.world.location.dungeon;

import java.util.List;

import javelin.controller.challenge.Tier;
import javelin.model.world.location.dungeon.branch.temple.Temple;

public class DungeonTier{
	public static final DungeonTier CAVE=new DungeonTier("Cave",Tier.LOW,"cave");
	public static final DungeonTier DUNGEON=new DungeonTier("Dungeon",Tier.MID,
			"");
	public static final DungeonTier KEEP=new DungeonTier("Keep",Tier.HIGH,"keep");
	/** @see Temple */
	public static final DungeonTier TEMPLE=new DungeonTier("Temple",Tier.EPIC,
			"temple");

	public static final List<DungeonTier> TIERS=List.of(CAVE,DUNGEON,KEEP,TEMPLE);
	public static final DungeonTier HIGHEST=TEMPLE;

	public String name;
	public int minrooms;
	public int maxrooms;
	public String floor;
	public String wall;
	public Tier tier;

	public DungeonTier(String name,Tier tier,String tilesuffix){
		this.name=name;
		this.tier=tier;
		minrooms=tier.maxlevel;
		maxrooms=tier.maxlevel*3;
		floor="floor"+tilesuffix;
		wall="wall"+tilesuffix;
	}

	static public DungeonTier get(int level){
		for(var t:TIERS)
			if(level<=t.tier.maxlevel) return t;
		return KEEP;
	}

	public String getimagename(){
		return name.toLowerCase();
	}

	@Override
	public String toString(){
		return name;
	}
}