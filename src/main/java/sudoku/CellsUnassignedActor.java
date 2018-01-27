package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import akka.actor.Terminated;

class CellsUnassignedActor extends AbstractLoggingActor {
    private int cellCount = 0;
    private int cellCountNoChange;
    private Cell.SetCell lastSetCell;

    private Receive running;
    private Receive solved;
    private Receive stalled;

    {
        running = receiveBuilder()
                .match(Cell.SetCell.class, this::setCell)
                .match(Cell.NoChange.class, this::noChange)
                .match(Terminated.class, this::cellStopped)
                .match(Clone.Board.class, this::cloneBoard)
                .build();

        solved = receiveBuilder()
                .match(Cell.SetCell.class, this::setCellSolved)
                .match(Cell.NoChange.class, this::noChangeSolved)
                .build();

        stalled = receiveBuilder()
                .match(Cell.SetCell.class, this::setCellStalled)
                .match(Cell.NoChange.class, this::noChangeStalled)
                .match(Terminated.class, this::cellStopped)
                .match(Clone.Board.class, this::cloneBoard)
                .build();
    }

    @Override
    public Receive createReceive() {
        return running;
    }

    @Override
    public void preStart() {
        for (int row = 1; row < 10; row++) {
            for (int col = 1; col < 10; col++) {
                String name = String.format("unassigned-row-%d-col-%d", row, col);
                getContext().watch(getContext().actorOf(CellUnassignedActor.props(row, col), name));
                cellCount++;
            }
        }
    }

    private void setCell(Cell.SetCell setCell) {
        if (getContext().getChildren().iterator().hasNext()) {
            getContext().getChildren().forEach(child -> child.forward(setCell, getContext()));
        } else {
            getContext().parent().tell(new Board.AllCellsAssigned(), getSelf());
            getContext().become(solved);
        }

        lastSetCell = setCell;
        cellCountNoChange = 0;
    }

    private void setCellStalled(Cell.SetCell setCell) {
        getContext().become(running);
        setCell(setCell);
    }

    @SuppressWarnings("unused")
    private void setCellSolved(Cell.SetCell setCell) {
    }

    private void noChange(Cell.NoChange noChange) {
        if (lastSetCell.equals(noChange.setCell)) {
            cellCountNoChange++;
            checkIfStalled();
        }
    }

    @SuppressWarnings("unused")
    private void noChangeSolved(Cell.NoChange noChange) {
    }

    @SuppressWarnings("unused")
    private void noChangeStalled(Cell.NoChange noChange) {
    }

    @SuppressWarnings("unused")
    private void cellStopped(Terminated terminated) {
        cellCount--;
        if (cellCount == 0) {
            getSender().tell(new Board.AllCellsAssigned(), getSelf());
        }

        cellCountNoChange = 0;
        setCell(lastSetCell);
    }

    private void checkIfStalled() {
        if (cellCountNoChange >= cellCount && cellCount > 0) {
            log().debug("Cells stalled, get unsolved {} stalled {}", cellCount, cellCountNoChange);

            getContext().getParent().tell(new Board.Stalled(), getSelf());
            getContext().become(stalled);
        }
    }

    private void cloneBoard(Clone.Board cloneBoard) {
        getContext().getChildren().forEach(child -> child.forward(cloneBoard, getContext()));
    }

    static Props props() {
        return Props.create(CellsUnassignedActor.class);
    }
}
