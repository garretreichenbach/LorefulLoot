package thederpgamer.lorefulloot.lua.data.item.meta;

import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.element.meta.MetaObjectManager;
import thederpgamer.lorefulloot.lua.data.item.ItemStack;

public class MetaItem extends ItemStack {

	public MetaItem(String name) {
		super(name, 1);
	}

	public MetaObject getAsMetaObject() {
		MetaObjectManager.MetaObjectType type = MetaObjectManager.MetaObjectType.getById(getId());
		return MetaObjectManager.instantiate(type, (short) -1, true);
	}
}
