package com.assessment.common;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemorySummaryProcessor {

    private final Map<String, InMemorySummary> summaryEntityMap = new ConcurrentHashMap<>();

    public InMemorySummary add(InMemorySummary entity) {
        InMemorySummary summaryEntity = summaryEntityMap.computeIfAbsent(entity.getId(),
                k -> new InMemorySummary());
        summaryEntity.setId(entity.getId());
        summaryEntity.setName(entity.getName());
        summaryEntity.setDimensions(entity.getDimensions());
        for (Map.Entry<String, Double> incomingEntry : entity.getMeasures().entrySet()) {
            Double existingVal = summaryEntity.getMeasures().getOrDefault(incomingEntry.getKey(), 0d);
            summaryEntity.setMeasureValue(incomingEntry.getKey(), (existingVal + incomingEntry.getValue()));
        }
        return summaryEntity;
    }

    public InMemorySummary get(String id) {
        return summaryEntityMap.get(id);
    }

    public InMemorySummary delete(String id) {
        return summaryEntityMap.remove(id);
    }

    public Collection<InMemorySummary> getByName(String name) {
        return summaryEntityMap.values().stream().filter(e -> e.getName().equals(name)).collect(Collectors.toList());
    }

    public Collection<InMemorySummary> getAll() {
        return summaryEntityMap.values();
    }
}
