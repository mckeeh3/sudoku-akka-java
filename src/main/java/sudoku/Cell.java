package sudoku;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

interface Cell {
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

    class SetCell implements Serializable {
        final int row;
        final int col;
        final int value;
        final String who;

        SetCell(int row, int col, int value, String who) {
            this.row = row;
            this.col = col;
            this.value = value;
            this.who = who;
        }

        @Override
        public String toString() {
            return String.format("%s[(%d, %d) = %d, '%s']", getClass().getSimpleName(), row, col, value, who);
        }
    }

    class Ack implements Serializable {
        final int row;
        final int col;
        final List<Integer> possibleValues;

        Ack(int row, int col, List<Integer> possibleValues) {
            this.row = row;
            this.col = col;
            this.possibleValues = new ArrayList<>(possibleValues);
        }

        @Override
        public String toString() {
            return String.format("%s[(%d, %d) %s]", getClass().getSimpleName(), row, col, possibleValues);
        }
    }
}
