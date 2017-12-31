package sudoku;

import java.io.Serializable;

interface BoardState {
    class Status implements Serializable {
    }

    class Running implements Serializable {
    }

    class Solved implements Serializable {
    }

    class Invalid implements Serializable {
        final CellState.Invalid invalid;

        Invalid(CellState.Invalid invalid) {
            this.invalid = invalid;
        }
    }
}
