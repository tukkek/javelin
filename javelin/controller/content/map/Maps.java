package javelin.controller.content.map;

import java.util.ArrayList;

import javelin.old.RPG;

/**
 * @see #pick()
 *
 * @author alex
 */
public class Maps extends ArrayList<Map>{
	/**
	 * @return a random map from this list.
	 */
	public Map pick(){
		ArrayList<Map> clone=new ArrayList<>(this);
		for(Map m:this)
			if(!m.validate()) clone.remove(m);
		return RPG.pick(clone);
	}
}