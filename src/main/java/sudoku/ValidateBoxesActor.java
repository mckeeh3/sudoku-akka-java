package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

class ValidateBoxesActor extends AbstractLoggingActor {
    private int validBoxes = 0;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Cell.class, this::validateCell)
                .match(Validate.Valid.class, this::validBox)
                .match(Validate.Invalid.class, this::invalidBox)
                .build();
    }

    @Override
    public void preStart() {
        for (int box = 1; box <= 9; box++) {
            getContext().actorOf(ValidateBoxActor.props(box), String.format("validate-box-%d", box));
        }
    }

    private void validateCell(Cell cell) {
        getContext().getChildren().forEach(box -> box.tell(cell, getSelf()));
    }

    private void validBox(Validate.Valid valid) {
        validBoxes++;
        if (validBoxes == 9) {
            getContext().getParent().tell(new Validate.Valid("All boxes valid"), getSelf());
        }
    }

    private void invalidBox(Validate.Invalid invalid) {
        getContext().getParent().tell(invalid, getSelf());
    }

    static Props props() {
        return Props.create(ValidateBoxesActor.class);
    }
}
