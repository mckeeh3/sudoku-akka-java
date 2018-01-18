package sudoku;

import java.io.Serializable;

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

    class Detail implements Serializable {
        final int row;
        final int col;
        final int value;
        final String who;

        Detail(int row, int col, int value) {
            this.row = row;
            this.col = col;
            this.value = value;
            who = null;
        }

        Detail(int row, int col, int value, String who) {
            this.row = row;
            this.col = col;
            this.value = value;
            this.who = who;
        }

        @Override
        public String toString() {
            return who == null || who.trim().isEmpty()
                    ? String.format("%s[(%d, %d) = %d]", getClass().getSimpleName(), row, col, value)
                    : String.format("%s[(%d, %d) = %d, '%s']", getClass().getSimpleName(), row, col, value, who);
        }
    }
}
