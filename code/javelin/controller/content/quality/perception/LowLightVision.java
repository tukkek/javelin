package javelin.controller.content.quality.perception;

import javelin.model.unit.Monster;

/**
 * Comprehends low-light vision and blindsight.
 *
 * @author alex
 */
public class LowLightVision extends Vision{
	/** See {@link Vision#Vision(String, int)}. */
	public LowLightVision(String name,int target){
		super(name,target);
	}

	@Override
	public boolean apply(String text,Monster m){
		return super.apply(text,m)||text.contains("keen vision")
				||text.contains("keen senses");
	}
}
