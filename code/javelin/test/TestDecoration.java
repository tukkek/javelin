/**
 *
 */
package javelin.test;

import java.util.List;

import javelin.controller.table.dungeon.feature.DecorationTable;
import javelin.model.world.location.dungeon.feature.Decoration;
import javelin.view.Images;

/**
 * @see Decoration
 * @see DecorationTable
 * @author alex
 */
public class TestDecoration{
	/** Loads all {@link Images} to make sure files are on disk. */
	public static void test(){
		for(var a:DecorationTable.CATEGORIES)
			for(var r:a.getrows())
				Images.get(List.of("dungeon","decoration",r.toString()));
		System.out.println("Total registered images: "+DecorationTable.count());
	}
}
