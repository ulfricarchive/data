package com.ulfric.data.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import com.google.common.truth.Truth;

import com.ulfric.commons.nio.FileHelper;
import com.ulfric.dragoon.ObjectFactory;
import com.ulfric.dragoon.application.Container;
import com.ulfric.veracity.suite.FileSystemTestSuite;

import java.nio.file.FileSystem;

@RunWith(JUnitPlatform.class)
class DatabaseExtensionTest extends FileSystemTestSuite {

	private ObjectFactory factory;

	@BeforeEach
	void setup() {
		factory = new ObjectFactory();
		factory.bind(FileSystem.class).toValue(fileSystem);
		factory.install(DatabaseExtension.class);
	}

	@Test
	void testReadDataFile() {
		FileHelper.createDefaultFile(fileSystem.getPath("database", "hello", "hello.json"));
		Greeting greeting = factory.request(Greeting.class);
		Truth.assertThat(greeting.hello.getData("hello").getString("hello")).isEqualTo("saying hello");
		greeting.hello.close();
	}

	@Test
	void testReadDataFileInContainer() {
		FileHelper.createDefaultFile(fileSystem.getPath("database", "greeting", "hello", "hello.json"));
		GreetingContainer greeting = factory.request(GreetingContainer.class);
		Truth.assertThat(greeting.hello.getData("hello").getString("hello")).isEqualTo("say hello");
		greeting.hello.close();
	}

	public static class GreetingContainer extends Container {
		@Database
		Store hello;
	}

	public static class Greeting {
		@Database("hello")
		Store hello;
	}

}
