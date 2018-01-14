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
        @Override
        public String toString() {
            return String.format("%s[]", getClass().getSimpleName());
        }
    }

    class Invalid implements Serializable {
        final CellState.Invalid invalid;

        Invalid(CellState.Invalid invalid) {
            this.invalid = invalid;
        }
    }

    class AllCellsAssigned implements Serializable {
        @Override
        public String toString() {
            return String.format("%s[]", getClass().getSimpleName());
        }
    }
}
