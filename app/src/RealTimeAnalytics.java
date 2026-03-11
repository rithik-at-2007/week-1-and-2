import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class RealTimeAnalytics {
    private final ConcurrentHashMap<String, Integer> pageViewCounts;
    private final ConcurrentHashMap<String, Set<String>> uniqueVisitors;
    private final ConcurrentHashMap<String, Integer> trafficSourceCounts;
    private final PriorityQueue<PageVisit> topPagesQueue;
    private final ScheduledExecutorService scheduler;
    private final int topN;

    public RealTimeAnalytics(int topN) {
        this.pageViewCounts = new ConcurrentHashMap<>();
        this.uniqueVisitors = new ConcurrentHashMap<>();
        this.trafficSourceCounts = new ConcurrentHashMap<>();
        this.topPagesQueue = new PriorityQueue<>(topN, Comparator.comparingInt(PageVisit::getVisitCount));
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.topN = topN;

        // Schedule periodic dashboard updates
        scheduler.scheduleAtFixedRate(this::updateDashboard, 0, 5, TimeUnit.SECONDS);
    }

    public void processPageView(String pageUrl, String userId, String trafficSource) {
        pageViewCounts.merge(pageUrl, 1, Integer::sum);

        uniqueVisitors.computeIfAbsent(pageUrl, k -> new HashSet<>()).add(userId);

        trafficSourceCounts.merge(trafficSource, 1, Integer::sum);

        updateTopPages(pageUrl);
    }

    private void updateTopPages(String pageUrl) {
        int visitCount = pageViewCounts.getOrDefault(pageUrl, 0);
        topPagesQueue.offer(new PageVisit(pageUrl, visitCount));
        if (topPagesQueue.size() > topN) {
            topPagesQueue.poll();  // Remove the page with the least visits if we exceed the top N
        }
    }

    private void updateDashboard() {
        System.out.println("Dashboard Updated:");
        System.out.println("Top " + topN + " Most Visited Pages:");
        topPagesQueue.stream()
                .sorted(Comparator.comparingInt(PageVisit::getVisitCount).reversed())
                .forEach(pageVisit -> System.out.println(pageVisit.pageUrl + ": " + pageVisit.visitCount));

        System.out.println("\nTraffic Source Counts:");
        trafficSourceCounts.forEach((source, count) -> System.out.println(source + ": " + count));

        System.out.println("\nUnique Visitors per Page:");
        uniqueVisitors.forEach((pageUrl, users) -> System.out.println(pageUrl + ": " + users.size()));
        System.out.println("-------------------------------------");
    }

    public static void main(String[] args) throws InterruptedException {
        RealTimeAnalytics analytics = new RealTimeAnalytics(10);

        // Simulating incoming page view events
        analytics.processPageView("news1", "user1", "Google");
        analytics.processPageView("news2", "user2", "Facebook");
        analytics.processPageView("news1", "user3", "Direct");
        analytics.processPageView("news3", "user1", "Google");
        analytics.processPageView("news1", "user4", "Google");
        analytics.processPageView("news2", "user5", "Facebook");

        // Wait to see the updates
        Thread.sleep(10000);
    }

    static class PageVisit {
        String pageUrl;
        int visitCount;

        PageVisit(String pageUrl, int visitCount) {
            this.pageUrl = pageUrl;
            this.visitCount = visitCount;
        }

        public int getVisitCount() {
            return visitCount;
        }
    }
}