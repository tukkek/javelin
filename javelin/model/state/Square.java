package javelin.model.state;

import javelin.controller.content.map.Map;

/**
 * A singular area of a {@link Map}.
 *
 * @author alex
 */
public class Square{
	public boolean blocked=false;
	public boolean obstructed=false;
	public boolean flooded=false;

	@Override
	public String toString(){
		if(blocked) return "#";
		if(obstructed) return "-";
		if(flooded) return "~";
		return " ";
	}

	public void clear(){
		blocked=false;
		obstructed=false;
		flooded=false;
	}
}