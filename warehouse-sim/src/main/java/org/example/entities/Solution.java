package org.example.entities;

import java.util.HashMap;
import java.util.Map;

public class Solution {
    private Map<Cargo, Cell> mapping;

    public Solution() {
        mapping = new HashMap<>();
    }

    public Solution(Solution solution) {
        mapping = new HashMap<>(solution.mapping);
        // создаем копию
    }

    public void addMapping(Cargo cargo, Cell cell) {
        mapping.put(cargo, cell);
    }

    public Map<Cargo, Cell> getMapping() {
        return mapping;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Cargo cargo : mapping.keySet()) {
            stringBuilder.append("cargo with demand: ")
                    .append(cargo.getDemand())
                    .append(" in cell with distance: ")
                    .append(mapping.get(cargo).getDistance())
                    .append("\n");
        }
        return stringBuilder.toString();
    }
}
