package javelin.controller.quality.perception;

import javelin.model.unit.Monster;

/**
 * Comprehends darkvision, keen vision and keen senses.
 *
 * @author alex
 */
public class Darkvision extends Vision{
	/** See {@link Vision#Vision(String, int)}. */
	public Darkvision(String name,int target){
		super(name,target);
	}

	@Override
	public boolean apply(String text,Monster m){
		return super.apply(text,m)||text.contains("blindsight");
	}
}
