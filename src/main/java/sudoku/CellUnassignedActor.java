package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

import java.util.ArrayList;
import java.util.List;

class CellUnassignedActor extends AbstractLoggingActor {
    private final int row;
    private final int col;
    private final List<Integer> possibleValues;
    private final int boxIndex;

    private CellUnassignedActor(int row, int col) {
        this.row = row;
        this.col = col;
        boxIndex = boxFor(row, col);

        possibleValues = new ArrayList<>();

        for (int value = 1; value < 10; value++) {
            possibleValues.add(value);
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SetCell.class, this::setCell)
                .build();
    }

    private void setCell(SetCell setCell) {
        if (isSameCell(setCell)) {
            cellSetByBoardRowColOrBox();
        } else if (isSameRow(setCell)) {
            trimPossibleValues(setCell);
        } else if (isSameCol(setCell)) {
            trimPossibleValues(setCell);
        } else if (isSameBox(setCell)) {
            trimPossibleValues(setCell);
        }

        checkPossibleValues();
    }

    private boolean isSameCell(SetCell setCell) {
        return setCell.row == this.row && setCell.col == this.col;
    }

    private boolean isSameRow(SetCell setCell) {
        return setCell.row == this.row;
    }

    private boolean isSameCol(SetCell setCell) {
        return setCell.col == this.col;
    }

    private boolean isSameBox(SetCell setCell) {
//        return setCell.row / 3 == row / 3 && setCell.col / 3 == col / 3;
        return boxIndex == boxFor(setCell.row, setCell.col);
    }

    private int boxFor(int row, int col) {
        int boxRow = (row - 1) / 3 + 1;
        int boxCol = (col - 1) / 3 + 1;
        return (boxRow - 1) * 3 + boxCol;
    }

    private void trimPossibleValues(SetCell setCell) {
        possibleValues.removeIf(value -> value == setCell.value);
    }

    private void checkPossibleValues() {
        if (possibleValues.size() == 1) {
            cellSetByThisCell();
        } else if (possibleValues.isEmpty()) {
            cellIsInvalid();
        }
    }

    private void cellSetByBoardRowColOrBox() {
        getContext().stop(getSelf());
    }

    private void cellSetByThisCell() {
        String who = String.format("Set by cell (%d, %d) = %d", row, col, possibleValues.get(0));
        getSender().tell(new SetCell(row, col, possibleValues.get(0), who), getSelf());
        getContext().stop(getSelf());
    }

    private void cellIsInvalid() {
        getSender().tell(new CellState.Invalid(row, col), getSelf());
    }

    static Props props(int row, int col) {
        return Props.create(CellUnassignedActor.class, () -> new CellUnassignedActor(row, col));
    }
}
