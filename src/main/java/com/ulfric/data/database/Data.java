package com.ulfric.data.database;

import org.apache.commons.lang3.reflect.TypeUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.ulfric.commons.json.JsonHelper;
import com.ulfric.commons.nio.FileHelper;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

public final class Data {

	private static final Type STRING_LIST = TypeUtils.parameterize(List.class, String.class);
	private static final Type STRING_MAP = TypeUtils.parameterize(Map.class, String.class, String.class);

	public static Data at(Path location) {
		Objects.requireNonNull(location, "location");

		FileHelper.createFile(location);

		return new Data(location, JsonHelper.read(location, JsonElement.class).getAsJsonObject(), new NeedsChange());
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

	public UUID getUniqueId(String path) {
		return read(path, element -> JsonHelper.read(element, UUID.class));
	}

	public Integer getInteger(String path) {
		return read(path, JsonElement::getAsInt);
	}

	public boolean getBoolean(String path) {
		return readBoolean(path, JsonElement::getAsBoolean);
	}

	@SuppressWarnings("unchecked")
	public List<String> getStringList(String path) {
		return read(path, element -> JsonHelper.read(element, STRING_LIST, List.class));
	}

	@SuppressWarnings("unchecked")
	public Map<String, String> getStringMap(String path) {
		return read(path, element -> JsonHelper.read(element, STRING_MAP, Map.class));
	}

	public <T> T getAs(Class<T> type) {
		return JsonHelper.read(data, type);
	}

	public Set<String> getKeys() {
		return data.keySet();
	}

	public void set(String path, Object value) {
		delete(path);
		data.add(path, JsonHelper.toJsonObject(value));
	}

	public void setBoolean(String path, Boolean value) {
		delete(path);
		data.addProperty(path, value);
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

	private boolean readBoolean(String path, Predicate<JsonElement> transformer) {
		Objects.requireNonNull(path, "path");

		JsonElement element = data.get(path);
		if (element == null) {
			return false;
		}
		return transformer.test(element);
	}

	public void save() {
		if (needsChange.needsChange) {
			needsChange.needsChange = false;
			FileHelper.write(file, JsonHelper.toJson(data));
		}
	}

	public Path getPath() {
		return file;
	}

	private static class NeedsChange {
		boolean needsChange;
	}

}