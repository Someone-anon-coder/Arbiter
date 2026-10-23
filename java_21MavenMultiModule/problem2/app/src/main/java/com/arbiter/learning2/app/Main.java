package com.arbiter.learning2.app;

import com.arbiter.learning2.core.CoreClass;
import com.arbiter.learning2.service.ServiceClass;
import org.apache.commons.lang3.StringUtils;

public class Main {
    public static void main(String[] args) {
        String input = " hello multi module ";
        System.out.println(CoreClass.shout(input));
        System.out.println(ServiceClass.process(input));
        System.out.println(StringUtils.countMatches(input.trim(), "l"));
        System.out.println(StringUtils.reverse("Hello"));
    }
}