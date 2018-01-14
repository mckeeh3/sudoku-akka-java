package sudoku;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class Sudoku {
    public static void main(String[] args) {
//        if (args.length == 0) {
//            System.err.println("A Sudoku 9x9 comma separated grid is required!");
//            System.exit(1);
//        }
        final String valuesEasy01 =
                "        2,7,4,3,6,8, ,1,5," +
                        " ,5,3,1, ,7, , , ," +
                        " , ,6,4, , , , ,7," +
                        " , ,1, , , ,7, ,9," +
                        " ,4, ,9, ,3, ,6,1," +
                        "7,8, , ,1, , , , ," +
                        "3, ,5, ,4, ,1,9,8," +
                        "4, , , , , , ,2,3," +
                        " , , ,2,3,5,6, ,4";

        final String valuesMedium01 =
                "         , ,6, , , ,7, , ," +
                        "2, , , , ,1,4, , ," +
                        " , , , , , , , ,1," +
                        "6, ,9, ,3, , , , ," +
                        " , ,8,5, , , ,2, ," +
                        " , ,5, ,7, , ,4, ," +
                        "3, , , , , ,1, , ," +
                        " , , , ,9,7,2, , ," +
                        "8, , , , ,4,5,9, ";

        final String valuesDifficult01 =
                "         , , , ,5,9,7,4, ," +
                        " , , , , , ,9, , ," +
                        " ,8, ,4, , , , , ," +
                        " ,9, ,2,8, , , ,3," +
                        "4, , ,9, ,5, ,8, ," +
                        "2, , , , ,3, , ,1," +
                        " ,4, , ,6, , , , ," +
                        " ,1, , , , , , , ," +
                        "3,5,9,1, , , , ,6";

        final String valuesDifficult02 =
                "         , , ,2,3, ,1, , ," +
                        " ,8, , , , , ,9, ," +
                        " ,3, ,1, , ,8, ,2," +
                        "1, , ,4,2,5, , , ," +
                        " , , , ,9,1, , , ," +
                        " , , , , , ,9,5, ," +
                        "7,2, ,6, , ,5,1, ," +
                        " , ,9, ,7, , , ,6," +
                        " ,5,3, , , ,4, , ";

        final String valuesHard01 =
                "         , , , , ,1, , ,9," +
                        " ,5,1, , ,9, , , ," +
                        " , , , , , ,2, , ," +
                        " , ,6, ,1, ,8,2, ," +
                        " , ,7, , , , ,5, ," +
                        "8, , ,3, , , , , ," +
                        " , ,4, ,8, , ,6,1," +
                        "3, , , , ,4, , , ," +
                        "7, ,5, ,6, ,3, , ";

        final String diabolical01 =
                "        8, ,4,3, ,5, , ,1," +
                        " ,7, ,4, ,1, , ,8," +
                        " , , , , , , , , ," +
                        "9, , , ,4, , , , ," +
                        "4,1, , , ,2, , ,3," +
                        " , , , ,5, , ,2, ," +
                        " ,2, , , , , ,8,9," +
                        "6,8, , , , ,4, ,2," +
                        " , , ,5, , , , ,7";

        final String diabolical02 =
                "         , ,5, ,1,9,8, ,6," +
                        " , ,4, , , , , , ," +
                        " , , , , ,8, , ,5," +
                        "1, , ,9, , , , ,2," +
                        "5, , , , , ,7, , ," +
                        " , ,3,6, , , ,1,8," +
                        "6, , ,7, , , ,5, ," +
                        " ,9,8, , ,5, , , ," +
                        " , , ,1, , ,2, , ";

        String gridValues = diabolical02;
        Grid grid = new Grid(args.length == 0 ? gridValues : args[0]);

        ActorSystem actorSystem = ActorSystem.create("Sudoku");
        ActorRef sudoku = actorSystem.actorOf(SudokuActor.props(grid), "sudoku");

        // TODO
    }
}
