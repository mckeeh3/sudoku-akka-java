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
    private AbstractLoggingActor.Receive cloning;

    private enum State {
        running("running"),
        solved("solved"),
        failed("failed"),
        stalled("stalled"),
        cloning("cloning");

        String state;

        State(String state) {
            this.state = state;
        }
    }

    {
        cellsAssigned = getContext().actorOf(CellsAssignedActor.props(), "cellsAssigned");
        cellsUnassigned = getContext().actorOf(CellsUnassignedActor.props(), "cellsUnassigned");
        rows = getContext().actorOf(RowsActor.props(), "rows");
        columns = getContext().actorOf(ColumnsActor.props(), "columns");
        boxes = getContext().actorOf(BoxesActor.props(), "boxes");

        running = receiveBuilder()
                .match(SetCell.class, this::setCell)
                .match(Board.AllCellsAssigned.class, this::allCellsAssigned)
                .match(CellState.Invalid.class, this::cellInvalid)
                .match(Board.Stalled.class, this::boardStalled)
                .match(Board.Clone.class, this::boardClone)
                .match(CellState.CloneUnassigned.class, this::cloneUnassignedIgnore)
                .match(Validate.Valid.class, this::boardValid)
                .build();

        solved = receiveBuilder()
                .match(SetCell.class, this::setCellNotRunning)
                .match(Board.AllCellsAssigned.class, this::allCellsAssignedSolved)
                .match(Validate.Board.class, this::validateBoard)
                .build();

        failed = receiveBuilder()
                .match(SetCell.class, this::setCellNotRunning)
                .build();

        stalled = receiveBuilder()
                .match(Board.CloneUnassigned.class, this::boardStalledCloneUnassigned)
                .match(Board.CloneAssigned.class, this::boardStalledCloneAssigned)
                .match(SetCell.class, this::boardStalledBreakStall)
                .build();

        cloning = receiveBuilder()
                .match(CellState.CloneUnassigned.class, this::boardCloneUnassigned)
                .match(Board.CloneAssigned.class, this::boardCloneAssigned)
                .build();
    }

    @Override
    public Receive createReceive() {
        return running;
    }

    private void setCell(SetCell setCell) {
        log().debug("{}", setCell);

        cellsAssigned.tell(setCell, getSelf());
        cellsUnassigned.tell(setCell, getSelf());
        rows.tell(setCell, getSelf());
        columns.tell(setCell, getSelf());
        boxes.tell(setCell, getSelf());
    }

    private void allCellsAssigned(Board.AllCellsAssigned allCellsAssigned) {
        log().info("All cells assigned");
        become(State.solved);
        getContext().getParent().tell(allCellsAssigned, getSelf());

        ActorRef validate = getContext().actorOf(ValidateBoardActor.props(), "validateBoard");
        validate.tell(new Validate.Board(), getSelf());
    }

    @SuppressWarnings("unused")
    private void boardValid(Validate.Valid valid) {
        log().debug("Board valid");
    }

    private void cellInvalid(CellState.Invalid invalid) {
        log().info("{}", invalid);
        become(State.failed);
        getContext().getParent().tell(new Board.Invalid(invalid), getSelf());
    }

    @SuppressWarnings("unused")
    private void setCellNotRunning(SetCell setCell) {
    }

    private void allCellsAssignedSolved(Board.AllCellsAssigned allCellsAssigned) {
        log().info("Board solved {}, sender {}", allCellsAssigned, getSender());
    }

    private void boardStalled(Board.Stalled boardStalled) {
        log().info("Board stalled {}, sender {}", boardStalled, getSender());
        getContext().getParent().tell(boardStalled, getSelf());
        become(State.stalled);
    }

    private void boardStalledCloneUnassigned(Board.CloneUnassigned cloneUnassigned) {
        log().debug("Clone stalled {}, sender {}", cloneUnassigned, getSender());
        cellsUnassigned.tell(cloneUnassigned, getSelf());
    }

    private void boardStalledCloneAssigned(Board.CloneAssigned cloneAssigned) {
        log().debug("Clone stalled {}, sender {}", cloneAssigned, getSender());
        cellsAssigned.tell(cloneAssigned, getSelf());
    }

    private void boardClone(Board.Clone clone) {
        log().debug("Clone from stalled board {} to board {}, sender {}", clone.boardStalled, clone.boardClone, getSender());
        clone.boardStalled.tell(new Board.CloneUnassigned(clone.boardStalled, clone.boardClone), getSelf());
        become(State.cloning);
    }

    private void boardCloneUnassigned(CellState.CloneUnassigned cloneUnassigned) {
        if (cloneUnassigned.possibleValues.size() == 2) {
            SetCell setCell1 = new SetCell(
                    cloneUnassigned.row,
                    cloneUnassigned.col,
                    cloneUnassigned.possibleValues.get(0),
                    String.format("Stall breaker 1, (%d, %d) = %d", cloneUnassigned.row, cloneUnassigned.col, cloneUnassigned.possibleValues.get(0))
            );

            SetCell setCell2 = new SetCell(
                    cloneUnassigned.row,
                    cloneUnassigned.col,
                    cloneUnassigned.possibleValues.get(1),
                    String.format("Stall breaker 2, (%d, %d) = %d", cloneUnassigned.row, cloneUnassigned.col, cloneUnassigned.possibleValues.get(1))
            );

            cloneUnassigned.boardStalled.tell(new Board.CloneAssigned(cloneUnassigned.boardClone), getSelf());

            getSelf().tell(setCell1, getSelf());
            cloneUnassigned.boardStalled.tell(setCell2, getSelf());

            become(State.running);
        }
    }

    @SuppressWarnings("unused")
    private void cloneUnassignedIgnore(CellState.CloneUnassigned cloneUnassigned) {
    }

    private void boardStalledBreakStall(SetCell setCell) {
        become(State.running);
        setCell(setCell);
    }

    private void boardCloneAssigned(Board.CloneAssigned cloneAssigned) {
        cellsAssigned.forward(cloneAssigned, getContext());
    }

    private  void validateBoard(Validate.Board validateBoard) {
        log().debug("{}", validateBoard);

        cellsAssigned.forward(validateBoard, getContext());
    }

    private void become(State state) {
        log().debug("become state {}", state);
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
            case cloning:
                getContext().become(cloning);
                break;
        }
    }

    static Props props() {
        return Props.create(BoardActor.class);
    }
}
