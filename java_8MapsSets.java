import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class java_8MapsSets {
    public static void main(String[] args) {
        String[] words = new String[] {"the", "quick", "brown", "fox", "the", "lazy", "dog", "the", "fox", "runs", "quick", "fox"};
        HashMap<String, Integer> wordHashMap = new HashMap<>();

        for (String word : words) {
            wordHashMap.put(word, wordHashMap.getOrDefault(word, 0) + 1);
        }

        for (Map.Entry<String, Integer> map : wordHashMap.entrySet()) {
            System.out.println(map.getKey() + ": " + map.getValue());
        }

        System.out.println("\nDistinct Words: " + wordHashMap.size());
        System.out.println();

        String[] eventArray = new String[] {
            "login", "crash", "login", "timeout",
            "crash", "logout", "timeout", "crash",
            "retry", "login"
        };
        int[] severityArray = new int[] {
            2, 9, 2, 5,
            9, 1, 5, 9,
            5, 2
        };

        LinkedHashMap<String, Integer> eventsLinkedHashMap = new LinkedHashMap<>();
        for (int i = 0; i < eventArray.length; i++) {
            eventsLinkedHashMap.put(eventArray[i], eventsLinkedHashMap.getOrDefault(eventArray[i], 0) + 1);
        }

        System.out.print("Occurrence Counts: ");
        List<String> formattedList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry: eventsLinkedHashMap.entrySet()) {
            formattedList.add(entry.getKey() + " -> " + entry.getValue());
        }
        System.out.println(String.join(", ", formattedList));

        System.out.print("First seen order: ");
        formattedList.clear();
        for (Map.Entry<String, Integer> entry: eventsLinkedHashMap.entrySet()) {
            formattedList.add(entry.getKey());
        }
        System.out.println(String.join(", ", formattedList));
        
        Map<String, Integer> severityLookup = new HashMap<>();
        for (int i = 0; i < eventArray.length; i++) {
            severityLookup.putIfAbsent(eventArray[i], severityArray[i]);
        }
        
        Map<String, Integer> sortedMap = new TreeMap<>((a, b) -> {
            int sevA = severityLookup.get(a);
            int sevB = severityLookup.get(b);

            int severityCompare = Integer.compare(sevA, sevB);

            if (severityCompare != 0) return severityCompare;
            else return a.compareTo(b);
        });

        sortedMap.putAll(severityLookup);
        
        System.out.print("Severity Ascending Order: ");
        formattedList.clear();
        for (Map.Entry<String, Integer> entry: sortedMap.entrySet()) {
            formattedList.add(entry.getKey() + "(" + entry.getValue() + ")");
        }
        System.out.println(String.join(", ", formattedList));

        String maxEvent = null;
        int maxScore = -1;

        for (Map.Entry<String, Integer> entry: eventsLinkedHashMap.entrySet()) {
            String eventName = entry.getKey();
            int count = entry.getValue();
            int severity = severityLookup.get(eventName);

            int weightedScore = count * severity;

            if (weightedScore > maxScore) {
                maxScore = weightedScore;
                maxEvent = eventName;
            }
        }
        System.out.println("Highest weighted score: " + maxEvent + " -> " + maxScore);
    }    
}
