package mytown.new_datasource.operations;

public class Operation {
    private final String type;
    private final Object[] args;

    public Operation(String type, Object...args) {
        this.type = type;
        this.args = args;
    }

    public String getType() {
        return type;
    }

    public Object[] getArgs() {
        return args;
    }
}
