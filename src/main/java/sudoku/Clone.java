package sudoku;

import akka.actor.ActorRef;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

interface Clone {
    class Board implements Serializable {
        }

    class Boards implements Serializable {
        final ActorRef boardFrom;
        final ActorRef boardTo;

        Boards(ActorRef boardFrom, ActorRef boardTo) {
            this.boardFrom = boardFrom;
            this.boardTo = boardTo;
        }

        @Override
        public String toString() {
            return String.format("%s[clone from %s, to %s]", getClass().getSimpleName(), boardFrom, boardTo);
        }
    }
    class CloneUnassigned implements Serializable {
        final int row;
        final int col;
        final List<Integer> possibleValues;

        CloneUnassigned(int row, int col, List<Integer> possibleValues) {
            this.row = row;
            this.col = col;
            this.possibleValues = new ArrayList<>(possibleValues);
        }

        @Override
        public String toString() {
            return String.format("%s[(%d, %d) %s]", getClass().getSimpleName(), row, col, possibleValues);
        }
    }

    class CloneAssigned implements Serializable {
        final int row;
        final int col;
        final int value;
        final String who;

        CloneAssigned(int row, int col, int value, String who) {
            this.row = row;
            this.col = col;
            this.value = value;
            this.who = who;
        }

        @Override
        public String toString() {
            return String.format("%s[(%d, %d) = %d, %s]", getClass().getSimpleName(), row, col, value, who);
        }
    }

//    class CloneUnassigned implements Serializable {
//        final ActorRef boardFrom;
//        final ActorRef boardTo;
//
//        CloneUnassigned(ActorRef boardFrom, ActorRef boardTo) {
//            this.boardFrom = boardFrom;
//            this.boardTo = boardTo;
//        }
//
//        @Override
//        public String toString() {
//            return String.format("%s[from %s, to %s]", getClass().getSimpleName(), boardFrom, boardTo);
//        }
//    }

//    class CloneAssigned implements Serializable {
//        final ActorRef boardTo;
//
//        CloneAssigned(ActorRef boardTo) {
//            this.boardTo = boardTo;
//        }
//
//        @Override
//        public String toString() {
//            return String.format("%s[to %s]", getClass().getSimpleName(), boardTo);
//        }
//    }
}
