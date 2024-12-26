public class Main {
    public static void main(String[] args) {
        Cell[][] cells = Cell.getRandomCells(10);
        Cargo[][] cargos = Cargo.getRandomCargos(10);
        System.out.println("всего затрачено: " + randomisePlace(cells, cargos));


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
                while (cells[randomCell1][randomCell2].isEmpty());

                total += cargo2.getDemand() * cells[randomCell1][randomCell2].getDistance();
            }
        }
        return total;
    }
}
