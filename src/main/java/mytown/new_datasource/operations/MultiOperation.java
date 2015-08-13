package mytown.new_datasource.operations;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class MultiOperation extends Operation implements Iterator<Operation> {
    private final List<Operation> operations;
    private int currIndex = 0;

    public MultiOperation(List<Operation> operations) {
        super(null, null);
        this.operations = operations;
    }

    public MultiOperation(Operation...operations) {
        this(Arrays.asList(operations));
    }

    @Override
    public boolean hasNext() {
        return (currIndex+1) < operations.size();
    }

    @Override
    public Operation next() {
        currIndex++;
        return operations.get(currIndex-1);
    }

    @Override
    public String getType() {
        return getCurrentOperation().getType();
    }

    @Override
    public Object[] getArgs() {
        return getCurrentOperation().getArgs();
    }

    private Operation getCurrentOperation() {
        return operations.get(currIndex);
    }
}
