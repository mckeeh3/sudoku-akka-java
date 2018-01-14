package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;

class ValidateBoardActor extends AbstractLoggingActor {
    private final ActorRef validateRows;
    private final ActorRef validateColumns;
    private final ActorRef validateBoxes;

    private int validRowColBox = 0;

    {
        validateRows = getContext().actorOf(ValidateRowsActor.props(), "validateRows");
        validateColumns = getContext().actorOf(ValidateColumnsActor.props(), "validateColumns");
        validateBoxes = getContext().actorOf(ValidateBoxesActor.props(), "validateBoxes");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Validate.Board.class, this::validateBoard)
                .match(Cell.class, this::validateCell)
                .match(Validate.Valid.class, this::validRowColBox)
                .build();
    }

    private void validateBoard(Validate.Board validateBoard) {
        log().debug("{}", validateBoard);

        getSender().tell(validateBoard, getSelf());
    }

    private void validateCell(Cell cell) {
        validateRows.tell(cell, getSelf());
        validateColumns.tell(cell, getSelf());
        validateBoxes.tell(cell, getSelf());
    }

    @SuppressWarnings("unused")
    private void validRowColBox(Validate.Valid valid) {
        validRowColBox++;
        if (validRowColBox == 3) {
            getContext().getParent().tell(valid, getSelf());
        }
    }

    static Props props() {
        return Props.create(ValidateBoardActor.class);
    }
}
