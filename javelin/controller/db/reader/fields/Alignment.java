package javelin.controller.db.reader.fields;

import java.beans.PropertyVetoException;

import javelin.controller.db.reader.MonsterReader;
import javelin.model.unit.Alignment.Morality;
import javelin.model.unit.Alignment.Ethics;
import javelin.model.unit.Monster;

/**
 * Alignment is defined in 2 axis: lawful-neutral-chaotic and good-neutral-evil.
 * Monsters of opposite alignement in either axes won't be fighting together,
 * neutral ones will fight with anyone.
 *
 * @see Monster#morals
 * @see Monster#ethics
 * @see Monster#conflict
 * @author alex
 */
public class Alignment extends FieldReader{

	public Alignment(MonsterReader reader,String fieldname){
		super(reader,fieldname);
	}

	@Override
	public void read(String value)
			throws NumberFormatException,PropertyVetoException{
		value=value.toLowerCase();
		var a=reader.monster.alignment;
		if(value.contains("good"))
			a.morals=Morality.GOOD;
		else if(value.contains("evil")) a.morals=Morality.EVIL;
		if(value.contains("lawful"))
			a.ethics=Ethics.LAWFUL;
		else if(value.contains("chaotic")) a.ethics=Ethics.CHAOTIC;
	}
}
