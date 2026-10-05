package javelin.controller.db.reader.fields;

import java.beans.PropertyVetoException;

import javelin.controller.ai.BattleAi;
import javelin.controller.db.reader.MonsterReader;
import javelin.model.unit.Monster;

/**
 * @see FieldReader
 */
public class Speed extends FieldReader{
	/**
	 * It really isn't fun watching the AI waste movement because it can gain 20
	 * squares in a single move-action. Perhaps this could be later tweaked in
	 * {@link BattleAi}.
	 */
	public static final int MAXSPEED=50;

	/** Constructor. */
	public Speed(MonsterReader monsterReader,final String fieldname){
		super(monsterReader,fieldname);
	}

	@Override
	public void read(String value) throws PropertyVetoException{
		try{
			var commentBegin=value.lastIndexOf("(");
			if(value.substring(commentBegin,value.lastIndexOf(")")).contains(","))
				value=value.substring(0,commentBegin).trim();
		}catch(final StringIndexOutOfBoundsException e){
			// doesn't have commentaries
		}
		var m=reader.monster;
		for(var type:value.split(",")){
			type=type.toLowerCase().replace(" feet.","").replace(" ft.","").trim();
			register(m,type);
		}
		if(m.fly>0) m.walk=0;
		m.walk=limit(m.walk);
		m.fly=limit(m.fly);
		m.swim=limit(m.swim);
	}

	void register(Monster m,String type){
		var or=type.indexOf(" or");
		if(or!=-1) type=type.substring(0,or).trim();
		if(type.contains("(")&&!type.contains(" ("))
			type=type.replaceAll("\\("," \\(");
		if(type.contains("climb ")){
			// TODO
		}else if(type.contains("fly ")){
			var maneuverability=type.substring(type.indexOf("(")+1,type.indexOf(")"));
			type=type.replace("fly ","").replace(" ("+maneuverability+")","").trim();
			m.fly=Integer.parseInt(type);
		}else if(type.contains("swim "))
			m.swim=Integer.parseInt(MonsterReader.clean(type).replace("swim ",""));
		else if(type.contains("burrow "))
			m.burrow=Integer.parseInt(type.replace("burrow ",""));
		else if(!type.contains("base"))
			m.walk=Integer.parseInt(MonsterReader.clean(type));
	}

	/** @see #MAXSPEED */
	static int limit(final int x){
		return x>MAXSPEED?50:x;
	}
}