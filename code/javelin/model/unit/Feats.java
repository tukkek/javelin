package javelin.model.unit;

import javelin.model.unit.feat.Feat;

public class Feats extends CloneableList<Feat>{
	@Override
	public Feats clone(){
		return (Feats)super.clone();
	}

	public int count(){
		int count=0;
		for(Feat f:this)
			count+=f.count();
		return count;
	}
}
