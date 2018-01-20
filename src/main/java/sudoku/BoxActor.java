package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

import java.util.ArrayList;
import java.util.List;

class BoxActor extends AbstractLoggingActor {
    private final int row;
    private final int col;
    private final int monitoredValue;
    private List<Board.Cell> monitoredCells = new ArrayList<>();
    private final int boxIndex;

    private BoxActor(int row, int col, int monitoredValue) {
        this.row = row;
        this.col = col;
        this.monitoredValue = monitoredValue;

        boxIndex = (row - 1) * 3 + col;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Cell.SetCell.class, this::setCell)
                .build();
    }

    @Override
    public void preStart() {
        for (int r = 1; r <= 9; r++) {
            for (int c = 1; c <= 9; c++) {
                if (boxIndex == boxFor(r, c)) {
                    monitoredCells.add(new Board.Cell(r, c, monitoredValue));
                }
            }
        }
    }

    private void setCell(Cell.SetCell setCell) {
//        List<Cell> mc = new ArrayList<>(monitoredCells);

        removeCellFromBoxSameValue(setCell);
        removeCellFromBoxAnyValue(setCell);
        removeCellFromRow(setCell);
        removeCellFromCol(setCell);

        if (monitoredCells.isEmpty()) {
            monitoringComplete();
        } else if (monitoredCells.size() == 1) {
            Board.Cell cell = monitoredCells.get(0);
            String who = String.format("Set by box (%d, %d)", row, col);
            getSender().tell(new Cell.SetCell(cell.row, cell.col, monitoredValue, who), getSelf());
            monitoringComplete();
        }

//        if (monitoredCells.size() != mc.size()) {
//            String msg = String.format("%s trimmed (%d)%s -> (%d)%s", setCell, mc.size(), mc, monitoredCells.size(), monitoredCells);
//            log().debug("{}", msg);
//        }
    }

    private int boxFor(int row, int col) {
        int boxRow = (row - 1) / 3 + 1;
        int boxCol = (col - 1) / 3 + 1;
        return (boxRow - 1) * 3 + boxCol;
    }

    private boolean isInBox(Cell.SetCell setCell) {
        return boxIndex == boxFor(setCell.row, setCell.col);
    }

    private boolean isSameCell(Cell.SetCell setCell, Board.Cell cell) {
        return cell.row == setCell.row && cell.col == setCell.col;
    }

    private boolean isMonitoredValue(Cell.SetCell setCell) {
        return monitoredValue == setCell.value;
    }

    private void monitoringComplete() {
        getContext().stop(getSelf());
    }

    private void removeCellFromBoxSameValue(Cell.SetCell setCell) {
        if (isInBox(setCell) && isMonitoredValue(setCell)) {
            monitoredCells = new ArrayList<>();
        }
    }

    private void removeCellFromBoxAnyValue(Cell.SetCell setCell) {
        monitoredCells.removeIf(cell -> isSameCell(setCell, cell));
    }

    private void removeCellFromRow(Cell.SetCell setCell) {
        if (isMonitoredValue(setCell)) {
            monitoredCells.removeIf(cell -> cell.row == setCell.row);
        }
    }

    private void removeCellFromCol(Cell.SetCell setCell) {
        if (isMonitoredValue(setCell)) {
            monitoredCells.removeIf(cell -> cell.col == setCell.col);
        }
    }

    static Props props(int row, int col, int monitoredValue) {
        return Props.create(BoxActor.class, row, col, monitoredValue);
    }
}
