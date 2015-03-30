package chidato;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author carlos
 */
public class Board
{    
    public Board(String filepath) throws FileNotFoundException, IOException
    {
        loadFromFile(filepath);
    }
    
    public Board(int[][] instance)
    {
        loadFromArray(instance);
    }
    
    public int getHeight()
    {
        return height;
    }
    
    public int getWidth()
    {
        return width;
    }
    
    private void loadFromFile(String filepath) throws FileNotFoundException, IOException
    {
        BufferedReader inputStream = new BufferedReader(new FileReader(filepath));
        Scanner scanner = new Scanner(inputStream);
                
        width = scanner.nextInt();
        height = scanner.nextInt();
        
        cells = new int[height][width];
        
        int value;
        for (int i = 0; i < height; ++i)
        {
            for (int j = 0; j < width; ++j)
            {                
                if(!scanner.hasNextInt())
                    throw new RuntimeException("Invalid board file");
                
                value = scanner.nextInt();
                                
                cells[i][j] = value;
            }
        }
    }
    
    private void loadFromArray(int instance[][])
    {
        height = instance.length;
        width = instance[0].length;
        
        cells = instance;
    }
    
    public int[][] getCells()
    {
        return cells;
    }
    
    public String getDisplay()
    {
        StringBuilder sb = new StringBuilder();
        
        int value;
        String disp;
        
        for (int i = 0; i < height; ++i)
        {
            for (int j = 0; j < width; ++j)
            {
                value = cells[i][j];
                
                if (value < 0)
                    disp = "x";
                else if (value == 0)
                    disp = "-";
                else
                    disp = Integer.toString(value);
                
                sb.append(String.format("%3s", disp));
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    private int width, height;
    private int[][] cells;
}
