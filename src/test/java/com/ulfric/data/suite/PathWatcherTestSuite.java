package com.ulfric.data.suite;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.ulfric.commons.concurrent.ThreadHelper;
import com.ulfric.commons.nio.FileHelper;
import com.ulfric.commons.nio.PathWatcher;
import com.ulfric.commons.reflect.FieldHelper;
import com.ulfric.veracity.suite.FileSystemTestSuite;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.time.Duration;

public abstract class PathWatcherTestSuite extends FileSystemTestSuite { // TODO copied directly from Commons, refactor projects to remove duplicate

	private Field threadField = FieldHelper.getDeclaredField(PathWatcher.class, "THREAD").orElse(null);
	private Thread originalThread;

	private Field tickField = FieldHelper.getDeclaredField(PathWatcher.class, "ROUTINE_UPDATE_DELAY").orElse(null);
	private Duration originalTick;

	@BeforeEach
	final void tickSetup() throws Exception {
		makeMutable(tickField);

		originalTick = (Duration) tickField.get(null);
		tickField.set(null, Duration.ofMillis(1));
	}

	@AfterEach
	final void tickTeardown() throws Exception {
		tickField.set(null, originalTick);
	}

	@BeforeEach
	final void threadSetup() throws Exception {
		makeMutable(threadField);

		originalThread = (Thread) threadField.get(null);
		mockThread();
	}

	@AfterEach
	final void threadTeardown() throws Exception {
		threadField.set(null, originalThread);
	}

	private void makeMutable(Field field) throws Exception {
		field.setAccessible(true);
		Field modifiers = Field.class.getDeclaredField("modifiers");
		modifiers.setAccessible(true);
		modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
	}

	protected final void mockThread() throws Exception {
		threadField.set(null, ThreadHelper.start(originalThread::run, "mock-tick-task"));
	}

	protected final void notifyWatcher(Path file, String text) {
		pause();
		FileHelper.write(file, text);
		pause();
	}

	protected final void pause() {
		ThreadHelper.sleep(Duration.ofMillis(2));
	}

}