package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

import java.util.ArrayList;
import java.util.List;

class ColumnActor extends AbstractLoggingActor {
    private final int col;
    private final int monitoredValue;
    private List<Cell> monitoredCells = new ArrayList<>();

    public ColumnActor(int col, int monitoredValue) {
        this.col = col;
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
        for (int row = 1; row <= 9; row++) {
            monitoredCells.add(new Cell(row, col, monitoredValue));
        }
    }

    private void setCell(SetCell setCell) {
//        List<Cell> mc = new ArrayList<>(monitoredCells);

        removeInRow(setCell);
        removeInCol(setCell);
        removeInBox(setCell);
        removeInColAnyValue(setCell);

        if (monitoredCells.isEmpty()) {
            monitoringComplete();
        } else if (monitoredCells.size() == 1) {
            Cell cell = monitoredCells.get(0);
            String who = String.format("Set by column (%d, %d) = %d", cell.row, cell.col, monitoredValue);
            getSender().tell(new SetCell(cell.row, cell.col, monitoredValue, who), getSelf());
            monitoringComplete();
        }

//        if (mc.size() != monitoredCells.size()) {
//            String msg = String.format("(%d)%s -> (%d)%s ", mc.size(), mc, monitoredCells.size(), monitoredCells);
//            log().debug("{} trimmed {}", setCell, msg);
//        }
    }

    private boolean isInRow(SetCell setCell, Cell cell) {
        return setCell.row == cell.row;
    }

    private boolean isInCol(SetCell setCell) {
        return setCell.col == col;
    }

    private boolean isMonitoredValue(SetCell setCell) {
        return setCell.value == monitoredValue;
    }

    private void monitoringComplete() {
        getContext().stop(getSelf());
    }

    private void removeInRow(SetCell setCell) {
        if (isMonitoredValue(setCell)) {
            monitoredCells.removeIf(cell -> isInRow(setCell, cell));
        }
    }

    private void removeInCol(SetCell setCell) {
        if (isInCol(setCell) && isMonitoredValue(setCell)) {
            monitoredCells = new ArrayList<>();
        }
    }

    private void removeInBox(SetCell setCell) {
        if (isMonitoredValue(setCell)) {
            int setCellBox = boxFor(setCell);
            monitoredCells.removeIf(cell -> setCellBox == boxFor(cell));
        }
    }

    private void removeInColAnyValue(SetCell setCell) {
        if (isInCol(setCell)) {
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

    static Props props(int col, int monitoredValue) {
        return Props.create(ColumnActor.class, col, monitoredValue);
    }
}
