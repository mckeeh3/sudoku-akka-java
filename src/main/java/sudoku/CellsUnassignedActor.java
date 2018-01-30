package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.ArrayList;
import java.util.List;

class CellsUnassignedActor extends AbstractLoggingActor {
    private int requiredAck;
    private List<ActorRef> cells = new ArrayList<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Cell.SetCell.class, this::setCell)
                .match(Cell.Ack.class, this::ack)
                .match(Clone.Board.class, this::cloneBoard)
                .build();
    }

    @Override
    public void preStart() {
        for (int row = 1; row < 10; row++) {
            for (int col = 1; col < 10; col++) {
                String name = String.format("unassigned-%d-%d", row, col);
                cells.add(getContext().actorOf(CellUnassignedActor.props(row, col), name));
            }
        }
    }

    private void setCell(Cell.SetCell setCell) {
        if (cells.isEmpty()) {
            getContext().parent().tell(new Board.AllCellsAssigned(), getSelf());
        } else {
            requiredAck += cells.size();

            for (ActorRef cell : cells) {
                cell.tell(setCell, getSelf());
            }
        }
    }

    private void ack(Cell.Ack ack) {
        if (ack.possibleValues.isEmpty()) {
            cells.remove(getSender());
        } else if (ack.possibleValues.size() == 1) {
            String who = String.format("Set by cell (%d, %d)", ack.row, ack.col);
            getContext().getParent().tell(new Cell.SetCell(ack.row, ack.col, ack.possibleValues.get(0), who), getSelf());
        }

        if (--requiredAck == 0) {
            getContext().getParent().tell(new Board.Stalled(), getSelf());
        }
    }

    private void cloneBoard(Clone.Board cloneBoard) {
        getContext().getChildren().forEach(child -> child.forward(cloneBoard, getContext()));
    }

    static Props props() {
        return Props.create(CellsUnassignedActor.class);
    }
}
