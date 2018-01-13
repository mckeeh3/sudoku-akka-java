package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

import java.util.ArrayList;
import java.util.List;

class RowActor extends AbstractLoggingActor {
    private final int row;
    private final int monitoredValue;
    private List<Cell> monitoredCells = new ArrayList<>();

    private RowActor(int row, int monitoredValue) {
        this.row = row;
        this.monitoredValue = monitoredValue;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SetCell.class, this::setCell)
                .build();
    }

    @Override
    public void preStart() {
        for (int col = 1; col <= 9; col++) {
            monitoredCells.add(new Cell(row, col, monitoredValue));
        }
    }

    private void setCell(SetCell setCell) {
        removeInRow(setCell);
        removeInCol(setCell);
        removeInBox(setCell);
        removeInRowAnyValue(setCell);

        if (monitoredCells.isEmpty()) {
            monitoringComplete();
        } else if (monitoredCells.size() == 1) {
            Cell cell = monitoredCells.get(0);
            String who = String.format("Set by row (%d) = %d", row, cell.value);
            getSender().tell(new SetCell(cell.row, cell.col, monitoredValue, who), getSelf());
            monitoringComplete();
        }
    }

    private boolean isInRow(SetCell setCell) {
        return setCell.row == row;
    }

    private boolean isInCol(SetCell setCell, Cell cell) {
        return setCell.col == cell.col;
    }

    private boolean isMonitoredValue(SetCell setCell) {
        return setCell.value == monitoredValue;
    }

    private void monitoringComplete() {
        getContext().stop(getSelf());
    }

    private void removeInRow(SetCell setCell) {
        if (isInRow(setCell) && isMonitoredValue(setCell)) {
            monitoredCells = new ArrayList<>();
        }
    }

    private void removeInCol(SetCell setCell) {
        if (isMonitoredValue(setCell)) {
            monitoredCells.removeIf(cell -> isInCol(setCell, cell));
        }
    }

    private void removeInBox(SetCell setCell) {
        if (isMonitoredValue(setCell)) {
            int setCellBox = boxFor(setCell);
            monitoredCells.removeIf(cell -> setCellBox == boxFor(cell));
        }
    }

    private void removeInRowAnyValue(SetCell setCell) {
        if (isInRow(setCell)) {
            monitoredCells.removeIf(cell -> cell.row == setCell.row && cell.col == setCell.col);
        }
    }

    private int boxFor(SetCell setCell) {
        return boxFor(setCell.row, setCell.col);
    }

    private int boxFor(Cell cell) {
        return boxFor(cell.row, cell.col);
    }

    private int boxFor(int row, int col) {
        int boxRow = (row - 1) / 3 + 1;
        int boxCol = (col - 1) / 3 + 1;

        return (boxRow - 1) * 3 + boxCol;
    }

    static Props props(int row, int monitoredValue) {
        return Props.create(RowActor.class, row, monitoredValue);
    }
}
