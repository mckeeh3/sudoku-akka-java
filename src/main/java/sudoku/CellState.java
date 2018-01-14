package sudoku;

import java.io.Serializable;

interface CellState {
    class Invalid implements Serializable {
        final int row;
        final int col;

        Invalid(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public String toString() {
            return String.format("%s[(%d, %d)]", getClass().getSimpleName(), row, col);
        }
    }

    class NoChange implements Serializable {
        final SetCell setCell;

        NoChange(SetCell setCell) {
            this.setCell = setCell;
        }

        @Override
        public String toString() {
            return String.format("%s[%s]", getClass().getSimpleName(), setCell);
        }
    }
}
