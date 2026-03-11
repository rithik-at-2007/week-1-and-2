import java.util.*;

public class AutocompleteSystem{
    private static final int MAX_RESULTS = 10;  // Maximum number of suggestions
    private final TrieNode root;
    private final Map<String, Integer> queryFrequencyMap;

    // Constructor
    public AutocompleteSystem() {
        root = new TrieNode();
        queryFrequencyMap = new HashMap<>();
    }

    // Trie Node
    private static class TrieNode {
        Map<Character, TrieNode> children;
        PriorityQueue<String> topSuggestions;

        TrieNode() {
            children = new HashMap<>();
            topSuggestions = new PriorityQueue<>(MAX_RESULTS, (a, b) -> queryFrequencyMap.get(b) - queryFrequencyMap.get(a));
        }
    }

    // Insert a search query into the Trie and update the frequency map
    public void insertQuery(String query) {
        TrieNode node = root;

        // Insert the query into the Trie
        for (char ch : query.toCharArray()) {
            node = node.children.computeIfAbsent(ch, c -> new TrieNode());
            node.topSuggestions.offer(query);  // Add the query to this node's suggestions
        }

        // Update the frequency of the query
        queryFrequencyMap.put(query, queryFrequencyMap.getOrDefault(query, 0) + 1);
    }

    // Get top 10 autocomplete suggestions for a given prefix
    public List<String> getSuggestions(String prefix) {
        TrieNode node = root;
        List<String> result = new ArrayList<>();

        // Traverse the Trie to find the node corresponding to the prefix
        for (char ch : prefix.toCharArray()) {
            if (!node.children.containsKey(ch)) {
                return result;  // No suggestions if the prefix is not found
            }
            node = node.children.get(ch);
        }

        // Collect the top 10 suggestions based on the frequency
        result.addAll(node.topSuggestions);
        return result;
    }

    // Main Method for testing the Autocomplete System
    public static void main(String[] args) {
        AutocompleteSystem autocomplete = new AutocompleteSystem();

        // Insert queries (simulating previous searches)
        autocomplete.insertQuery("hello world");
        autocomplete.insertQuery("hello");
        autocomplete.insertQuery("hell");
        autocomplete.insertQuery("help");
        autocomplete.insertQuery("hero");
        autocomplete.insertQuery("high");
        autocomplete.insertQuery("how to");
        autocomplete.insertQuery("hello there");
        autocomplete.insertQuery("how are you");
        autocomplete.insertQuery("hermit");

        // Get suggestions for a given prefix
        System.out.println("Suggestions for 'he': " + autocomplete.getSuggestions("he"));
        System.out.println("Suggestions for 'ho': " + autocomplete.getSuggestions("ho"));
        System.out.println("Suggestions for 'hel': " + autocomplete.getSuggestions("hel"));
        System.out.println("Suggestions for 'high': " + autocomplete.getSuggestions("high"));
    }
}