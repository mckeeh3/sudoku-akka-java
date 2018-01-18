package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

class CloneBoardActor extends AbstractLoggingActor {
    private final int boardNumber;
    private Clone.Boards boards;

    private final Receive beforeSplit;
    private final Receive afterSplit;

    private CloneBoardActor(int boardNumber) {
        this.boardNumber = boardNumber;

        beforeSplit = receiveBuilder()
                .match(Clone.Boards.class, this::cloneBoards)
                .match(Clone.CloneAssigned.class, this::cloneAssigned)
                .match(Clone.CloneUnassigned.class, this::cloneUnassignedBeforeSplit)
                .build();

        afterSplit = receiveBuilder()
                .match(Clone.CloneAssigned.class, this::cloneAssigned)
                .match(Clone.CloneUnassigned.class, this::cloneUnassignedAfterSplit)
                .build();
    }

    @Override
    public Receive createReceive() {
        return beforeSplit;
    }

    private void cloneBoards(Clone.Boards boards) {
        log().info("Clone board {} to {}", boardNumber - 1, boardNumber);
        this.boards = boards;

        boards.boardFrom.tell(new Clone.Board(), getSelf());
    }

    private void cloneAssigned(Clone.CloneAssigned cloneAssigned) {
        Cell.SetCell setCell = new Cell.SetCell(
                cloneAssigned.row,
                cloneAssigned.col,
                cloneAssigned.value,
                String.format("%s (clone board %d)", cloneAssigned.who, boardNumber - 1)
        );

        boards.boardTo.tell(setCell, getSelf());
    }

    private void cloneUnassignedBeforeSplit(Clone.CloneUnassigned cloneUnassigned) {
        if (cloneUnassigned.possibleValues.size() == 2) {
            Cell.SetCell setCell1 = new Cell.SetCell(
                    cloneUnassigned.row,
                    cloneUnassigned.col,
                    cloneUnassigned.possibleValues.get(0),
                    String.format("Stall breaker 1, (%d, %d) = %d", cloneUnassigned.row, cloneUnassigned.col, cloneUnassigned.possibleValues.get(0))
            );

            Cell.SetCell setCell2 = new Cell.SetCell(
                    cloneUnassigned.row,
                    cloneUnassigned.col,
                    cloneUnassigned.possibleValues.get(1),
                    String.format("Stall breaker 2, (%d, %d) = %d", cloneUnassigned.row, cloneUnassigned.col, cloneUnassigned.possibleValues.get(1))
            );

            boards.boardFrom.tell(setCell1, getSelf());
            boards.boardTo.tell(setCell2, getSelf());

            getContext().become(afterSplit);
        }
    }

    @SuppressWarnings("unused")
    private void cloneUnassignedAfterSplit(Clone.CloneUnassigned cloneUnassigned) {
    }

    static Props props(int boardNumber) {
        return Props.create(CloneBoardActor.class, boardNumber);
    }
}
