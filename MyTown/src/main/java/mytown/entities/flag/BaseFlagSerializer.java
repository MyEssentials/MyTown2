package mytown.entities.flag;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

public class BaseFlagSerializer implements ISerializer {
	private static Gson gson = new Gson();
	private List<String> types = Lists.newArrayList("java.lang.Integer", "java.lang.Double", "java.lang.Float", "java.lang.String");
	
	@Override
	public String serialize(Flag<?> flag) {
		return gson.toJson(flag.getValue());
	}

	@Override
	public Flag<?> deserialize(String name, String value, String type) {
		try {
			Class<?> typeClass = Class.forName(type);
			return new Flag<Object>(name, gson.fromJson(value, typeClass));
		} catch (Exception e) {
			e.printStackTrace(); // TODO Log error better
		}
		return null; // If serialization fails, just return null
	}

	@Override
	public void setValue(Flag<?> flag, String str) {
		Class<?> flagType = flag.getType();
		if (Integer.class.isInstance(flagType)){
			flag.setValue(flagType.cast(Integer.parseInt(str)));
		} else if (Double.class.isInstance(flagType)){
			flag.setValue(flagType.cast(Double.parseDouble(str)));
		} else if (Float.class.isInstance(flagType)){
			flag.setValue(flagType.cast(Float.parseFloat(str)));
		} else {
			flag.setValue(flagType.cast(str));
		}
	}

	@Override
	public List<String> getFlagTypes() {
		return types;
	}
}