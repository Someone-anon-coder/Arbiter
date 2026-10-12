package com.arbiter.learning.textkit;
import org.apache.commons.lang3.StringUtils;

public class App {
    public static void main(String[] args) {
        String[] inputs = {"  arbiter  ", null, "", "   ", "execution-engine", "\tqueue\n"};
        int total = inputs.length;
        int nonBlank = 0;

        for (int i = 0; i < total; i++) {
            String input = inputs[i];
            if (StringUtils.isBlank(input)) {
                System.out.println("[" + i + "] <blank>");
            } else {
                nonBlank++;
                String trimmed = StringUtils.trim(input);
                int len = trimmed.length();
                String capitalized = StringUtils.capitalize(trimmed);
                String abbreviated = StringUtils.abbreviate(capitalized, 10);
                System.out.println("[" + i + "] " + abbreviated + " (len=" + len + ")");
            }
        }
        System.out.println("non-blank: " + nonBlank + " of " + total);
    }
}