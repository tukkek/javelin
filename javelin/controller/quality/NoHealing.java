package javelin.controller.quality;

import javelin.model.unit.Monster;

/**
 * Monsters than cannot heal through hest or cure spells.
 *
 * TODO not actually implemented, update rate to -1cr.
 *
 * @author alex
 */
public class NoHealing extends Quality{
	public NoHealing(){
		super("no natural healing");
	}

	@Override
	public void add(String declaration,Monster m){
		m.heal=false;
	}

	@Override
	public boolean has(Monster m){
		return !m.heal;
	}

	@Override
	public float rate(Monster m){
		return 0;
	}
}
