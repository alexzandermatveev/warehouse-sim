import lombok.Data;

@Data
public class Cargo {
    private final int length;
    private final int width;
    private final int height;
    private final int demand;

    public static Cargo[][] getRandomCargos(int size) {
        Cargo[][] cargos = new Cargo[size][size];
        for (int i = 0; i < cargos.length; i++) {
            for (int j = 0; j < cargos[0].length; j++) {
                cargos[i][j] = new Cargo((int) Math.round(Math.random() * 100),
                        (int) Math.round(Math.random() * 100),
                        (int) Math.round(Math.random() * 100),
                        (int) Math.round(Math.random() * 10));
            }
        }
        return cargos;
    }

}
