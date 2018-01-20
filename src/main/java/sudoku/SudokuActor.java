package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;

class SudokuActor extends AbstractLoggingActor {
    private int boardNumber = 0;
    private final long timeStart = System.currentTimeMillis();
    private ActorRef runner;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Grid.class, this::solveBoard)
                .match(Board.Solved.class, this::boardSolved)
                .match(Board.Invalid.class, this::boardInvalid)
                .match(Board.Stalled.class, this::boardStalled)
                .build();
    }

    private void solveBoard(Grid grid) {
        runner = getSender();
        initializeBoardFromGrid(grid);
    }

    private void boardSolved(Board.Solved solved) {
        log().info("Board solved {} ms", System.currentTimeMillis() - timeStart);
        runner.tell(solved, getSelf());

        getContext().getChildren().forEach(child -> child.tell(new Board.Stop(), getSelf()));
    }

    private void boardInvalid(Board.Invalid boardInvalid) {
        log().info("Board invalid {} ms, {}, {}", System.currentTimeMillis() - timeStart, getSender().path().name(), boardInvalid);
//        runner.tell(boardInvalid, getSelf());
        getContext().stop(getSender());
        getContext().getChildren().forEach(child -> log().debug("{}", child.path().name()));
    }

    @SuppressWarnings("unused")
    private void boardStalled(Board.Stalled stalled) {
        log().info("Board stalled, sender {}", getSender());
        ActorRef board = getContext().actorOf(BoardActor.props(), String.format("board-%d", ++boardNumber));

        Clone.Boards cloneBoards = new Clone.Boards(getSender(), board);
        ActorRef cloneBoard = getContext().actorOf(CloneBoardActor.props(boardNumber), String.format("cloneBoard-%d", boardNumber));
        cloneBoard.tell(cloneBoards, getSelf());
    }

    private void initializeBoardFromGrid(Grid grid) {
        ActorRef board = getContext().actorOf(BoardActor.props(), String.format("board-%d", ++boardNumber));

        for (int row = 1; row <= 9; row++) {
            for (int col = 1; col <= 9; col++) {
                int value = grid.get(row, col).value;
                if (value > 0) {
                    String who = String.format("Initialize cell (%d, %d) = %d", row, col, value);
                    Cell.SetCell setCell = new Cell.SetCell(row, col, value, who);
                    board.tell(setCell, getSelf());
                }
            }
        }
    }

    static Props props() {
        return Props.create(SudokuActor.class);
    }
}
