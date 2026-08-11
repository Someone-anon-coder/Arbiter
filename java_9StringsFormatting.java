public class java_9StringsFormatting {
    public static void main(String[] args) {
        String sentence = "the   QUICK brown foX   jumped over    the Lazy dog";

        sentence = sentence.replaceAll("\\s{2,}", " ");
        sentence = sentence.trim();
        String[] words = sentence.split(" ");
        
        int characters = 0;
        for (int i = 0; i < words.length; i++) {
            String word = words[i].substring(0, 1).toUpperCase() + words[i].substring(1).toLowerCase();
            
            words[i] = word;
            characters += words[i].length();
        }

        System.out.println(String.join(" ", words));
        String summary = String.format("Summary: %d words, %d characters (excluding spaces)\n", words.length, characters);
        System.out.println(summary);

        StringBuilder value = new StringBuilder("a1b22c333d4e55f6g777h8i99j0");
        
        int originalLength = value.length();
        int insertedMarkers = 0;
        int deletedDigits = 0;

        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);

            if (Character.isLowerCase(ch)) {
                value.insert(i + 1, '*');
                insertedMarkers++;
            } else if (Character.isDigit(ch)) {
                if (i > 0 && Character.isDigit(value.charAt(i - 1))) {
                    value.deleteCharAt(i);
                    
                    deletedDigits++;
                    i--;
                }
            }
        }

        System.out.println(value.toString());
        
        String report = String.format("Report: original length %d, final length %d, %d markers inserted, %d digits deleted", originalLength, value.length(), insertedMarkers, deletedDigits);
        System.out.println(report);
    }
}
