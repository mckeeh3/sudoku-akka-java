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
                .match(Board.Solved.class, this::boardSolved)
                .match(Board.Stalled.class, this::boardStalled)
                .match(Board.Invalid.class, this::boardInvalid)
                .match(Board.AllCellsAssigned.class, this::allCellsAssigned)
                .build();
    }

    private void boardSolved(Board.Solved solved) {
        log().info("{}", solved);
    }

    @SuppressWarnings("unused")
    private void boardStalled(Board.Stalled stalled) {
        log().info("Board stalled, sender {}", getSender());
        ActorRef board = getContext().actorOf(BoardActor.props(), String.format("board-%d", ++boardNumber));
//        board.tell(new Board.Clone(getSender(), board), getSelf());

        Clone.Boards boards = new Clone.Boards(getSender(), board);
        ActorRef cloneBoards = getContext().actorOf(CloneBoardActor.props(boardNumber));
        cloneBoards.tell(boards, getSelf());
    }

    private void boardInvalid(Board.Invalid invalid) {
        log().info("{}", invalid);
    }

    @SuppressWarnings("unused")
    private void allCellsAssigned(Board.AllCellsAssigned allCellsAssigned) {
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
