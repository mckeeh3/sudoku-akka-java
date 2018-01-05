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
    private AbstractLoggingActor.Receive failed;
    private AbstractLoggingActor.Receive solved;

    private BoardActor() {
        cellsAssigned = getContext().actorOf(CellsAssignedActor.props(), "cellsAssigned");
        cellsUnassigned = getContext().actorOf(CellsUnassignedActor.props(), "cellsUnassigned");
        rows = getContext().actorOf(RowsActor.props(), "rows");
        columns = getContext().actorOf(ColumnsActor.props(), "columns");
        boxes = getContext().actorOf(BoxesActor.props(), "boxes");

        solved = receiveBuilder()
                .match(SetCell.class, this::setCellNotRunning)
                .match(BoardState.AllCellsAssigned.class, this::allCellsAssignedSolved)
                .build();

        failed = receiveBuilder()
                .match(SetCell.class, this::setCellNotRunning)
                .build();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SetCell.class, this::setCell)
                .match(BoardState.AllCellsAssigned.class, this::allCellsAssigned)
                .match(CellState.Invalid.class, this::cellInvalid)
                .build();
    }

    private void setCell(SetCell setCell) {
        log().debug("{}", setCell);

        cellsAssigned.tell(setCell, getSelf());
        cellsUnassigned.tell(setCell, getSelf());
        rows.tell(setCell, getSelf());
        columns.tell(setCell, getSelf());
        boxes.tell(setCell, getSelf());
    }

    private void setCellNotRunning(SetCell setCell) {

    }

    private void allCellsAssignedSolved(BoardState.AllCellsAssigned allCellsAssigned) {

    }

    private void allCellsAssigned(BoardState.AllCellsAssigned allCellsAssigned) {
        log().info("All cells assigned");
        getContext().become(solved);
        getContext().getParent().tell(allCellsAssigned, getSelf());
    }

    private void cellInvalid(CellState.Invalid invalid) {
        log().info("{}", invalid);
        getContext().become(failed);
        getContext().getParent().tell(new BoardState.Invalid(invalid), getSelf());
    }

    static Props props() {
        return Props.create(BoardActor.class);
    }
}
