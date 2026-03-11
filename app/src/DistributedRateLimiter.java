import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import java.time.Instant;

public class DistributedRateLimiter {
    private final JedisPool jedisPool;
    private final int maxRequestsPerHour;
    private final int maxTokens;
    private final String rateLimitKeyPrefix;

    // Constructor
    public DistributedRateLimiter(JedisPool jedisPool, int maxRequestsPerHour, int maxTokens) {
        this.jedisPool = jedisPool;
        this.maxRequestsPerHour = maxRequestsPerHour;
        this.maxTokens = maxTokens;
        this.rateLimitKeyPrefix = "rate_limit:";
    }

    // Check rate limit for a client
    public String checkRateLimit(String clientId) {
        try (Jedis jedis = jedisPool.getResource()) {
            long currentTime = Instant.now().getEpochSecond();
            long currentHour = currentTime / 3600; // Time in hours

            String key = rateLimitKeyPrefix + clientId;
            String hourlyKey = key + ":hourly:" + currentHour;
            String tokensKey = key + ":tokens:" + currentHour;

            // Increment request count for the client in the current hour
            long currentRequests = jedis.hincrBy(hourlyKey, "requests", 1);

            // Check if the client has exceeded the maximum request limit for this hour
            if (currentRequests > maxRequestsPerHour) {
                return "Rate limit exceeded, try again in " + (3600 - (currentTime % 3600)) + " seconds";
            }

            // Track tokens for the current hour (reset every hour)
            String availableTokensStr = jedis.get(tokensKey);
            if (availableTokensStr == null) {
                jedis.setex(tokensKey, 3600, String.valueOf(maxTokens)); // Replenish tokens at the start of each hour
                availableTokensStr = String.valueOf(maxTokens);
            }

            int availableTokens = Integer.parseInt(availableTokensStr);
            if (availableTokens > 0) {
                // Consume a token for this request
                jedis.decr(tokensKey);
                return "Request allowed";
            } else {
                return "Rate limit exceeded";
            }
        }
    }

    // Main method for simulation
    public static void main(String[] args) {
        JedisPool jedisPool = new JedisPool("localhost", 6379); // Connect to Redis
        DistributedRateLimiter rateLimiter = new DistributedRateLimiter(jedisPool, 1000, 1000); // Max 1000 requests per hour

        // Simulate incoming requests from a client
        String clientId = "client123";
        for (int i = 0; i < 1005; i++) {
            String response = rateLimiter.checkRateLimit(clientId);
            System.out.println(response);
            try {
                Thread.sleep(10);  // Simulate some delay between requests
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}