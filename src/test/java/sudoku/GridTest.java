package sudoku;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class GridTest {
    @Test
    void retrieveSelected() {
        String values =
                "        2,7,4,3,6,8, ,1,5," +
                        " ,5,3,1, ,7, , , ," +
                        " , ,6,4, , , , ,7," +
                        " , ,1, , , ,7, ,9," +
                        " ,4, ,9, ,3, ,6,1," +
                        "7,8, , ,1, , , , ," +
                        "3, ,5, ,4, ,1,9,8," +
                        "4, , , , , , ,2,3," +
                        " , , ,2,3,5,6, ,4";

        Grid grid = new Grid(values);

        assertEquals(2, grid.get(1, 1).value);
        assertEquals(3, grid.get(5, 6).value);
        assertEquals(4, grid.get(9, 9).value);

        System.err.println(grid);
    }

    @Test
    void retrieveAll() {
        String values =
                "        2,7,4,3,6,8, ,1,5," +
                        " ,5,3,1, ,7, , , ," +
                        " , ,6,4, , , , ,7," +
                        " , ,1, , , ,7, ,9," +
                        " ,4, ,9, ,3, ,6,1," +
                        "7,8, , ,1, , , , ," +
                        "3, ,5, ,4, ,1,9,8," +
                        "4, , , , , , ,2,3," +
                        " , , ,2,3,5,6, ,4";

        Grid grid = new Grid(values);

        for (int row = 1; row <= 9; row++) {
            for (int col = 1; col <= 9; col++) {
                assertNotNull(grid.get(row, col));
            }
        }
    }

    @Test
    void setAndGetWorks() {
        Grid grid = new Grid();

        assertEquals(0, grid.get(5,5).value);

        grid.set(5, 5, 5);
        assertEquals(5, grid.get(5,5).value);
    }
}
