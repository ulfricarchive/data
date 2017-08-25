package com.ulfric.data.config;

import com.google.gson.JsonElement;

import com.ulfric.commons.json.JsonHelper;
import com.ulfric.commons.properties.PropertiesHelper;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

public enum ConfigType {

	JSON {
		@Override
		public JsonElement read(Path file) {
			return JsonHelper.read(file, JsonElement.class);
		}
	},
	PROPERTIES {
		@Override
		public JsonElement read(Path file) {
			Properties properties = PropertiesHelper.loadProperties(file);
			return JsonHelper.toJsonObject(properties);
		}
	};

	public static ConfigType findType(Path file) {
		Objects.requireNonNull(file, "file");

		String name = file.toString();
		for (ConfigType type : values()) {
			if (name.endsWith(type.extension)) {
				return type;
			}
		}

		throw new IllegalArgumentException("No file type for " + name);
	}

	private final String extension = '.' + name().toLowerCase();

	public final String getExtension() {
		return extension;
	}

	public abstract JsonElement read(Path file);

}