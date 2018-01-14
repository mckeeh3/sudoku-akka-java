package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class ValidateRowActor extends AbstractLoggingActor {
    private final int row;
    private final List<Integer> values;

    private ValidateRowActor(int row) {
        this.row = row;

        values = new ArrayList<>();
        Collections.addAll(values, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Cell.class, this::validateCell)
                .build();
    }

    private void validateCell(Cell cell) {
        if (isInRow(cell)) {
            if (!values.removeIf(value -> value == cell.value)) {
                getSender().tell(new Validate.Invalid(String.format("Invalid row %d, %s", row, cell)), getSelf());
            }

            if (values.isEmpty()) {
                getSender().tell(new Validate.Valid(String.format("Valid row %d", row)), getSelf());
            }
        }
    }

    private boolean isInRow(Cell cell) {
        return row == cell.row;
    }

    static Props props(int row) {
        return Props.create(ValidateRowActor.class, row);
    }
}
