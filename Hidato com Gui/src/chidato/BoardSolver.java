package chidato;

/**
 *
 * @author carlos
 */

import com.microsoft.z3.*;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public final class BoardSolver
{    
    public BoardSolver(Board b) throws Z3Exception
    {
        max = 0;
        ctx = new Context();
        moves = new Stack<>();
        empty_cells = new ArrayList<>();
        
        loadFromInstance(b);
    }
    
    public void loadFromInstance(Board b) throws Z3Exception
    {
        height = b.getHeight();
        width = b.getWidth();
        
        int[][] instance = b.getCells();
        
        countValidCells(instance);
        
        board = new IntExpr[height][width];
        
        BoolExpr lconst = ctx.mkTrue();
        BoolExpr iconst = ctx.mkTrue();
        
        IntExpr exprs[] = new IntExpr[max];
        int z = 0;
        
        IntExpr expr;
        
        for (int i = 0; i < height; ++i)
        {
            for (int j = 0; j < width; ++j)
            {
                if(instance[i][j] < 0)
                {
                    board[i][j] = null;
                    continue;
                }
                
                if(instance[i][j] == 0)
                    empty_cells.add( new Point(j, i) );
                
                                
                expr = (IntExpr) ctx.mkConst(
                                        ctx.mkSymbol("x_"+i+"_"+j),
                                        ctx.getIntSort());
                    
                // Limit constraint: 1 <= xij <= max
                lconst = ctx.mkAnd(lconst, ctx.mkGe(expr, ctx.mkInt(1)));
                lconst = ctx.mkAnd(lconst, ctx.mkLe(expr, ctx.mkInt(max)));
                    
                if (instance[i][j] > 0)
                    // Instance constraint: xij = value
                    iconst = ctx.mkAnd(iconst, ctx.mkEq(expr, ctx.mkInt(instance[i][j])));
                    
                board[i][j] = expr;
                
                exprs[z++] = expr;
                
                

            }
        }
        
        // Distinct constraint: All cells have different values
        BoolExpr dconst = ctx.mkDistinct(exprs);
        
        // Neighbour constraint: The successor to each cell's value must be in an adjacent cell
        BoolExpr nconst = ctx.mkTrue();
        BoolExpr nsubconst;
        
        List<IntExpr> neighbours;

        for (int i = 0; i < height; ++i)
        {
            for (int j = 0; j < width; ++j)
            {
                if(board[i][j] == null)
                    continue;
                
                neighbours = getNeighboringCells(i, j);

                nsubconst = ctx.mkFalse();
                
                // xij < max ==> n1 = (xij +1) \/ n2 = (xij +1) \/ ...
                for (IntExpr nExpr : neighbours)
                {
                    nsubconst = (BoolExpr) ctx.mkOr(
                                nsubconst,
                                ctx.mkImplies(ctx.mkLt(board[i][j], ctx.mkInt(max)),
                                ctx.mkEq(
                                    nExpr,
                                    ctx.mkAdd(board[i][j], ctx.mkInt(1))))).simplify();
                }

                nconst = ctx.mkAnd(nconst, nsubconst);
            }
        }
        
        sol = ctx.mkSolver();
        
        sol.add(lconst);
        sol.add(iconst);
        sol.add(dconst);
        sol.add(nconst);
    }
    
    public void makeMove(int col, int row, int value) throws Z3Exception
    {
        moves.push(new Move(col, row, value) );
        
        sol.push(); 
        sol.add (ctx.mkEq(board[col][row], ctx.mkInt(value)));
        
        empty_cells.remove( new Point(row,col) );
        
        System.out.println("New move added " + moves);
    }
    
    public void undoMove(int value) throws Z3Exception
    {
        ArrayList<Move> temp = new ArrayList<>();
        
        if(moves.isEmpty())
            return;
        
        while(moves.peek().value != value)
        {
            temp.add(moves.pop());
            sol.pop();
        }
        
        if(moves.peek().value == value)
        {
            Move m = moves.pop();
            
            empty_cells.add( new Point(m.col, m.row) );
            
            sol.pop();
        }
        
        for(Move m : temp)
            makeMove(m.col, m.row, m.value);
    }
    
    public Move getHint() throws Z3Exception
    {
        Point p = empty_cells.get(0);
        
        checkSolution();
        
        Move m = new Move (p.y, p.x, getSolution()[p.y][p.x]);
        
        makeMove(m.col, m.row, m.value);
        
        return m;
    }
    
    public boolean checkSolution() throws Z3Exception
    {
        return sol.check() == Status.SATISFIABLE;        
    }
    
    public int getMax()
    {
        return max;
    }
    
    public int[][] getSolution() throws Z3Exception
    {
        int solvedInstance[][] = new int[height][width];
        
        Model m = sol.getModel();
        
        for(int i = 0; i < height; ++i)
        {
            for(int j = 0; j < width; ++j)
            {
                if(board[i][j] == null)
                {
                    solvedInstance[i][j] = -1;
                    continue;
                }

                solvedInstance[i][j] = Integer.parseInt (m.evaluate (board[i][j], false).toString());
            }
        }
        
        return solvedInstance;
    }
    
    private List<IntExpr> getNeighboringCells(int col, int row)
    {
        int first_col = col > 0 ? col - 1 : 0;
        int last_col = col < height - 1 ? col + 1 : height - 1;
        
        int first_row = row > 0 ? row - 1 : 0;
        int last_row = row < width - 1 ? row + 1 : width - 1;
        
        List<IntExpr> neighbours = new ArrayList<>();
        
        for (int i = first_col; i < last_col + 1; ++i)
        {
            for(int j = first_row; j < last_row + 1; ++j)
            {
                if((i == col && j == row) || board[i][j] == null)
                    continue;
                
                neighbours.add(board[i][j]);
            }
        }
        
        return neighbours;
    }
    
    private void countValidCells(int instance[][])
    {
        max = 0;
        
        for (int i = 0; i < height; ++i)
        {
            for (int j = 0; j < width; ++j)
            {
                if (instance[i][j] >= 0)
                    ++max;
            }
        }
    }
    
    
    private final Context ctx;
    private Solver sol;
    private IntExpr board[][];
    private int max;
    
    public class Move
    {
        public Move(int col, int row, int value) {
            this.value = value;
            this.row = row;
            this.col = col;
        }

        @Override
        public String toString() {
            return "Move{" + "row=" + row + ", col=" + col + ", value=" + value + '}';
        }
        
        
        public int row, col;
        public int value;
    }
    
    private Stack<Move> moves;
    
    private int width;
    private int height;

    private ArrayList<Point> empty_cells; 
    
    
    
}
