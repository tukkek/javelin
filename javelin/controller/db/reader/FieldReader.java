package javelin.controller.db.reader;

import java.beans.PropertyVetoException;

public abstract class FieldReader {
	String fieldname = "";
	protected MonsterReader reader;

	abstract void read(String value) throws NumberFormatException,
			PropertyVetoException;

	/**
	 * @deprecated TODO
	 */
	@Deprecated
	public FieldReader(final String fieldname) {
		super();
		this.fieldname = fieldname;
	}

	public FieldReader(MonsterReader reader, String fieldname) {
		this(fieldname);
		this.reader = reader;
	}
}