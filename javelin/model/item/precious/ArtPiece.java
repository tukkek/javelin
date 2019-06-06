package javelin.model.item.precious;

import java.util.ArrayList;
import java.util.List;

/**
 * Art objects.
 *
 * @author alex
 */
public class ArtPiece extends PreciousObject{
	/** Constructor. */
	public ArtPiece(String name,int dice,int sides,int multiplier){
		super(name,dice,sides,multiplier,"art piece");
	}

	/**
	 * @return All art pieces in the game, to be registered as loot during
	 *         startup.
	 */
	public static List<ArtPiece> generate(){
		var gems=new ArrayList<ArtPiece>();
		for(var gem:new String[]{"Silver ewer","Carved bone statuette",
				"Ivory statuette","Finely wrought small gold bracelet"})
			gems.add(new ArtPiece(gem,1,10,10));
		for(var gem:new String[]{"Cloth of gold vestments",
				"Black velvet mask with numerous citrines",
				"Silver chalice with lapis lazuli gems"})
			gems.add(new ArtPiece(gem,3,6,10));
		for(var gem:new String[]{"Large well-done wool tapestry",
				"Brass mug with jade inlays"})
			gems.add(new ArtPiece(gem,1,6,100));
		for(var gem:new String[]{"Silver comb with moonstones",
				"Silver-plated steel longsword"})
			gems.add(new ArtPiece(gem,1,10,100));
		for(var gem:new String[]{"Carved harp of exotic wood","Solid gold idol"})
			gems.add(new ArtPiece(gem,2,6,100));
		for(var gem:new String[]{"Gold dragon comb with red garnet eye",
				"Gold and topaz bottle stopper cork","Ceremonial electrum dagger"})
			gems.add(new ArtPiece(gem,3,6,100));
		for(var gem:new String[]{"Eyepatch with sapphire-moonstone mock eye",
				"Fire opal pendant on a fine gold chain","Old masterpiece painting"})
			gems.add(new ArtPiece(gem,4,6,100));
		for(var gem:new String[]{"Embroidered silk and velvet mantle",
				"Sapphire pendant on gold chain"})
			gems.add(new ArtPiece(gem,5,6,100));
		for(var gem:new String[]{"Embroidered and bejeweled glove","Jeweled anklet",
				"Gold music box"})
			gems.add(new ArtPiece(gem,1,4,1000));
		for(var gem:new String[]{"Golden circlet with four aquamarines",
				"Small pink pearls necklace"})
			gems.add(new ArtPiece(gem,1,6,1000));
		for(var gem:new String[]{"Jeweled gold crown","Jeweled electrum ring"})
			gems.add(new ArtPiece(gem,2,4,1000));
		for(var gem:new String[]{"Gold and ruby ring","Gold cup set with emeralds"})
			gems.add(new ArtPiece(gem,2,6,1000));
		return gems;
	}
}
