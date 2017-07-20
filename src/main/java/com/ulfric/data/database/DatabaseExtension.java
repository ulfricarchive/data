package com.ulfric.data.database;

import org.apache.commons.lang3.reflect.FieldUtils;

import com.ulfric.dragoon.application.Container;
import com.ulfric.dragoon.extension.Extension;
import com.ulfric.dragoon.extension.inject.Inject;
import com.ulfric.tryto.Try;

import java.lang.reflect.Field;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DatabaseExtension extends Extension {

	private final Map<Class<?>, List<DatabaseField>> fields = new IdentityHashMap<>();

	@Inject
	private FileSystem fileSystem;

	@Override
	public <T> T transform(T value) {
		List<DatabaseField> fields = this.fields.computeIfAbsent(value.getClass(), type -> {
			return FieldUtils.getAllFieldsList(value.getClass())
					.stream()
					.filter(field -> field.isAnnotationPresent(Database.class))
					.peek(field -> field.setAccessible(true))
					.map(DatabaseField::new)
					.collect(Collectors.toList());
		});

		if (!fields.isEmpty()) {
			Path root = getRoot(Container.getOwningContainer(value));
			fields.forEach(field -> field.inject(value, root));
		}

		return value;
	}

	private Path getRoot(Container owner) {
		Path root = fileSystem.getPath(System.getProperty("default-database-location", "database"));
		if (owner != null) {
			return root.resolve(owner.getName());
		}
		return root;
	}

	private static class DatabaseField {
		private final Field field;
		private final String fileName;

		DatabaseField(Field field) {
			this.field = field;
			this.fileName = getFileName(field);
		}

		private String getFileName(Field field) {
			String store = field.getAnnotation(Database.class).value(); // TODO support stereotypes
			return store.isEmpty() ? field.getName() : store;
		}

		void inject(Object value, Path root) {
			Path location = root.resolve(fileName);
			field.setAccessible(true);
			Try.toRun(() -> field.set(value, Store.getDatabase(location)));
		}
	}

}
