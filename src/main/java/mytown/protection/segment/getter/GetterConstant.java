package mytown.protection.segment.getter;

import com.google.gson.internal.LazilyParsedNumber;

public class GetterConstant extends Getter {

    public final Object constant;

    public GetterConstant(Object constant) {
        this.constant = constant;
    }

    @Override
    public Object invoke(Class<?> returnType, Object instance, Object... parameters) {
        if (returnType.equals(Integer.class)) {
            return (Object)((LazilyParsedNumber)constant).intValue();
        }
        return constant;
    }
}
