package mytown.entities.flag;

import java.util.List;

/**
 * Handles serialization, deserialization, and setting flags values. Each serializer is associated with a set of flag types (full class names)
 * @author Joe Goett
 */
public interface ISerializer {
	/**
	 * Turns the given {@link Flag} into a JSON string
	 * @param flag
	 * @return
	 */
	public String serialize(Flag<?> flag);
	
	/**
	 * Deserialize's the given JSON string, creating a {@link Flag} with the given name and type
	 * @param name
	 * @param value
	 * @param type
	 * @return
	 */
	public Flag<?> deserialize(String name, String value, String type);
	
	/**
	 * Sets the value of the flag to the given value
	 * @param flag
	 * @param str
	 */
	public void setValue(Flag<?> flag, String value);
	
	/**
	 * Returns an array of {@link String}s that represent flag types this serializer handles
	 * @return
	 */
	public List<String> getFlagTypes();
}