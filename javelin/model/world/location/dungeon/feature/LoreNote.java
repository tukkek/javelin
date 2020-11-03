/**
 *
 */
package javelin.model.world.location.dungeon.feature;

import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.table.dungeon.feature.FeatureModifierTable;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
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
	static final String SUCCESS="In this book you find the following information about %s:\n"
			+"- %s\n\n"
			+"(You can view all your lore notes at any time through the Known Lore creen).";
	static final String FAILURE="%s could not extract any useful information from this text...";

	int dc=10+Dungeon.active.level
			+Dungeon.active.tables.get(FeatureModifierTable.class).roll();

	/** Constructor. */
	public LoreNote(){
		super("lore note");
	}

	@Override
	public boolean activate(){
		var reader=Squad.active.getbest(Skill.KNOWLEDGE);
		if(reader.taketen(Skill.KNOWLEDGE)<dc){
			Javelin.message(String.format(FAILURE,reader),false);
			return false;
		}
		remove();
		var tier=Dungeon.active.gettier().tier;
		var candidates=Dungeon.getdungeonsandtemples().stream().filter(
				d->tier.minlevel<=d.level&&d.level<=tier.maxlevel+5&&d!=Dungeon.active)
				.collect(Collectors.toList());
		var d=RPG.pick(candidates);
		var lore=RPG.pick(d.lore);
		lore.discovered=true;
		Javelin.message(String.format(SUCCESS,d,lore),true);
		return false;
	}
}
