package javelin.model.world.location.dungeon.branch.temple;

import javelin.controller.content.template.KitTemplate;
import javelin.model.world.location.dungeon.branch.Branch;

public class TempleBranch extends Branch{
	public TempleBranch(String prefix,String suffix,String floor,String wall){
		super(prefix,suffix,floor,wall);
		templates.add(KitTemplate.SINGLETON);
	}
}
