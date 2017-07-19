package com.ulfric.data.config;

import org.apache.commons.lang3.reflect.FieldUtils;

import com.ulfric.dragoon.application.Container;
import com.ulfric.dragoon.exception.Try;
import com.ulfric.dragoon.extension.Extension;
import com.ulfric.dragoon.extension.inject.Inject;

import java.lang.reflect.Field;
import java.nio.file.FileSystem;
import java.nio.file.Path;

public class SettingsExtension extends Extension {

	@Inject
	private FileSystem fileSystem;

	@Override
	public <T> T transform(T value) {
		if (value.getClass().isAnnotationPresent(Configured.class)) { // TODO support stereotypes
			Path root = getRoot(Container.getOwningContainer(value));

			for (Field field : FieldUtils.getAllFieldsList(value.getClass())) {
				if (!field.isAnnotationPresent(Settings.class)) {
					continue;
				}

				Path location = root.resolve(getFileName(field));
				field.setAccessible(true);
				Try.to(() -> field.set(value, Configuration.create(location, field.getType())));
			}
		}

		return value;
	}

	private Path getRoot(Container owner) {
		Path root = fileSystem.getPath(System.getProperty("default-config-location", "containers"));
		if (owner != null) {
			return root.resolve(owner.getName());
		}
		return root;
	}

	private String getFileName(Field field) {
		Settings settings = field.getAnnotation(Settings.class); // TODO support stereotypes
		String name = settings.value();
		return (name.isEmpty() ? field.getName() : name) + ".json";
	}

}
