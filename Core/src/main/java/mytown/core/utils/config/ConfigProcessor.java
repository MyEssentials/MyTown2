package mytown.core.utils.config;

import java.lang.reflect.Field;
import java.util.Map;

import mytown.core.MyTownCore;
import mytown.core.utils.Log;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import com.google.common.collect.ImmutableMap;

public class ConfigProcessor {
	/**
	 * Maps classes to the appropriate Property.Type
	 */
	private static Map<Class<?>, Property.Type> CONFIG_TYPES = ImmutableMap.<Class<?>, Property.Type> builder().put(Integer.class, Property.Type.INTEGER).put(int.class, Property.Type.INTEGER).put(Integer[].class, Property.Type.INTEGER).put(int[].class, Property.Type.INTEGER).put(Double.class, Property.Type.DOUBLE).put(double.class, Property.Type.DOUBLE).put(Double[].class, Property.Type.DOUBLE).put(double[].class, Property.Type.DOUBLE).put(Boolean.class, Property.Type.BOOLEAN).put(boolean.class, Property.Type.BOOLEAN).put(Boolean[].class, Property.Type.BOOLEAN).put(boolean[].class, Property.Type.BOOLEAN).put(String.class, Property.Type.STRING).put(String[].class, Property.Type.STRING).build();

	private static Log log;
	
	/**
	 * @see ConfigProcessor#load(Configuration, Class, Object)
	 * @param config
	 * @param c
	 */
	public static void load(Configuration config, Class<?> c) {
		load(config, c, null);
	}

	/**
	 * load all static fields in c with {@link ConfigProperty} annotation and loads their value from the given config
	 * 
	 * @param config
	 * @param c
	 */
	public static void load(Configuration config, Class<?> c, Object obj) {
		getLog().debug("Loading Class: %s", c.getName());
		for (Field f : c.getDeclaredFields()) {
			getLog().debug("- Field: %s", f.getName());
			ConfigProperty propAnnot = f.getAnnotation(ConfigProperty.class);
			if (propAnnot == null)
				return;
			String category = propAnnot.category();
			String key = (propAnnot.name().isEmpty() || propAnnot.name() == null) ? f.getName() : propAnnot.name();
			String comment = propAnnot.comment();
			ConfigProcessor.setField(f, obj, config, category, key, comment);
			config.save();
		}
	}
	
	/**
	 * @see ConfigProcessor#save(Configuration, Class, Object)
	 * @param config
	 * @param c
	 */
	public static void save(Configuration config, Class<?> c) {
		save(config, c, null);
	}
	
	/**
	 * Saves all static fields in c with {@link ConfigProcessor} annotation to the config
	 * 
	 * @param config
	 * @param c
	 */
	public static void save(Configuration config, Class<?> c, Object obj) {
        getLog().debug("Saving Class: %s", c.getName());
		for (Field f : c.getFields()) {
            getLog().debug("- Field: %s", f.getName());
			ConfigProperty propAnnot = f.getAnnotation(ConfigProperty.class);
			if (propAnnot == null)
				return;
			String category = propAnnot.category();
			String key = (propAnnot.name().isEmpty() || propAnnot.name() == null) ? f.getName() : propAnnot.name();
			String comment = propAnnot.comment();
			ConfigProcessor.setConfig(f, obj, config, category, key, comment);
			config.save();
		}
	}

	private static void setField(Field f, Object obj, Configuration config, String category, String key, String comment) {
		if (f == null || config == null) {
			ConfigProcessor.getLog().warn("Field or Config was null");
			return;
		}
		Property.Type type = ConfigProcessor.CONFIG_TYPES.get(f.getType());
		if (type == null) {
			ConfigProcessor.getLog().warn("Unknown config type for field type: %s", f.getType().getName());
			return;
		}
		try {
			f.setAccessible(true);
			Object defaultValue = f.get(obj);
            if (defaultValue != null && comment != null && !comment.isEmpty()) { // Display the default value in the comment! :D
                //comment += "\nDefault: " + defaultValue.toString();
            }
			switch (type) {
				case INTEGER:
					if (f.getType().isArray()) {
						f.set(obj, config.get(category, key, (int[]) defaultValue, comment).getIntList());
					} else {
						f.set(obj, config.get(category, key, (Integer) defaultValue, comment).getInt());
					}
					break;
				case DOUBLE:
					if (f.getType().isArray()) {
						f.set(obj, config.get(category, key, (double[]) defaultValue, comment).getDoubleList());
					} else {
						f.set(obj, config.get(category, key, (Double) defaultValue, comment).getDouble((Double) defaultValue));
					}
					break;
				case BOOLEAN:
					if (f.getType().isArray()) {
						f.set(obj, config.get(category, key, (boolean[]) defaultValue, comment).getBooleanList());
					} else {
						f.set(obj, config.get(category, key, (Boolean) defaultValue, comment).getBoolean((Boolean) defaultValue));
					}
					break;
				case STRING:
					if (f.getType().isArray()) {
						f.set(obj, config.get(category, key, (String[]) defaultValue, comment).getStringList());
					} else {
						f.set(obj, config.get(category, key, (String) defaultValue, comment).getString());
					}
					break;
				default:
					log.warn("Unknown type %s", type);
			}
		} catch (Exception ex) {
			ConfigProcessor.getLog().warn("An exception has occurred while loading field: %s", ex, f.getName());
		}
	}
	
	private static void setConfig(Field f, Object obj, Configuration config, String category, String key, String comment) {
		if (f == null || config == null) {
			ConfigProcessor.getLog().warn("Field or Config was null");
			return;
		}
		Property.Type type = ConfigProcessor.CONFIG_TYPES.get(f.getType());
		if (type == null) {
			ConfigProcessor.getLog().warn("Unknown config type for field type: %s", f.getType().getName());
			return;
		}
		try {
			f.setAccessible(true);
			Object val = f.get(obj);
			switch (type) {
				case INTEGER:
					if (f.getType().isArray()) {
						config.get(category, key, (int[]) val, comment);
					} else {
						config.get(category, key, (Integer) val, comment);
					}
					break;
				case DOUBLE:
					if (f.getType().isArray()) {
						config.get(category, key, (double[]) val, comment);
					} else {
						config.get(category, key, (Double) val, comment);
					}
					break;
				case BOOLEAN:
					if (f.getType().isArray()) {
						config.get(category, key, (boolean[]) val, comment);
					} else {
						config.get(category, key, (Boolean) val, comment);
					}
					break;
				case STRING:
					if (f.getType().isArray()) {
						config.get(category, key, (String[]) val, comment);
					} else {
						config.get(category, key, (String) val, comment);
					}
					break;
				default:
					log.warn("Unknown type %s", type);
			}
		} catch (Exception ex) {
			ConfigProcessor.getLog().warn("An exception has occurred while processing field: %s", ex, f.getName());
		}
	}

	private static Log getLog() {
		if (ConfigProcessor.log == null) {
			ConfigProcessor.log = MyTownCore.Instance.log.createChild("ConfigProcessor");
		}
		return ConfigProcessor.log;
	}
}