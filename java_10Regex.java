import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class java_10Regex {
    public static void main(String[] args) {
        String[] productCodes = new String[] {
            "AB-1234-XYZ", "ab-1234-XYZ",
            "AB-123-XYZ", "AB-1234-XY",
            "AB-1234-XYZZ", "AB1234XYZ"
        };

        Pattern producPattern = Pattern.compile("^[A-Z]{2}-[0-9]{4}-[A-Z]{3}$");
        for (String code : productCodes) {
            Matcher m = producPattern.matcher(code);
            System.out.println(code + " -> " + m.matches());
        }

        System.out.println();
        String log = """
[2026-08-10 09:15:22] ERROR user=alice code=E502 msg=connection refused
[2026-08-10 09:16:03] INFO user=bob code=I100 msg=login successful
random line that is not a log entry at all
[2026-08-10 09:17:45] ERROR user=carol code=E404 msg=resource not found
[2026-08-10 09:18:12] WARN user=alice code=W301 msg=slow response
[2026-08-10 09:19:59] ERROR user=bob code=E502 msg=connection refused
not a log entry either, just noise
[2026-08-10 09:21:30] INFO user=dave code=I100 msg=login successful
        """;

        String regex = "^\\[([^\\]]+)\\]\\s+([A-Z]+)\\s+user=(\\w+)\\s+code=(\\w+)\\s+msg=(.+)$";
        Pattern logPattern = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher logMatcher = logPattern.matcher(log);

        int totalValidEntries = 0;
        Map<String, Integer> levelCounts = new LinkedHashMap<>();
        Map<String, Integer> userErrorCounts = new LinkedHashMap<>();
        Map<String, Integer> codeCounts = new HashMap<>();
        
        while(logMatcher.find()) {
            totalValidEntries++;

            // String timestamp = logMatcher.group(1); // Not used, but extracted
            String level = logMatcher.group(2);
            String user = logMatcher.group(3);
            String code = logMatcher.group(4);
            // String message = logMatcher.group(5); // Not used, but extracted

            levelCounts.merge(level, 1, Integer::sum);
            codeCounts.merge(code, 1, Integer::sum);
            if ("ERROR".equals(level)) userErrorCounts.merge(user, 1, Integer::sum);
        }

        String mostFrequentCode = null;
        int maxCodeCount = 0;

        for (Map.Entry<String, Integer> entry: codeCounts.entrySet()) {
            if (entry.getValue() > maxCodeCount) {
                maxCodeCount = entry.getValue();
                mostFrequentCode = entry.getKey();
            }
        }

        System.out.println("Total number of valid log entries found: " + totalValidEntries);
        
        System.out.println();
        System.out.println("A count of entries per level: ");
        for (Map.Entry<String, Integer> entry: levelCounts.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }

        System.out.println();
        System.out.println("ERROR-level entries for each user: ");
        for (Map.Entry<String, Integer> entry: userErrorCounts.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }

        System.out.println("\nMost Frequent Code: " + mostFrequentCode + " (appeared " + maxCodeCount + " times)");
    }
}
