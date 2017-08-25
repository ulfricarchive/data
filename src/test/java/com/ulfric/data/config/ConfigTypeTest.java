package com.ulfric.data.config;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
import com.google.gson.JsonObject;

import com.ulfric.veracity.Veracity;
import com.ulfric.veracity.suite.EnumTestSuite;

import java.nio.file.Paths;

class ConfigTypeTest extends EnumTestSuite {

	public ConfigTypeTest() {
		super(ConfigType.class);
	}

	@Test
	void testFindTypeNull() {
		Veracity.assertThat(() -> ConfigType.findType(null)).doesThrow(NullPointerException.class);
	}

	@Test
	void testFindTypeNoType() {
		Veracity.assertThat(() -> ConfigType.findType(Paths.get(""))).doesThrow(IllegalArgumentException.class);
	}

	@Test
	void testFindTypeJson() {
		Truth.assertThat(ConfigType.findType(Paths.get(".json"))).isSameAs(ConfigType.JSON);
	}

	@Test
	void testFindTypeProperties() {
		Truth.assertThat(ConfigType.findType(Paths.get(".properties"))).isSameAs(ConfigType.PROPERTIES);
	}

	@Test
	void testPropertiesExtension() {
		Truth.assertThat(ConfigType.PROPERTIES.getExtension()).isEqualTo(".properties");
	}

	@Test
	void testJsonExtension() {
		Truth.assertThat(ConfigType.JSON.getExtension()).isEqualTo(".json");
	}

	@Test
	void testLoadProperties() {
		JsonObject element = (JsonObject) ConfigType.PROPERTIES.read(Paths.get("src/test/resources/test.properties"));
		Truth.assertThat(element.get("hello").getAsString()).isEqualTo("World!");
	}

}