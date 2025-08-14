package org.busybee.clearlaggenhanced.scheduler;

import org.busybee.clearlaggenhanced.ClearLaggEnhanced;
import org.busybee.clearlaggenhanced.utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.function.Consumer;

/**
 * Centralized async task manager for thread-safe operations
 */
public class AsyncTaskManager {
    
    private final ClearLaggEnhanced plugin;
    private final ExecutorService asyncExecutor;
    private final ScheduledExecutorService scheduledExecutor;
    private final AtomicInteger taskIdCounter = new AtomicInteger(0);
    
    public AsyncTaskManager(ClearLaggEnhanced plugin) {
        this.plugin = plugin;
        
        // Create thread pools
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
    
    /**
     * Run a task asynchronously
     */
    public CompletableFuture<Void> runAsync(Runnable task) {
        return CompletableFuture.runAsync(task, asyncExecutor);
    }
    
    /**
     * Run a task asynchronously with a result
     */
    public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, asyncExecutor);
    }
    
    /**
     * Run a task on the main thread
     */
    public BukkitTask runSync(Runnable task) {
        return Bukkit.getScheduler().runTask(plugin, task);
    }
    
    /**
     * Run a task on the main thread with delay
     */
    public BukkitTask runSyncLater(Runnable task, long delayTicks) {
        return Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }
    
    /**
     * Run a repeating task on the main thread
     */
    public BukkitTask runSyncRepeating(Runnable task, long delayTicks, long periodTicks) {
        return Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
    }
    
    /**
     * Schedule a task to run repeatedly in the background
     */
    public ScheduledFuture<?> scheduleRepeating(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return scheduledExecutor.scheduleAtFixedRate(task, initialDelay, period, unit);
    }
    
    /**
     * Schedule a task to run once after a delay
     */
    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        return scheduledExecutor.schedule(task, delay, unit);
    }
    
    /**
     * Schedule a task with a result
     */
    public <T> ScheduledFuture<T> schedule(Callable<T> callable, long delay, TimeUnit unit) {
        return scheduledExecutor.schedule(callable, delay, unit);
    }
    
    /**
     * Execute a task that requires switching between async and sync contexts
     */
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
    
    /**
     * Get the number of active async tasks
     */
    public int getActiveAsyncTasks() {
        if (asyncExecutor instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) asyncExecutor).getActiveCount();
        }
        return 0;
    }
    
    /**
     * Get the number of queued async tasks
     */
    public long getQueuedAsyncTasks() {
        if (asyncExecutor instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) asyncExecutor).getTaskCount() - 
                   ((ThreadPoolExecutor) asyncExecutor).getCompletedTaskCount();
        }
        return 0;
    }
    
    /**
     * Shutdown the task manager
     */
    public void shutdown() {
        Logger.info("Shutting down AsyncTaskManager...");
        
        // Shutdown executors gracefully
        asyncExecutor.shutdown();
        scheduledExecutor.shutdown();
        
        try {
            // Wait for tasks to complete
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