package javelin.model.world.location.fortification;

import java.util.List;

import javelin.controller.challenge.CrCalculator;
import javelin.controller.kit.Kit;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.World;
import javelin.model.world.location.town.labor.military.Academy;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.hiringacademy.HiringGuildScreen;
import javelin.view.screen.hiringacademy.RecruitingGuildScreen;
import javelin.view.screen.upgrading.AcademyScreen;
import tyrant.mikera.engine.RPG;

/**
 * TODO all methods marked final to help with refactoring, may remove it
 * 
 * @author alex
 */
public abstract class Guild extends Academy {
	Combatant[] hires;
	Kit kit;
	boolean hire;

	public Guild(String string, Kit k, boolean hire) {
		super(string, string, k.upgrades);
		this.kit = k;
		this.hire = hire;
		hires = generatehires();
		while (!hashire()) {
			turn();
		}
	}

	@Override
	public final boolean isworking() {
		return super.isworking() || (!hashire() && !ishostile());
	}

	boolean hashire() {
		for (Combatant c : gethires()) {
			if (c != null) {
				return true;
			}
		}
		return false;
	}

	protected final Combatant generatehire(int chance, String title,
			int minlevel, int maxlevel, Kit kit) {
		return RPG.chancein(chance) ? Guild.generatehire(title, minlevel,
				maxlevel, kit, RPG.pick(getcandidates())) : null;
	}

	protected Combatant generatehire(int chance, String title, int minlevel,
			int maxlevel) {
		return generatehire(chance, title, minlevel, maxlevel, kit);
	}

	protected abstract List<Monster> getcandidates();

	@Override
	protected final AcademyScreen getscreen() {
		return hire ? new HiringGuildScreen(this)
				: new RecruitingGuildScreen(this);
	}

	public final Combatant[] gethires() {
		return hires;
	}

	public final void clearhire(Combatant hire) {
		for (int i = 0; i < hires.length; i++) {
			if (hires[i] == hire) {
				hires[i] = null;
				return;
			}
		}
	}

	@Override
	public final List<Combatant> getcombatants() {
		List<Combatant> combatants = super.getcombatants();
		for (Combatant hire : hires) {
			if (hire != null) {
				combatants.add(hire);
			}
		}
		return combatants;
	}

	@Override
	public final int getlabor() {
		return super.getlabor() + gethires().length;
	}

	@Override
	protected final void generategarrison(int minlevel, int maxlevel) {
		targetel = RPG.r(CrCalculator.leveltoel(minlevel),
				CrCalculator.leveltoel(maxlevel));
		if (World.scenario.clearlocations) {
			return;
		}
		while (CrCalculator.calculateel(garrison) < targetel) {
			Combatant[] hires = generatehires();
			for (Combatant hire : hires) {
				if (hire != null) {
					garrison.add(hire);
				}
			}
		}
	}

	protected abstract Combatant[] generatehires();

	@Override
	public final void turn(long time, WorldScreen world) {
		turn();
	}

	void turn() {
		if (ishostile()) {
			return;
		}
		Combatant[] candidates = generatehires();
		for (int i = 0; i < candidates.length; i++) {
			if (candidates[i] != null) {
				hires[i] = candidates[i];
			}
		}
	}

	public static Combatant generatehire(String title, int minlevel,
			int maxlevel, Kit k, Monster m) {
		Combatant c = new Combatant(m.clone(), true);
		int target = RPG.r(minlevel, maxlevel);
		int tries = target * 100;
		while (c.source.challengerating < target) {
			c.upgrade(k.upgrades);
			tries -= 1;
			if (tries == 0) {
				break;
			}
		}
		c.source.customName = title;
		return c;
	}
}