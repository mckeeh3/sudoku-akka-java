package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;

class BoardActor extends AbstractLoggingActor {
    private final ActorRef cellsAssigned;
    private final ActorRef cellsUnassigned;
    private final ActorRef rows;
    private final ActorRef columns;
    private final ActorRef boxes;

    private AbstractLoggingActor.Receive running;
    private AbstractLoggingActor.Receive solved;
    private AbstractLoggingActor.Receive failed;
    private AbstractLoggingActor.Receive stalled;

    private enum State {
        running, solved, failed, stalled
    }

    {
        cellsAssigned = getContext().actorOf(CellsAssignedActor.props(), "cellsAssigned");
        cellsUnassigned = getContext().actorOf(CellsUnassignedActor.props(), "cellsUnassigned");
        rows = getContext().actorOf(RowsActor.props(), "rows");
        columns = getContext().actorOf(ColumnsActor.props(), "columns");
        boxes = getContext().actorOf(BoxesActor.props(), "boxes");

        running = receiveBuilder()
                .match(Cell.SetCell.class, this::setCell)
                .match(Board.AllCellsAssigned.class, this::allCellsAssigned)
                .match(Cell.Invalid.class, this::cellInvalid)
                .match(Validate.Invalid.class, this::boardInvalid)
                .match(Validate.ValidBoard.class, this::boardValid)
                .match(Board.Stalled.class, this::boardStalled)
                .match(Clone.Board.class, this::cloneBoard)
                .match(Board.Stop.class, this::boardStop)
                .build();

        solved = receiveBuilder()
                .match(Cell.SetCell.class, this::setCellNotRunning)
                .match(Board.AllCellsAssigned.class, this::allCellsAssignedSolved)
                .match(Validate.Invalid.class, this::boardInvalid)
                .match(Validate.ValidBoard.class, this::boardValid)
                .match(Board.Stop.class, this::boardStop)
                .build();

        failed = receiveBuilder()
                .match(Cell.SetCell.class, this::setCellNotRunning)
                .match(Validate.Invalid.class, this::boardInvalidAlreadyFailed)
                .match(Board.Stop.class, this::boardStop)
                .build();

        stalled = receiveBuilder()
                .match(Cell.SetCell.class, this::boardStalledBreakStall)
                .match(Clone.Board.class, this::cloneBoard)
                .match(Board.Stop.class, this::boardStop)
                .build();
    }

    @Override
    public Receive createReceive() {
        return running;
    }

    private void setCell(Cell.SetCell setCell) {
        cellsAssigned.tell(setCell, getSelf());
        cellsUnassigned.tell(setCell, getSelf());
        rows.tell(setCell, getSelf());
        columns.tell(setCell, getSelf());
        boxes.tell(setCell, getSelf());
    }

    @SuppressWarnings("unused")
    private void allCellsAssigned(Board.AllCellsAssigned allCellsAssigned) {
        log().info("All cells assigned");
    }

    private void boardValid(Validate.ValidBoard validBoard) {
        log().info("Board solved {}", validBoard);
        become(State.solved);
        getContext().getParent().tell(new Board.Solved(validBoard.grid), getSelf());
    }

    private void boardInvalid(Validate.Invalid invalid) {
        log().info("Board invalid {}", invalid);
        become(State.failed);
        getContext().getParent().tell(new Board.Invalid(invalid.message), getSelf());
    }

    @SuppressWarnings("unused")
    private void boardInvalidAlreadyFailed(Validate.Invalid invalid) {
    }

    private void cellInvalid(Cell.Invalid invalid) {
        log().info("{}", invalid);
        become(State.failed);
        getContext().getParent().tell(new Board.Invalid(invalid.toString()), getSelf());
    }

    @SuppressWarnings("unused")
    private void setCellNotRunning(Cell.SetCell setCell) {
    }

    @SuppressWarnings("unused")
    private void allCellsAssignedSolved(Board.AllCellsAssigned allCellsAssigned) {
    }

    private void boardStalled(Board.Stalled boardStalled) {
        log().info("Board stalled {}, sender {}", boardStalled, getSender().path().name());
        getContext().getParent().tell(boardStalled, getSelf());
        become(State.stalled);
    }

    private void cloneBoard(Clone.Board cloneBoard) {
        cellsUnassigned.forward(cloneBoard, getContext());
        cellsAssigned.forward(cloneBoard, getContext());
    }

    private void boardStalledBreakStall(Cell.SetCell setCell) {
        become(State.running);
        setCell(setCell);
    }

    private void become(State state) {
        log().debug("Become state {}", state);
        switch (state) {
            case running:
                getContext().become(running);
                break;
            case solved:
                getContext().become(solved);
                break;
            case failed:
                getContext().become(failed);
                break;
            case stalled:
                getContext().become(stalled);
                break;
        }
    }

    @SuppressWarnings("unused")
    private void boardStop(Board.Stop stop) {
        log().debug("Stop {}", getSelf().path().name());
        getContext().stop(getSelf());
    }

    @Override
    public void postStop() {
        log().debug("Stopped board");
    }

    static Props props() {
        return Props.create(BoardActor.class);
    }
}
