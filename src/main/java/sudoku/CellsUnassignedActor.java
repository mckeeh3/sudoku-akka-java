package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

import java.io.Serializable;

class CellsUnassignedActor extends AbstractLoggingActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SetCell.class, this::setCell)
                .build();
    }

    @Override
    public void preStart() {
        for (int row = 1; row < 10; row++) {
            for (int col = 1; col < 10; col++) {
                String name = String.format("unassigned-row-%d-col-%d", row, col);
                getContext().actorOf(CellUnassignedActor.props(row, col), name);
            }
        }
    }

    private void setCell(SetCell setCell) {
        if (getContext ().getChildren().iterator().hasNext()) {
            getContext ().getChildren().forEach(child -> child.forward(setCell, getContext()));
        } else {
            getContext().parent().tell(new BoardState.AllCellsAssigned(), getSelf());
        }
    }

    static Props props() {
        return Props.create(CellsUnassignedActor.class);
    }
}
