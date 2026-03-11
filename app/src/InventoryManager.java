import java.util.*;
import java.util.concurrent.*;

public class InventoryManager {
    private final Map<String, Integer> inventory;
    private final Map<String, Queue<String>> waitingList;
    private final int maxStock;
    private final int maxConcurrency;

    public InventoryManager(int maxStock, int maxConcurrency) {
        this.inventory = new ConcurrentHashMap<>();
        this.waitingList = new ConcurrentHashMap<>();
        this.maxStock = maxStock;
        this.maxConcurrency = maxConcurrency;
    }

    public void addProduct(String productId, int initialStock) {
        inventory.put(productId, initialStock);
        waitingList.put(productId, new LinkedList<>());
    }

    public boolean checkStock(String productId) {
        return inventory.getOrDefault(productId, 0) > 0;
    }

    public synchronized boolean processPurchase(String productId, String customerId) {
        if (inventory.containsKey(productId)) {
            int stock = inventory.get(productId);
            if (stock > 0) {
                inventory.put(productId, stock - 1);
                return true;
            } else {
                addToWaitingList(productId, customerId);
            }
        }
        return false;
    }

    private void addToWaitingList(String productId, String customerId) {
        waitingList.get(productId).offer(customerId);
    }

    public synchronized void processWaitingList(String productId) {
        Queue<String> waitingQueue = waitingList.get(productId);
        if (!waitingQueue.isEmpty()) {
            String nextCustomer = waitingQueue.poll();
            if (checkStock(productId)) {
                processPurchase(productId, nextCustomer);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int maxStock = 100;
        int maxConcurrency = 50000;
        InventoryManager manager = new InventoryManager(maxStock, maxConcurrency);
        manager.addProduct("Product1", 100);

        ExecutorService executor = Executors.newFixedThreadPool(maxConcurrency);
        List<Callable<Boolean>> tasks = new ArrayList<>();

        for (int i = 0; i < 50000; i++) {
            String customerId = "Customer" + i;
            tasks.add(() -> manager.processPurchase("Product1", customerId));
        }

        List<Future<Boolean>> results = executor.invokeAll(tasks);
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        int successfulPurchases = 0;
        for (Future<Boolean> result : results) {
            if (result.get()) successfulPurchases++;
        }

        System.out.println("Successful purchases: " + successfulPurchases);
        System.out.println("Waiting List size: " + manager.waitingList.get("Product1").size());
    }
}