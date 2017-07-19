package com.ulfric.data.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import com.google.common.truth.Truth;

import com.ulfric.commons.nio.FileHelper;
import com.ulfric.veracity.suite.FileSystemTestSuite;

import java.nio.file.Path;

@RunWith(JUnitPlatform.class)
class DatabaseTest extends FileSystemTestSuite {

	private Path hello;
	private Database database;

	@BeforeEach
	void writeDefaultData() {
		hello = file.resolve("hello.json");
		FileHelper.createDirectories(file);
		FileHelper.write(hello, "{\"hello\":\"hi\"}");
		database = Database.getDatabase(file);
	}

	@Test
	void testGetAllData() {
		Truth.assertThat(database.getAllData().count()).isEqualTo(1);
	}

	@Test
	void testGetData() {
		Truth.assertThat(database.getData("hello").getString("hello")).isEqualTo("hi");
	}

}