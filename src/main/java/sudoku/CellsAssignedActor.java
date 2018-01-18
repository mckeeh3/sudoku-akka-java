package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.Optional;

class CellsAssignedActor extends AbstractLoggingActor {
    private final ActorRef validateBoard;

    {
        validateBoard = getContext().actorOf(ValidateBoardActor.props(), "validate-board");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Cell.SetCell.class, this::setCell)
                .match(Validate.ValidBoard.class, this::validBoard)
                .match(Validate.Invalid.class, this::invalidBoard)
                .match(Clone.Board.class, this::cloneBoard)
                .build();
    }

    private void setCell(Cell.SetCell setCell) {
        Optional<ActorRef> cellAssigned = getContext().findChild(cellName(setCell));

//        if (cellAssigned.isPresent()) {
//            log().debug("Already assigned {}", setCell);
//        } else {
        if (!cellAssigned.isPresent()) {
            log().debug("Assign {}", setCell);
            String name = cellName(setCell);
            getContext().actorOf(CellAssignedActor.props(setCell.row, setCell.col, setCell.value, setCell.who), name);
            validateBoard.tell(setCell, getSelf());
        }
    }

    private void cloneBoard(Clone.Board cloneBoard) {
        getContext().getChildren().forEach(child -> child.forward(cloneBoard, getContext()));
    }

    private void validBoard(Validate.ValidBoard validBoard) {
        getContext().getParent().tell(validBoard, getSelf());
    }

    private void invalidBoard(Validate.Invalid invalid) {
        getContext().getParent().tell(invalid, getSelf());
    }

    static Props props() {
        return Props.create(CellsAssignedActor.class);
    }

    private String cellName(Cell.SetCell setCell) {
        return String.format("assigned-row-%d-col-%d", setCell.row, setCell.col);
    }
}
