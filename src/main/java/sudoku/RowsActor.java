package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

public class RowsActor extends AbstractLoggingActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SetCell.class, this::setCell)
                .build();
    }

    private void setCell(SetCell setCell) {
        log().debug("{}", setCell);
    }

    static Props props() {
        return Props.create(RowsActor::new);
    }
}
