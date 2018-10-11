package javelin.controller.db.reader.fields;

import java.beans.PropertyVetoException;

import javelin.controller.db.reader.MonsterReader;
import javelin.model.unit.Monster;

/**
 * Alignment is defined in 2 axis: lawful-neutral-chaotic and good-neutral-evil.
 * Monsters of opposite alignement in either axes won't be fighting together,
 * neutral ones will fight with anyone.
 *
 * @see Monster#good
 * @see Monster#lawful
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
		if(value.contains("good"))
			reader.monster.good=true;
		else if(value.contains("evil")) reader.monster.good=false;
		if(value.contains("lawful"))
			reader.monster.lawful=true;
		else if(value.contains("chaotic")) reader.monster.lawful=false;
	}
}
