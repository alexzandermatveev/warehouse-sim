package org.example.entities;

import lombok.Data;

@Data
public class Cargo implements Cloneable {
    private final int length;
    private final int width;
    private final int height;
    private final int demand;
    private final double volume;

    public static Cargo[][] getRandomCargos(int size) {
        Cargo[][] cargos = new Cargo[size][size];
        for (int i = 0; i < cargos.length; i++) {
            for (int j = 0; j < cargos[0].length; j++) {
                int a = (int) Math.round(Math.random() * 100);
                int b = (int) Math.round(Math.random() * 100);
                int c = (int) Math.round(Math.random() * 100);
                cargos[i][j] = new Cargo(a, b, c,
                        (int) Math.round(Math.random() * 10),
                        getVolume(a, b, c));
            }
        }
        return cargos;
    }

    public static double getVolume(int length, int width, int height) {
        return length * width * height;
    }

    @Override
    public Cargo clone() {
        try {
            return (Cargo) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
