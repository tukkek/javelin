package javelin.controller.db.reader.fields;

import java.util.HashMap;
import java.util.HashSet;

import javelin.controller.challenge.factor.FeatsFactor;
import javelin.controller.db.reader.MonsterReader;
import javelin.controller.kit.Kit;
import javelin.controller.upgrade.FeatUpgrade;
import javelin.model.unit.feat.Feat;

/**
 * @see FieldReader
 */
public class Feats extends FieldReader{
	HashMap<String,Feat> FEATS=new HashMap<>();

	/** Constructor. */
	public Feats(MonsterReader reader,final String fieldname){
		super(reader,fieldname);
		var feats=new HashSet<>(FeatsFactor.INTERNAL);
		for(var k:Kit.KITS)
			for(var f:k.filter(FeatUpgrade.class))
				feats.add(f.feat);
		for(var f:feats)
			FEATS.put(f.name.toLowerCase(),f);
	}

	@Override
	public void read(final String value){
		for(final String entry:value.split(",")){
			final String name=entry.trim();
			if(name.isEmpty()) continue;
			Feat f=getfeat(name);
			if(f==null)
				reader.debugfeats.add(name);
			else{
				f=f.generate(name);
				reader.monster.addfeat(f);
				f.read(reader.monster);
			}
		}
	}

	Feat getfeat(String name){
		name=name.toLowerCase();
		Feat f=FEATS.get(name);
		if(f!=null) return f;
		for(String feat:FEATS.keySet())
			if(name.startsWith(feat)) return FEATS.get(feat);
		return null;
	}
}