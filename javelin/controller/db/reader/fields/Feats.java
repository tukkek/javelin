package javelin.controller.db.reader.fields;

import java.util.Set;

import javelin.controller.db.reader.MonsterReader;
import javelin.controller.db.reader.SpecialtiesLog;
import javelin.model.feat.Feat;

/**
 * @see FieldReader
 */
public class Feats extends FieldReader {

	public Feats(MonsterReader reader, final String fieldname) {
		super(reader, fieldname);
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
					reader.monster.addfeat(f);
					continue readfeats;
				}
			}
			reader.debugfeats.add(name);
		}
		if (!reader.monster.feats.isEmpty()) {
			SpecialtiesLog.log("    Feats: " + reader.monster.feats.toString());
		}
	}
}