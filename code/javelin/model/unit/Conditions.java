package javelin.model.unit;

import javelin.controller.collection.CountingSet;
import javelin.model.unit.condition.Condition;

public class Conditions extends CloneableList<Condition>{
	@Override
	public String toString(){
		if(isEmpty()) return "Conditions: none.";
		sort();
		String s="Conditions: ";
		CountingSet cs=new CountingSet();
		for(Condition c:this)
			cs.add(c.toString());
		for(String c:cs.getelements()){
			int n=cs.getcount(c);
			String amount=n==1?"":"x"+n;
			s+=c+amount+", ";
		}
		return s.substring(0,s.length()-2);
	}

	public void sort(){
		sort(null);
	}

	@Override
	public Conditions clone(){
		return (Conditions)super.clone();
	}
}
