package javelin.controller.fight;

import java.awt.Image;
import java.util.ArrayList;

import javelin.Javelin;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.Incursion;
import tyrant.mikera.tyrant.QuestApp;

/**
 * @see Incursion
 * @author alex
 */
public class IncursionFight extends Fight {
	/** See {@link Javelin#settexture(Image)}. */
	public static final Image INCURSIONTEXTURE = QuestApp
			.getImage("/images/texture2.png");

	/** Incursion being fought. */
	public final Incursion incursion;

	/** Constructor. */
	public IncursionFight(final Incursion incursion) {
		this.incursion = incursion;
		texture = IncursionFight.INCURSIONTEXTURE;
		meld = true;
		hide = false;
		canflee = false;
	}

	@Override
	public Integer getel(final int teamel) {
		return incursion.getel();
	}

	@Override
	public ArrayList<Combatant> getmonsters(Integer teamel) {
		return Incursion.getsafeincursion(incursion.squad);
	}

	@Override
	public void bribe() {
		incursion.remove();
	}

	@Override
	public boolean onend() {
		super.onend();
		if (Fight.victory) {
			incursion.remove();
		} else {
			for (Combatant incursant : new ArrayList<Combatant>(
					incursion.squad)) {
				Combatant alive = null;
				for (Combatant inbattle : Fight.state.getcombatants()) {
					if (inbattle.id == incursant.id) {
						alive = inbattle;
						break;
					}
				}
				if (alive == null) {
					incursion.squad.remove(incursant);
				}
			}
		}
		return true;
	}

	@Override
	public ArrayList<Combatant> generate(Integer teamel) {
		ArrayList<Combatant> foes = super.generate(teamel);
		incursion.squad = Incursion.getsafeincursion(foes);
		return foes;
	}
}