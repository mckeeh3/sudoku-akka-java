package sudoku;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class Sudoku {
    public static void main(String[] args) {
//        if (args.length == 0) {
//            System.err.println("A Sudoku 9x9 comma separated grid is required!");
//            System.exit(1);
//        }
        final String values =
                "        2,7,4,3,6,8, ,1,5," +
                        " ,5,3,1, ,7, , , ," +
                        " , ,6,4, , , , ,7," +
                        " , ,1, , , ,7, ,9," +
                        " ,4, ,9, ,3, ,6,1," +
                        "7,8, , ,1, , , , ," +
                        "3, ,5, ,4, ,1,9,8," +
                        "4, , , , , , ,2,3," +
                        " , , ,2,3,5,6, ,4";

        Grid grid = new Grid(args.length == 0 ? values : args[0]);


        ActorSystem actorSystem = ActorSystem.create("Sudoku");
        ActorRef sudoku = actorSystem.actorOf(SudokuActor.props(grid), "sudoku");

        // TODO
    }
}
