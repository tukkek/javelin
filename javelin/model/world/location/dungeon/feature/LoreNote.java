/**
 *
 */
package javelin.model.world.location.dungeon.feature;

import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.Lore;
import javelin.old.RPG;

/**
 * Discovers {@link Lore} about another {@link Dungeon}. Generated on-demand and
 * not while generating {@link Dungeon} so we can have access to all
 * {@link Dungeon#lore} .
 *
 * @author alex
 */
public class LoreNote extends Feature{
	static final String MESSAGE="In this book you find the following information about %s:\n"
			+"- %s\n\n"
			+"(You can view all your lore notes at any time through the Known Lore creen).";

	/** Constructor. */
	public LoreNote(){
		super("dungeonlorenote","lore note");
	}

	@Override
	public boolean activate(){
		remove();
		var tier=Dungeon.active.gettier().tier;
		var candidates=Dungeon.getdungeonsandtemples().stream().filter(
				d->tier.minlevel<=d.level&&d.level<=tier.maxlevel+5&&d!=Dungeon.active)
				.collect(Collectors.toList());
		var d=RPG.pick(candidates);
		var lore=RPG.pick(d.lore);
		lore.discovered=true;
		Javelin.message(String.format(MESSAGE,d,lore),true);
		return false;
	}
}
