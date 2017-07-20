package com.ulfric.data.config;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import com.google.common.truth.Truth;

import com.ulfric.commons.concurrent.ThreadHelper;
import com.ulfric.commons.value.Bean;
import com.ulfric.data.suite.PathWatcherTestSuite;
import com.ulfric.veracity.Veracity;

import java.time.Duration;

@RunWith(JUnitPlatform.class)
class ConfigurationTest extends PathWatcherTestSuite {

	@BeforeAll
	static void disableShutdownHook() {
		Configuration.getShutdownHook().unregister();
	}

	@AfterAll
	static void reenableShutdownHook() {
		Configuration.getShutdownHook().register();
	}

	@AfterEach
	void teardownConfigs() {
		Configuration.getActiveConfigs().forEach(Configuration::close);
	}

	@Test
	void testDefaultConfig() {
		Hello hello = hello();
		Truth.assertThat(hello.getHello()).isEqualTo("default hello message");
	}

	@Test
	void testDoubleClose() {
		hello();
		Configuration config = Configuration.getActiveConfigs().iterator().next();
		config.close();
		Veracity.assertThat(config::close).doesThrow(IllegalStateException.class);
	}

	@Test
	void testBeanChangesAreWritten() {
		Hello original = hello();
		original.setHello("something else");
		Configuration.getActiveConfigs().forEach(Configuration::close);
		Hello roundtrip = hello();
		Truth.assertThat(original).isNotSameAs(roundtrip);
		Truth.assertThat(original).isEqualTo(roundtrip);
	}

	@Test
	void testFileChangesAreObserved() {
		Hello hello = hello();

		Hello newValue = new Hello();
		newValue.setHello("new value");

		notifyWatcher(fileSystem.getPath("hello.json"), newValue.toJson());
		ThreadHelper.sleep(Duration.ofMillis(10));

		Truth.assertThat(hello).isEqualTo(newValue);
	}

	private Hello hello() {
		return Configuration.create(fileSystem.getPath("hello.json"), Hello.class);
	}

	static class Hello extends Bean {
		private String hello;

		public String getHello() {
			return hello;
		}

		public void setHello(String hello) {
			this.hello = hello;
		}
	}

}