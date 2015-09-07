package mytown.protection.segment.getter;

public abstract class Getter {

    protected String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public abstract Object invoke(Class<?> returnType, Object instance, Object... parameters);
}
