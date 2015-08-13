package mytown.new_datasource;

import mytown.new_datasource.operations.Operation;

public abstract class DatasourceBackend implements Runnable {
    protected abstract void processOperation(Operation operation);

    public abstract void init();

    @Override
    public final void run() {
        while (Datasource.get().running.get() || !Datasource.get().operationQueue.isEmpty()) {
            try {
                processOperation(getOperation());
            } catch (Throwable t) { // Catching all so we don't crash the DB thread
                t.printStackTrace();
            }
        }
    }

    private final Operation getOperation() {
        return Datasource.get().operationQueue.poll();
    }
}
