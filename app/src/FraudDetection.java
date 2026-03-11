import java.util.*;
import java.time.*;

public class FraudDetection {

    private static final int MAX_TIME_WINDOW = 60 * 60; // 1 hour in seconds

    // HashMap for storing amounts and their indices (for classic Two-Sum)
    private Map<Integer, List<Integer>> amountIndicesMap;

    // HashMap for detecting duplicate payments: (amount, merchant) -> accountId set
    private Map<String, Set<String>> duplicatePaymentsMap;

    // Constructor to initialize the maps
    public FraudDetection() {
        this.amountIndicesMap = new HashMap<>();
        this.duplicatePaymentsMap = new HashMap<>();
    }

    // Classic Two-Sum: Find pairs that sum to the target amount
    public List<int[]> findTwoSum(int[] transactions, int target) {
        List<int[]> result = new ArrayList<>();
        Set<Integer> seen = new HashSet<>();

        for (int i = 0; i < transactions.length; i++) {
            int complement = target - transactions[i];
            if (seen.contains(complement)) {
                result.add(new int[]{complement, transactions[i]});
            }
            seen.add(transactions[i]);
        }
        return result;
    }

    // Two-Sum with Time Window: Pairs within 1 hour (60 minutes)
    public List<int[]> findTwoSumWithTimeWindow(List<Transaction> transactions, int target) {
        List<int[]> result = new ArrayList<>();
        Deque<Transaction> window = new LinkedList<>();

        for (Transaction currentTransaction : transactions) {
            // Remove transactions older than 1 hour
            while (!window.isEmpty() && currentTransaction.timestamp.minusSeconds(MAX_TIME_WINDOW).isAfter(window.peekFirst().timestamp)) {
                window.pollFirst();
            }

            // Check for a complement within the window
            for (Transaction transaction : window) {
                if (transaction.amount + currentTransaction.amount == target) {
                    result.add(new int[]{transaction.amount, currentTransaction.amount});
                }
            }

            // Add the current transaction to the window
            window.offerLast(currentTransaction);
        }

        return result;
    }

    // K-Sum: Find K transactions that sum to the target amount (simplified for K=3)
    public List<int[]> findKSum(int[] transactions, int target, int k) {
        List<int[]> result = new ArrayList<>();
        findKSumRecursive(transactions, target, k, 0, new ArrayList<>(), result);
        return result;
    }

    // Helper function for recursive K-Sum
    private void findKSumRecursive(int[] transactions, int target, int k, int start, List<Integer> current, List<int[]> result) {
        if (current.size() == k) {
            int sum = 0;
            for (int num : current) {
                sum += num;
            }
            if (sum == target) {
                result.add(current.stream().mapToInt(Integer::intValue).toArray());
            }
            return;
        }

        for (int i = start; i < transactions.length; i++) {
            current.add(transactions[i]);
            findKSumRecursive(transactions, target, k, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    // Duplicate detection: Same amount, same merchant, different accounts
    public boolean detectDuplicatePayment(String amount, String merchant, String accountId) {
        String key = amount + ":" + merchant;
        Set<String> accountIds = duplicatePaymentsMap.computeIfAbsent(key, k -> new HashSet<>());
        if (accountIds.contains(accountId)) {
            return true;  // Duplicate detected
        }
        accountIds.add(accountId);
        return false;
    }

    // Helper class for storing transaction details
    public static class Transaction {
        int amount;
        String accountId;
        String merchant;
        LocalDateTime timestamp;

        public Transaction(int amount, String accountId, String merchant, LocalDateTime timestamp) {
            this.amount = amount;
            this.accountId = accountId;
            this.merchant = merchant;
            this.timestamp = timestamp;
        }
    }

    // Main method for testing
    public static void main(String[] args) {
        FraudDetection fraudDetection = new FraudDetection();

        // Test Classic Two-Sum
        int[] transactions = {5, 7, 11, 15};
        int target = 12;
        List<int[]> twoSumResults = fraudDetection.findTwoSum(transactions, target);
        System.out.println("Two-Sum Results: " + Arrays.deepToString(twoSumResults.toArray()));

        // Test Two-Sum with Time Window
        List<Transaction> transactionList = Arrays.asList(
                new Transaction(10, "A1", "Merchant1", LocalDateTime.now().minusMinutes(10)),
                new Transaction(5, "A2", "Merchant1", LocalDateTime.now().minusMinutes(5)),
                new Transaction(7, "A3", "Merchant1", LocalDateTime.now())
        );
        List<int[]> timeWindowResults = fraudDetection.findTwoSumWithTimeWindow(transactionList, target);
        System.out.println("Two-Sum with Time Window Results: " + Arrays.deepToString(timeWindowResults.toArray()));

        // Test K-Sum
        int[] kSumTransactions = {1, 2, 3, 4, 5};
        List<int[]> kSumResults = fraudDetection.findKSum(kSumTransactions, 9, 3);
        System.out.println("K-Sum Results: " + Arrays.deepToString(kSumResults.toArray()));

        // Test Duplicate Detection
        boolean isDuplicate = fraudDetection.detectDuplicatePayment("10", "Merchant1", "A1");
        System.out.println("Duplicate Payment: " + isDuplicate);
    }
}