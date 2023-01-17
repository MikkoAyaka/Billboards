package de.blablubbabc.billboards;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;

public class SignEdit {

	public final BillboardSign billboard;
	public final Location location;

	public SignEdit(BillboardSign billboard, Location location) {
		Validate.notNull(billboard, "Billboard is null!");
		Validate.notNull(location, "Source location is null!");
		this.billboard = billboard;
		this.location = location;
	}
}
