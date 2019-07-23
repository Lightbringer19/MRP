package utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CustomExecutor {
    private ExecutorService executor;

    public CustomExecutor(int numberOfThreads) {
        executor = Executors.newFixedThreadPool(numberOfThreads);
    }

    public void submit(Runnable object) {
        executor.submit(object);
    }

    public void WaitUntilTheEnd() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(3 * 600, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException ex) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}
