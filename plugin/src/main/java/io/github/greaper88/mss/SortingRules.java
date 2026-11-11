package io.github.greaper88.mss;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

record SortingRules(Map<NamespacedKey, List<NamespacedKey>> routes, Set<NamespacedKey> markers,
                    List<NamespacedKey> misc) {
    private record Group(NamespacedKey marker, String fallback) {}

    static SortingRules load(JavaPlugin plugin, String variant) {
        String resource = switch (variant) {
            case "standard" -> "rules/config.json";
            case "kelp-as-fuel" -> "rules/config-kelp-as-fuel.json";
            default -> throw new IllegalArgumentException("Unknown sorting variant: " + variant);
        };
        var groups = new HashMap<String, Group>();
        var items = new HashMap<NamespacedKey, String>();
        var markers = new HashSet<NamespacedKey>();
        try (var reader = new InputStreamReader(Objects.requireNonNull(plugin.getResource(resource), resource),
                StandardCharsets.UTF_8)) {
            for (var entry : JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("groups")) {
                JsonObject config = entry.getAsJsonObject();
                String id = config.get("group_name").getAsString();
                var marker = key(config.get("item_frame").getAsString());
                String fallback = config.has("fallback") ? config.get("fallback").getAsString() : null;
                if (!markers.add(marker) || groups.putIfAbsent(id, new Group(marker, fallback)) != null) {
                    throw new IllegalArgumentException("Duplicate group or receiver: " + id);
                }
                for (var item : config.getAsJsonArray("items")) {
                    var itemId = key(item.getAsString());
                    if (items.putIfAbsent(itemId, id) != null) {
                        throw new IllegalArgumentException("Duplicate item: " + itemId);
                    }
                }
            }
        } catch (java.io.IOException ex) {
            throw new IllegalStateException("Cannot read " + resource, ex);
        }
        var chains = new HashMap<String, List<NamespacedKey>>();
        for (String id : groups.keySet()) {
            var chain = new ArrayList<NamespacedKey>();
            var visited = new HashSet<String>();
            for (String next = id; next != null; ) {
                if (!visited.add(next) || !groups.containsKey(next)) {
                    throw new IllegalArgumentException("Invalid fallback chain for " + id);
                }
                Group group = groups.get(next);
                chain.add(group.marker());
                next = group.fallback();
            }
            chains.put(id, List.copyOf(chain));
        }
        var routes = new HashMap<NamespacedKey, List<NamespacedKey>>();
        items.forEach((item, group) -> routes.put(item, chains.get(group)));
        return new SortingRules(Map.copyOf(routes), Set.copyOf(markers),
                Objects.requireNonNull(chains.get("misc"), "Missing misc group"));
    }

    List<NamespacedKey> route(NamespacedKey item) {
        return routes.getOrDefault(item, misc);
    }

    private static NamespacedKey key(String name) {
        return Objects.requireNonNull(NamespacedKey.fromString(name), "Invalid item ID: " + name);
    }
}
