package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class ValidateBoxActor extends AbstractLoggingActor {
    private final int box;
    private final List<Integer> values;

    private ValidateBoxActor(int box) {
        this.box = box;

        values = new ArrayList<>();
        Collections.addAll(values, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Board.Cell.class, this::validateCell)
                .build();
    }

    private void validateCell(Board.Cell cell) {
        if (isInBox(cell)) {
            if (!values.removeIf(value -> value == cell.value)) {
                getSender().tell(new Validate.Invalid(String.format("Invalid box %d, %s", box, cell)), getSelf());
            }

            if (values.isEmpty()) {
                getSender().tell(new Validate.Valid(String.format("Valid box %d", box)), getSelf());
            }
        }
    }

    private boolean isInBox(Board.Cell cell) {
        return box == boxFor(cell);
    }

    private int boxFor(Board.Cell cell) {
        int boxRow = (cell.row - 1) / 3 + 1;
        int boxCol = (cell.col - 1) / 3 + 1;
        return (boxRow - 1) * 3 + boxCol;
    }

    static Props props(int box) {
        return Props.create(ValidateBoxActor.class, box);
    }
}
