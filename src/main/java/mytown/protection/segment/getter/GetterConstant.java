package mytown.protection.segment.getter;

public class GetterConstant extends Getter {

    public final Object constant;

    public GetterConstant(Object constant) {
        this.constant = constant;
    }

    public GetterConstant(String name, Object constant) {
        this(constant);
        setName(name);
    }

    @Override
    public Object invoke(Class<?> returnType, Object instance, Object... parameters) {
        return constant;
    }
}
