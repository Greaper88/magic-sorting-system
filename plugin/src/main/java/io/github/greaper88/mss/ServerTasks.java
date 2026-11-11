package io.github.greaper88.mss;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

final class ServerTasks {
    interface Job { void cancel(); }

    private final JavaPlugin plugin;
    private final Folia folia;
    private final Map<Long, List<BatchJob>> due = new HashMap<>();
    private BukkitTask batch;
    private long tick;

    ServerTasks(JavaPlugin plugin) {
        this.plugin = plugin;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
        } catch (ClassNotFoundException ignored) {
            folia = null;
            batch = Bukkit.getScheduler().runTaskTimer(plugin, this::heartbeat, 1L, 1L);
            return;
        }
        folia = new Folia();
    }

    boolean isFolia() { return folia != null; }

    Job repeat(Entity entity, long delay, long period, Runnable run, Runnable retired) {
        if (folia != null) {
            Object task = call(folia.repeat, call(folia.entityScheduler, entity), plugin,
                    (Consumer<Object>) ignored -> run.run(), retired, delay, period);
            if (task == null) {
                retired.run();
                return () -> {};
            }
            return () -> call(folia.cancel, task);
        }
        var job = new BatchJob(period, run);
        due.computeIfAbsent(tick + delay, ignored -> new ArrayList<>()).add(job);
        return job;
    }

    private void heartbeat() {
        List<BatchJob> jobs = due.remove(++tick);
        if (jobs == null) return;
        for (BatchJob job : jobs) {
            if (job.cancelled) continue;
            try {
                job.action.run();
            } catch (RuntimeException ex) {
                plugin.getLogger().log(Level.WARNING, "Sorting task failed", ex);
            }
            if (!job.cancelled) due.computeIfAbsent(tick + job.period, ignored -> new ArrayList<>()).add(job);
        }
    }

    private static final class BatchJob implements Job {
        final long period;
        final Runnable action;
        boolean cancelled;
        BatchJob(long period, Runnable action) { this.period = period; this.action = action; }
        @Override public void cancel() { cancelled = true; }
    }

    void next(Entity entity, Runnable run, Runnable retired) {
        if (!plugin.isEnabled()) { retired.run(); return; }
        if (folia == null) {
            Bukkit.getScheduler().runTask(plugin, () -> { if (entity.isValid()) run.run(); else retired.run(); });
        } else if (!(boolean) call(folia.entityExecute, call(folia.entityScheduler, entity), plugin, run, retired, 1L)) {
            retired.run();
        }
    }

    void global(Runnable run) {
        if (folia == null) Bukkit.getScheduler().runTask(plugin, run);
        else call(folia.globalExecute, folia.globalScheduler, plugin, run);
    }

    boolean owns(World world, int chunkX, int chunkZ) {
        return folia == null ? Bukkit.isPrimaryThread() : (boolean) call(folia.owns, null, world, chunkX, chunkZ);
    }

    void at(World world, int chunkX, int chunkZ, Runnable run) {
        if (!plugin.isEnabled()) return;
        if (owns(world, chunkX, chunkZ)) run.run();
        else if (folia == null) Bukkit.getScheduler().runTask(plugin, run);
        else call(folia.regionExecute, folia.regionScheduler, plugin, world, chunkX, chunkZ, run);
    }

    @SuppressWarnings("unchecked")
    CompletableFuture<Boolean> move(Entity entity, Location to) {
        if (folia != null) return (CompletableFuture<Boolean>) call(folia.teleport, entity, to);
        return CompletableFuture.completedFuture(entity.teleport(to));
    }

    void stop() {
        if (batch != null) batch.cancel();
        due.clear();
    }

    // Isolate the Folia API so the same plugin JAR also loads on Spigot.
    private static final class Folia {
        final Method entityScheduler, repeat, entityExecute, cancel, owns, regionExecute, globalExecute, teleport;
        final Object regionScheduler, globalScheduler;
        Folia() {
            try {
                String api = "io.papermc.paper.threadedregions.scheduler.";
                Class<?> entities = Class.forName(api + "EntityScheduler");
                Class<?> region = Class.forName(api + "RegionScheduler");
                Class<?> global = Class.forName(api + "GlobalRegionScheduler");
                entityScheduler = Entity.class.getMethod("getScheduler");
                repeat = entities.getMethod("runAtFixedRate", Plugin.class, Consumer.class,
                        Runnable.class, long.class, long.class);
                entityExecute = entities.getMethod("execute", Plugin.class, Runnable.class, Runnable.class, long.class);
                cancel = Class.forName(api + "ScheduledTask").getMethod("cancel");
                owns = Bukkit.class.getMethod("isOwnedByCurrentRegion", World.class, int.class, int.class);
                regionExecute = region.getMethod("execute", Plugin.class, World.class, int.class, int.class, Runnable.class);
                globalExecute = global.getMethod("execute", Plugin.class, Runnable.class);
                regionScheduler = call(Bukkit.class.getMethod("getRegionScheduler"), null);
                globalScheduler = call(Bukkit.class.getMethod("getGlobalRegionScheduler"), null);
                teleport = Entity.class.getMethod("teleportAsync", Location.class);
            } catch (ReflectiveOperationException ex) {
                throw new IllegalStateException("Incompatible Folia scheduler API", ex);
            }
        }
    }

    private static Object call(Method method, Object receiver, Object... args) {
        try {
            return method.invoke(receiver, args);
        } catch (InvocationTargetException ex) {
            if (ex.getCause() instanceof RuntimeException failure) throw failure;
            if (ex.getCause() instanceof Error failure) throw failure;
            throw new IllegalStateException(method.getName(), ex.getCause());
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(method.getName(), ex);
        }
    }
}
