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
                .match(SetCell.class, this::setCell)
                .match(Validate.Valid.class, this::validRowColBox)
                .match(Validate.Invalid.class, this::invalidRowColBox)
                .match(Clone.Board.class, this::cloneBoard)
                .build();
    }

    private void setCell(SetCell setCell) {
        validateCell(new Cell(setCell.row, setCell.col, setCell.value));
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
            getContext().getParent().tell(new Validate.Valid("Board solved"), getSelf());
        }
    }

    private void invalidRowColBox(Validate.Invalid invalid) {
        log().debug("{}", invalid);
        getContext().getParent().tell(invalid, getSelf());
    }

    @SuppressWarnings("unused")
    private void cloneBoard(Clone.Board cloneBoard) {
    }

    static Props props() {
        return Props.create(ValidateBoardActor.class);
    }
}
