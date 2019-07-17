package javelin.controller.db.reader.fields;

import java.beans.PropertyVetoException;

import javelin.controller.db.reader.MonsterReader;

/**
 * @see FieldReader
 */
public class Name extends FieldReader{
	public Name(MonsterReader reader,final String fieldname){
		super(reader,fieldname);
	}

	@Override
	public void read(final String value) throws PropertyVetoException{
		var typeSeparator=value.indexOf(":");
		int reverseSeparator;
		String name;
		String type;
		if(typeSeparator==-1){
			name=value;
			type="";
		}else{
			name=value.substring(typeSeparator+1);
			type=value.substring(0,typeSeparator).toLowerCase();
		}
		reverseSeparator=name.indexOf(",");
		if(reverseSeparator>0) name=name.substring(reverseSeparator+2)+" "
				+name.substring(0,reverseSeparator);
		name=name.substring(0,1).toUpperCase()+name.substring(1).toLowerCase();
		reader.monster.name=name;
		reader.monster.group=type;
	}
}