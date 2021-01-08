package javelin.controller.db.reader.fields;

import java.util.ArrayList;

import javelin.controller.content.quality.Quality;
import javelin.controller.db.reader.MonsterReader;

/**
 * Reads the <SpecialQualites> XML tag, using several {@link Quality} instances
 * to process them.
 */
public class SpecialQualities extends FieldReader{
	/** See {@link FieldReader#FieldReader(MonsterReader, String)}. */
	public SpecialQualities(MonsterReader monsterReader,String fieldname){
		super(monsterReader,fieldname);
	}

	@Override
	public void read(String value){
		ArrayList<Quality> qualities=new ArrayList<>(Quality.qualities);
		reading:for(String quality:value.split(",")){
			quality=quality.trim().toLowerCase();
			for(Quality q:new ArrayList<>(qualities))
				if(q.apply(quality,reader.monster)){
					q.add(quality,reader.monster);
					qualities.remove(q);
					continue reading;
				}
			reader.unimplementedqualities.add(quality);
		}
	}
}
