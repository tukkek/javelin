package javelin.controller.challenge.factor.quality;

import java.util.HashSet;

import javelin.controller.challenge.factor.CrFactor;
import javelin.controller.quality.Quality;
import javelin.model.unit.Monster;

/**
 * @see CrFactor
 */
public class QualitiesFactor extends CrFactor{

	@Override
	public float calculate(Monster monster){
		float rate=0;
		HashSet<String> calculated=new HashSet<>();
		for(Quality q:Quality.qualities)
			if(calculated.add(q.getClass().getTypeName())&&q.has(monster))
				rate+=q.rate(monster);
		return rate;
	}

	@Override
	public String log(Monster m){
		String s="";
		for(Quality q:Quality.qualities)
			if(q.has(m)) s+=q.getClass().getSimpleName()+" ";
		return s.isEmpty()?s:"("+s.substring(0,s.length()-1)+")";
	}
}
