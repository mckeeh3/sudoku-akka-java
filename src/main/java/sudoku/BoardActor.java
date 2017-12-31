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

    private BoardActor() {
        cellsAssigned = getContext().actorOf(CellsAssignedActor.props());
        cellsUnassigned = getContext().actorOf(CellsUnassignedActor.props());
        rows = getContext().actorOf(RowsActor.props());
        columns = getContext().actorOf(ColumnsActor.props());
        boxes = getContext().actorOf(BoxesActor.props());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SetCell.class, this::setCell)
                .match(CellState.Invalid.class, this::cellInvalid)
                .build();
    }

    private void setCell(SetCell setCell) {
        cellsAssigned.tell(setCell, getSelf());
        cellsUnassigned.tell(setCell, getSelf());
        rows.tell(setCell, getSelf());
        columns.tell(setCell, getSelf());
        boxes.tell(setCell, getSelf());
    }

    private void cellInvalid(CellState.Invalid invalid) {
        getContext().getParent().tell(new BoardState.Invalid(invalid), getSelf());
    }

    static Props props() {
        return Props.create(BoardActor.class, BoardActor::new);
    }
}
