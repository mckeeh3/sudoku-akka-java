package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

import java.util.ArrayList;
import java.util.List;

class CellUnassignedActor extends AbstractLoggingActor {
    private final int row;
    private final int col;
    private final List<Integer> possibleValues;

    private CellUnassignedActor(int row, int col) {
        this.row = row;
        this.col = col;

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
        return setCell.row / 3 == row / 3 && setCell.col / 3 == col / 3;
    }

    private void trimPossibleValues(SetCell setCell) {
        int i = possibleValues.indexOf(setCell.value);
        if (i >= 0) {
            possibleValues.remove(i);
        }
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
        String who = String.format("Cell row %d, col %d, value %d", row, col, possibleValues.get(0));
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
