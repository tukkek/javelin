package javelin.model.world.location.dungeon.branch.temple;

import java.util.List;

import javelin.Javelin;
import javelin.controller.fight.Fight;
import javelin.controller.fight.mutator.Mutator;
import javelin.controller.terrain.Marsh;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.item.artifact.Skull;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.Period;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.branch.Branch;
import javelin.model.world.location.dungeon.branch.DungeonHazard;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.StairsDown;
import javelin.model.world.location.dungeon.feature.StairsUp;
import javelin.model.world.location.dungeon.feature.chest.ArtifactChest;

/**
 * Found drowning in the {@link Marsh}. Good creatures are never found in the
 * temple. An evil force can bring you back to the stairs up at any point (or
 * stairs down if you have the {@link Skull}).
 *
 * @see Temple
 * @see Monster#morals
 * @author alex
 */
public class EvilTemple extends Temple{
	/** Branch singleton. */
	public static final Branch BRANCH=new EvilBranch();

	private static final String FLUFF="You have heard of this fort once before, upon a starless night.\n"
			+"You recognize the looming towers from that tale. It was related to you as the Fortress of Regrets.\n"
			+"It is said that a powerful king oversaw his domain from his throne here but ill tidings befell his reign.\n"
			+"The once-great castle became a prison, torture chamber and hall of twisted pleasures as the kingdom slowly faded into oblivion.";

	static class Dark extends Mutator{
		@Override
		public void setup(Fight f){
			super.setup(f);
			f.period=Period.NIGHT;
		}
	}

	static class MacabreForce extends DungeonHazard{
		public MacabreForce(){
			chancemodifier/=9;
		}

		@Override
		public boolean trigger(){
			Javelin.message("A macabre force draws upon you...",true);
			Class<? extends Feature> targettype;
			if(Squad.active.equipment.get(Skull.class)==null)
				targettype=StairsUp.class;
			else if(Dungeon.active.features.has(ArtifactChest.class))
				targettype=ArtifactChest.class;
			else
				targettype=StairsDown.class;
			var target=Dungeon.active.features.stream()
					.filter(f->targettype.isInstance(f)).findAny().orElse(null);
			if(target==null) return false;
			Dungeon.active.squadlocation=target.getlocation();
			return true;
		}
	}

	static class EvilBranch extends Branch{
		protected EvilBranch(){
			super("floortempleevil","walltempleevil");
			doorbackground=false;
			hazard=new MacabreForce();
			terrains.add(Terrain.MARSH);
			mutators.add(new Dark());
		}

		@Override
		public void define(Dungeon d){
			super.define(d);
			d.vision/=2;
		}

		@Override
		public boolean validate(List<Combatant> foes){
			for(var c:foes)
				if(c.source.alignment.isgood()) return false;
			return true;
		}
	}

	/** Constructor. */
	public EvilTemple(){
		super(Realm.EVIL,new EvilBranch(),FLUFF);
	}
}
