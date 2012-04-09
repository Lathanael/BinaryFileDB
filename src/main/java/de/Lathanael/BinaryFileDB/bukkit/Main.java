/*************************************************************************
 * Copyright (C) 2012 Philippe Leipold
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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import de.Lathanael.BinaryFileDB.bukkit.Metrics.Graph;
import de.Lathanael.BinaryFileDB.bukkit.Metrics.Graph.Type;

/**
 * @author Lathanael (aka Philippe Leipold)
 */
public class Main extends JavaPlugin {

	private FileConfiguration config;
	public static Logger log;
	public static Graph graph = null;
	public static int dbAccessCount = 0;
	public static int dbAccessLockedCount = 0;
	public static int dbCustomAccessCount = 0;
	public static int dbCustomAccessLockedCount = 0;

	public void onDisable() {
		DebugLog.stopLogging();
		log.info("Version " + this.getDescription().getVersion() + " disabled.");
	}

	public void onEnable() {
		log = getLogger();
		DebugLog.setFile(getDataFolder().getPath());
		try {
			final Metrics metrics = new Metrics();
			graph = metrics.createGraph(this, Type.Line, "Records");
			metrics.addCustomData(this, new Metrics.Plotter() {

				@Override
				public int getValue() {
					return dbAccessCount;
				}

				@Override
				public String getColumnName() {
					return "Total default DBs";
				}

			});
			metrics.addCustomData(this, new Metrics.Plotter() {

				@Override
				public int getValue() {
					return dbAccessLockedCount;
				}

				@Override
				public String getColumnName() {
					return "Total default locked DBs";
				}

			});
			metrics.addCustomData(this, new Metrics.Plotter() {

				@Override
				public int getValue() {
					return dbCustomAccessCount;
				}

				@Override
				public String getColumnName() {
					return "Total custom DBs";
				}

			});
			metrics.addCustomData(this, new Metrics.Plotter() {

				@Override
				public int getValue() {
					return dbCustomAccessLockedCount;
				}

				@Override
				public String getColumnName() {
					return "Total custom locked DBs";
				}

			});
			getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
				@Override
				public void run() {
					metrics.beginMeasuringPlugin(Main.this);
					log.info("Stats logging started, you can opt-out via the config in the PluginMetrics folder");
				}
			}, 30 * 20);
		} catch (IOException e) {
			DebugLog.INSTANCE.log(Level.SEVERE, "Stats loggin problem", e);
		}
		PluginDescriptionFile pdfFile = this.getDescription();
		config = getConfig();
		ConfigEnum.setPluginInfos(pdfFile);
		config.addDefaults(ConfigEnum.getDefaultvalues());
		config.options().header(ConfigEnum.getHeader());
		config.options().copyDefaults(true).copyHeader(true);
		ConfigEnum.setConfig(config);
		saveConfig();
		if (!ConfigEnum.DEBUG.getBoolean()) {
			DebugLog.stopLogging();
		}
		log.info("Version " + pdfFile.getVersion() + " enabled.");
	}

	public static void addToAccess() {
		dbAccessCount++;
	}

	public static void removeFromAccess() {
		dbAccessCount--;
	}

	public static void addToAccessLocked() {
		dbAccessLockedCount++;
	}

	public static void removeFromAccessLocked() {
		dbAccessLockedCount--;
	}

	public static void addToCustomAccess() {
		dbCustomAccessCount++;
	}

	public static void removeFromCustomAccess() {
		dbCustomAccessCount--;
	}

	public static void addToCustomAccessLocked() {
		dbCustomAccessLockedCount++;
	}

	public static void removeFromCustomAccessLocked() {
		dbCustomAccessLockedCount--;
	}
}
