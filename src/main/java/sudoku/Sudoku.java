package sudoku;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class Sudoku {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("A Sudoku 9x9 comma separated grid is required!");
            System.exit(1);
        }
        ActorSystem actorSystem = ActorSystem.create("Sudoku");
        ActorRef sudoku = actorSystem.actorOf(SudokuActor.props(new Grid(args[0])), "sudoku");

    }
}
