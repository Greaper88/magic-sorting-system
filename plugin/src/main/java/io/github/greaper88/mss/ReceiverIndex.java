package io.github.greaper88.mss;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;

final class ReceiverIndex {
    private static final int CELL_SIZE = 64;

    record Point(World world, double x, double y, double z) {
        static Point of(Location at) { return new Point(at.getWorld(), at.getX(), at.getY(), at.getZ()); }
        Location location() { return new Location(world, x, y, z); }
        BlockPoint block() { return new BlockPoint(world, Location.locToBlock(x), Location.locToBlock(y), Location.locToBlock(z)); }
        double distanceSquared(Point other) {
            double dx = x - other.x, dy = y - other.y, dz = z - other.z;
            return dx * dx + dy * dy + dz * dz;
        }
    }

    record BlockPoint(World world, int x, int y, int z) {
        static BlockPoint of(Block block) { return new BlockPoint(block.getWorld(), block.getX(), block.getY(), block.getZ()); }
        BlockPoint offset(int dx, int dy, int dz) { return new BlockPoint(world, x + dx, y + dy, z + dz); }
        int chunkX() { return x >> 4; }
        int chunkZ() { return z >> 4; }
        Block block() { return world.getBlockAt(x, y, z); }
        Location stagingPoint() { return new Location(world, x + 0.5, y + 0.76, z + 0.5); }
    }

    record Receiver(UUID id, NamespacedKey marker, Point frame, BlockPoint hopper) {}
    private record Bucket(World world, NamespacedKey marker, int x, int z) {}

    private final ConcurrentHashMap<Bucket, ConcurrentHashMap<UUID, Receiver>> cells = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Receiver> receivers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<BlockPoint, Integer> hoppers = new ConcurrentHashMap<>();

    void replace(Receiver old, Receiver next) {
        if (old != null) {
            receivers.remove(old.id(), old);
            cells.computeIfPresent(bucket(old), (key, values) -> {
                values.remove(old.id(), old);
                return values.isEmpty() ? null : values;
            });
            if (old.hopper() != null) hoppers.computeIfPresent(old.hopper(), (key, count) -> count == 1 ? null : count - 1);
        }
        if (next != null) {
            receivers.put(next.id(), next);
            cells.compute(bucket(next), (key, values) -> {
                if (values == null) values = new ConcurrentHashMap<>();
                values.put(next.id(), next);
                return values;
            });
            if (next.hopper() != null) hoppers.merge(next.hopper(), 1, Integer::sum);
        }
    }

    boolean current(Receiver receiver) { return receiver.equals(receivers.get(receiver.id())); }
    boolean isReceiverHopper(BlockPoint point) { return hoppers.containsKey(point); }
    int size() { return receivers.size(); }
    int hopperCount() { return hoppers.size(); }

    List<Receiver> nearby(NamespacedKey marker, Point from, int distance, boolean requireHopper) {
        var found = new ArrayList<Receiver>();
        double squared = (double) distance * distance;
        for (int x = cell(from.x() - distance); x <= cell(from.x() + distance); x++) {
            for (int z = cell(from.z() - distance); z <= cell(from.z() + distance); z++) {
                var values = cells.get(new Bucket(from.world(), marker, x, z));
                if (values == null) continue;
                for (Receiver receiver : values.values()) {
                    if ((!requireHopper || receiver.hopper() != null) && from.distanceSquared(receiver.frame()) <= squared) {
                        found.add(receiver);
                    }
                }
            }
        }
        return found;
    }

    private static Bucket bucket(Receiver r) {
        return new Bucket(r.frame().world(), r.marker(), cell(r.frame().x()), cell(r.frame().z()));
    }

    private static int cell(double coordinate) { return Math.floorDiv(Location.locToBlock(coordinate), CELL_SIZE); }
}
