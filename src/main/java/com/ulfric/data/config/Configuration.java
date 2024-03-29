package com.ulfric.data.config;

import net.bytebuddy.description.modifier.FieldPersistence;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.matcher.ElementMatchers;

import com.google.gson.JsonElement;

import com.ulfric.commons.collection.MapHelper;
import com.ulfric.commons.json.JsonHelper;
import com.ulfric.commons.nio.FileHelper;
import com.ulfric.commons.nio.PathWatcher;
import com.ulfric.commons.reflect.MethodHelper;
import com.ulfric.commons.runtime.ShutdownHook;
import com.ulfric.commons.runtime.ShutdownHookHelper;
import com.ulfric.data.Savable;
import com.ulfric.dragoon.reflect.Classes;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

public final class Configuration implements Savable {

	private static final ConcurrentMap<Path, Configuration> CONFIGS = MapHelper.newConcurrentMap(2);
	private static final Map<Class<?>, Class<? extends Change>> TRANSFORMED_CLASSES =
			Collections.synchronizedMap(new WeakHashMap<>());

	private static final ShutdownHook SHUTDOWN_HOOK = registerShutdownHook();
	private static final Method SET_CHANGED =
			MethodHelper.getDeclaredMethod(Change.class, "setChanged", boolean.class)
				.orElseThrow(NullPointerException::new);

	private static ShutdownHook registerShutdownHook() {
		return ShutdownHookHelper.registerShutdownHook(() ->
			Configuration.getActiveConfigs().forEach(Configuration::close), "config-shutdown-hook");
	}

	public static ShutdownHook getShutdownHook() {
		return SHUTDOWN_HOOK;
	}

	public static List<Configuration> getActiveConfigs() {
		return Collections.unmodifiableList(new ArrayList<>(CONFIGS.values()));
	}

	public static <T> T create(Path file, Class<T> type) {
		Objects.requireNonNull(type, "type");

		return create(file).subscribe(type);
	}

	public static Configuration create(Path file) {
		Objects.requireNonNull(file, "file");

		return CONFIGS.computeIfAbsent(file, Configuration::newConfig);
	}

	private static Configuration newConfig(Path file) {
		FileHelper.createDefaultFile(file);

		return new Configuration(file, ConfigType.findType(file));
	}

	private final PathWatcher watcher;
	private final Path file;
	private final ConfigType type;
	private final List<Consumer<JsonElement>> subscriptions = new ArrayList<>();
	private final ConcurrentMap<Class<?>, Change> beans = MapHelper.newConcurrentMap(1);
	private final Runnable callback = this::update;

	private Configuration(Path file, ConfigType type) {
		this.file = file;
		this.type = type;

		watcher = PathWatcher.watch(file);
		watcher.callback(callback);
	}

	public <T> T subscribe(Class<T> type) {
		Objects.requireNonNull(type, "type");

		Object bean = beans.computeIfAbsent(type, this::createBean);
		return type.cast(bean);
	}

	public void subscribe(Runnable subscription) {
		Objects.requireNonNull(subscription, "subscription");

		subscribe(ignore -> subscription.run());
	}

	public void subscribe(Consumer<JsonElement> subscription) {
		Objects.requireNonNull(subscription, "subscription");

		this.subscriptions.add(subscription);
	}

	private Change createBean(Class<?> type) {
		Change bean = readAs(transform(type));
		subscribe(json -> JsonHelper.override(json, bean));
		return bean;
	}

	private <T> T readAs(Class<T> type) {
		return JsonHelper.read(file, type);
	}

	private Class<? extends Change> transform(Class<?> type) {
		return TRANSFORMED_CLASSES.computeIfAbsent(type, this::performTransformation);
	}

	@SuppressWarnings("unchecked")
	private Class<? extends Change> performTransformation(Class<?> type) {
		return (Class<? extends Change>) Classes.extend(type)
				.implement(Change.class)
				.defineField("change$changed", boolean.class, FieldPersistence.TRANSIENT)
				.method(ElementMatchers.isDeclaredBy(Change.class))
					.intercept(FieldAccessor.ofField("change$changed"))
				.method(ElementMatchers.isSetter().and(ElementMatchers.not(ElementMatchers.isDeclaredBy(Change.class))))
					.intercept(SuperMethodCall.INSTANCE.andThen(MethodCall.invoke(SET_CHANGED).with(true)))
				.make()
				.load(type.getClassLoader())
				.getLoaded();
	}

	private void update() {
		JsonElement json = type.read(file);
		subscriptions.forEach(consumer -> consumer.accept(json));
	}

	@Override
	public void close() {
		if (CONFIGS.remove(this.file, this)) {
			save();
			watcher.removeCallback(callback);
			watcher.closeIfInactive();
		} else {
			throw new IllegalStateException("Config not open");
		}
	}

	@Override
	public void save() {
		watcher.pause();
		for (Change bean : beans.values()) {
			if (!bean.getChanged()) {
				continue;
			}

			String json = JsonHelper.toJson(bean);
			FileHelper.write(file, json);
			bean.setChanged(false);
		}
		watcher.resume();
	}

	public static interface Change {
		boolean getChanged();

		void setChanged(boolean changed);
	}

}