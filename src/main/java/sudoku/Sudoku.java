package sudoku;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.PatternsCS;
import akka.util.Timeout;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Sudoku {
    public static void main(String[] args) {
//        if (args.length == 0) {
//            System.err.println("A Sudoku 9x9 comma separated grid is required!");
//            System.exit(1);
//        }
        final String easy01 =
                "        2,7,4,3,6,8, ,1,5," +
                        " ,5,3,1, ,7, , , ," +
                        " , ,6,4, , , , ,7," +
                        " , ,1, , , ,7, ,9," +
                        " ,4, ,9, ,3, ,6,1," +
                        "7,8, , ,1, , , , ," +
                        "3, ,5, ,4, ,1,9,8," +
                        "4, , , , , , ,2,3," +
                        " , , ,2,3,5,6, ,4";

        final String medium01 =
                "         , ,6, , , ,7, , ," +
                        "2, , , , ,1,4, , ," +
                        " , , , , , , , ,1," +
                        "6, ,9, ,3, , , , ," +
                        " , ,8,5, , , ,2, ," +
                        " , ,5, ,7, , ,4, ," +
                        "3, , , , , ,1, , ," +
                        " , , , ,9,7,2, , ," +
                        "8, , , , ,4,5,9, ";

        final String difficult01 =
                "         , , , ,5,9,7,4, ," +
                        " , , , , , ,9, , ," +
                        " ,8, ,4, , , , , ," +
                        " ,9, ,2,8, , , ,3," +
                        "4, , ,9, ,5, ,8, ," +
                        "2, , , , ,3, , ,1," +
                        " ,4, , ,6, , , , ," +
                        " ,1, , , , , , , ," +
                        "3,5,9,1, , , , ,6";

        final String difficult02 =
                "         , , ,2,3, ,1, , ," +
                        " ,8, , , , , ,9, ," +
                        " ,3, ,1, , ,8, ,2," +
                        "1, , ,4,2,5, , , ," +
                        " , , , ,9,1, , , ," +
                        " , , , , , ,9,5, ," +
                        "7,2, ,6, , ,5,1, ," +
                        " , ,9, ,7, , , ,6," +
                        " ,5,3, , , ,4, , ";

        final String difficult03 =
                "         ,5, ,7, , , , , ," +
                        " , ,3, , , ,1, ,8," +
                        "1,9,8, ,3, , ,4, ," +
                        " , , , ,4,3,5, , ," +
                        " , ,7,8, ,5,3, , ," +
                        " ,4, ,6, , , , , ," +
                        " , , , , ,2, , , ," +
                        "9, , , , , ,7, , ," +
                        " , , , ,8, ,9,3, ";

        final String difficult04 =
                "         , , ,6, , ,4, , ," +
                        " , ,8,7, , , ,1, ," +
                        " ,1, , , ,5, , ,6," +
                        "4, , , , , , ,9, ," +
                        " , , , , ,3,8,7,5," +
                        " , , ,8, , ,2, , ," +
                        " , ,5,4, ,8, , , ," +
                        " , ,3, ,1, , , , ," +
                        "7, , ,2, , , , ,4";

        final String hard01 =
                "         , , , , ,1, , ,9," +
                        " ,5,1, , ,9, , , ," +
                        " , , , , , ,2, , ," +
                        " , ,6, ,1, ,8,2, ," +
                        " , ,7, , , , ,5, ," +
                        "8, , ,3, , , , , ," +
                        " , ,4, ,8, , ,6,1," +
                        "3, , , , ,4, , , ," +
                        "7, ,5, ,6, ,3, , ";

        final String evil01 =
                "         ,1, ,3, , , ,7,5," +
                        "8, , , , , , , , ," +
                        " , , , ,4,7, ,3,9," +
                        " ,8, , , , , ,9, ," +
                        "1, ,5,6, , , , , ," +
                        " , , ,4, ,5, , , ," +
                        " ,2, ,8, , , , , ," +
                        "7,9, , , , , , ,1," +
                        " , , , ,2,6, ,8, ";

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

        final String diabolical03 =
                "         , , , , ,6, ,3,7," +
                        "1, , , , , , , , ," +
                        " , ,6, ,4, , , , ," +
                        " ,7, , , , , ,2,8," +
                        "3, , ,4, , , , , ," +
                        " ,2, ,3,8, , ,1, ," +
                        "7,9, ,8, , , , , ," +
                        " , , ,9, ,4, , ,3," +
                        " ,5, , , , ,4, ,2";

        Grid grid = new Grid(args.length == 0 ? diabolical03 : args[0]);
        System.out.println("Solve board");
        System.out.println(grid);

        ActorSystem actorSystem = ActorSystem.create("Sudoku");

        long startTime = System.currentTimeMillis();
        ActorRef sudoku = actorSystem.actorOf(SudokuActor.props(), "sudoku");

        Timeout timeout = new Timeout(1, TimeUnit.MINUTES);
        CompletableFuture<Object> responseCF = PatternsCS.ask(sudoku, grid, timeout).toCompletableFuture();

        showResult(startTime, responseCF);
        actorSystem.terminate();
    }

    private static void showResult(long startTime, CompletableFuture<Object> responseCF) {
        try {
            Object response = responseCF.get();

            if (response instanceof Board.Solved) {
                Board.Solved boardSolved = (Board.Solved) response;

                System.out.printf("Board solved %d ms%n", System.currentTimeMillis() - startTime);
                System.out.println(boardSolved.grid);
            } else {
                System.out.printf("Board not solved, %s%n", response);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
