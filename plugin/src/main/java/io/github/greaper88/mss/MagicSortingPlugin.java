package io.github.greaper88.mss;

import static io.github.greaper88.mss.ReceiverIndex.BlockPoint;
import static io.github.greaper88.mss.ReceiverIndex.Point;
import static io.github.greaper88.mss.ReceiverIndex.Receiver;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Hopper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.HopperInventorySearchEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class MagicSortingPlugin extends JavaPlugin implements Listener {
    private final ReceiverIndex receivers = new ReceiverIndex();
    private final ConcurrentHashMap<UUID, Watch> watches = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Point> players = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Transit> transfers = new ConcurrentHashMap<>();
    private final ThreadLocal<Inventory> emptySource = ThreadLocal.withInitial(
            () -> Bukkit.createInventory(null, InventoryType.HOPPER));
    private final LongAdder deposited = new LongAdder();
    private final LongAdder handoffs = new LongAdder();
    private final AtomicLong lastEffect = new AtomicLong(Long.MIN_VALUE);
    private ServerTasks tasks;
    private SortingRules rules;
    private boolean hopperDelivery, virtualGlass, effects;
    private int distance, interval;
    private String variant;
    private volatile boolean running;

    @Override public void onEnable() {
        saveDefaultConfig();
        variant = getConfig().getString("variant", "standard");
        rules = SortingRules.load(this, variant);
        hopperDelivery = switch (getConfig().getString("delivery", "hopper")) {
            case "hopper" -> true;
            case "frame" -> false;
            default -> throw new IllegalArgumentException("delivery must be hopper or frame");
        };
        virtualGlass = hopperDelivery && getConfig().getBoolean("virtual-glass", true);
        effects = getConfig().getBoolean("effects", true);
        distance = getConfig().getInt("max-distance", 128);
        interval = getConfig().getInt("sort-interval-ticks", 20);
        if (distance < 1 || distance > 2048 || interval < 1 || interval > 200) {
            throw new IllegalArgumentException("max-distance must be 1–2048 and sort-interval-ticks must be 1–200");
        }
        tasks = new ServerTasks(this);
        running = true;
        getServer().getPluginManager().registerEvents(this, this);
        tasks.global(this::indexLoadedEntities);
        getLogger().info("Loaded " + rules.routes().size() + " item rules (" + variant + "), using "
                + (tasks.isFolia() ? "Folia region scheduling" : "Spigot tick scheduling") + ".");
    }

    @Override public void onDisable() {
        running = false;
        for (Watch watch : watches.values()) retire(watch);
        if (tasks != null) tasks.stop();
        players.clear();
        // A handoff always keeps the real entity alive until inventory insertion commits.
        transfers.clear();
        emptySource.remove();
    }

    private void indexLoadedEntities() {
        if (!running) return;
        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                tasks.at(world, chunk.getX(), chunk.getZ(), () -> {
                    if (running && chunk.isLoaded() && chunk.isEntitiesLoaded()) {
                        for (Entity entity : chunk.getEntities()) track(entity);
                    }
                });
            }
        }
        for (Player player : Bukkit.getOnlinePlayers()) track(player);
    }

    private void track(Entity entity) {
        if (!running || !(entity instanceof Item || entity instanceof ItemFrame || entity instanceof Player)) return;
        UUID id = entity.getUniqueId();
        var watch = new Watch(id);
        if (watches.putIfAbsent(id, watch) != null) return;
        int period = entity instanceof Item ? interval : entity instanceof Player ? 5 : 20;
        int delay = entity instanceof Item ? 1 + Math.floorMod(id.hashCode(), period) : 1;
        watch.setJob(tasks.repeat(entity, delay, period, () -> {
            if (!running || !entity.isValid()) { retire(watch); return; }
            switch (entity) {
                case Item item -> sort(item, id);
                case ItemFrame frame -> refreshFrame(frame, watch);
                case Player player -> players.put(id, Point.of(player.getLocation()));
                default -> {}
            }
        }, () -> retire(watch)));
    }

    private void untrack(Entity entity) {
        Watch watch = watches.get(entity.getUniqueId());
        if (watch != null) retire(watch);
    }

    private void retire(Watch watch) {
        synchronized (watch) {
            if (watch.stopped) return;
            watch.stopped = true;
            if (watch.job != null) watch.job.cancel();
            receivers.replace(watch.receiver, null);
            players.remove(watch.id);
            watches.remove(watch.id, watch);
        }
    }

    private static final class Watch {
        final UUID id;
        ServerTasks.Job job;
        boolean stopped;
        long revision;
        Receiver receiver;
        Watch(UUID id) { this.id = id; }
        synchronized void setJob(ServerTasks.Job job) {
            if (stopped) job.cancel();
            else this.job = job;
        }
    }

    private void refreshFrame(ItemFrame frame, Watch watch) {
        long revision;
        synchronized (watch) { revision = ++watch.revision; }
        var marker = frame.getItem().getType().getKey();
        if (!rules.markers().contains(marker)) { publish(watch, revision, null); return; }
        Point point = Point.of(frame.getLocation());
        if (!hopperDelivery) { publish(watch, revision, new Receiver(watch.id, marker, point, null)); return; }
        BlockPoint at = point.block();
        BlockPoint below = at.offset(0, -1, 0);
        if (isHopper(below)) { publish(watch, revision, new Receiver(watch.id, marker, point, below)); return; }
        // A side-mounted frame can label a hopper under the block it hangs on.
        var attached = frame.getAttachedFace();
        if (attached.getModY() != 0 || attached.getModX() == 0 && attached.getModZ() == 0) {
            publish(watch, revision, null);
            return;
        }
        BlockPoint underSupport = at.offset(attached.getModX(), -1, attached.getModZ());
        tasks.at(underSupport.world(), underSupport.chunkX(), underSupport.chunkZ(), () ->
                publish(watch, revision, isHopper(underSupport) ? new Receiver(watch.id, marker, point, underSupport) : null));
    }

    private void publish(Watch watch, long revision, Receiver next) {
        synchronized (watch) {
            if (!running || watch.stopped || watch.revision != revision || Objects.equals(watch.receiver, next)) return;
            receivers.replace(watch.receiver, next);
            watch.receiver = next;
        }
    }

    private static boolean isHopper(BlockPoint point) {
        return point.world().isChunkLoaded(point.chunkX(), point.chunkZ()) && point.block().getType() == Material.HOPPER;
    }

    private static boolean isController(Point at) {
        BlockPoint block = at.block();
        return block.offset(0, -1, 0).block().getType() == Material.LAPIS_BLOCK
                && block.offset(0, -2, 0).block().getType() == Material.GOLD_BLOCK;
    }

    private void sort(Item item, UUID id) {
        if (transfers.containsKey(id)) return;
        Point origin = Point.of(item.getLocation());
        if (!isController(origin)) return;
        ItemStack stack = item.getItemStack();
        for (var marker : rules.route(stack.getType().getKey())) {
            var candidates = receivers.nearby(marker, origin, distance, hopperDelivery);
            if (candidates.isEmpty()) continue;
            if (!hopperDelivery) {
                moveTo(item, id, origin, candidates.get(ThreadLocalRandom.current().nextInt(candidates.size())).frame());
                return;
            }
            Collections.shuffle(candidates, ThreadLocalRandom.current());
            boolean hasHopper = false;
            for (Receiver receiver : candidates) {
                BlockPoint to = receiver.hopper();
                if (!tasks.owns(to.world(), to.chunkX(), to.chunkZ())) {
                    relay(item, id, origin, stack.clone(), receiver);
                    return;
                }
                Inventory inventory = liveInventory(receiver);
                if (inventory == null) continue;
                hasHopper = true;
                if (deposit(item, inventory, origin) > 0) return;
            }
            // A full matching group is an overflow condition, not an alternate category.
            if (hasHopper) return;
        }
        Point nearest = null;
        double best = Double.POSITIVE_INFINITY;
        for (Point player : players.values()) {
            if (player.world() != origin.world()) continue;
            double squared = origin.distanceSquared(player);
            if (squared < best) { best = squared; nearest = player; }
        }
        if (nearest != null) moveTo(item, id, origin, nearest);
    }

    private Inventory liveInventory(Receiver receiver) {
        BlockPoint to = receiver.hopper();
        if (!receivers.current(receiver) || !isHopper(to)) return null;
        return to.block().getState() instanceof Hopper hopper ? hopper.getInventory() : null;
    }

    private int deposit(Item item, Inventory inventory, Point origin) {
        ItemStack stack = item.getItemStack();
        int before = stack.getAmount();
        int leftover = inventory.addItem(stack.clone()).values().stream().mapToInt(ItemStack::getAmount).sum();
        int inserted = before - leftover;
        if (inserted <= 0) return 0;
        if (leftover == 0) item.remove();
        else {
            ItemStack rest = stack.clone();
            rest.setAmount(leftover);
            item.setItemStack(rest);
        }
        deposited.add(inserted);
        effect(origin);
        return inserted;
    }

    private static boolean hasRoom(Inventory inventory, ItemStack item) {
        for (ItemStack slot : inventory.getStorageContents()) {
            if (slot == null || slot.getType().isAir() || slot.isSimilar(item)
                    && slot.getAmount() < Math.min(slot.getMaxStackSize(), inventory.getMaxStackSize())) return true;
        }
        return false;
    }

    private static final class Transit { volatile boolean moving; }

    private void relay(Item item, UUID id, Point origin, ItemStack snapshot, Receiver receiver) {
        var transfer = new Transit();
        if (transfers.putIfAbsent(id, transfer) != null) return;
        Runnable done = () -> transfers.remove(id, transfer);
        BlockPoint target = receiver.hopper();
        tasks.at(target.world(), target.chunkX(), target.chunkZ(), () -> {
            if (!running) { done.run(); return; }
            Inventory inventory = liveInventory(receiver);
            if (inventory == null || !hasRoom(inventory, snapshot)) { done.run(); return; }
            tasks.next(item, () -> {
                Point current = Point.of(item.getLocation());
                if (!running || !origin.block().equals(current.block()) || !isController(current)
                        || !snapshot.equals(item.getItemStack()) || !receivers.current(receiver)) {
                    done.run();
                    return;
                }
                // Keep the actual item entity until it can be inserted in the hopper's region.
                // This prevents a crash, cancelled move, or full receiver from duplicating its stack.
                transfer.moving = true;
                move(item, target.stagingPoint(), success -> {
                    if (!success || !running) { done.run(); return; }
                    tasks.next(item, () -> {
                        if (!running) { done.run(); return; }
                        Inventory destination = tasks.owns(target.world(), target.chunkX(), target.chunkZ())
                                ? liveInventory(receiver) : null;
                        int amount = item.getItemStack().getAmount();
                        int inserted = destination == null ? 0 : deposit(item, destination, origin);
                        if (inserted > 0) handoffs.increment();
                        if (inserted == amount) done.run();
                        else move(item, origin.location(), ignored -> done.run());
                    }, done);
                });
            }, done);
        });
    }

    private void moveTo(Item item, UUID id, Point origin, Point destination) {
        var transfer = new Transit();
        transfer.moving = true;
        if (transfers.putIfAbsent(id, transfer) != null) return;
        move(item, destination.location(), moved -> {
            transfers.remove(id, transfer);
            if (moved) effect(origin);
        });
    }

    private void move(Item item, Location destination, java.util.function.Consumer<Boolean> complete) {
        try {
            tasks.move(item, destination).whenComplete((moved, error) -> {
                if (error != null) getLogger().log(Level.WARNING, "Unable to move a sorting item", error);
                complete.accept(error == null && Boolean.TRUE.equals(moved));
            });
        } catch (RuntimeException ex) {
            getLogger().log(Level.WARNING, "Unable to move a sorting item", ex);
            complete.accept(false);
        }
    }

    private void effect(Point origin) {
        if (!effects || !running) return;
        long now = System.nanoTime(), last = lastEffect.get();
        if (last != Long.MIN_VALUE && now - last < 1_000_000_000L || !lastEffect.compareAndSet(last, now)) return;
        BlockPoint block = origin.block();
        tasks.at(block.world(), block.chunkX(), block.chunkZ(), () -> {
            if (!running) return;
            Location loc = origin.location();
            block.world().playSound(loc, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, SoundCategory.BLOCKS, 1f, 1f);
            block.world().spawnParticle(Particle.PORTAL, loc, 100, 1, 1, 1, 1);
        });
    }

    private boolean moving(Item item) {
        Transit transfer = transfers.get(item.getUniqueId());
        return transfer != null && transfer.moving;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void sourceSearch(HopperInventorySearchEvent event) {
        if (virtualGlass && event.getContainerType() == HopperInventorySearchEvent.ContainerType.SOURCE
                && receivers.isReceiverHopper(BlockPoint.of(event.getBlock()))) {
            // A non-null, empty source makes vanilla skip its loose-entity lookup.
            // The DESTINATION search stays untouched, so the hopper still pushes.
            event.setInventory(emptySource.get());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void hopperPickup(InventoryPickupItemEvent event) {
        if (moving(event.getItem())) { event.setCancelled(true); return; }
        if (!virtualGlass || event.getInventory().getType() != InventoryType.HOPPER) return;
        Location location = event.getInventory().getLocation();
        if (location != null && receivers.isReceiverHopper(Point.of(location).block())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void entityPickup(EntityPickupItemEvent event) { if (moving(event.getItem())) event.setCancelled(true); }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void itemMerge(ItemMergeEvent event) {
        if (moving(event.getEntity()) || moving(event.getTarget())) event.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void itemDespawn(ItemDespawnEvent event) { if (moving(event.getEntity())) event.setCancelled(true); }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void itemDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Item item && moving(item)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void spawn(ItemSpawnEvent event) { track(event.getEntity()); }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void frameSpawn(EntitySpawnEvent event) { if (event.getEntity() instanceof ItemFrame) track(event.getEntity()); }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void framePlace(HangingPlaceEvent event) { if (event.getEntity() instanceof ItemFrame) track(event.getEntity()); }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void frameBreak(HangingBreakEvent event) { if (event.getEntity() instanceof ItemFrame) untrack(event.getEntity()); }
    @EventHandler(priority = EventPriority.MONITOR)
    public void entitiesLoad(EntitiesLoadEvent event) { event.getEntities().forEach(this::track); }
    @EventHandler(priority = EventPriority.MONITOR)
    public void entitiesUnload(EntitiesUnloadEvent event) { event.getEntities().forEach(this::untrack); }
    @EventHandler(priority = EventPriority.MONITOR)
    public void join(PlayerJoinEvent event) { track(event.getPlayer()); }
    @EventHandler(priority = EventPriority.MONITOR)
    public void quit(PlayerQuitEvent event) { untrack(event.getPlayer()); }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 1 || args.length == 1 && !args[0].equalsIgnoreCase("status")) return false;
        sender.sendMessage("Magic Sorting System: " + variant + ", " + (hopperDelivery ? "hopper" : "frame")
                + " delivery, " + receivers.size() + " receivers, " + receivers.hopperCount() + " hoppers, virtual glass "
                + (virtualGlass ? "on" : "off") + ", " + deposited.sum() + " items deposited, "
                + handoffs.sum() + " cross-region deposits.");
        return true;
    }
}
