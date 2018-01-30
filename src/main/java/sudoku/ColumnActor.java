package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

import java.util.ArrayList;
import java.util.List;

class ColumnActor extends AbstractLoggingActor {
    private final int col;
    private final int monitoredValue;
    private List<Board.Cell> monitoredCells = new ArrayList<>();

    private ColumnActor(int col, int monitoredValue) {
        this.col = col;
        this.monitoredValue = monitoredValue;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Cell.SetCell.class, this::setCell)
                .build();
    }

    @Override
    public void preStart() {
        for (int row = 1; row <= 9; row++) {
            monitoredCells.add(new Board.Cell(row, col, monitoredValue));
        }
    }

    private void setCell(Cell.SetCell setCell) {
        removeInRow(setCell);
        removeInCol(setCell);
        removeInBox(setCell);
        removeInColAnyValue(setCell);

        if (monitoredCells.isEmpty()) {
            monitoringComplete();
        } else if (monitoredCells.size() == 1) {
            Board.Cell cell = monitoredCells.get(0);
            String who = String.format("Set by column (%d)", col);
            getSender().tell(new Cell.SetCell(cell.row, cell.col, monitoredValue, who), getSelf());
            monitoringComplete();
        }
    }

    private boolean isInRow(Cell.SetCell setCell, Board.Cell cell) {
        return setCell.row == cell.row;
    }

    private boolean isInCol(Cell.SetCell setCell) {
        return setCell.col == col;
    }

    private boolean isMonitoredValue(Cell.SetCell setCell) {
        return setCell.value == monitoredValue;
    }

    private void monitoringComplete() {
        getContext().stop(getSelf());
    }

    private void removeInRow(Cell.SetCell setCell) {
        if (isMonitoredValue(setCell)) {
            monitoredCells.removeIf(cell -> isInRow(setCell, cell));
        }
    }

    private void removeInCol(Cell.SetCell setCell) {
        if (isInCol(setCell) && isMonitoredValue(setCell)) {
            monitoredCells = new ArrayList<>();
        }
    }

    private void removeInBox(Cell.SetCell setCell) {
        if (isMonitoredValue(setCell)) {
            int setCellBox = boxFor(setCell);
            monitoredCells.removeIf(cell -> setCellBox == boxFor(cell));
        }
    }

    private void removeInColAnyValue(Cell.SetCell setCell) {
        if (isInCol(setCell)) {
            monitoredCells.removeIf(cell -> cell.row == setCell.row && cell.col == setCell.col);
        }
    }

    private int boxFor(Cell.SetCell setCell) {
        return boxFor(setCell.row, setCell.col);
    }

    private int boxFor(Board.Cell cell) {
        return boxFor(cell.row, cell.col);
    }

    private int boxFor(int row, int col) {
        int boxRow = (row - 1) / 3 + 1;
        int boxCol = (col - 1) / 3 + 1;

        return (boxRow - 1) * 3 + boxCol;
    }

    static Props props(int col, int monitoredValue) {
        return Props.create(ColumnActor.class, col, monitoredValue);
    }
}
