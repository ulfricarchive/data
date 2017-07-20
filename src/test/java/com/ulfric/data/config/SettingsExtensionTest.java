package com.ulfric.data.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import com.google.common.truth.Truth;

import com.ulfric.commons.value.Bean;
import com.ulfric.data.suite.PathWatcherTestSuite;
import com.ulfric.dragoon.ObjectFactory;
import com.ulfric.dragoon.application.Container;

import java.nio.file.FileSystem;

@RunWith(JUnitPlatform.class)
class SettingsExtensionTest extends PathWatcherTestSuite {

	private ObjectFactory factory;

	@BeforeEach
	void setup() {
		factory = new ObjectFactory();
		factory.bind(FileSystem.class).toValue(fileSystem);
		factory.install(SettingsExtension.class);
	}

	@Test
	void testReadContainerFile() {
		Greeting greeting = factory.request(Greeting.class);
		Truth.assertThat(greeting.hello.hello).isEqualTo("hello world");
	}

	@Test
	void testReadContainerFileInContainer() {
		GreetingContainer greeting = factory.request(GreetingContainer.class);
		Truth.assertThat(greeting.hello.hello).isEqualTo("some other hello message");
	}

	@Configured
	public static class GreetingContainer extends Container {
		@Settings
		Hello hello;
	}

	@Configured
	static class Greeting extends Bean {
		@Settings("hello")
		Hello hello;
	}

	static class Hello extends Bean {
		String hello;
	}

}
