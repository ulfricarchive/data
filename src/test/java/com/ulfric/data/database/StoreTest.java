package com.ulfric.data.database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import com.google.common.truth.Truth;

import com.ulfric.commons.nio.FileHelper;
import com.ulfric.veracity.suite.FileSystemTestSuite;

import java.nio.file.Path;

@RunWith(JUnitPlatform.class)
class StoreTest extends FileSystemTestSuite {

	private Path hello;
	private Store database;

	@BeforeEach
	void writeDefaultData() {
		hello = file.resolve("hello.json");
		FileHelper.createDirectories(file);
		FileHelper.write(hello, "{\"hello\":\"hi\"}");
		database = Store.getDatabase(file);
	}

	@AfterEach
	void shutdown() {
		database.close();
	}

	@Test
	void testGetAllData() {
		Truth.assertThat(database.getAllData().count()).isEqualTo(1);
	}

	@Test
	void testGetData() {
		Truth.assertThat(database.getData("hello").getString("hello")).isEqualTo("hi");
	}

	@Test
	void getDatabases() {
		Truth.assertThat(Store.getDatabases()).containsExactly(database);
	}

}