/**
 *
 */
package javelin.test;

import java.util.List;

import javelin.controller.table.dungeon.feature.FurnitureTable;
import javelin.model.world.location.dungeon.feature.Furniture;
import javelin.view.Images;

/**
 * @see Furniture
 * @see FurnitureTable
 * @author alex
 */
public class TestFurniture{
	/** Loads all {@link Images} to make sure files are on disk. */
	public static void test(){
		for(var a:FurnitureTable.CATEGORIES)
			for(var r:a.getrows())
				Images.get(List.of("dungeon","furniture",r.toString()));
		System.out.println("Total registered images: "+FurnitureTable.count());
	}
}
