package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class ValidateColumnActor extends AbstractLoggingActor {
    private final int col;
    private final List<Integer> values;

    private ValidateColumnActor(int col) {
        this.col = col;

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
        if (isInCol(cell)) {
            if (!values.removeIf(x -> x == cell.value)) {
                getSender().tell(new Validate.Invalid(String.format("Invalid col %d, %s", col, cell)), getSelf());
            }

            if (values.isEmpty()) {
                getSender().tell(new Validate.Valid(String.format("Valid col %d", col)), getSelf());
            }
        }
    }

    private boolean isInCol(Cell cell) {
        return col == cell.col;
    }

    static Props props(int col) {
        return Props.create(ValidateColumnActor.class, col);
    }
}
