/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hidato;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import com.microsoft.z3.Z3Exception;
import java.util.ArrayList;

/**
 *
 * @author tlsl
 */
public class ancientCode {
      
      void HiDatoStatic(Context ctx) throws Z3Exception
    {
        
        int imax = 3;
        int jmax = 3;
        int nmaxcasas = imax * jmax;
                
                
        // imax X jmax  matrix of integer variables
        IntExpr[][] X = new IntExpr[imax][];
        for (int i = 0; i < imax; i++)
        {
            X[i] = new IntExpr[jmax];
            for (int j = 0; j < jmax; j++)
                X[i][j] = (IntExpr) ctx.mkConst(
                        ctx.mkSymbol("x_" + (i + 1) + "_" + (j + 1)),
                        ctx.getIntSort());
        }
        
        // each cell contains a value in {1, ..., nmaxcasas}
        BoolExpr[][] cells_c = new BoolExpr[imax][];
        for (int i = 0; i < imax; i++)
        {
            cells_c[i] = new BoolExpr[jmax];
            for (int j = 0; j < jmax; j++)
                cells_c[i][j] = ctx.mkAnd(ctx.mkLe(ctx.mkInt(1), X[i][j]),
                        ctx.mkLe(X[i][j], ctx.mkInt(nmaxcasas)));
        }
        
        
        // All cells distinct
       
        IntExpr[] aux = new IntExpr[imax * jmax];
        int it = 0;
        for (int i = 0; i < imax; i++){
            for (int j = 0; j < jmax; j++){
                aux[it] = X[i][j];
                it++;
            }
        }
        BoolExpr n_distinct = ctx.mkDistinct(aux);
        
        
        // Each cell must have a neightbour that his number is +1 of the actual
        ArrayList<BoolExpr> sq_c = new ArrayList<>();
        for (int i0 = 0; i0 < imax; i0++)
        {
            for (int j0 = 0; j0 < jmax; j0++){
                //IntExpr[] square = new IntExpr[9];
                
                // cada celula tem de ter uma vizinha cujo numero seja +1
                ArrayList<BoolExpr> auxa = new ArrayList<>();
                for (int i = -1; i < 2; i++){
                    for (int j = -1; j < 2; j++){
                        int x = i0 + i;
                        int y = j0 + j;
                        
                        if(x < 0 || y < 0 || x >= imax || y >= jmax || (x==i0 && y == j0) ){}
                        else {
                            auxa.add(ctx.mkEq(X[i0][j0], ctx.mkSub(X[x][y],ctx.mkInt(1))));   
                        }
                    }
                }
                
                
                // OU
                
                // Todas as vizinhas sao menores que a celula (max cell)
                ArrayList<BoolExpr> auxb = new ArrayList<>();
                for (int i = -1; i < 2; i++){
                    for (int j = -1; j < 2; j++){
                        int x = i0 + i;
                        int y = j0 + j;

                        if(x < 0 || y < 0 || x >= imax || y >= jmax || (x==i0 && y == j0)  ){}
                        else {
                            auxb.add(ctx.mkLe( X[x][y] , X[i0][j0]));   
                        }
                    }
                }
                
                // cada celula tem de ter uma vizinha cujo numero seja +1
                BoolExpr a = ctx.mkTrue();
                if(auxa.size() > 0){
                    BoolExpr[] arraya = auxa.toArray(new BoolExpr[auxa.size()]);
                    a = ctx.mkOr(arraya);
                }
                // OU
                
                // Todas as vizinhas sao menores que a celula (max cell)
                BoolExpr b = ctx.mkTrue();
                if(auxb.size() > 0){
                    
                    
                    BoolExpr[] arrayb = auxb.toArray(new BoolExpr[auxb.size()]);
                    b = ctx.mkAnd(arrayb);
                }        
                
                sq_c.add(ctx.mkOr(a,b));
            }
        }
        
        BoolExpr[] array = sq_c.toArray(new BoolExpr[sq_c.size()]);
        BoolExpr cell_rule = ctx.mkAnd(array);
        
        System.out.println(cell_rule);
        

        // Concatenation of Rules
        BoolExpr Hidato_c = ctx.mkTrue();
        for (BoolExpr[] t : cells_c)
            Hidato_c = ctx.mkAnd(ctx.mkAnd(t), Hidato_c);
        Hidato_c = ctx.mkAnd(cell_rule,Hidato_c);
        Hidato_c = ctx.mkAnd(ctx.mkAnd(n_distinct), Hidato_c);

     
        int[][] instance = { {1 , -1, -1} , 
                             {-1 , -1, -1} , 
                             {-1, -1, 9} };
        
        // Definindo instancia no contexto atual
        BoolExpr instance_c = ctx.mkTrue();
        for (int i = 0; i < imax; i++)
            for (int j = 0; j < jmax; j++)
                instance_c = ctx.mkAnd( 
                        instance_c, 
                        (BoolExpr) ctx.mkITE(
                                ctx.mkEq(ctx.mkInt(instance[i][j]),
                                        ctx.mkInt(-1)), 
                                ctx.mkTrue(),
                                ctx.mkEq(X[i][j], ctx.mkInt(instance[i][j])))
                       
                );
        
        
        Solver s = ctx.mkSolver();
        s.add(Hidato_c);
        s.add(instance_c);

        if (s.check() == Status.SATISFIABLE)
        {
            Model m = s.getModel();
            Expr[][] R = new Expr[9][9];
            for (int i = 0; i < imax; i++)
                for (int j = 0; j < jmax; j++)
                    R[i][j] = m.evaluate(X[i][j], false);
            System.out.println("Hidato solution:");
            for (int i = 0; i < imax; i++)
            {
                for (int j = 0; j < jmax; j++)
                    System.out.print(" " + R[i][j]);
                System.out.println();
            }
        } else
        {
            System.out.println("Failed to solve Hidato");
        }
        
    }
        
           
}
