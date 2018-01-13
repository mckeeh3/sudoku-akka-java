package sudoku;

import akka.actor.ActorRef;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

interface CellState {
    class CloneUnassigned implements Serializable {
        final int row;
        final int col;
        final List<Integer> possibleValues;
        final ActorRef boardStalled;
        final ActorRef boardClone;

        CloneUnassigned(int row, int col, List<Integer> possibleValues, ActorRef boardStalled, ActorRef boardClone) {
            this.row = row;
            this.col = col;
            this.possibleValues = new ArrayList<>(possibleValues);
            this.boardStalled = boardStalled;
            this.boardClone = boardClone;
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
