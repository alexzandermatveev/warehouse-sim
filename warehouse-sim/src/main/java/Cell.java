import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Cell {

    private boolean empty;
    private int distance;

    public static Cell[][] getRandomCells(int size) {
        Cell[][] cells = new Cell[size][size];
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                cells[i][j] = new Cell(false, (int) Math.round(Math.random() * 100));
            }
        }
        return cells;
    }
}
