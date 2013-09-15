package javelin.controller.db.reader;

import java.util.Set;

import javelin.model.feat.Feat;

class Feats extends FieldReader {
	/**
	 * 
	 */
	private final MonsterReader monsterReader;

	Feats(MonsterReader monsterReader, final String fieldname) {
		super(fieldname);
		this.monsterReader = monsterReader;
	}

	@Override
	void read(final String value) {
		final Set<String> featnames = Feat.all.keySet();
		readfeats: for (final String entry : value.split(",")) {
			final String name = entry.trim().toLowerCase();
			if (name.isEmpty()) {
				continue;
			}
			for (final String featname : featnames) {
				if (name.startsWith(featname)) {
					this.monsterReader.monster.addfeat(Feat.all.get(featname));
					continue readfeats;
				}
			}
			this.monsterReader.debugfeats.add(name);
		}
	}
}