package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class SudokuActor extends AbstractLoggingActor {
    private final Grid grid;
    private final ActorRef board;

    SudokuActor(Grid grid) {
        this.grid = grid;
        board = getContext().actorOf(BoardActor.props());
    }

    @Override
    public Receive createReceive() {
        return null;
    }

    @Override
    public void preStart() {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                SetCell setCell = new SetCell(row, col, grid.cell(row, col).value);
                board.tell(setCell, getSelf());
            }
        }
    }

    static Props props(Grid grid) {
        return Props.create(SudokuActor.class, () -> new SudokuActor(grid));
    }
}
