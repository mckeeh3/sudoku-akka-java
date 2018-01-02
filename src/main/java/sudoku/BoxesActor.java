package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

class BoxesActor extends AbstractLoggingActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SetCell.class, this::setCell)
                .build();
    }

    @Override
    public void preStart() {
        for (int row = 1; row <= 3; row++) {
            for (int col = 1; col <= 3; col++) {
                for (int value = 1; value <= 9; value++) {
                    String name = String.format("box-row-%d-col-%d-value-%d", row, col, value);
                    getContext().actorOf(BoxActor.props(row, col, value), name);
                }
            }
        }
    }

    private void setCell(SetCell setCell) {
        log().debug("{}", setCell);
        getContext().getChildren().forEach(box -> box.forward(setCell, getContext()));
    }

    static Props props() {
        return Props.create(BoxesActor.class);
    }
}
