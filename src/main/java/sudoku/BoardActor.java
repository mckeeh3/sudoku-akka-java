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

//        @Override
//        public String toString() {
//            return state;
//        }
    }

    {
        cellsAssigned = getContext().actorOf(CellsAssignedActor.props(), "cellsAssigned");
        cellsUnassigned = getContext().actorOf(CellsUnassignedActor.props(), "cellsUnassigned");
        rows = getContext().actorOf(RowsActor.props(), "rows");
        columns = getContext().actorOf(ColumnsActor.props(), "columns");
        boxes = getContext().actorOf(BoxesActor.props(), "boxes");

        running = receiveBuilder()
                .match(SetCell.class, this::setCell)
                .match(BoardState.AllCellsAssigned.class, this::allCellsAssigned)
                .match(CellState.Invalid.class, this::cellInvalid)
                .match(BoardState.Stalled.class, this::boardStalled)
                .match(BoardState.Clone.class, this::boardClone)
                .match(CellState.CloneUnassigned.class, this::cloneUnassignedIgnore)
                .build();

        solved = receiveBuilder()
                .match(SetCell.class, this::setCellNotRunning)
                .match(BoardState.AllCellsAssigned.class, this::allCellsAssignedSolved)
                .build();

        failed = receiveBuilder()
                .match(SetCell.class, this::setCellNotRunning)
                .build();

        stalled = receiveBuilder()
                .match(BoardState.CloneUnassigned.class, this::boardStalledCloneUnassigned)
                .match(BoardState.CloneAssigned.class, this::boardStalledCloneAssigned)
                .match(SetCell.class, this::boardStalledBreakStall)
                .build();

        cloning = receiveBuilder()
                .match(CellState.CloneUnassigned.class, this::boardCloneUnassigned)
                .match(BoardState.CloneAssigned.class, this::boardCloneAssigned)
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

    private void allCellsAssigned(BoardState.AllCellsAssigned allCellsAssigned) {
        log().info("All cells assigned");
        become(State.solved);
        getContext().getParent().tell(allCellsAssigned, getSelf());
    }

    private void cellInvalid(CellState.Invalid invalid) {
        log().info("{}", invalid);
        become(State.failed);
        getContext().getParent().tell(new BoardState.Invalid(invalid), getSelf());
    }

    @SuppressWarnings("unused")
    private void setCellNotRunning(SetCell setCell) {
    }

    private void allCellsAssignedSolved(BoardState.AllCellsAssigned allCellsAssigned) {
        log().info("Board solved {}, sender {}", allCellsAssigned, getSender());
    }

    private void boardStalled(BoardState.Stalled boardStalled) {
        log().info("Board stalled {}, sender {}", boardStalled, getSender());
        getContext().getParent().tell(boardStalled, getSelf());
        become(State.stalled);
    }

    private void boardStalledCloneUnassigned(BoardState.CloneUnassigned cloneUnassigned) {
        log().debug("Clone stalled {}, sender {}", cloneUnassigned, getSender());
        cellsUnassigned.tell(cloneUnassigned, getSelf());
    }

    private void boardStalledCloneAssigned(BoardState.CloneAssigned cloneAssigned) {
        log().debug("Clone stalled {}, sender {}", cloneAssigned, getSender());
        cellsAssigned.tell(cloneAssigned, getSelf());
    }

    private void boardClone(BoardState.Clone clone) {
        log().debug("Clone from stalled board {} to board {}, sender {}", clone.boardStalled, clone.boardClone, getSender());
        clone.boardStalled.tell(new BoardState.CloneUnassigned(clone.boardStalled, clone.boardClone), getSelf());
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

            cloneUnassigned.boardStalled.tell(new BoardState.CloneAssigned(cloneUnassigned.boardClone), getSelf());

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

    private void boardCloneAssigned(BoardState.CloneAssigned cloneAssigned) {
        cellsAssigned.forward(cloneAssigned, getContext());
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
