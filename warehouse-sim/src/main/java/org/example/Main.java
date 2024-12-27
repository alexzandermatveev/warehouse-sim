package org.example;

import org.example.entities.Cargo;
import org.example.entities.Cell;
import org.example.optimisation.Optimisation;

public class Main {
    public static void main(String[] args) {
        double randomScore = 0;
        double electreTriScore = 0;
        double topsisScore = 0;
        double topsisAndTriScore = 0;

        if(false) {
            for (int i = 0; i < 100; i++) {
                Cell[][] cells = Cell.getRandomCells(10);
                Cargo[][] cargos = Cargo.getRandomCargos(10);

                randomScore += randomisePlace(cloneCell(cells), cloneCargo(cargos));
                electreTriScore += Optimisation.electreTri(cloneCell(cells), cloneCargo(cargos));
                topsisScore += Optimisation.rankAndPlaceWithTopsis(cloneCell(cells), cloneCargo(cargos));
                topsisAndTriScore += Optimisation.integrateElectreTriAndTopsis(cloneCell(cells), cloneCargo(cargos));
            }

            System.out.printf("Итого (среднее за 100 прогонов):%n random: %,.2f%n ELECTRE TRI: %,.2f%n TOPSIS: %,.2f%n ELECTRE TRI + TOPSIS: %,.2f",
                    randomScore / 100, electreTriScore / 100, topsisScore / 100, topsisAndTriScore / 100);

        }
        Cell[][] cells = Cell.getRandomCells(10);
        Cargo[][] cargos = Cargo.getRandomCargos(10);

        double bmaScore = Optimisation.bacterialMemeticAlgorithm(cells, cargos);
        System.out.printf("Итого для BMA:%n%,.2f", bmaScore);

    }

    public static double randomisePlace(Cell[][] cells, Cargo[][] cargos) {
        double total = 0;
        int randomCell1;
        int randomCell2;
        for (Cargo[] cargo1 : cargos) {
            for (Cargo cargo2 : cargo1) {
                do {
                    randomCell1 = (int) Math.round(Math.random() * cells.length) - 1;
                    randomCell2 = (int) Math.round(Math.random() * cells.length) - 1;

                    randomCell1 = randomCell1 < 0 ? randomCell1 + 1 : randomCell1;
                    randomCell2 = randomCell2 < 0 ? randomCell2 + 1 : randomCell2;

                }
                while (!cells[randomCell1][randomCell2].isEmpty());
                cells[randomCell1][randomCell2].setEmpty(false);
                total += cargo2.getDemand() * cells[randomCell1][randomCell2].getDistance();
            }
        }
        System.out.println("Значение ЦФ при случайном назначении: " + total);
        return total;
    }

    public static Cell[][] cloneCell(Cell[][] array) {
        Cell[][] newArray = new Cell[array.length][array[0].length];
        for (int i = 0; i < newArray.length; i++) {
            for (int j = 0; j < newArray[i].length; j++) {
                newArray[i][j] = array[i][j].clone();
            }
        }
        return newArray;
    }


    public static Cargo[][] cloneCargo(Cargo[][] array) {
        Cargo[][] newArray = new Cargo[array.length][array[0].length];
        for (int i = 0; i < newArray.length; i++) {
            for (int j = 0; j < newArray[i].length; j++) {
                newArray[i][j] = array[i][j].clone();
            }
        }
        return newArray;
    }

}
