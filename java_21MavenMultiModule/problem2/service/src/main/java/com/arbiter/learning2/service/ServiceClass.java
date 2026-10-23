package com.arbiter.learning2.service;

import org.apache.commons.text.WordUtils;

public class ServiceClass {
    public static String process(String input) {
        return WordUtils.capitalizeFully(input.trim());
    }
}