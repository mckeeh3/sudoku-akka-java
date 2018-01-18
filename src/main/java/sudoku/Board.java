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
        final Cell.Invalid invalid;

        Invalid(Cell.Invalid invalid) {
            this.invalid = invalid;
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
}
