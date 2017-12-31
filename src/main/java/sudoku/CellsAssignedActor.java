package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.io.Serializable;
import java.util.Optional;

class CellsAssignedActor extends AbstractLoggingActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SetCell.class, this::setCell)
                .match(Clone.class, this::cloneBoard)
                .build();
    }

    private void setCell(SetCell setCell) {
        Optional<ActorRef> cellAssigned = getContext().findChild(cellName(setCell));

        if (cellAssigned.isPresent()) {
            log().debug("Already assigned {}", setCell);
        } else {
            log().debug("Assign {}", setCell);
            getContext().actorOf(CellAssignedActor.props(setCell.row, setCell.col, setCell.value, setCell.who));
        }
    }

    private void cloneBoard(Clone clone) {
        getContext().getChildren().forEach(child -> child.forward(clone, getContext()));
    }

    static Props props() {
        return Props.create(CellsAssignedActor::new);
    }

    private String cellName(SetCell setCell) {
        return String.format("%d-%d", setCell.row, setCell.col);
    }

    static class Clone implements Serializable {
    }
}
