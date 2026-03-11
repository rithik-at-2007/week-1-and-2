import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class DNSCache {
    private final int cacheSize;
    private final long ttlInMillis;
    private final Map<String, CacheEntry> cache;
    private final LinkedHashMap<String, Long> accessOrder;
    private final Supplier<String> upstreamDnsQuery;
    private long cacheHits = 0;
    private long cacheMisses = 0;

    public DNSCache(int cacheSize, long ttlInMillis, Supplier<String> upstreamDnsQuery) {
        this.cacheSize = cacheSize;
        this.ttlInMillis = ttlInMillis;
        this.cache = new ConcurrentHashMap<>();
        this.accessOrder = new LinkedHashMap<>();
        this.upstreamDnsQuery = upstreamDnsQuery;
    }

    public String resolveDomain(String domain) {
        CacheEntry entry = cache.get(domain);
        long currentTime = System.currentTimeMillis();

        if (entry != null && (currentTime - entry.timestamp) <= ttlInMillis) {
            accessOrder.put(domain, currentTime);
            cacheHits++;
            return entry.ipAddress;
        } else {
            cacheMisses++;
            if (entry != null) {
                cache.remove(domain);
            }
            String ipAddress = queryUpstreamDNS(domain);
            addToCache(domain, ipAddress);
            return ipAddress;
        }
    }

    private void addToCache(String domain, String ipAddress) {
        if (cache.size() >= cacheSize) {
            evictLRU();
        }
        long timestamp = System.currentTimeMillis();
        cache.put(domain, new CacheEntry(domain, ipAddress, timestamp));
        accessOrder.put(domain, timestamp);
    }

    private void evictLRU() {
        long oldestTimestamp = Long.MAX_VALUE;
        String lruDomain = null;
        for (Map.Entry<String, Long> entry : accessOrder.entrySet()) {
            if (entry.getValue() < oldestTimestamp) {
                oldestTimestamp = entry.getValue();
                lruDomain = entry.getKey();
            }
        }
        if (lruDomain != null) {
            cache.remove(lruDomain);
            accessOrder.remove(lruDomain);
        }
    }

    private String queryUpstreamDNS(String domain) {
        return upstreamDnsQuery.get(); // Simulate upstream DNS query
    }

    public void reportCacheStats() {
        System.out.println("Cache Hits: " + cacheHits);
        System.out.println("Cache Misses: " + cacheMisses);
        System.out.println("Hit/Miss Ratio: " + (cacheHits + cacheMisses == 0 ? 0 : (double) cacheHits / (cacheHits + cacheMisses)));
    }

    private static class CacheEntry {
        String domain;
        String ipAddress;
        long timestamp;

        CacheEntry(String domain, String ipAddress, long timestamp) {
            this.domain = domain;
            this.ipAddress = ipAddress;
            this.timestamp = timestamp;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        DNSCache dnsCache = new DNSCache(5, 5000, () -> "192.168.1.1");  // Simulated upstream DNS IP address

        // Simulating domain resolutions
        System.out.println(dnsCache.resolveDomain("example.com"));
        System.out.println(dnsCache.resolveDomain("example.com")); // Cache hit
        Thread.sleep(3000);
        System.out.println(dnsCache.resolveDomain("another.com"));
        Thread.sleep(3000);
        System.out.println(dnsCache.resolveDomain("example.com")); // Cache hit
        Thread.sleep(3000);
        dnsCache.resolveDomain("newdomain.com");

        dnsCache.reportCacheStats();  // Report stats
    }
}