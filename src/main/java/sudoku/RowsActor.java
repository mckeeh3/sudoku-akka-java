package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

class RowsActor extends AbstractLoggingActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SetCell.class, this::setCell)
                .build();
    }

    @Override
    public void preStart() {
        for (int row = 1; row <= 9; row++) {
            for (int value = 1; value <= 9; value++) {
                getContext().actorOf(RowActor.props(row, value));
            }
        }
    }

    private void setCell(SetCell setCell) {
        log().debug("{}", setCell);
        getContext().getChildren().forEach(row -> row.forward(setCell, getContext()));
    }

    static Props props() {
        return Props.create(RowsActor.class);
    }
}
