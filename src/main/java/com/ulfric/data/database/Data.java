package com.ulfric.data.database;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.ulfric.commons.json.JsonHelper;
import com.ulfric.commons.nio.FileHelper;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public final class Data {

	public static Data at(Path location) {
		Objects.requireNonNull(location, "location");

		FileHelper.createFile(location);

		return new Data(location, JsonHelper.read(location, JsonObject.class), new NeedsChange());
	}

	private final Path file;
	private final JsonObject data;
	private final transient Map<String, Data> subdata = new HashMap<>();
	private final transient NeedsChange needsChange;

	private Data(Path file, JsonObject data, NeedsChange needsChange) {
		this.file = file;
		this.data = data;
		this.needsChange = needsChange;
	}

	public Data getData(String path) {
		return subdata.computeIfAbsent(path, ignore -> {
			JsonElement element = data.get(path);
			if (element == null) {
				return null;
			}
			return new Data(null, element.getAsJsonObject(), needsChange);
		});
	}

	public String getString(String path) {
		return read(path, JsonElement::getAsString);
	}

	public Integer getInteger(String path) {
		return read(path, JsonElement::getAsInt);
	}

	public Boolean getBoolean(String path) {
		return read(path, JsonElement::getAsBoolean);
	}

	public void set(String path, Object value) {
		delete(path);
		data.add(path, JsonHelper.toJsonObject(value));
	}

	public void delete(String path) {
		Objects.requireNonNull(path, "path");

		data.remove(path);
		needsChange.needsChange = true;
	}

	private <T> T read(String path, Function<JsonElement, T> transformer) {
		Objects.requireNonNull(path, "path");

		JsonElement element = data.get(path);
		if (element == null) {
			return null;
		}
		return transformer.apply(element);
	}

	public void save() {
		if (needsChange.needsChange) {
			needsChange.needsChange = false;
			FileHelper.write(file, JsonHelper.toJson(data));
		}
	}

	private static class NeedsChange {
		boolean needsChange;
	}

}