package sudoku;

class Cell {
    final int row;
    final int col;
    final int value;

    Cell(int row, int col, int value) {
        this.row = row;
        this.col = col;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("%s[(%d, %d) %d]", getClass().getSimpleName(), row, col, value);
    }
}
