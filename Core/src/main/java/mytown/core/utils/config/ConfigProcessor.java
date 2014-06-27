package mytown.core.utils.config;

import java.lang.reflect.Field;
import java.util.Map;

import mytown.core.MyTownCore;
import mytown.core.utils.Log;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

import com.google.common.collect.ImmutableMap;

public class ConfigProcessor {
	/**
	 * Maps classes to the appropriate Property.Type
	 */
	protected static Map<Class<?>, Property.Type> CONFIG_TYPES = ImmutableMap.<Class<?>, Property.Type> builder().put(Integer.class, Property.Type.INTEGER).put(int.class, Property.Type.INTEGER).put(Integer[].class, Property.Type.INTEGER).put(int[].class, Property.Type.INTEGER).put(Double.class, Property.Type.DOUBLE).put(double.class, Property.Type.DOUBLE).put(Double[].class, Property.Type.DOUBLE).put(double[].class, Property.Type.DOUBLE).put(Boolean.class, Property.Type.BOOLEAN).put(boolean.class, Property.Type.BOOLEAN).put(Boolean[].class, Property.Type.BOOLEAN).put(boolean[].class, Property.Type.BOOLEAN).put(String.class, Property.Type.STRING).put(String[].class, Property.Type.STRING).build();

	protected static Log log;

	/**
	 * Processes all static fields in c with ConfigProperty annotation and loads their value from the given config
	 * 
	 * @param config
	 * @param c
	 */
	public static void processConfig(Configuration config, Class<?> c) {
		for (Field f : c.getFields()) {
			ConfigProperty propAnnot = f.getAnnotation(ConfigProperty.class);
			if (propAnnot == null)
				return;
			String category = propAnnot.category();
			String key = (propAnnot.name().isEmpty() || propAnnot.name() == null) ? f.getName() : propAnnot.name();
			String comment = propAnnot.comment();
			ConfigProcessor.setField(f, config, category, key, comment);
		}
	}

	protected static void setField(Field f, Configuration config, String category, String key, String comment) {
		if (f == null || config == null) {
			ConfigProcessor.log.config("Field or Config was null");
			return;
		}
		Property.Type type = ConfigProcessor.CONFIG_TYPES.get(f.getType());
		if (type == null) {
			ConfigProcessor.log.config("Unknown config type for field type: %s", f.getType().getName());
			return;
		}
		try {
			Object defaultValue = f.get(null);
			switch (type) {
				case INTEGER:
					if (f.getType().isArray()) {
						f.set(null, config.get(category, key, (int[]) defaultValue, comment).getIntList());
					} else {
						f.set(null, config.get(category, key, (Integer) defaultValue, comment).getInt());
					}
					break;
				case DOUBLE:
					if (f.getType().isArray()) {
						f.set(null, config.get(category, key, (double[]) defaultValue, comment).getDoubleList());
					} else {
						f.set(null, config.get(category, key, (Double) defaultValue, comment).getDouble((Double) defaultValue));
					}
					break;
				case BOOLEAN:
					if (f.getType().isArray()) {
						f.set(null, config.get(category, key, (boolean[]) defaultValue, comment).getBooleanList());
					} else {
						f.set(null, config.get(category, key, (Boolean) defaultValue, comment).getBoolean((Boolean) defaultValue));
					}
					break;
				case STRING:
					if (f.getType().isArray()) {
						f.set(null, config.get(category, key, (String[]) defaultValue, comment).getStringList());
					} else {
						f.set(null, config.get(category, key, (String) defaultValue, comment).getString());
					}
					break;
			}
		} catch (Exception ex) {
			ConfigProcessor.log.config("An exception has occurred while processing field: %s", ex, f.getName());
		}
	}

	public static Log getLog() {
		if (ConfigProcessor.log == null) {
			ConfigProcessor.log = MyTownCore.Instance.log.createChild("ConfigProcessor");
		}
		return ConfigProcessor.log;
	}
}