package javelin.controller.db.reader.factor;

import java.util.Set;

import javelin.controller.db.reader.MonsterReader;
import javelin.controller.db.reader.SpecialtiesLog;
import javelin.model.feat.Feat;

/**
 * @see FieldReader
 */
public class Feats extends FieldReader {
	private final MonsterReader monsterReader;

	public Feats(MonsterReader monsterReader, final String fieldname) {
		super(fieldname);
		this.monsterReader = monsterReader;
	}

	@Override
	public void read(final String value) {
		final Set<String> featnames = Feat.ALL.keySet();
		readfeats: for (final String entry : value.split(",")) {
			final String name = entry.trim().toLowerCase();
			if (name.isEmpty()) {
				continue;
			}
			for (final String featname : featnames) {
				if (name.startsWith(featname)) {
					final Feat f = Feat.ALL.get(featname);
					monsterReader.monster.addfeat(f);
					continue readfeats;
				}
			}
			monsterReader.debugfeats.add(name);
		}
		if (!monsterReader.monster.feats.isEmpty()) {
			SpecialtiesLog.log(
					"    Feats: " + monsterReader.monster.feats.toString());
		}
	}
}