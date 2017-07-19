package com.ulfric.data.database;

import com.ulfric.commons.nio.FileHelper;
import com.ulfric.data.Savable;
import com.ulfric.tryto.Try;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public final class Database implements Savable {

	private static final Map<Path, Database> DATABASES = new HashMap<>();

	public static Database getDatabase(Path directory) {
		Objects.requireNonNull(directory, "directory");

		return DATABASES.computeIfAbsent(directory, Database::createDatabase);
	}

	private static Database createDatabase(Path directory) {
		FileHelper.createDirectories(directory);

		return new Database(directory);
	}

	public static List<Database> getDatabases() {
		return new ArrayList<>(DATABASES.values());
	}

	private final Path directory;
	private final Map<String, Data> data = new HashMap<>();

	private Database(Path directory) {
		this.directory = directory;
	}

	public Stream<Data> getAllData() {
		return Try.toGetIo(() -> Files.list(directory))
				.map(Path::getFileName)
				.map(Path::toString)
				.map(path -> path.substring(0, path.length() - ".json".length()))
				.map(this::getData);
	}

	public Data getData(String name) {
		Objects.requireNonNull(name, "name");
		return data.computeIfAbsent(name, this::loadData);
	}

	private Data loadData(String name) {
		return Data.at(directory.resolve(name + ".json"));
	}

	@Override
	public void save() {
		data.values().parallelStream().forEach(Data::save);
	}

	@Override
	public void close() {
		save();
		DATABASES.remove(directory, this);
	}

}