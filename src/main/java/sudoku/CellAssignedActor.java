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
                .match(Clone.Board.class, this::cloneBoard)
                .build();
    }

    @SuppressWarnings("unused")
    private void cloneBoard(Clone.Board cloneBoard) {
        getSender().tell(new Clone.CloneAssigned(row, col, value, who), getSelf());
    }

    static Props props(int row, int col, int value, String who) {
        return Props.create(CellAssignedActor.class, row, col, value, who);
    }
}
