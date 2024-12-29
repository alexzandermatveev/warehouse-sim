package org.example.entities;

import lombok.*;

import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
@Data
public class Cell implements Cloneable, Comparable<Cell>{

    @NonNull
    private boolean empty;
    @NonNull
    private int distance;
    @NonNull
    private String category; // Класс ячейки ("близкий", "средний", "дальний")
    @Getter
    private final UUID uuid = UUID.randomUUID();



    public static Cell[][] getRandomCells(int size) {
        Cell[][] cells = new Cell[size][size];
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                int distance = (int) Math.round(Math.random() * 100);
                cells[i][j] = new Cell(true, distance, classifyDistance(distance));
            }
        }
        return cells;
    }

    private static String classifyDistance(int distance) {
        if (distance <= 30) {
            return "A";
        } else if (distance <= 70) {
            return "B";
        } else {
            return "C";
        }
    }

    @Override
    public Cell clone() {
        try {
            return (Cell) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public int compareTo(Cell anotherCell) {
        return Integer.compare(distance, anotherCell.distance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isEmpty(), getDistance(), getCategory(), getUuid());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cell cell = (Cell) o;
        return isEmpty() == cell.isEmpty() && getDistance() == cell.getDistance() && Objects.equals(getCategory(), cell.getCategory()) && Objects.equals(getUuid(), cell.getUuid());
    }
}
