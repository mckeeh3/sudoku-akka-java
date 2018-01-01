package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

import java.util.ArrayList;
import java.util.List;

class BoxActor extends AbstractLoggingActor {
    private final int row;
    private final int col;
    private final int monitoredValue;
    private List<Cell> monitoredCells = new ArrayList<>();

    private final int rowLeft;
    private final int rowRight;
    private final int colTop;
    private final int colBottom;

    BoxActor(int row, int col, int monitoredValue) {
        this.row = row;
        this.col = col;
        this.monitoredValue = monitoredValue;

        rowLeft = row * 3 - 3 + 1;
        rowRight = row * 3;
        colTop = col * 3 - 3 + 1;
        colBottom = col * 3;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SetCell.class, this::setCell)
                .build();
    }

    @Override
    public void preStart() {
        for (int r = row * 3 - 3 + 1; r <= row * 3; r++) {
            for (int c = col * 3 - 3 + 1; c < col * 3; c++) {
                monitoredCells.add(new Cell(r, c, monitoredValue));
            }
        }
    }

    private void setCell(SetCell setCell) {
        removeCellFromBoxSameValue(setCell);
        removeCellFromBoxAnyValue(setCell);
        removeCellFromRow(setCell);
        removeCellFromCol(setCell);

        if (monitoredCells.isEmpty()) {
            monitoringComplete();
        } else if (monitoredCells.size() == 1) {
            Cell cell = monitoredCells.get(0);
            String who = String.format("Set by box (%d, %d) = %d", row, col, monitoredValue);
            getSender().tell(new SetCell(cell.row, cell.col, monitoredValue, who), getSelf());
            monitoringComplete();
        }
    }

    private boolean isInBox(SetCell setCell) {
        return setCell.row >= rowLeft && setCell.row <= rowRight
                && setCell.col >= colTop && setCell.col <= colBottom;
    }

    private boolean isSameCell(SetCell setCell, Cell cell) {
        return cell.row == setCell.row && cell.col == setCell.col;
    }

    private boolean isMonitoredValue(SetCell setCell) {
        return monitoredValue == setCell.value;
    }

    private void monitoringComplete() {
        getContext().stop(getSelf());
    }

    private void removeCellFromBoxSameValue(SetCell setCell) {
        if (isInBox(setCell) && isMonitoredValue(setCell)) {
            monitoredCells = new ArrayList<>();
        }
    }

    private void removeCellFromBoxAnyValue(SetCell setCell) {
        monitoredCells.removeIf(cell -> isSameCell(setCell, cell));
    }

    private void removeCellFromRow(SetCell setCell) {
        if (isMonitoredValue(setCell)) {
            monitoredCells.removeIf(cell -> cell.row == setCell.row);
        }
    }

    private void removeCellFromCol(SetCell setCell) {
        if (isMonitoredValue(setCell)) {
            monitoredCells.removeIf(cell -> cell.col == setCell.col);
        }
    }

    static Props props(int row, int col, int monitoredValue) {
        return Props.create(BoxActor.class, row, col, monitoredValue);
    }
}
