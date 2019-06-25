package javelin.model.world.location.dungeon;

import java.util.List;

import javelin.model.item.Tier;

public class DungeonTier{
	public static final DungeonTier CAVE=new DungeonTier("Cave",Tier.LOW,"cave");
	public static final DungeonTier DUNGEON=new DungeonTier("Dungeon",Tier.MID,
			"");
	public static final DungeonTier KEEP=new DungeonTier("Keep",Tier.HIGH,"keep");
	public static final DungeonTier RUINS=new DungeonTier("Ruins",Tier.EPIC,
			"ruins");

	public static final List<DungeonTier> TIERS=List.of(CAVE,DUNGEON,KEEP,RUINS);
	public static final DungeonTier HIGHEST=RUINS;

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
		maxrooms=tier.maxlevel*4;
		floor="dungeonfloor"+tilesuffix;
		wall="dungeonwall"+tilesuffix;
	}

	static public DungeonTier get(int level){
		for(var t:TIERS)
			if(level<=t.tier.maxlevel) return t;
		return KEEP;
	}

	public String getimagename(){
		return "location"+name.toLowerCase();
	}
}