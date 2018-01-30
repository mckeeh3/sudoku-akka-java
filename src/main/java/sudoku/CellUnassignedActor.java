package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class CellUnassignedActor extends AbstractLoggingActor {
    private final int row;
    private final int col;
    private final int box;
    private final List<Integer> possibleValues;

    private CellUnassignedActor(int row, int col) {
        this.row = row;
        this.col = col;
        box = boxFor(row, col);

        possibleValues = new ArrayList<>();
        Collections.addAll(possibleValues, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Cell.SetCell.class, this::setCell)
                .match(Clone.Board.class, this::cloneBoard)
                .build();
    }

    private void setCell(Cell.SetCell setCell) {
        if (isSameCell(setCell)) {
            possibleValues.clear();
        } else if (isSameRowColOrBox(setCell)) {
            trimPossibleValues(setCell);
        }

        getContext().getParent().tell(new Cell.Ack(row, col, possibleValues), getSelf());
    }

    private boolean isSameCell(Cell.SetCell setCell) {
        return setCell.row == this.row && setCell.col == this.col;
    }

    private boolean isSameRowColOrBox(Cell.SetCell setCell) {
        return isSameRow(setCell) || isSameCol(setCell) || isSameBox(setCell);
    }

    private boolean isSameRow(Cell.SetCell setCell) {
        return setCell.row == this.row;
    }

    private boolean isSameCol(Cell.SetCell setCell) {
        return setCell.col == this.col;
    }

    private boolean isSameBox(Cell.SetCell setCell) {
        return box == boxFor(setCell.row, setCell.col);
    }

    private int boxFor(int row, int col) {
        int boxRow = (row - 1) / 3 + 1;
        int boxCol = (col - 1) / 3 + 1;
        return (boxRow - 1) * 3 + boxCol;
    }

    private void trimPossibleValues(Cell.SetCell setCell) {
        possibleValues.removeIf(value -> value == setCell.value);
    }

    @SuppressWarnings("unused")
    private void cloneBoard(Clone.Board cloneBoard) {
        getSender().tell(new Clone.CloneUnassigned(row, col, possibleValues), getSelf());
    }

    static Props props(int row, int col) {
        return Props.create(CellUnassignedActor.class, () -> new CellUnassignedActor(row, col));
    }
}
