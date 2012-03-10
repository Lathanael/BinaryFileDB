/*************************************************************************
 * Copyright (C) 2012 Philippe Leipold
 *
 * This file is part of BinaryFileDB.
 *
 * BinaryFileDB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BinaryFileDB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BinaryFileDB. If not, see <http://www.gnu.org/licenses/>.
 *
 **************************************************************************/

package de.Lathanael.BinaryFileDB.bukkit;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Lathanael (aka Philippe Leipold)
 *
 */
public class Main extends JavaPlugin {

	public static Logger log;

	public void onDisable() {
		log.info("Version " + this.getDescription().getVersion() + " disabled.");
	}

	public void onEnable() {
		log = getLogger();
		log.info("Version " + this.getDescription().getVersion() + " enabled.");
	}
}
