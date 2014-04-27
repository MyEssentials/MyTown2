package mytown.core.utils.config;

import java.lang.reflect.Field;
import java.util.Map;

import mytown.core.Log;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

import com.google.common.collect.ImmutableMap;

public class ConfigProcessor {
	/**
	 * Maps classes to the appropriate Property.Type
	 */
	protected static Map<Class<?>, Property.Type> CONFIG_TYPES = ImmutableMap.<Class<?>, Property.Type> builder().put(Integer.class, Property.Type.INTEGER).put(int.class, Property.Type.INTEGER).put(Boolean.class, Property.Type.BOOLEAN).put(boolean.class, Property.Type.BOOLEAN).put(Byte.class, Property.Type.INTEGER).put(byte.class, Property.Type.INTEGER).put(Double.class, Property.Type.DOUBLE)
			.put(double.class, Property.Type.DOUBLE).put(Float.class, Property.Type.DOUBLE).put(float.class, Property.Type.DOUBLE).put(Long.class, Property.Type.INTEGER).put(long.class, Property.Type.INTEGER).put(Short.class, Property.Type.INTEGER).put(short.class, Property.Type.INTEGER).put(String.class, Property.Type.STRING).build();

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
			if (propAnnot == null) return;
			String category = propAnnot.category();
			String key = propAnnot.name().isEmpty() ? f.getName() : propAnnot.name();
			String comment = propAnnot.comment();
			String defaultValue = getFieldValue(f).toString();
			Property prop = config.get(category, key, defaultValue, comment, CONFIG_TYPES.get(f.getType()));
			setField(f, prop);
		}
	}

	protected static void setField(Field f, Property prop) {
		Object val = null;
		Object defaultValue = getFieldValue(f);
		if (CONFIG_TYPES.get(f.getType()).equals(Property.Type.BOOLEAN)) {
			val = prop.getBoolean(defaultValue == null ? false : (Boolean) defaultValue);
		} else if (CONFIG_TYPES.get(f.getType()).equals(Property.Type.INTEGER)) {
			val = prop.getInt();
		} else if (CONFIG_TYPES.get(f.getType()).equals(Property.Type.DOUBLE)) {
			val = prop.getDouble(defaultValue == null ? 0 : (Integer) defaultValue);
		} else {
			val = prop.getString();
		}
		getLog().info("Property: Name: %s, Type: %s, Value: %s", prop.getName(), prop.getType(), prop.getString());
		setField(f, val);
	}

	protected static void setField(Field f, Object value) {
		try {
			getLog().info("Setting field %s to %s", f.getName(), value);
			f.set(null, value);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected static Object getFieldValue(Field f) {
		try {
			return f.get(null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static Log getLog() {
		if (log == null) {
			log = new Log("ConfigProcessor");
		}
		return log;
	}
}