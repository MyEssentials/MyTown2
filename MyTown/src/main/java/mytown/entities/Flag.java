package mytown.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic Flag class that should be able to serialize/deserialize any 
 * @author Joe Goett
 *
 * @param <T>
 */
public class Flag<T> {
	private T value;
	private Flag<T> parent;
	private List<Flag<T>> children;
	
	public Flag(T value, Flag<T> parent, List<Flag<T>> children) {
		this.value = value;
		this.parent = parent;
		this.children = children;
	}
	
	public Flag(T value, Flag<T> parent) {
		this(value, parent, new ArrayList<Flag<T>>());
	}
	
	public Flag(T value) {
		this(value, null);
	}
	
	public T getValue() {
		return value;
	}
	
	public void setValue(T value) {
		this.value = value;
	}
	
	public Flag<T> getParent() {
		return parent;
	}
	
	public void setParent(Flag<T> parent) {
		this.parent = parent;
	}
	
	public void addChild(Flag<T> child) {
		children.add(child);
	}
	
	public List<Flag<T>> getChildren() {
		return children;
	}
	
	public Class<?> getType() {
		return value.getClass();
	}
	
	public boolean isArray() {
		return value.getClass().isArray();
	}
}