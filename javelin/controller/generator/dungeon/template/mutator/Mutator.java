package javelin.controller.generator.dungeon.template.mutator;

import javelin.controller.generator.dungeon.template.FloorTile;

public abstract class Mutator{
	public Double chance=null;
	public boolean allowcorridor=false;

	public abstract void apply(FloorTile t);
}
