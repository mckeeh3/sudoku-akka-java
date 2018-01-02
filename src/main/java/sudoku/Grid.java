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
            return String.format("%s(%d, %d, %d)", getClass().getSimpleName(), row, col, value);
        }
    }

    static class Cells {
        final ArrayList<Cell> cells;

        Cells() {
            cells = new ArrayList<>();
        }
    }

    private Cells cells;

    Grid(String values) {
        if (values == null || values.trim().isEmpty()) {
            throw new IllegalArgumentException("Must provide grid values.");
        }

        String[] cellValues = values.split(",");
        if (cellValues.length < 9 * 9) {
            throw new IllegalArgumentException(String.format("Must provide %s comma delimited grid cell values", 9 * 9));
        }

        cells = new Cells();

        for (int row = 1; row <= 9; row++) {
            for (int col = 1; col <= 9; col++) {
                cells.cells.add(new Cell(row, col, cellValue(row, col, cellValues)));
            }
        }
    }

    Cell cell(int row, int col) {
        return cells.cells.get((row - 1) * 9 + col - 1);
    }

    private int cellValue(int row, int col, String[] cellValues) {
        String cellValue = cellValues[(row - 1) * 9 + col - 1];
        cellValue = cellValue.trim().isEmpty() ? "0" : cellValue.trim();

        return Integer.valueOf(cellValue);
    }
}
