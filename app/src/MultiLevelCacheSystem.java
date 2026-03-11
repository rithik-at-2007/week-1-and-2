import java.util.*;

public class MultiLevelCacheSystem {
    private static final int L1_CACHE_SIZE = 10000; // L1 Cache size (10,000 most popular videos)
    private static final int L2_CACHE_SIZE = 100000; // L2 Cache size (100,000 frequently accessed videos)

    private LRUCache l1Cache; // L1 Cache (in-memory)
    private LRUCache l2Cache; // L2 Cache (SSD-backed)
    private Map<String, String> l3Cache; // L3 Cache (Database)

    private Map<String, Integer> accessCountL1;
    private Map<String, Integer> accessCountL2;

    private int totalAccessesL1 = 0;
    private int totalAccessesL2 = 0;
    private int totalAccessesL3 = 0;

    public MultiLevelCacheSystem() {
        l1Cache = new LRUCache(L1_CACHE_SIZE);
        l2Cache = new LRUCache(L2_CACHE_SIZE);
        l3Cache = new HashMap<>();

        accessCountL1 = new HashMap<>();
        accessCountL2 = new HashMap<>();
    }

    // Fetch video by videoId from the cache hierarchy
    public String getVideo(String videoId) {
        // Try to get from L1 (memory cache)
        String video = l1Cache.get(videoId);
        if (video != null) {
            totalAccessesL1++;
            accessCountL1.put(videoId, accessCountL1.getOrDefault(videoId, 0) + 1);
            return video;
        }

        // Try to get from L2 (SSD cache)
        video = l2Cache.get(videoId);
        if (video != null) {
            totalAccessesL2++;
            accessCountL2.put(videoId, accessCountL2.getOrDefault(videoId, 0) + 1);
            // Promote video to L1 if it is frequently accessed
            l1Cache.put(videoId, video);
            return video;
        }

        // Try to get from L3 (database)
        video = l3Cache.get(videoId);
        if (video != null) {
            totalAccessesL3++;
            // If found in L3, promote to L2 and then to L1
            l2Cache.put(videoId, video);
            l1Cache.put(videoId, video);
            return video;
        }

        return null; // Video not found
    }

    // Add video to the cache system
    public void addVideo(String videoId, String videoData) {
        // Add video to L3 (database)
        l3Cache.put(videoId, videoData);
    }

    // Invalidate video across all caches
    public void invalidateVideo(String videoId) {
        l1Cache.remove(videoId);
        l2Cache.remove(videoId);
        l3Cache.remove(videoId);
    }

    // Cache Hit Ratio Calculation
    public double getL1CacheHitRatio() {
        return (double) totalAccessesL1 / (totalAccessesL1 + totalAccessesL3);
    }

    public double getL2CacheHitRatio() {
        return (double) totalAccessesL2 / (totalAccessesL2 + totalAccessesL3);
    }

    // Internal LRU Cache Implementation
    private class LRUCache {
        private int capacity;
        private LinkedHashMap<String, String> cache;

        public LRUCache(int capacity) {
            this.capacity = capacity;
            cache = new LinkedHashMap<>(capacity, 0.75f, true);
        }

        public String get(String key) {
            return cache.get(key);
        }

        public void put(String key, String value) {
            if (cache.size() >= capacity) {
                Iterator<Map.Entry<String, String>> iter = cache.entrySet().iterator();
                iter.next(); // Evict the least recently used entry
                iter.remove();
            }
            cache.put(key, value);
        }

        public void remove(String key) {
            cache.remove(key);
        }
    }

    public static void main(String[] args) {
        MultiLevelCacheSystem cacheSystem = new MultiLevelCacheSystem();

        // Adding videos to the L3 cache (database)
        cacheSystem.addVideo("video1", "Video data 1");
        cacheSystem.addVideo("video2", "Video data 2");

        // Access video (this will promote it through L3 to L1 cache)
        System.out.println(cacheSystem.getVideo("video1"));
        System.out.println(cacheSystem.getVideo("video2"));

        // Invalidate video
        cacheSystem.invalidateVideo("video1");

        // Check cache hit ratios
        System.out.println("L1 Cache Hit Ratio: " + cacheSystem.getL1CacheHitRatio());
        System.out.println("L2 Cache Hit Ratio: " + cacheSystem.getL2CacheHitRatio());
    }
}