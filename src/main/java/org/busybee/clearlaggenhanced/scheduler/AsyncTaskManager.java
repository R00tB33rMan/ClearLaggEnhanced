package org.busybee.clearlaggenhanced.scheduler;

import org.busybee.clearlaggenhanced.ClearLaggEnhanced;
import org.busybee.clearlaggenhanced.utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.function.Consumer;

public class AsyncTaskManager {
    
    private final ClearLaggEnhanced plugin;
    private final ExecutorService asyncExecutor;
    private final ScheduledExecutorService scheduledExecutor;
    private final AtomicInteger taskIdCounter = new AtomicInteger(0);
    
    public AsyncTaskManager(ClearLaggEnhanced plugin) {
        this.plugin = plugin;

        int asyncThreads = plugin.getConfigManager() != null ? 
            plugin.getConfigManager().getMainConfig().getPerformance().getAsyncThreads() : 2;
            
        this.asyncExecutor = Executors.newFixedThreadPool(asyncThreads, r -> {
            Thread thread = new Thread(r, "ClearLaggEnhanced-Async-" + taskIdCounter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
        
        this.scheduledExecutor = Executors.newScheduledThreadPool(2, r -> {
            Thread thread = new Thread(r, "ClearLaggEnhanced-Scheduled-" + taskIdCounter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
        
        Logger.info("AsyncTaskManager initialized with " + asyncThreads + " async threads");
    }

    public CompletableFuture<Void> runAsync(Runnable task) {
        return CompletableFuture.runAsync(task, asyncExecutor);
    }

    public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, asyncExecutor);
    }

    public BukkitTask runSync(Runnable task) {
        return Bukkit.getScheduler().runTask(plugin, task);
    }

    public BukkitTask runSyncLater(Runnable task, long delayTicks) {
        return Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }

    public BukkitTask runSyncRepeating(Runnable task, long delayTicks, long periodTicks) {
        return Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
    }

    public ScheduledFuture<?> scheduleRepeating(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return scheduledExecutor.scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        return scheduledExecutor.schedule(task, delay, unit);
    }

    public <T> ScheduledFuture<T> schedule(Callable<T> callable, long delay, TimeUnit unit) {
        return scheduledExecutor.schedule(callable, delay, unit);
    }

    public CompletableFuture<Void> runAsyncThenSync(Supplier<Object> asyncTask, Consumer<Object> syncTask) {
        return supplyAsync(asyncTask).thenCompose(result -> {
            CompletableFuture<Void> future = new CompletableFuture<>();
            runSync(() -> {
                try {
                    syncTask.accept(result);
                    future.complete(null);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            });
            return future;
        });
    }

    public int getActiveAsyncTasks() {
        if (asyncExecutor instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) asyncExecutor).getActiveCount();
        }
        return 0;
    }

    public long getQueuedAsyncTasks() {
        if (asyncExecutor instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) asyncExecutor).getTaskCount() - 
                   ((ThreadPoolExecutor) asyncExecutor).getCompletedTaskCount();
        }
        return 0;
    }

    public void shutdown() {
        Logger.info("Shutting down AsyncTaskManager...");

        asyncExecutor.shutdown();
        scheduledExecutor.shutdown();
        
        try {
            if (!asyncExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                Logger.warning("Async tasks did not terminate in time, forcing shutdown");
                asyncExecutor.shutdownNow();
            }
            
            if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                Logger.warning("Scheduled tasks did not terminate in time, forcing shutdown");
                scheduledExecutor.shutdownNow();
            }
            
            Logger.info("AsyncTaskManager shutdown complete");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.warning("Interrupted during shutdown");
        }
    }
}
