package javelin.controller.db.reader;

import javelin.controller.CountingSet;

public class ErrorHandler {
	private String invalid;
	final CountingSet treeError = new CountingSet();

	public String getInvalid() {
		return invalid;
	}

	public void setInvalid(final String invalid) {
		this.invalid = invalid;
		if (invalid != null) {
			treeError.add(invalid);
		}
	}

	void informInvalid(final MonsterReader monsterReader) {
		MonsterReader.log("Couldn't load monster '" + monsterReader.monster
				+ "', " + invalid);
	}
}