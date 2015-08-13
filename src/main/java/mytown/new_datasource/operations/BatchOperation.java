package mytown.new_datasource.operations;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class BatchOperation extends Operation implements Iterator<Object[]> {
    private final List<Object[]> argsList;
    private int currIndex = 0;
    private final Iterator<Object[]> argsIter;

    public BatchOperation(String type, Object[]...args) {
        super(type, null);
        argsList = Arrays.asList(args);
        argsIter = argsList.iterator();
    }

    @Override
    public boolean hasNext() {
        return argsIter.hasNext();
    }

    @Override
    public Object[] next() {
        return argsIter.next();
    }

    @Override
    public Object[] getArgs() {
        return next();
    }
}
