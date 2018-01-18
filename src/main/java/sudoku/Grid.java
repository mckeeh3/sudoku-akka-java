package sudoku;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

class Grid implements Serializable {
    static class Cell {
        final int row;
        final int col;
        final int value;

        Cell(int row, int col, int value) {
            this.row = row;
            this.col = col;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Cell cell = (Cell) o;
            return row == cell.row &&
                    col == cell.col &&
                    value == cell.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, col, value);
        }

        @Override
        public String toString() {
            return String.format("%s[(%d, %d) = %d]", getClass().getSimpleName(), row, col, value);
        }
    }

    static class Cells {
        private final ArrayList<Cell> cells;

        Cells() {
            cells = new ArrayList<>();
        }

        void add(Cell cell) {
            cells.add(cell);
        }

        Cell get(int row, int col) {
            return cells.get((row - 1) * 9 + col - 1);
        }

        void set(int row, int col, int value) {
            Cell cell = get(row, col);
            cells.replaceAll(c -> cell.equals(c) ? new Cell(row, col, value) : c);
        }
    }

    private Cells cells;

    Grid() {
        cells = new Cells();

        for (int row = 1; row <= 9; row++) {
            for (int col = 1; col <= 9; col++) {
                cells.add(new Cell(row, col, 0));
            }
        }
    }

    Grid(String values) {
        if (values == null || values.trim().isEmpty()) {
            throw new IllegalArgumentException("Must provide grid values.");
        }

        String[] cellValues = values.split(",");
        if (cellValues.length < 9 * 9) {
            throw new IllegalArgumentException(String.format("Must provide %s comma delimited grid get values", 9 * 9));
        }

        cells = new Cells();

        for (int row = 1; row <= 9; row++) {
            for (int col = 1; col <= 9; col++) {
                cells.add(new Cell(row, col, cellValue(row, col, cellValues)));
            }
        }
    }

    void set(int row, int col, int value) {
        cells.set(row, col, value);
    }

    Cell get(int row, int col) {
        return cells.get(row, col);
    }

    private int cellValue(int row, int col, String[] cellValues) {
        String cellValue = cellValues[(row - 1) * 9 + col - 1];
        cellValue = cellValue.trim().isEmpty() ? "0" : cellValue.trim();

        return Integer.valueOf(cellValue);
    }

    @Override
    public String toString() {
        String delimiter = "| ";
        StringBuilder grid = new StringBuilder();
        for (int row = 1; row <= 9; row++) {
            grid.append("-------------------------------------\n");
            for (int col = 1; col <= 9; col++) {
                int value = get(row, col).value;
                grid.append(delimiter).append(value == 0 ? " " : value);
                delimiter = " | ";
            }
            grid.append(" |\n");
            delimiter = "| ";
        }
        grid.append("-------------------------------------\n");

        return grid.toString();
    }
}
