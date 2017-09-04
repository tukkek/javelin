package javelin.controller.db.reader.fields;

import java.util.HashMap;

import javelin.controller.db.reader.MonsterReader;
import javelin.controller.db.reader.SpecialtiesLog;
import javelin.controller.upgrade.FeatUpgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.feat.Feat;

/**
 * @see FieldReader
 */
public class Feats extends FieldReader {
	static HashMap<String, Feat> FEATS = new HashMap<String, Feat>();
	static {
		for (FeatUpgrade f : UpgradeHandler.singleton.getfeats()) {
			FEATS.put(f.feat.name.toLowerCase(), f.feat);
		}
	}

	public Feats(MonsterReader reader, final String fieldname) {
		super(reader, fieldname);
	}

	@Override
	public void read(final String value) {
		for (final String entry : value.split(",")) {
			final String name = entry.trim();
			if (name.isEmpty()) {
				continue;
			}
			Feat f = getfeat(name);
			if (f == null) {
				reader.debugfeats.add(name);
			} else {
				reader.monster.addfeat(f.generate(name));
			}
		}
		if (!reader.monster.feats.isEmpty()) {
			SpecialtiesLog.log("    Feats: " + reader.monster.feats.toString());
		}
	}

	Feat getfeat(String name) {
		name = name.toLowerCase();
		Feat f = FEATS.get(name);
		if (f != null) {
			return f;
		}
		for (String feat : FEATS.keySet()) {
			if (name.startsWith(feat)) {
				return FEATS.get(feat);
			}
		}
		return null;
	}
}