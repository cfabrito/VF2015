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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tlsl
 */
public class Hidato {

    public static final String ANSI_B_YELLOW = "\u001B[46m";
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String	BACKGROUND_BLACK	= "\u001B[47m";
    
    public Hidato() {
    }
 
     public static void main(String[] args) throws Z3Exception, Exception
    {
        Hidato h = new Hidato();
        
        
                HashMap<String, String> cfg = new HashMap<String, String>();
                cfg.put("model", "true");
                Context ctx = new Context(cfg);

                
                int[][] instance = { {1 , -1, -1} , 
                             {-1 , -1, -1} , 
                             {-1, -1, 9} };
                
                //h.HiDato(ctx,instance);
                
                //int[][] instance2 = h.readPuzzle();
                
                //h.writePuzzle(instance, "default.txt");
                
                h.MenuGame(ctx);
    }
    
    
    void printTabuleiro(int[][] instance){
        System.out.print(BACKGROUND_BLACK);
        for(int[] aux : instance){
            System.out.print(BACKGROUND_BLACK);
            System.out.print("+");
            for(int i = 0 ; i < aux.length ; i++){
                System.out.print("-----+");
            }
            
            
            System.out.print("\n|");
            for(int aux2 : aux ){
                if(aux2 == -1){
                    System.out.printf("%s%4s %s|",ANSI_B_YELLOW," ",BACKGROUND_BLACK);
                }
                else{
                    System.out.printf("%s%4s %s|",BACKGROUND_BLACK,aux2,BACKGROUND_BLACK);
                }
            }
            System.out.println();
        }
        System.out.print(BACKGROUND_BLACK);
        System.out.print("+");
        for(int i = 0 ; i < instance[0].length ; i++){
            System.out.print("-----+");
        }
        System.out.println();
        System.out.print(ANSI_RESET);
    }
     
     
    void MenuGame(Context ctx) throws Z3Exception{
        
        Scanner inp = new Scanner(System.in);
        
        String path_tabuleiro_de_jogo_atual = "default.txt";
        
        
        String opcoes [][] =  {{"x <coordX> <coordY> <valor>","coloca valor se possivel"},
                               {"r <coordX> <coordY>","remove valor caso possivel"},
                               {"l <path>","carrega puzzle"},
                               {"g <path>","grava puzzle"},
                               {"reset","limpa tabuleiro"}
                                
        };
        
        
        int[][] instance_base = { {1 , -1, -1} , 
                             {-1 , -1, -1} , 
                             {-1, -1, 9} };
        int[][] instance_working = { {1 , -1, -1} , 
                             {-1 , -1, -1} , 
                             {-1, -1, 9} };
        try{
            instance_base = readPuzzle(path_tabuleiro_de_jogo_atual);
            instance_working = readPuzzle(path_tabuleiro_de_jogo_atual);
        } catch(Exception e){}
        
        
        
        
        for(String [] aux : opcoes){
            System.out.printf("%s - %s\n",aux[0],aux[1]);
        }
        
        printTabuleiro(instance_working);
        String input;
        System.out.print("@> ");
        while( (input = inp.nextLine() ) != null ){
            String args[] = input.split(" ",0);
            
            int x,y;
            try{
            switch(args[0]){
                case "x":
                    x = Integer.parseInt(args[1]) - 1 ;
                    y = Integer.parseInt(args[2]) - 1 ;
                    int value = Integer.parseInt(args[3]);
                    instance_working[x][y] = value;
                    if(HiDato(ctx,instance_working) == false){
                        System.out.println("Jogada nao realizada pois torna tabuleiro de realizacao impossivel.");
                        instance_working[x][y] = -1;
                    }
                    else{
                        // Check if complete
                        boolean complete = true;
                        for(int aux = 0 ; aux < instance_working.length && complete; aux++)
                            for(int auxa = 0 ; auxa < instance_working[aux].length && complete; auxa++)
                                if(instance_working[aux][auxa] == -1){
                                    complete = false;
                                }
                        
                        if(complete)                            
                            System.out.println(
                            "\n( )( )( )( )( )( )( )( )( )( )( )( )( )( )( )( )( )( )( )( )( )( )( )( )\n" +
                            "-H--H--H--H--H--H--H--H--  CONGRATZ U MADE IT  --H--H--H--H--H--H--H--H-\n" +
                            "(_)(_)(_)(_)(_)(_)(_)(_)(_)(_)(_)(_)(_)(_)(_)(_)(_)(_)(_)(_)(_)(_)(_)(_)\n"
                            );
                        
                    }
                    break;
                case "r":
                    x = Integer.parseInt(args[1]) - 1 ;
                    y = Integer.parseInt(args[2]) - 1 ;
                    if(instance_base[x][y] != -1){
                        System.out.println("Casa Reservada");
                    }
                    else{
                        instance_working[x][y] = -1;
                    }
                    break;
                case "l":
                    {
                        try {
                            instance_base = readPuzzle(args[1]);
                            instance_working = readPuzzle(args[1]);
                            System.out.printf("Ficheiro %s carregado com sucesso!\n",args[1]);
                        } catch (Exception ex) {
                            System.out.println("Ficheiro Especificado nao existe");
                        }
                    }
                    
                    break;
                case "g":
                    System.exit(0);
                    break;
                case "q":
                    System.exit(0);
                    break;
                case "reset":
                    {
                        try {
                            instance_base = readPuzzle(path_tabuleiro_de_jogo_atual);
                            instance_working = readPuzzle(path_tabuleiro_de_jogo_atual);
                            System.out.printf("Ficheiro %s carregado com sucesso!\n",path_tabuleiro_de_jogo_atual);
                        } catch (Exception ex) {
                            System.out.println("Ficheiro Especificado nao existe");
                        }
                    }
                    break;
            }
            
            } catch (NumberFormatException e){
                System.out.println("Nao foi possivel fazer parse de um numero, tente outra vez.");
            }
            
            printTabuleiro(instance_working);
            System.out.print("@> ");
        
        }
    }
     
    
     
     
     
     
     
      
    boolean HiDato(Context ctx,int[][] instance) throws Z3Exception
    {
        
        int imax = instance.length;
        int jmax = instance[0].length;
        
        int nmaxcasas = imax * jmax;  // Need a Tweek devido a puzzles nao quadrados
                
                
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
                    
                    auxb.add(
                            ctx.mkEq(X[i0][j0],
                                    ctx.mkInt(imax*jmax)
                            )
                    );
                    
                    BoolExpr[] arrayb = auxb.toArray(new BoolExpr[auxb.size()]);
                    b = ctx.mkAnd(arrayb);
                }        
                
                sq_c.add(ctx.mkOr(a,b));
            }
        }
        
        BoolExpr[] array = sq_c.toArray(new BoolExpr[sq_c.size()]);
        BoolExpr cell_rule = ctx.mkAnd(array);
        
        //System.out.println(cell_rule);
        

        // Concatenation of Rules
        BoolExpr Hidato_c = ctx.mkTrue();
        for (BoolExpr[] t : cells_c)
            Hidato_c = ctx.mkAnd(ctx.mkAnd(t), Hidato_c);
        Hidato_c = ctx.mkAnd(cell_rule,Hidato_c);
        Hidato_c = ctx.mkAnd(ctx.mkAnd(n_distinct), Hidato_c);

        
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

//        if (s.check() == Status.SATISFIABLE)
//        {
//            Model m = s.getModel();
//            Expr[][] R = new Expr[9][9];
//            for (int i = 0; i < imax; i++)
//                for (int j = 0; j < jmax; j++)
//                    R[i][j] = m.evaluate(X[i][j], false);
//            System.out.println("Hidato solution:");
//            for (int i = 0; i < imax; i++)
//            {
//                for (int j = 0; j < jmax; j++)
//                    System.out.print(" " + R[i][j]);
//                System.out.println();
//            }
//        } else
//        {
//            System.out.println("Failed to solve Hidato");
//        }
        
        return (s.check()== Status.SATISFIABLE)?true:false;
        
    }
        
           
     
     
     
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
        
           
      
    public void writePuzzle(int[][] instance,String path) throws IOException{
        File file = new File(path);
 
        if (!file.exists()) {
                file.createNewFile();
        }

        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        
        bw.write(instance.length+"\n");
        bw.write(instance[0].length+"\n");
        for(int[] aux : instance){
            for(int aux1 : aux){
                switch(aux1){
                    case -1:
                        bw.write("_ "); 
                        break;
                    case -2:
                        bw.write("X "); 
                        break;
                    default :
                        bw.write(aux1 + " "); 
                }
            }
            bw.write("\n");
        }
        
        bw.close();

        System.out.println("Done");
    }
    
    public int[][] readPuzzle(String path) throws FileNotFoundException, IOException, Exception{

      FileReader fr = new FileReader(path);
      BufferedReader br = new BufferedReader(fr);
      
      
      int imax = Integer.parseInt(br.readLine());
      int jmax = Integer.parseInt(br.readLine());
      
      if(imax <= 0 || jmax <= 0){
          throw new Exception("tabuleiro invalido em termos de dimensoes");
      }
      
      int[][] instance = new int[imax][jmax];
      String s;
      int i = 0;
      while((s = br.readLine()) != null) {
          String[] line = s.split(" ",0);
          int j = 0;
          for(String aux : line){
              switch(aux){
                  case "X" :
                      instance[i][j] = -1;
                      break;
                  case "_" :
                      instance[i][j] = -1;
                      break;
                  default: 
                      instance[i][j] = Integer.parseInt(aux);
              }
              
              j++;
          };
          i++;
      }
      fr.close();
      
      //this.printTabuleiro(instance);
      return instance;
    }
}
