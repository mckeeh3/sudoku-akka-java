package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import akka.actor.Terminated;

class CellsUnassignedActor extends AbstractLoggingActor {
    private int cellCount = 0;
    private int cellCountNoChange;
    private SetCell lastSetCell;

    private Receive running;
    private Receive solved;
    private Receive stalled;

    {
        running = receiveBuilder()
                .match(SetCell.class, this::setCell)
                .match(CellState.NoChange.class, this::noChange)
                .match(Terminated.class, this::cellStopped)
                .build();

        solved = receiveBuilder()
                .match(CellState.NoChange.class, this::noChangeSolved)
                .build();

        stalled = receiveBuilder()
                .match(SetCell.class, this::setCellStalled)
                .match(CellState.NoChange.class, this::noChangeStalled)
                .match(Terminated.class, this::cellStopped)
                .match(BoardState.CloneUnassigned.class, this::cloneCells)
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

    private void setCell(SetCell setCell) {
        if (getContext().getChildren().iterator().hasNext()) {
            getContext().getChildren().forEach(child -> child.forward(setCell, getContext()));
        } else {
            getContext().parent().tell(new BoardState.AllCellsAssigned(), getSelf());
            getContext().become(solved);
        }

        lastSetCell = setCell;
        cellCountNoChange = 0;
    }

    private void setCellStalled(SetCell setCell) {
        getContext().become(running);
        setCell(setCell);
    }

    private void noChange(CellState.NoChange noChange) {
        if (lastSetCell.equals(noChange.setCell)) {
            cellCountNoChange++;
            checkIfStalled();
        }
    }

    @SuppressWarnings("unused")
    private void noChangeSolved(CellState.NoChange noChange) {
    }

    @SuppressWarnings("unused")
    private void noChangeStalled(CellState.NoChange noChange) {
    }

    @SuppressWarnings("unused")
    private void cellStopped(Terminated terminated) {
        cellCount--;
        checkIfStalled();
//        log().debug("Cell stopped ({}) {}", cellCount, terminated);
    }

    private void checkIfStalled() {
        if (cellCountNoChange >= cellCount) {
            log().debug("Cells stalled, cell unsolved {} stalled {}", cellCount, cellCountNoChange);

            getContext().getParent().tell(new BoardState.Stalled(), getSelf());
            getContext().become(stalled);
        }
    }

    private void cloneCells(BoardState.CloneUnassigned cloneUnassigned) {
        log().debug("{}", cloneUnassigned);
        getContext().getChildren().forEach(child -> child.tell(cloneUnassigned, getSelf()));
    }

    static Props props() {
        return Props.create(CellsUnassignedActor.class);
    }
}
