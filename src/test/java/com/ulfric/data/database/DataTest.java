package com.ulfric.data.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import com.google.common.truth.Truth;

import com.ulfric.commons.nio.FileHelper;
import com.ulfric.veracity.suite.FileSystemTestSuite;

@RunWith(JUnitPlatform.class)
class DataTest extends FileSystemTestSuite {

	private Data data;

	@BeforeEach
	void setup() {
		file = fileSystem.getPath("datatest.json");
		FileHelper.createDefaultFile(file);
		data = Data.at(file);
	}

	@Test
	void testGetString() {
		Truth.assertThat(data.getString("string")).isEqualTo("some string");
	}

	@Test
	void testGetStringNothingPresent() {
		Truth.assertThat(data.getString("not-real-value")).isNull();
	}

	@Test
	void testGetBoolean() {
		Truth.assertThat(data.getBoolean("bool")).isTrue();
	}

	@Test
	void testGetBooleanNothingPresent() {
		Truth.assertThat(data.getBoolean("not-real-value")).isNull();
	}

	@Test
	void testGetInteger() {
		Truth.assertThat(data.getInteger("integer")).isEqualTo(5);
	}

	@Test
	void testGetIntegerNothingPresent() {
		Truth.assertThat(data.getInteger("not-real-value")).isNull();
	}

	@Test
	void testGetStringList() {
		Truth.assertThat(data.getStringList("stringlist")).containsExactly("first", "second");
	}

	@Test
	void testGetStringListNothingPresent() {
		Truth.assertThat(data.getStringList("not-real-value")).isNull();
	}

	@Test
	void testGetStringMap() {
		Truth.assertThat(data.getStringMap("stringmap")).containsEntry("key", "value");
	}

	@Test
	void testGetStringMapNothingPresent() {
		Truth.assertThat(data.getStringMap("not-real-value")).isNull();
	}

	@Test
	void testGetData() {
		Truth.assertThat(data.getData("data").getString("string")).isEqualTo("some other string");
	}

	@Test
	void testGetDataNothingPresent() {
		Truth.assertThat(data.getData("not-real-value")).isNull();
	}

	@Test
	void testSave() {
		data.set("hello", "some greeting");
		data.save();
		data.save();
		setup();
		Truth.assertThat(data.getString("hello")).isEqualTo("some greeting");
	}

}