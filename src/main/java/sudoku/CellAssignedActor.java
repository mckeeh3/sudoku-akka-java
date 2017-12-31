package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

public class CellAssignedActor extends AbstractLoggingActor {
    private final int row;
    private final int col;
    private final int value;
    private final String who;

    public CellAssignedActor(int row, int col, int value, String who) {
        this.row = row;
        this.col = col;
        this.value = value;
        this.who = who;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CellsAssignedActor.Clone.class, this::clone)
                .build();
    }

    private void clone(CellsAssignedActor.Clone clone) {
        log().debug("{}", clone);
        getSender().tell(new CellState.Assigned(row, col, value, who), getSelf());
    }

    static Props props(int row, int col, int value, String who) {
        return Props.create(CellAssignedActor.class, row, col, value, who);
    }
}
