package thederpgamer.lorefulloot.lua.data.item.meta;

import org.schema.game.common.data.element.meta.Logbook;
import org.schema.game.common.data.element.meta.MetaObject;

public class LogBook extends MetaItem {

	private String contents;

	public LogBook(String contents) {
		super("LOG_BOOK");
		this.contents = contents;
	}

	public String getContents() {
		return contents;
	}

	public void setContents(String contents) {
		this.contents = contents;
	}

	@Override
	public MetaObject getAsMetaObject() {
		MetaObject object = super.getAsMetaObject();
		if(object instanceof Logbook) {
			Logbook logbook = (Logbook) object;
			logbook.setTxt(contents);
			return logbook;
		} else {
			throw new IllegalStateException("MetaObject is not an instance of Logbook");
		}
	}
}
