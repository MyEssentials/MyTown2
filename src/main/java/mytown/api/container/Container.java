package mytown.api.container;

public class Container<T> {

    private T object;

    public boolean exists() {
        return object != null;
    }

    public void set(T object) {
        this.object = object;
    }

    public void remove() {
        this.object = null;
    }

    public T get() {
        return object;
    }

}
