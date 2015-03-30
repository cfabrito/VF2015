/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import chidato.*;
import com.microsoft.z3.Z3Exception;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 *
 * @author tlsl
 */
public class SwingHidato extends javax.swing.JFrame {

    /**
     * Creates new form NewJFrame
     */
    
    private int CELL_TYPE_BLOCK = -1;
    private int CELL_TYPE_EMPTY = 0;
    
    // GAME MODE:
    // 1 -> incremental
    // 2 -> Ask Value to player
    private int GAME_MODE = 1;
    
    
    
    
    private JButton[][] botoes ;
    private Board board;
    private BoardSolver bsolver;
    private boolean PUZZLE_COMPLETE = false;
    
    
    // Para uma interface de insercao de numeros sequencial
    private int calculate_nextValueToInsert(){
        boolean still_not = false;
        int nextValueToInsert = 1; 
        while( ! still_not && /*NOT MAX*/ nextValueToInsert < botoes.length * botoes[0].length ){
            nextValueToInsert++;
            still_not = true;
            
            for(int i = 0 ; i < botoes.length && still_not ; i++){
                for(int j = 0 ; j < botoes[0].length && still_not ; j++){
  
                    if(Integer.parseInt(botoes[i][j].getText()) ==  nextValueToInsert){
                        still_not = false;
                    }
                }
            }
            
        }
        return nextValueToInsert;
    }
    
    public SwingHidato() {
        initComponents();
        
        createMenuBar();
        
        
        
        
        int[][] instance = { {1 , -1, -2} , 
                             {-1 , -1, -1} , 
                             {-1, -1, 9} };
        
        try {
            board = new Board("test.txt");
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
        
        
        try {
            this.bsolver = new BoardSolver(board);
        } catch (Z3Exception ex) {
            Logger.getLogger(SwingHidato.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        makeBoardGUI(board.getCells());
    }

    private void makeBoardGUI(int[][] instance){
        
          
        this.botoes = new JButton[instance.length][instance[0].length];
        
        
        int x_init = 0, y_init = 0;
        int largura_botao = 60, altura_botao = 40;
        
        this.jPanel1.setPreferredSize(new Dimension(instance.length * largura_botao, 480));
        this.setBounds(50,50,instance.length * largura_botao + 30 , instance[0].length * altura_botao + 70);
        
        
        for(int i = 0 ; i < instance.length ; i++){
            for(int j = 0 ; j < instance[0].length ; j++){
                
                JButton b=new JButton("" + instance[i][j]);//creating instance of JButton
                b.setFocusable(false);
                
                // Button Action
                final int auxx = i;
                final int auxy = j;
                b.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        buttonActionClick( evt , auxx,auxy);
                    }
                }); 
                

                if(instance[i][j] != CELL_TYPE_EMPTY){    
                    b.setEnabled(false);
                }
                
                // caixas bloqueio visiveis mas pretas
                //if(instance[i][j] != -2)
                   // b.setBackground(Color.DARK_GRAY);
                
                b.setBounds(
                        y_init + largura_botao * i /* x axis */,
                        x_init + altura_botao *j /* y axis */,
                        largura_botao /*width */, 
                        altura_botao /*height */
                );
                
                this.botoes[i][j] = b;
                        
                if(instance[i][j] != CELL_TYPE_BLOCK)
                    this.jPanel1.add(b);//adding button in JFrame  
            }
        } 
    }
    
    private String fileChooser(){
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
          File selectedFile = fileChooser.getSelectedFile();
          System.out.println(selectedFile.getPath());
          return selectedFile.getPath();
        }
        return null;
    }
    
    
    private void createMenuBar() {

        
        ImageIcon icon = new ImageIcon("exit.png");

        // MODE MENU ===========================================================
        JMenu mode = new JMenu("Game Mode");
        mode.setMnemonic(KeyEvent.VK_M);
        
        
            JMenuItem eMenuItem = new JMenuItem("Sequencial", icon);
            eMenuItem.setMnemonic(KeyEvent.VK_S);
            eMenuItem.setToolTipText("When pressed a button you try to input the next int value available");
            eMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                   GAME_MODE = 1;
                }
            });
            mode.add(eMenuItem);
            
            eMenuItem = new JMenuItem("Value", icon);
            eMenuItem.setMnemonic(KeyEvent.VK_V);
            eMenuItem.setToolTipText("When pressed a button the value is asked to you");
            eMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                   GAME_MODE = 2;
                }
            });
            mode.add(eMenuItem);
            
        
        // MODE MENU ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^   
            
        // FILE MENU ===========================================================
        
        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);
        
        
            eMenuItem = new JMenuItem("Load", icon);
            eMenuItem.setMnemonic(KeyEvent.VK_L);
            eMenuItem.setToolTipText("Load new puzzle");
            eMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    try {
                        board = new Board(fileChooser());
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "Ficheiro especifado nao pode ser carregado");
                    }
                }
            });
            file.add(eMenuItem);

            eMenuItem = new JMenuItem("Save", icon);
            eMenuItem.setMnemonic(KeyEvent.VK_S);
            eMenuItem.setToolTipText("Save current puzzle");
            eMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    fileChooser();
                }
            });
            file.add(eMenuItem);

            eMenuItem = new JMenuItem("Hint", icon);
            eMenuItem.setMnemonic(KeyEvent.VK_H);
            eMenuItem.setToolTipText("Get a hint for the puzzle");
            eMenuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {

                        if(PUZZLE_COMPLETE)
                            return;
                        
                        //NEEEDS PRE NOT COMPLETE
                        boolean got = false;
                        int i = 0 ,j = 0;
                        Random rnd = new Random();
                        while(!got){
                            i = rnd.nextInt(botoes.length);
                            j = rnd.nextInt(botoes[0].length);
                            got = botoes[i][j].getText().matches("0")?true:false;
                        }

                        boolean valid_hint = false;
                        while(! valid_hint ){
                            int value = rnd.nextInt(botoes.length * botoes[0].length);

                            valid_hint = pushValue(i,j,  value);

                        }

                    }
                });
            file.add(eMenuItem);
            
            eMenuItem = new JMenuItem("Rules", icon);
            eMenuItem.setMnemonic(KeyEvent.VK_R);
            eMenuItem.setToolTipText("Rules");
            eMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    JOptionPane.showMessageDialog(null, printFicheiro("rules.txt"));
                }
            });
            file.add(eMenuItem);
            
            eMenuItem = new JMenuItem("Exit", icon);
            eMenuItem.setMnemonic(KeyEvent.VK_E);
            eMenuItem.setToolTipText("Exit application");
            eMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    System.exit(0);
                }
            });
            file.add(eMenuItem);

        // FILE MENU ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
            
 
        
        
        
        // menubar and add the components
        JMenuBar menubar = new JMenuBar();
        menubar.add(file);
        menubar.add(mode);

        setJMenuBar(menubar);
    }
    
    
    
    
    
    
    
    
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        System.exit(0);
    }//GEN-LAST:event_formWindowClosed

    
   
    
    private void buttonActionClick(java.awt.event.ActionEvent evt , int x , int y) {   
        
        if(! botoes[x][y].getText().equals("0")){
            botoes[x][y].setText("0");
            System.out.println("NEED HELP CAN?T POP VALUE FROM BOARDSOLVER");
            return;
        }
        
        
        int next_to_place = 0;
        
        if(this.GAME_MODE == 1)
            next_to_place = calculate_nextValueToInsert();
        
        if(this.GAME_MODE == 2){
            try{
            next_to_place = Integer.parseInt( JOptionPane.showInputDialog(null, "What value you wanna insert?") );
            } catch (Exception e){
                return;
            }
        }
        
        System.out.println("--" + next_to_place);
        
        this.pushValue(x, y, next_to_place);
      

        System.out.printf("Click [%2d,%2d]\n",x,y);
        
    }                 
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SwingHidato.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SwingHidato.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SwingHidato.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SwingHidato.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                SwingHidato f = new SwingHidato();
                f.setVisible(true);
                
                
                
                
            }
        });
    }
    
    public boolean pushValue(int i , int j , int  value){

        try {
            bsolver.setValue(i,j, value);
            
            
            if( bsolver.checkSolution()){
                botoes[i][j].setText("" + value);
                this.PUZZLE_COMPLETE = check_complete_puzzle();
                return true;
            }
            
            bsolver.undoSetValue();
            
            
        } catch (Z3Exception ex) {
            System.out.println(ex);
        }
        return false;
    }
    
    public boolean check_complete_puzzle(){
        for(int i = 0 ; i < botoes.length ; i++){
            for(int j = 0 ; j < botoes[0].length; j++){
                if(botoes[i][j].getText().matches("0")){
                    return false;
                }
            }
        }
        return true;
    }
    
    
    public static String printFicheiro(String path){
        StringBuilder res = new StringBuilder();
        try {

            String content = "This is the content to write into file";

            File file = new File(path);

            // if file doesnt exists, then create it

            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);


            String line ;
            while((line = br.readLine()) != null) {
                res.append(line + "\n");
            }    


            br.close();

        } catch (IOException e) {
                e.printStackTrace();
        } 
        return res.toString();
    }
    
    
    
    
    
    
    
    
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
