package mss.smoke;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.GlowItemFrame;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

// Test-only plugin; schedule fixture edits on their owning region on Folia.
public final class ServerProbe extends JavaPlugin {
    private static final int FAR = 1408;
    private Object region;
    private Method execute, loadChunk, owns;
    private ItemFrame woodFrame;
    private Item overflow, remoteOverflow, cappedJunk, ordinaryJunk, offController, legacyDrop;
    private ItemStack payload;

    @Override public void onEnable() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            region = Bukkit.class.getMethod("getRegionScheduler").invoke(null);
            execute = Class.forName("io.papermc.paper.threadedregions.scheduler.RegionScheduler")
                    .getMethod("execute", Plugin.class, World.class, int.class, int.class, Runnable.class);
            loadChunk = World.class.getMethod("getChunkAtAsync", int.class, int.class, boolean.class);
            owns = Bukkit.class.getMethod("isOwnedByCurrentRegion", World.class, int.class, int.class);
        } catch (ClassNotFoundException spigot) {
            // Bukkit's primary thread owns all chunks.
        } catch (ReflectiveOperationException ex) { throw new IllegalStateException(ex); }
    }

    private void at(World world, int x, Runnable action) {
        Runnable guarded = () -> {
            try { action.run(); }
            catch (Throwable ex) { getLogger().severe("MSS_PROBE_FAIL " + ex); ex.printStackTrace(); }
        };
        if (region == null) Bukkit.getScheduler().runTask(this, guarded);
        else try { execute.invoke(region, this, world, x >> 4, 0, guarded); }
        catch (ReflectiveOperationException ex) { throw new IllegalStateException(ex); }
    }

    private void prepare(World world, int x, Runnable action) {
        // The console command owns global force-load state; block edits belong to the region.
        world.setChunkForceLoaded(x >> 4, 0, true);
        if (region == null) at(world, x, action);
        else try {
            ((CompletableFuture<?>) loadChunk.invoke(world, x >> 4, 0, true)).thenRun(() -> at(world, x, action));
        } catch (ReflectiveOperationException ex) { throw new IllegalStateException(ex); }
    }

    private static void block(World w, int x, int y, int z, Material material) {
        w.getBlockAt(x, y, z).setType(material, false);
    }

    private static Inventory inv(World w, int x, int y, int z) {
        return ((Container) w.getBlockAt(x, y, z).getState()).getInventory();
    }

    private static ItemFrame receiver(World w, int x, Material marker, Material cap, boolean output) {
        int z = 2;
        block(w, x, 79, z, output ? Material.CHEST : Material.STONE);
        block(w, x, 80, z, Material.HOPPER);
        int frameZ = cap == null ? z : z - 1;
        block(w, x, 81, frameZ + 1, cap == null ? Material.STONE : cap);
        Class<? extends ItemFrame> type = cap == Material.GLASS ? GlowItemFrame.class : ItemFrame.class;
        return w.spawn(new Location(w, x + 0.5, 81.5, frameZ + 0.5), type, frame -> {
                    frame.setFixed(true);
                    frame.setFacingDirection(BlockFace.NORTH, true);
                    frame.setItem(new ItemStack(marker));
                });
    }

    private static Item drop(World w, double x, double y, double z, ItemStack stack) {
        return w.dropItem(new Location(w, x, y, z), stack, item -> {
            item.setGravity(false);
            item.setVelocity(new Vector());
            item.setPickupDelay(32767);
        });
    }

    private static void require(boolean value, String what) {
        if (!value) throw new AssertionError(what);
    }

    private boolean owned(World world, int x) {
        if (region == null) return true;
        try { return (boolean) owns.invoke(null, world, x >> 4, 0); }
        catch (ReflectiveOperationException ex) { throw new IllegalStateException(ex); }
    }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        World w = Bukkit.getWorlds().getFirst();
        switch (args[0]) {
            case "prepare" -> {
                prepare(w, 0, () -> {
                    block(w, 1, 78, 1, Material.GOLD_BLOCK);
                    block(w, 1, 79, 1, Material.LAPIS_BLOCK);
                    woodFrame = receiver(w, 4, Material.OAK_LOG, null, true);
                    receiver(w, 6, Material.DIRT, Material.CHEST, true);
                    inv(w, 6, 81, 2).addItem(new ItemStack(Material.POTATO, 4));
                    receiver(w, 8, Material.WHITE_WOOL, Material.GLASS, true);
                    receiver(w, 10, Material.SAND, null, false);
                    inv(w, 10, 80, 2).setItem(0, new ItemStack(Material.SAND, 63));
                    for (int i = 1; i < 5; i++) inv(w, 10, 80, 2).setItem(i, new ItemStack(Material.DIRT, 64));
                    receiver(w, 12, Material.KELP, null, false);
                    receiver(w, 14, Material.COAL, null, false);
                    block(w, 2, 79, 5, Material.STONE);
                    block(w, 2, 80, 5, Material.HOPPER);
                    getLogger().info("MSS_PROBE_LOCAL_READY");
                });
                prepare(w, FAR, () -> {
                    receiver(w, FAR, Material.COBBLESTONE, null, false);
                    receiver(w, FAR + 2, Material.ICE, null, false);
                    inv(w, FAR + 2, 80, 2).setItem(0, new ItemStack(Material.SNOWBALL, 15));
                    for (int i = 1; i < 5; i++) inv(w, FAR + 2, 80, 2).setItem(i, new ItemStack(Material.DIRT, 64));
                    getLogger().info("MSS_PROBE_REMOTE_READY");
                });
            }
            case "drop" -> at(w, 0, () -> {
                if (region != null) require(!owned(w, FAR), "test fixture must span separate Folia regions");
                payload = new ItemStack(Material.POPLAR_LOG, 3);
                var meta = payload.getItemMeta();
                meta.setDisplayName("Component payload");
                payload.setItemMeta(meta);
                drop(w, 1.5, 80, 1.5, payload);
                drop(w, 1.5, 80, 1.5, new ItemStack(Material.MUD_BRICKS, 2));
                drop(w, 1.5, 80, 1.5, new ItemStack(Material.RED_CUSHION, 2));
                drop(w, 1.5, 80, 1.5, new ItemStack(Material.DRIED_KELP_BLOCK));
                drop(w, 1.5, 80, 1.5, new ItemStack(Material.CALCITE, 4));
                remoteOverflow = drop(w, 1.5, 80, 1.5, new ItemStack(Material.SNOWBALL, 4));
                overflow = drop(w, 1.5, 80, 1.5, new ItemStack(Material.SAND, 10));
                cappedJunk = drop(w, 4.5, 81.04, 2.5, new ItemStack(Material.STICK));
                ordinaryJunk = drop(w, 2.5, 81.04, 5.5, new ItemStack(Material.STICK));
                offController = drop(w, 1.5, 80, 4.5, new ItemStack(Material.POPLAR_LOG));
                getLogger().info("MSS_PROBE_DROPPED");
            });
            case "check" -> {
                at(w, 0, () -> {
                    require(inv(w, 4, 79, 2).containsAtLeast(payload, 3), "open hopper output or item components");
                    require(inv(w, 6, 79, 2).contains(Material.MUD_BRICKS, 2), "chained fallback through dirt receiver");
                    require(inv(w, 6, 81, 2).contains(Material.POTATO, 4), "virtual glass must block pulls from above");
                    require(inv(w, 8, 79, 2).contains(Material.RED_CUSHION, 2), "physical glass with side glow frame");
                    require(inv(w, 10, 80, 2).getItem(0).getAmount() == 64, "partial deposit");
                    require(overflow.isValid() && overflow.getItemStack().getAmount() == 9
                            && overflow.getLocation().getBlockX() == 1, "overflow remains at controller");
                    require(remoteOverflow.isValid() && remoteOverflow.getItemStack().getAmount() == 3
                            && remoteOverflow.getLocation().getBlockX() == 1, "distant overflow returns to controller");
                    int kelpX = System.getProperty("mss.test.variant", "standard").equals("standard") ? 12 : 14;
                    require(inv(w, kelpX, 80, 2).contains(Material.DRIED_KELP_BLOCK), "kelp variant routing");
                    require(cappedJunk.isValid() && !ordinaryJunk.isValid(), "virtual glass scoped to labeled hoppers");
                    require(offController.isValid(), "unrelated drops remain untouched");
                    woodFrame.remove();
                    getLogger().info("MSS_PROBE_LOCAL_PASS");
                });
                at(w, FAR, () -> {
                    require(inv(w, FAR, 80, 2).contains(Material.CALCITE, 4), "distant receiver deposit");
                    require(inv(w, FAR + 2, 80, 2).getItem(0).getAmount() == 16, "distant partial deposit");
                    getLogger().info("MSS_PROBE_REMOTE_PASS");
                });
            }
            case "unlabeled" -> at(w, 0, () -> {
                require(!cappedJunk.isValid(), "removing frame restores vanilla pickup");
                getLogger().info("MSS_PROBE_PASS");
            });
            case "legacy-prepare" -> prepare(w, 0, () -> {
                block(w, 1, 78, 1, Material.GOLD_BLOCK);
                block(w, 1, 79, 1, Material.LAPIS_BLOCK);
                woodFrame = receiver(w, 4, Material.OAK_LOG, null, false);
                // Keep a fixed frame with no supporting blocks, so item physics cannot
                // push the suspended probe away from the exact teleport destination.
                block(w, 4, 80, 2, Material.AIR);
                block(w, 4, 81, 3, Material.AIR);
                getLogger().info("MSS_PROBE_LOCAL_READY");
            });
            case "legacy-drop" -> at(w, 0, () -> {
                legacyDrop = drop(w, 1.5, 80, 1.5, new ItemStack(Material.POPLAR_LOG, 3));
                getLogger().info("MSS_PROBE_DROPPED");
            });
            case "legacy-check" -> at(w, 0, () -> {
                double moved = legacyDrop.getLocation().distanceSquared(woodFrame.getLocation());
                require(legacyDrop.isValid() && legacyDrop.getItemStack().getAmount() == 3
                        && moved < 0.01,
                        "frame mode delivers the intact item to a frame with no hopper (distance squared=" + moved + ")");
                getLogger().info("MSS_PROBE_PASS");
            });
            default -> { return false; }
        }
        return true;
    }
}
