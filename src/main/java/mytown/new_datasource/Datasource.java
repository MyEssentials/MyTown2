package mytown.new_datasource;

import com.google.common.collect.Queues;
import mytown.MyTown;
import mytown.new_datasource.operations.Operation;
import mytown.new_datasource.sql.SQLBackend;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Datasource {
    protected final ConcurrentLinkedQueue<Operation> operationQueue;
    protected final AtomicBoolean running;
    private final ExecutorService executorService;

    private Datasource() {
        this.operationQueue = Queues.newConcurrentLinkedQueue();
        this.running = new AtomicBoolean(true);
        this.executorService = Executors.newFixedThreadPool(1); // TODO Allow configuring this?
    }

    public void start() {
        new SQLBackend().init(); // Init the backend (Schema Updates, migrations, pre-filling, etc)

        this.executorService.execute(new SQLBackend());
    }

    public void stop() {
        running.set(false);
        getLogger().info("Stopping Datasource...");
        getLogger().warn("This may take some time! Do NOT force stop!");
        getLogger().info("{} operations remaining.", operationQueue.size());
        try {
            this.executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
        }
        getLogger().info("Datasource Stopped.");
    }

    private Logger getLogger() {
        return MyTown.instance.LOG;
    }

    private static Datasource INSTANCE;

    public static synchronized Datasource get() {
        if (INSTANCE == null) INSTANCE = new Datasource();
        return INSTANCE;
    }
}
