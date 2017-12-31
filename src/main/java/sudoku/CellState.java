package sudoku;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

interface CellState {
    class Assigned implements Serializable {
        final int row;
        final int col;
        final int value;
        final String who;

        Assigned(int row, int col, int value, String who) {
            this.row = row;
            this.col = col;
            this.value = value;
            this.who = who;
        }

        @Override
        public String toString() {
            return String.format("%s[(%d, %d) %s]", getClass().getSimpleName(), row, col, who);
        }
    }

    class Unassigned implements Serializable {
        final int row;
        final int col;
        final List<Integer> possibleValues;

        Unassigned(int row, int col) {
            this.row = row;
            this.col = col;

            possibleValues = new ArrayList<>();
        }

        @Override
        public String toString() {
            return String.format("%s[(%d, %d) %s]", getClass().getSimpleName(), row, col, possibleValues);
        }
    }

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
}
