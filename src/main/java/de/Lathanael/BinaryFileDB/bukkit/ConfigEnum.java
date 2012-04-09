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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.PluginDescriptionFile;

/**
 * @author Lathanael (aka Philippe Leipold)
 *
 */
public enum ConfigEnum {

	DEBUG("debug", true, "if true, a log-file will be created in the folder of the plugin");
	public final static String PREFIX = "ac_";
	private final String confVal;
	private final Object defaultVal;
	private final String description;
	private static Configuration config;
	private static String pluginVersion;
	private static String pluginName;

	/**
	 * @param confVal
	 * @param defaultVal
	 * @param description
	 */
	private ConfigEnum(final String confVal, final Object defaultVal, final String description) {
		this.confVal = confVal;
		this.defaultVal = defaultVal;
		this.description = description;
	}

	public String getString() {
		return config.getString(confVal);
	}

	public int getInt() {
		return config.getInt(confVal);
	}

	public double getDouble() {
		return config.getDouble(confVal);
	}

	public boolean getBoolean() {
		return config.getBoolean(confVal);
	}

	public long getLong() {
		return config.getLong(confVal);
	}

	public float getFloat() {
		return Float.parseFloat(config.getString(confVal));
	}

	public List<String> getStringList() {
		return config.getStringList(confVal);
	}

	public void setValue(final Object value) {
		config.set(confVal, value);
	}

	/**
	 * @return the defaultvalues
	 */
	public static Map<String, Object> getDefaultvalues() {
		final Map<String, Object> values = new LinkedHashMap<String, Object>();
		for (final ConfigEnum ce : values())
			values.put(ce.confVal, ce.defaultVal);
		return values;
	}

	public static String getHeader() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("Configuration file of ").append(pluginName).append('\n');
		buffer.append("Plugin Version: ").append(pluginVersion).append('\n').append('\n');
		for (final ConfigEnum ce : values())
			buffer.append(ce.confVal).append("\t:\t").append(ce.description).append(" (Default : ")
					.append(ce.defaultVal).append(')').append('\n');
		return buffer.toString();
	}

	/**
	 * @param config
	 *            the config to set
	 */
	public static void setConfig(final Configuration config) {
		ConfigEnum.config = config;
	}

	public static void setPluginInfos(final PluginDescriptionFile pdf) {
		ConfigEnum.pluginVersion = pdf.getVersion();
		ConfigEnum.pluginName = pdf.getName();
	}
}
