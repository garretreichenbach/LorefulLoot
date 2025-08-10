package thederpgamer.lorefulloot.lua.data.item.meta;

import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.element.meta.MetaObjectManager;

/**
 * [Description]
 *
 * @author VGoose (TheDerpGamer)
 */
public class Weapon extends MetaItem {

	private short subType;

	public Weapon(String name) {
		super("WEAPON");
		name = name.toUpperCase().replace(" ", "_");
		subType = org.schema.game.common.data.element.meta.weapon.Weapon.WeaponSubType.valueOf(name).type;
	}

	@Override
	public MetaObject getAsMetaObject() {
		MetaObject object = MetaObjectManager.instantiate(MetaObjectManager.MetaObjectType.WEAPON, subType, true);
		if(object instanceof org.schema.game.common.data.element.meta.weapon.Weapon) {
			return object;
		} else {
			throw new IllegalStateException("MetaObject is not an instance of Weapon");
		}
	}
}
