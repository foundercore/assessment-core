package com.assessment.common;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class InMemorySummary {
    private String id;
    private String name;
    private Map<String, String> dimensions = new TreeMap<>();
    private Map<String, Double> measures = new HashMap<>();

    private Map<String, InMemorySummary> summaryEntityMap = new ConcurrentHashMap<>();

    public String getId() {
        if (id == null) {
            id = createId();
        }
        return id;
    }

    public void setMeasureValue(String k, Double v) {
        measures.put(k, v);
    }

    public void setDimensionValue(String k, String v) {
        dimensions.put(k, v);
    }

    private String createId() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        for (Map.Entry<String, String> entry : new TreeMap<>(dimensions).entrySet()) {
           sb.append("-").append(entry.getKey()).append(":").append(entry.getValue());
        }
        return String.valueOf(sb.toString().hashCode());
    }
}
