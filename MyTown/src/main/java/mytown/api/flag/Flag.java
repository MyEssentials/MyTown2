package mytown.api.flag;

import java.util.ArrayList;
import java.util.List;

/**
 * Each {@link Flag} is given a name, type, and value.
 * Type is just an arbitrary way of determining what the value is.
 * Value is a JSON string. Its up to who retrieves the {@link Flag} to handle deserialization
 * 
 * @author Joe Goett
 */
public class Flag {
	private String name = "", type = "", value = "";
	private boolean inherited = false;
	private Flag parent = null;
	private List<Flag> children;
	
	public Flag(String name, String type, String value, Flag parent, List<Flag> children) {
		this.name = name;
		this.type = type;
		this.value = value;
		this.parent = parent;
		this.children = children;
	}
	
	public Flag(String name, String type, String value, Flag parent) {
		this(name, type, value, parent, new ArrayList<Flag>());
	}
	
	public Flag(String name, String type, String value) {
		this(name, type, value, null);
	}
	
	/**
	 * Returns the name of this {@link Flag}
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the "type" of this {@link Flag}
	 * @return
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Returns the JSON string representing the value
	 * @return
	 */
	public String getValue() {
		return inherited ? parent.getValue() : value; // TODO Make each flag store the inherited value for performance? (WeakRef them?)
	}
	
	/**
	 * Sets the value of this {@link Flag}
	 * @param value
	 */
	public void setValue(String value) {
		this.inherited = false;
		this.value = value;
	}

	/**
	 * Sets whether this {@link Flag} inherits from the parent or not
	 * @param inherited
	 */
	public void setInherit(boolean inherited) {
		this.inherited = inherited;
		if (inherited) this.value = null;
		else this.value = "";
	}
	
	/**
	 * Returns whether this {@link Flag} is inheriting from its parent
	 * @return
	 */
	public boolean isInherited() {
		return inherited;
	}
	
	/**
	 * Returns this {@link Flag}'s parent
	 * @return
	 */
	public Flag getParent() {
		return parent;
	}
	
	/**
	 * Sets this {@link Flag}'s parent
	 * @param parent
	 */
	public void setParent(Flag parent) {
		this.parent = parent;
		parent.addChild(this);
	}
	
	/**
	 * Returns the list of children
	 * @return
	 */
	public List<Flag> getChildren() {
		return children;
	}
	
	/**
	 * Adds the {@link Flag} as a child
	 * @param child
	 */
	public void addChild(Flag child) {
		children.add(child);
	}
	
	/**
	 * Removes the {@link Flag} as a child
	 * @param child
	 */
	public void removeChild(Flag child) {
		children.remove(child);
	}
	
	/**
	 * Creates a child {@link Flag} with the given name, and this {@link Flag}'s type and value
	 * 
	 * @param name
	 * @return
	 */
	public Flag createChild(String name) {
		return new Flag(name, this.type, this.value, this);
	}

	@Override
	public String toString() {
		return String.format("Flag(Name: %s, Type: %s, Value: %s, Inherited: %s)", getName(), getType(), getValue(), isInherited());
	}
}