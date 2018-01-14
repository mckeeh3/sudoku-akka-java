package sudoku;

import akka.actor.ActorRef;

import java.io.Serializable;

interface Board {
    class Stalled implements Serializable {
        @Override
        public String toString() {
            return String.format("%s[]", getClass().getSimpleName());
        }
    }

    class Clone implements Serializable {
        final ActorRef boardStalled;
        final ActorRef boardClone;

        Clone(ActorRef boardStalled, ActorRef boardClone) {
            this.boardStalled = boardStalled;
            this.boardClone = boardClone;
        }

        @Override
        public String toString() {
            return String.format("%s[stalled %s, clone %s]", getClass().getSimpleName(), boardStalled, boardClone);
        }
    }

    class CloneUnassigned implements Serializable {
        final ActorRef boardStalled;
        final ActorRef boardClone;

        CloneUnassigned(ActorRef boardStalled, ActorRef boardClone) {
            this.boardStalled = boardStalled;
            this.boardClone = boardClone;
        }

        @Override
        public String toString() {
            return String.format("%s[stalled %s, clone %s]", getClass().getSimpleName(), boardStalled, boardClone);
        }
    }

    class CloneAssigned implements Serializable {
        final ActorRef boardClone;

        CloneAssigned(ActorRef boardClone) {
            this.boardClone = boardClone;
        }

        @Override
        public String toString() {
            return String.format("%s[clone %s]", getClass().getSimpleName(), boardClone);
        }
    }

    class Solved implements Serializable {
    }

    class Invalid implements Serializable {
        final CellState.Invalid invalid;

        Invalid(CellState.Invalid invalid) {
            this.invalid = invalid;
        }
    }

    class AllCellsAssigned implements Serializable {
    }
}
