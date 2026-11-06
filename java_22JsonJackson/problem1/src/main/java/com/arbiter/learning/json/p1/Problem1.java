package com.arbiter.learning.json.p1;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Problem1 {
    public record Device(String name, int port, boolean enabled) {}

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        Device dev = new Device("gateway", 8080, true);
        
        // Serialize
        String json = mapper.writeValueAsString(dev);
        System.out.println(json);
        
        // Deserialize
        Device parsed = mapper.readValue(json, Device.class);
        System.out.println(parsed.port());
    }
}