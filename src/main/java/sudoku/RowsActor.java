package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

class RowsActor extends AbstractLoggingActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Cell.SetCell.class, this::setCell)
                .build();
    }

    @Override
    public void preStart() {
        for (int row = 1; row <= 9; row++) {
            for (int value = 1; value <= 9; value++) {
                String name = String.format("row-%d-value-%d", row, value);
                getContext().actorOf(RowActor.props(row, value), name);
            }
        }
    }

    private void setCell(Cell.SetCell setCell) {
        getContext().getChildren().forEach(row -> row.forward(setCell, getContext()));
    }

    static Props props() {
        return Props.create(RowsActor.class);
    }
}
