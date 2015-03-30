package chidato;

import com.microsoft.z3.Z3Exception;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author carlos
 */
public class Hidato
{
    public static void printUsage()
    {
        System.out.println("Usage: hidato [board_file.txt]");
    }
    public static void main(String[] args2)
    {
        String[] args = {"test.txt"};
        System.out.println(args.length);
        
        if(args.length < 1)
        {
            printUsage();
            return;
        }
        
        String filename = args[0];
        
        System.out.println("Loading \"" + filename + "\"...");
        Board board;
        
        try
        {
            board = new Board(filename);
        }
        catch(FileNotFoundException e)
        {
            System.out.println("File not found - " + e.getMessage());
            return;
        }
        catch (Exception e)
        {
            System.out.println("IOException - " + e.getMessage());
            return;
        }
        
        System.out.println("Done");
        
        System.out.println("Board:");
        
        System.out.println(board.getDisplay());
        
        try
        {
            BoardSolver bs = new BoardSolver(board);
            
            bs.setValue(0, 1, 3);
            bs.undoSetValue();
            
            if (bs.checkSolution())
            {
                System.out.println("Solution found");
                Board solved = new Board (bs.getSolution());
                
                System.out.println(solved.getDisplay());
            }
            else 
                System.out.println("No solution found");
        }
        catch(Z3Exception e)
        {
            System.err.println("Caught Z3Exception: " + e.getMessage());
        }
    }

}
