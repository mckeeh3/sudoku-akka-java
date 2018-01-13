package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

class CellAssignedActor extends AbstractLoggingActor {
    private final int row;
    private final int col;
    private final int value;
    private final String who;

    private CellAssignedActor(int row, int col, int value, String who) {
        this.row = row;
        this.col = col;
        this.value = value;
        this.who = who;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(BoardState.CloneAssigned.class, this::cloneAssigned)
                .build();
    }

    private void cloneAssigned(BoardState.CloneAssigned cloneAssigned) {
//        log().debug("{}", cloneAssigned);
        cloneAssigned.boardClone.tell(new SetCell(row, col, value, who), getSelf());
    }

    static Props props(int row, int col, int value, String who) {
        return Props.create(CellAssignedActor.class, row, col, value, who);
    }
}
