package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

class ColumnsActor extends AbstractLoggingActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SetCell.class, this::setCell)
                .build();
    }

    @Override
    public void preStart() {
        for (int col = 1; col <= 9; col++) {
            for (int value = 1; value <= 9; value++) {
                String name = String.format("col-%d-value-%d", col, value);
                getContext().actorOf(ColumnActor.props(col, value), name);
            }
        }
    }

    private void setCell(SetCell setCell) {
        log().debug("{}", setCell);
        getContext().getChildren().forEach(child -> child.forward(setCell, getContext()));
    }

    static Props props() {
        return Props.create(ColumnsActor.class);
    }
}
