package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;

class SudokuActor extends AbstractLoggingActor {
    private int boardNumber = 0;

    private SudokuActor(Grid grid) {
        initializeBoardFromGrid(grid);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(BoardState.Solved.class, this::boardSolved)
                .match(BoardState.Stalled.class, this::boardStalled)
                .match(BoardState.Invalid.class, this::boardInvalid)
                .match(BoardState.AllCellsAssigned.class, this::allCellsAssigned)
                .build();
    }

    private void boardSolved(BoardState.Solved solved) {
        log().info("{}", solved);
    }

    @SuppressWarnings("unused")
    private void boardStalled(BoardState.Stalled stalled) {
        log().info("Board stalled, sender {}", getSender());
        ActorRef board = getContext().actorOf(BoardActor.props(), String.format("board-%d", ++boardNumber));
        board.tell(new BoardState.Clone(getSender(), board), getSelf());
    }

    private void boardInvalid(BoardState.Invalid invalid) {
        log().info("{}", invalid);
    }

    @SuppressWarnings("unused")
    private void allCellsAssigned(BoardState.AllCellsAssigned allCellsAssigned) {
        log().info("All cells assigned");
    }

    private void initializeBoardFromGrid(Grid grid) {
        ActorRef board = getContext().actorOf(BoardActor.props(), String.format("board-%d", ++boardNumber));

        for (int row = 1; row <= 9; row++) {
            for (int col = 1; col <= 9; col++) {
                int value = grid.cell(row, col).value;
                if (value > 0) {
                    String who = String.format("Initialize cell (%d, %d) = %d", row, col, value);
                    SetCell setCell = new SetCell(row, col, value, who);
                    board.tell(setCell, getSelf());
                }
            }
        }
    }

    static Props props(Grid grid) {
        return Props.create(SudokuActor.class, () -> new SudokuActor(grid));
    }
}
