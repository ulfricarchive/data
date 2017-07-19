package com.ulfric.data.database;

import org.apache.commons.lang3.reflect.FieldUtils;

import com.ulfric.dragoon.application.Container;
import com.ulfric.dragoon.exception.Try;
import com.ulfric.dragoon.extension.Extension;
import com.ulfric.dragoon.extension.inject.Inject;

import java.lang.reflect.Field;
import java.nio.file.FileSystem;
import java.nio.file.Path;

public class DatabaseExtension extends Extension {

	@Inject
	private FileSystem fileSystem;

	@Override
	public <T> T transform(T value) {
		if (value.getClass().isAnnotationPresent(Store.class)) { // TODO support stereotypes
			Path root = getRoot(Container.getOwningContainer(value));

			for (Field field : FieldUtils.getAllFieldsList(value.getClass())) {
				if (!field.isAnnotationPresent(Store.class)) {
					continue;
				}

				Path location = root.resolve(getFileName(field));
				field.setAccessible(true);
				Try.to(() -> field.set(value, Database.getDatabase(location)));
			}
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

	private String getFileName(Field field) {
		String store = field.getAnnotation(Store.class).value(); // TODO support stereotypes
		return store.isEmpty() ? field.getName() : store;
	}

}
