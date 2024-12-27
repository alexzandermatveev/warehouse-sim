package org.example.optimisation;

import org.example.entities.Cargo;
import org.example.entities.Cell;
import org.example.entities.Solution;

import java.util.*;
import java.util.stream.Collectors;

public class Optimisation {
    public static double rankAndPlaceWithTopsis(Cell[][] cells, Cargo[][] cargos) {
        // 1 группируем ячейки по категориям
        TreeMap<String, List<Cell>> categorizedCells = new TreeMap<>(categorizeCells(cells));

        double total = 0;

        // 2 ранжируем товары для каждой категории
        for (String category : categorizedCells.keySet()) {
            List<Cell> availableCells = categorizedCells.get(category)
                    .stream()
                    .filter(Cell::isEmpty)
                    .toList();

            // собираем товары, которые будут размещены в этой категории
            List<Cargo> cargosForCategory = collectCargosForCategory(cargos, category);

            // проверяем размеры списков и добавляем фиктивные ячейки, если нужно
            availableCells = adjustAvailableCells(availableCells, cargosForCategory.size(), cells);

            // ранжируем товары с помощью TOPSIS
            rankCargosWithTopsis(availableCells, cargosForCategory);

            // размещаем товары
            total += placeCargosInCells(availableCells, cargosForCategory);
        }

        System.out.println("Итоговое значение ЦФ с TOPSIS: " + total);
        return total;
    }

    private static List<Cell> adjustAvailableCells(List<Cell> availableCells, int requiredSize, Cell[][] allCells) {
        List<Cell> result = new ArrayList<>(availableCells);

        if (result.size() < requiredSize) {
            // поиск дополнительных пустых ячеек
            for (Cell[] row : allCells) {
                Arrays.sort(row); // сортировка, чтобы сначала были с меньшим расстоянием
                for (Cell cell : row) {
                    if (cell.isEmpty() && !result.contains(cell)) {
                        result.add(cell);
                        if (result.size() >= requiredSize) {
                            break;
                        }
                    }
                }
                if (result.size() >= requiredSize) {
                    break;
                }
            }

            // если недостаточно пустых ячеек, добавляем фиктивные
//            while (result.size() < requiredSize) {
//                result.add(new Cell(true, Integer.MAX_VALUE, "D"));
//            }
        }
        return result;
    }


    // группируем ячейки по категориям
    private static Map<String, List<Cell>> categorizeCells(Cell[][] cells) {
        Map<String, List<Cell>> categories = new HashMap<>();
        for (Cell[] row : cells) {
            Arrays.sort(row);
            for (Cell cell : row) {
                if (!categories.containsKey(cell.getCategory())) {
                    categories.put(cell.getCategory(), new ArrayList<>());
                }
                categories.get(cell.getCategory()).add(cell);
            }
        }
        return categories;
    }

    // собираем товары для конкретной категории
    private static List<Cargo> collectCargosForCategory(Cargo[][] cargos, String category) {
        ArrayList<Cargo> cargosList = new ArrayList<>();
        int maxDemand = Arrays.stream(cargos)
                .map(mas -> Arrays.stream(mas).max(Comparator.comparing(Cargo::getDemand)).get())
                .max(Comparator.comparing(Cargo::getDemand))
                .get()
                .getDemand();

        for (Cargo[] row : cargos) {
            for (Cargo cargo : row) {

                double rate = (double) cargo.getDemand() / maxDemand;
                switch (category) {
                    case "A":
                        if (rate > 0.7) {
                            cargosList.add(cargo);
                        }
                        break;
                    case "B":
                        if (rate <= 0.7 && rate > 0.3) {
                            cargosList.add(cargo);
                        }
                        break;
                    case "C":
                        if (rate <= 0.3) {
                            cargosList.add(cargo);
                        }
                        break;
                }
            }
        }
        // например, товары с большим спросом - ближе
        return cargosList;
    }

    private static double[][] prepareCriteriaMatrix(List<Cargo> cargos, List<Cell> cells) {
        double[][] matrix = new double[cargos.size()][3]; // 3 критерия: спрос, расстояние, объем
        for (int i = 0; i < cargos.size(); i++) {
            Cargo cargo = cargos.get(i);
            Cell cell = null;
            try {
                cell = cells.get(i); // предполагаем соответствие индексам
            } catch (Exception e) {
                System.out.printf("cargo size: %d\n cells size: %d\n i=%d", cargos.size(), cells.size(), i);
                throw e;
            }
            matrix[i][0] = cargo.getDemand();
            matrix[i][1] = cell.getDistance();
            matrix[i][2] = cargo.getVolume();
        }
        return matrix;
    }

    private static double[][] normalizeMatrix(double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[][] normalized = new double[rows][cols];

        for (int j = 0; j < cols; j++) {
            double sumOfSquares = 0;
            for (int i = 0; i < rows; i++) {
                sumOfSquares += Math.pow(matrix[i][j], 2);
            }
            double normFactor = Math.sqrt(sumOfSquares);
            for (int i = 0; i < rows; i++) {
                normalized[i][j] = matrix[i][j] / normFactor;
            }
        }
        return normalized;
    }

    private static double[][] applyWeights(double[][] normalizedMatrix, double[] weights) {
        int rows = normalizedMatrix.length;
        int cols = normalizedMatrix[0].length;
        double[][] weightedMatrix = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                weightedMatrix[i][j] = normalizedMatrix[i][j] * weights[j];
            }
        }
        return weightedMatrix;
    }

    private static double[] calculateIdealSolution(double[][] weightedMatrix) {
        int cols = weightedMatrix[0].length;
        double[] idealSolution = new double[cols];

        for (int j = 0; j < cols; j++) {
            double max = Double.MIN_VALUE;
            for (double[] row : weightedMatrix) {
                max = Math.max(max, row[j]);
            }
            idealSolution[j] = max;
        }
        return idealSolution;
    }

    private static double[] calculateAntiIdealSolution(double[][] weightedMatrix) {
        int cols = weightedMatrix[0].length;
        double[] antiIdealSolution = new double[cols];

        for (int j = 0; j < cols; j++) {
            double min = Double.MAX_VALUE;
            for (double[] row : weightedMatrix) {
                min = Math.min(min, row[j]);
            }
            antiIdealSolution[j] = min;
        }
        return antiIdealSolution;
    }

    private static double calculateDistance(double[] vector, double[] solution) {
        double sum = 0;
        for (int i = 0; i < vector.length; i++) {
            sum += Math.pow(vector[i] - solution[i], 2);
        }
        return Math.sqrt(sum);
    }


    // ранжируем товары с помощью TOPSIS
    private static void rankCargosWithTopsis(List<Cell> cells, List<Cargo> cargos) {
        // 1 подготовка данных (матрица критериев)
        double[][] criteriaMatrix = prepareCriteriaMatrix(cargos, cells);

        // 2 нормализация критериев
        double[][] normalizedMatrix = normalizeMatrix(criteriaMatrix);

        // 3 применение весов
        double[] weights = {0.5, 0.3, 0.2}; // Например: спрос, расстояние, объем
        double[][] weightedMatrix = applyWeights(normalizedMatrix, weights);

        // 4 вычисление идеального и анти-идеального решений
        double[] idealSolution = calculateIdealSolution(weightedMatrix);
        double[] antiIdealSolution = calculateAntiIdealSolution(weightedMatrix);

        // 5 расчет расстояний до идеала и анти-идеала
        Map<Cargo, Double> closenessIndex = new HashMap<>();
        for (int i = 0; i < cargos.size(); i++) {
            double distanceToIdeal = calculateDistance(weightedMatrix[i], idealSolution);
            double distanceToAntiIdeal = calculateDistance(weightedMatrix[i], antiIdealSolution);

            double closeness = distanceToAntiIdeal / (distanceToIdeal + distanceToAntiIdeal);
            closenessIndex.put(cargos.get(i), closeness);
        }

        // 6 сортировка товаров по индексу близости
        cargos.sort((c1, c2) -> Double.compare(closenessIndex.get(c2), closenessIndex.get(c1)));
//        System.out.println("Товары ранжированы по TOPSIS:");
//        cargos.forEach(cargo -> System.out.println("Cargo hashCode: " + cargo.hashCode() + ", Closeness: " + closenessIndex.get(cargo)));
    }


    // размещаем товары в ячейки
    private static double placeCargosInCells(List<Cell> cells, List<Cargo> cargos) {
        double sum = 0;
        for (int i = 0; i < Math.min(cells.size(), cargos.size()); i++) {
            Cell cell = cells.get(i);
            Cargo cargo = cargos.get(i);
            cell.setEmpty(false);
//            логи
//            System.out.println("Размещен товар с спросом " + cargo.getDemand() + " в ячейке с расстоянием " + cell.getDistance());
            sum += cell.getDistance() * cargo.getDemand();
            cell.setEmpty(false);
        }
        return sum;
    }


    public static double electreTri(Cell[][] cells, Cargo[][] cargos) {
        double totalScore = 0;

        // установка порогов
        double indifferenceThreshold = 10; // порог безразличия
        double preferenceThreshold = 20;  // порог предпочтения
        double vetoThreshold = 50;        // порог вето

        for (int i = 0; i < cargos.length; i++) {
            for (int j = 0; j < cargos[0].length; j++) {
                Cargo cargo = cargos[i][j];
                Cell bestCell = findBestCell(cells, cargo, indifferenceThreshold, preferenceThreshold, vetoThreshold);
                if (bestCell != null) {
                    totalScore += cargo.getDemand() * bestCell.getDistance();
                }
            }
        }

        System.out.println("Итоговое значение ЦФ при ELECTRE TRI: " + totalScore);
        return totalScore;
    }

    // поиск лучшей ячейки
    private static Cell findBestCell(Cell[][] cells, Cargo cargo, double indifference, double preference, double veto) {
        Cell bestCell = null;
        double bestScore = Double.MAX_VALUE;

        for (Cell[] row : cells) {
            for (Cell cell : row) {
                if (!cell.isEmpty()) {
                    continue; // пропускаем занятые ячейки
                }

                double distanceDifference = Math.abs(cell.getDistance() - idealDistance(cargo));

                // проверяем пороги
                if (distanceDifference > veto) {
                    continue; // Исключаем ячейку из рассмотрения
                }

                if (distanceDifference <= preference) {
                    // предпочтительная ячейка
                    double score = calculateScore(cell, cargo, distanceDifference);
                    if (score < bestScore) {
                        bestScore = score;
                        bestCell = cell;
                    }
                }
            }
        }

        return bestCell;
    }

    // идеальное расстояние для товара (можно настроить на основе других данных)
    private static double idealDistance(Cargo cargo) {
        return cargo.getDemand() * 10; // Пример: чем больше спрос, тем ближе нужно разместить
    }

    // расчет оценки ячейки (можно усложнить с учетом других критериев)
    private static double calculateScore(Cell cell, Cargo cargo, double distanceDifference) {
        return distanceDifference + cell.getDistance(); // разница в расстоянии + объем
    }


    public static double integrateElectreTriAndTopsis(Cell[][] cells, Cargo[][] cargos) {
        // разделение ячеек на классы с помощью ELECTRE TRI
        TreeMap<String, List<Cell>> categorizedCells = new TreeMap<>();
        double total = 0;
        for (Cargo[] row : cargos) {
            for (Cargo cargo : row) {
                Cell bestCell = findBestCell(cells, cargo, 10, 20, 50);
                if (bestCell != null) {
                    String category = bestCell.getCategory(); // категория ячейки
                    categorizedCells.computeIfAbsent(category, k -> new ArrayList<>()).add(bestCell);
                }
            }
        }

        // ранжирование товаров с помощью TOPSIS внутри каждого класса
        for (Map.Entry<String, List<Cell>> entry : categorizedCells.entrySet()) {
            String category = entry.getKey();
            List<Cell> categoryCells = entry.getValue().stream()
                    .filter(Cell::isEmpty)
                    .toList();

            // собираем товары для категории
            List<Cargo> cargosForCategory = collectCargosForCategory(cargos, category);

            // проверяем паритет кол-ва ячеек и товара, добавляем ячейки если нужно
            ArrayList<Cell> availableCells = new ArrayList<>(adjustAvailableCells(categoryCells, cargosForCategory.size(), cells));

            // ранжируем товары с помощью TOPSIS
            rankCargosWithTopsis(availableCells, cargosForCategory);

            // размещаем товары
            total += placeCargosInCells(availableCells, cargosForCategory);
        }
        System.out.println("Итоговое значение ЦФ при TOPSIS + ELECTRE TRI: " + total);
        return total;
    }


    public static double bacterialMemeticAlgorithm(Cell[][] cells, Cargo[][] cargos) {
        int iterations = 300_000; // количество итераций
        double totalScore = Double.MAX_VALUE;

        // генерация начального решения
        Solution bestSolution = generateInitialSolution(cells, cargos);

        for (int i = 0; i < iterations; i++) {
            // мутация
            Solution mutatedSolution = bacterialMutation(bestSolution, 0.2); // мутация 20%

            // локальный поиск
            Solution improvedSolution = localSearch(mutatedSolution);

            // генетический перенос с другим случайным решением
            Solution otherSolution = generateInitialSolution(cells, cargos);
            Solution finalSolution = geneTransfer(improvedSolution, otherSolution, 0.1); // перенос 10%

            // обновление лучшего решения
            double score = calculateObjectiveFunction(finalSolution);
            if (score < totalScore) {
                totalScore = score;
                bestSolution = finalSolution;
            }
        }
        System.out.println(bestSolution);
        return totalScore;
    }


    // генерация начального решения
    private static Solution generateInitialSolution(Cell[][] cells, Cargo[][] cargos) {
        Solution solution = new Solution();
        List<Cell> allCells = Arrays.stream(cells)
                .flatMap(Arrays::stream)
                .filter(Cell::isEmpty)
                .collect(Collectors.toList());

        List<Cargo> allCargos = Arrays.stream(cargos)
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());

        Collections.shuffle(allCells); // перемешиваем ячейки
        Collections.shuffle(allCargos); // перемешиваем грузы

        for (int i = 0; i < Math.min(allCells.size(), allCargos.size()); i++) {
            solution.addMapping(allCargos.get(i), allCells.get(i));
        }

        return solution;
    }


    // локальный поиск
    private static Solution localSearch(Solution solution) {
        Solution bestSolution = new Solution(solution); // создаем копию текущего решения
        double bestScore = calculateObjectiveFunction(bestSolution);

        Map<Cargo, Cell> mapping = new HashMap<>(solution.getMapping());
        List<Cargo> cargos = new ArrayList<>(mapping.keySet());
        Random random = new Random();

        // ограничиваем количество перестановок (например, до 20)
        int maxNeighbors = 20;
        for (int n = 0; n < maxNeighbors; n++) {
            // случайно выбираем два груза
            int i = random.nextInt(cargos.size());
            int j = random.nextInt(cargos.size());
            while (i == j) {
                j = random.nextInt(cargos.size());
            }

            // меняем местами ячейки двух грузов
            Cell temp = mapping.get(cargos.get(i));
            mapping.put(cargos.get(i), mapping.get(cargos.get(j)));
            mapping.put(cargos.get(j), temp);

            // проверяем новое решение
            if (!containsDuplicateCells(mapping)) { // проверяем уникальность ячеек
                Solution newSolution = new Solution();
                newSolution.getMapping().putAll(mapping);
                double newScore = calculateObjectiveFunction(newSolution);

                if (newScore < bestScore) {
                    bestScore = newScore;
                    bestSolution = newSolution;
                } else {
                    // возвращаем обратно, если не улучшили
                    mapping.put(cargos.get(j), mapping.get(cargos.get(i)));
                    mapping.put(cargos.get(i), temp);
                }
            }
        }

        return bestSolution;
    }

    // проверка уникальности ячеек
    private static boolean containsDuplicateCells(Map<Cargo, Cell> mapping) {
        Set<UUID> uniqueIds = new HashSet<>();
        for (Cell cell : mapping.values()) {
            if (!uniqueIds.add(cell.getUuid())) {
                return true; // найден дубликат
            }
        }
        return false;
    }


    // целевая функция
    private static double calculateObjectiveFunction(Solution solution) {
        double total = 0;

        for (Map.Entry<Cargo, Cell> entry : solution.getMapping().entrySet()) {
            Cargo cargo = entry.getKey();
            Cell cell = entry.getValue();

            // расстояние * спрос как критерий
            total += cargo.getDemand() * cell.getDistance();
        }

        return total;
    }

    private static Solution bacterialMutation(Solution solution, double mutationRate) {
        Solution mutated = new Solution(solution); // создаем копию текущего решения
        Map<Cargo, Cell> mapping = mutated.getMapping();
        List<Cargo> cargos = new ArrayList<>(mapping.keySet());
        Random random = new Random();

        // определяем количество генов для мутации
        int genesToMutate = (int) (mutationRate * cargos.size());

        for (int n = 0; n < genesToMutate; n++) {
            // выбираем два случайных груза
            int i = random.nextInt(cargos.size());
            int j = random.nextInt(cargos.size());
            while (i == j) { // убедимся, что выбраны разные грузы
                j = random.nextInt(cargos.size());
            }

            // меняем их местами
            Cargo cargo1 = cargos.get(i);
            Cargo cargo2 = cargos.get(j);

            Cell cell1 = mapping.get(cargo1);
            Cell cell2 = mapping.get(cargo2);

            mapping.put(cargo1, cell2);
            mapping.put(cargo2, cell1);
        }

        return mutated;
    }

    private static Solution geneTransfer(Solution solution, Solution otherSolution, double transferRate) {
        Solution updatedSolution = new Solution(solution); // создаем копию текущего решения
        Map<Cargo, Cell> mapping = updatedSolution.getMapping();
        Map<Cargo, Cell> otherMapping = otherSolution.getMapping();

        List<Cargo> cargos = new ArrayList<>(mapping.keySet());
        Random random = new Random();

        // определяем количество грузов для переноса
        int genesToTransfer = (int) (transferRate * cargos.size());

        for (int n = 0; n < genesToTransfer; n++) {
            // выбираем случайный груз
            int i = random.nextInt(cargos.size());
            Cargo cargo = cargos.get(i);

            // находим ячейку для этого груза в другом решении
            Cell otherCell = otherMapping.get(cargo);

            if (otherCell != null) {
                // найти, какой груз использует эту ячейку в текущем решении
                Cargo cargoUsingOtherCell = mapping.entrySet().stream()
                        .filter(entry -> entry.getValue().equals(otherCell))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(null);

                if (cargoUsingOtherCell != null) {
                    // меняем местами ячейки для двух грузов
                    Cell currentCell = mapping.get(cargo);
                    mapping.put(cargo, otherCell);
                    mapping.put(cargoUsingOtherCell, currentCell);
                }
            }
        }

        return updatedSolution;
    }


}
