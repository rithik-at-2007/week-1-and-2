import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class PlagiarismDetector {
    private final int n; // n-gram size
    private final Map<String, Set<String>> ngramIndex;

    public PlagiarismDetector(int n) {
        this.n = n;
        this.ngramIndex = new ConcurrentHashMap<>();
    }

    public void processDocument(String document, String docId) {
        List<String> words = Arrays.asList(document.split("\\s+"));
        for (int i = 0; i <= words.size() - n; i++) {
            String ngram = String.join(" ", words.subList(i, i + n));
            ngramIndex.computeIfAbsent(ngram, k -> new HashSet<>()).add(docId);
        }
    }

    public double calculateSimilarity(String docId1, String docId2) {
        Set<String> commonNgrams = new HashSet<>();
        Set<String> doc1Ngrams = getNgramsForDocument(docId1);
        Set<String> doc2Ngrams = getNgramsForDocument(docId2);

        if (doc1Ngrams == null || doc2Ngrams == null) {
            return 0.0;
        }

        for (String ngram : doc1Ngrams) {
            if (doc2Ngrams.contains(ngram)) {
                commonNgrams.add(ngram);
            }
        }

        int totalNgrams = doc1Ngrams.size() + doc2Ngrams.size() - commonNgrams.size();
        if (totalNgrams == 0) return 0.0;
        return 2.0 * commonNgrams.size() / totalNgrams;
    }

    private Set<String> getNgramsForDocument(String docId) {
        Set<String> ngrams = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : ngramIndex.entrySet()) {
            if (entry.getValue().contains(docId)) {
                ngrams.add(entry.getKey());
            }
        }
        return ngrams.isEmpty() ? null : ngrams;
    }

    public List<String> findMostSimilarDocuments(String docId) {
        Map<String, Double> similarityScores = new HashMap<>();
        for (String otherDocId : ngramIndex.values().stream().flatMap(Set::stream).collect(Collectors.toSet())) {
            if (!otherDocId.equals(docId)) {
                double similarity = calculateSimilarity(docId, otherDocId);
                similarityScores.put(otherDocId, similarity);
            }
        }

        return similarityScores.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        PlagiarismDetector detector = new PlagiarismDetector(3);

        String doc1 = "This is a simple document that might have plagiarism issues";
        String doc2 = "This document is simple and might have some issues with plagiarism";
        String doc3 = "Completely different content without plagiarism";

        detector.processDocument(doc1, "doc1");
        detector.processDocument(doc2, "doc2");
        detector.processDocument(doc3, "doc3");

        System.out.println("Similarity between doc1 and doc2: " + detector.calculateSimilarity("doc1", "doc2"));
        System.out.println("Most similar documents to doc1: " + detector.findMostSimilarDocuments("doc1"));
    }
}