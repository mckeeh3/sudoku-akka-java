package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.Optional;

class CellsAssignedActor extends AbstractLoggingActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SetCell.class, this::setCell)
                .match(Board.CloneAssigned.class, this::cloneCells)
                .match(Validate.Board.class, this::validateBoard)
                .build();
    }

    private void setCell(SetCell setCell) {
        Optional<ActorRef> cellAssigned = getContext().findChild(cellName(setCell));

//        if (cellAssigned.isPresent()) {
//            log().debug("Already assigned {}", setCell);
//        } else {
        if (!cellAssigned.isPresent()) {
            log().debug("Assign {}", setCell);
            String name = cellName(setCell);
            getContext().actorOf(CellAssignedActor.props(setCell.row, setCell.col, setCell.value, setCell.who), name);
        }
    }

    private void cloneCells(Board.CloneAssigned cloneAssigned) {
        log().debug("{}", cloneAssigned);
        getContext().getChildren().forEach(child -> child.tell(cloneAssigned, getSelf()));
    }

    private void validateBoard(Validate.Board validateBoard) {
        getContext().getChildren().forEach(child -> child.forward(validateBoard, getContext()));
    }

    static Props props() {
        return Props.create(CellsAssignedActor.class);
    }

    private String cellName(SetCell setCell) {
        return String.format("assigned-row-%d-col-%d", setCell.row, setCell.col);
    }
}
