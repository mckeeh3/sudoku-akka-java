package sudoku;

import java.io.Serializable;

interface Board {
    class Stalled implements Serializable {
        @Override
        public String toString() {
            return String.format("%s[]", getClass().getSimpleName());
        }
    }

    class Solved implements Serializable {
        final Grid grid;

        Solved(Grid grid) {
            this.grid = grid;
        }

        @Override
        public String toString() {
            return String.format("%s[%s]", getClass().getSimpleName(), grid);
        }
    }

    class Invalid implements Serializable {
        final String message;

        Invalid(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return String.format("%s[%s]", getClass().getSimpleName(), message);
        }
    }

    class AllCellsAssigned implements Serializable {
        @Override
        public String toString() {
            return String.format("%s[]", getClass().getSimpleName());
        }
    }

    class Stop implements Serializable {
        @Override
        public String toString() {
            return String.format("%s[]", getClass().getSimpleName());
        }
    }

    class Cell implements Serializable {
        final int row;
        final int col;
        final int value;
        final String who;

        Cell(int row, int col, int value) {
            this.row = row;
            this.col = col;
            this.value = value;
            who = null;
        }

        Cell(int row, int col, int value, String who) {
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
