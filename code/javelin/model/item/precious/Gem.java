package javelin.model.item.precious;

import java.util.ArrayList;
import java.util.List;

/**
 * Precious stones.
 *
 * @author alex
 */
public class Gem extends PreciousObject{
	/** Constructor. */
	public Gem(String name,int dice,int sides,int multiplier){
		super(name,dice,sides,multiplier,"gem");
	}

	/**
	 * @return A list with all gems in the game, meant to be used during loot
	 *         registration procedures.
	 */
	public static List<Gem> generate(){
		var gems=new ArrayList<Gem>();
		for(var gem:new String[]{"Banded agate","Eye agate","Moss agate","Azurite",
				"Blue quartz","Hematite","Lapis lazuli","Malachite","Obsidian",
				"Rhodochrosite","Tiger eye turquoise","Irregular freshwater pearl"})
			gems.add(new Gem(gem,4,4,1));
		for(var gem:new String[]{"Bloodstone","Carnelian","Chalcedony",
				"Chrysoprase","Citrine","Iolite, Jasper","Moonstone","Onyx","Peridot",
				"Clear quartz","Sard","Sardonyx","Rose quartz","Smoky quartz",
				"Star rose quartz","Zircon"})
			gems.add(new Gem(gem,2,4,10));
		for(var gem:new String[]{"Amber","Amethyst","Chrysoberyl","Coral",
				"Red garnet","Brown-green garnet","Jade","Jet","Silver pearl",
				"White pearl","Golden pearl","Pink pearl","Red spinel",
				"Red-brown spinel","Deep green spinel","Tourmaline"})
			gems.add(new Gem(gem,4,4,10));
		for(var gem:new String[]{"Alexandrite","Aquamarine","Violet garnet",
				"Black pearl","Deep blue spinel","Golden yellow topaz"})
			gems.add(new Gem(gem,2,4,100));
		for(var gem:new String[]{"Emerald","Fire opal","White opal","Black opal",
				"Blue sapphire","Fiery yellow corundum","Rich purple corundum",
				"Blue star sapphire","Black star sapphire","Star ruby"})
			gems.add(new Gem(gem,4,4,100));
		for(var gem:new String[]{"Clearest bright green emerald",
				"Blue-white diamond","Canary diamond","Pink diamond","Brown diamond",
				"Blue diamond","Jacinth"})
			gems.add(new Gem(gem,2,4,1000));
		return gems;
	}
}
