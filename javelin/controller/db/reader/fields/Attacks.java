package javelin.controller.db.reader.fields;

import java.util.ArrayList;
import java.util.HashSet;

import javelin.controller.db.reader.MonsterReader;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.AttackSequence;

/**
 * @see FieldReader
 */
public class Attacks extends FieldReader{

	public Attacks(MonsterReader reader,final String fieldname){
		super(reader,fieldname);
	}

	@Override
	public void read(String value){
		value=value.toLowerCase();
		for(final String attackSequence:value.split(";")){
			final AttackSequence sequence=check(value,attackSequence);
			if(sequence==null) return;
			for(final String attack:attackSequence.split(","))
				try{
					parseattack(attack,sequence);
				}catch(final StringIndexOutOfBoundsException e){
					reader.errorhandler.setInvalid("incorrect attack syntax: "+attack);
					return;
				}
		}
	}

	void parseattack(final String attackp,final ArrayList<Attack> list){
		String attack=cleanor(attackp.replace("ranged","").replace("melee",""));
		int sign=attack.indexOf('+');
		if(sign==-1) sign=attack.indexOf('-');
		String modifiers=attack.substring(sign);
		attack=attack.substring(0,attack.length()-modifiers.length());
		boolean touch=modifiers.contains("touch");
		if(touch) modifiers=modifiers.replace("touch","").trim();
		attack+=modifiers;
		final char numberOfAtks=attack.charAt(0);
		final int nOfAtks;
		if(Character.isDigit(numberOfAtks)){
			nOfAtks=Integer.parseInt(Character.toString(numberOfAtks));
			attack=attack.substring(2).trim();
		}else
			nOfAtks=1;
		final String name=attack.substring(0,attack.lastIndexOf(" "));
		checkprevious(list,name);
		for(final int bonus:parsebonuses(
				attack.substring(attack.lastIndexOf(" ")+1)))
			for(int i=1;i<=nOfAtks;i++)
				list.add(new Attack(name,bonus,touch));
	}

	/**
	 * Needs to avoid replacing words like sword into swd.
	 *
	 * @return The given {@link String} without "or".
	 */
	static public String cleanor(String s){
		return s.replace(" or","").replace("or ","").replaceAll("  "," ").trim();
	}

	static Integer[] parsebonuses(final String bonusesP){
		final String[] split=bonusesP.replaceAll(" ","").replaceAll("\\+","")
				.replace(",",";").split("/");
		Integer[] bonuses=new Integer[split.length];
		for(int i=0;i<split.length;i++)
			bonuses[i]=Integer.parseInt(split[i].replace("+"," ").trim());

		return bonuses;
	}

	private void checkprevious(final ArrayList<Attack> list,final String name){
		final HashSet<String> previousNamesSet=new HashSet<>();
		for(final Attack a:list)
			previousNamesSet.add(a.name);
		for(final String previousName:previousNamesSet)
			if(name.equalsIgnoreCase(previousName)
					||name.equalsIgnoreCase(previousName+"s")
					||(name+"s").equalsIgnoreCase(previousName))
				throw new RuntimeException("adding attack '"+name+"': Monster '"
						+reader.monster+"' already has similar-named attack ('"+previousName
						+"'), this could confuse damage at this point. Please change the name.");
	}

	AttackSequence check(final String value,final String attackSequence){
		final boolean ismelee=attackSequence.contains("melee");
		final boolean isranged=attackSequence.contains("ranged");
		if(ismelee&&isranged){
			reader.errorhandler
					.setInvalid("Cannot have both types of attack in a sequence!");
			return null;
		}
		if(value.contains("repeating")){
			reader.errorhandler.setInvalid("repeating");
			return null;
		}
		final AttackSequence list=new AttackSequence();
		(ismelee?reader.monster.melee:reader.monster.ranged).add(list);
		return list;
	}
}