package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

class ValidateColumnsActor extends AbstractLoggingActor {
    private int validColumns = 0;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Cell.class, this::validateCell)
                .match(Validate.Valid.class, this::validColumn)
                .match(Validate.Invalid.class, this::invalidColumn)
                .build();
    }

    @Override
    public void preStart() {
        for (int col = 1; col <= 9; col++) {
            getContext().actorOf(ValidateColumnActor.props(col), String.format("validate-col-%d", col));
        }
    }

    private void validateCell(Cell cell) {
        getContext().getChildren().forEach(column -> column.tell(cell, getSelf()));
    }

    @SuppressWarnings("unused")
    private void validColumn(Validate.Valid valid) {
        validColumns++;
        if (validColumns == 9) {
            getContext().getParent().tell(new Validate.Valid("All columns valid"), getSelf());
        }
    }

    private void invalidColumn(Validate.Invalid invalid) {
        log().debug("{}", invalid);
    }

    static Props props() {
        return Props.create(ValidateColumnsActor.class);
    }
}
