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
    
    private JButton[][] botoes = null;
    private Board board;
    private BoardSolver bsolver;
    private boolean PUZZLE_COMPLETE = false;
    
    private int next_value = 1;
    
    
    
    // Para uma interface de insercao de numeros sequencial
    private int calculate_nextValueToInsert(){
        boolean still_not = false;
        int nextValueToInsert = 1;
        
        System.out.println(board.getDisplay());
        
        while( !still_not && nextValueToInsert < bsolver.getMax() )
        {
            nextValueToInsert++;
            still_not = true;
            
            for(int i = 0 ; i < board.getHeight() && still_not ; i++)
            {
                for(int j = 0 ; j < board.getWidth() && still_not ; j++)
                {
                    if(board.getCell(i, j) ==  nextValueToInsert)
                    {
                        still_not = false;
                    }
                }
            }
            
        }
        
        this.jTextField1.setText(nextValueToInsert +"");
        
        return nextValueToInsert;
    }
    
    public SwingHidato() {
        initComponents();
        
        createMenuBar();
        
        loadNewBoard("test.txt");
    }

    private void deleteButtons(){
        if(botoes != null)
        {
            for(JButton[] bs : botoes){
                for(JButton b : bs){
                    b.setVisible(false);
                }
            }
        }
    }
    private void makeBoardGUI()
    {
        int height = board.getHeight();
        int width = board.getWidth();
        
        
        deleteButtons();
        
        this.botoes = new JButton[height][width];
        
        int x_init = 0, y_init = 0;
        int largura_botao = 60, altura_botao = 40, altura_menu = 70;
       
        int status_width  = 120;
        int status_height = this.jPanel2.getHeight() + altura_menu;
        
        int window_width  = height * largura_botao + 30 + status_width;
        
        int window_height = status_height > width * altura_botao + altura_menu? status_height : width * altura_botao + altura_menu;
        
        this.jPanel1.setPreferredSize(new Dimension(height * largura_botao, 480));
        this.setBounds(50,50,window_width, window_height);
                
        int[][] instance = board.getCells();
        
        for(int i = 0 ; i < height; i++){
            for(int j = 0 ; j < width; j++){
                JButton b;
                if(instance[i][j] == 0)
                    b = new JButton(" ");//creating instance of JButton
                else
                    b = new JButton("" + instance[i][j]);//creating instance of JButton
                
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
                //if(instance[i][j] != 0)
                   // b.setBackground(Color.DARK_GRAY);
                
                b.setBounds(
                        y_init  + largura_botao  *j /* y axis */,
                        x_init  +  altura_botao* i /* x axis */,
                        largura_botao /*width */, 
                        altura_botao /*height */
                );
                
                this.botoes[i][j] = b;
                        
                if(instance[i][j] != CELL_TYPE_BLOCK)
                    this.jPanel1.add(b);//adding button in JFrame  
            }
        } 
    }
    
    private void loadNewBoard(String filepath)
    {
        try {            
            board = new Board(filepath);
            bsolver = new BoardSolver(board);
            makeBoardGUI();
            
            next_value = calculate_nextValueToInsert();
            
        } catch (IOException ex) {
            System.out.println("IOException at loadNewBoard(): " + ex.getMessage());
        } catch (Z3Exception ex) {
            System.out.println("Z3Exception at loadNewBoard(): " + ex.getMessage());
        }
    }
    
    private String fileChooser(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("./boards"));
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

        
        JMenuItem eMenuItem ;
            
        
        // FILE MENU ===========================================================
        
        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);
        
        
            eMenuItem = new JMenuItem("Load", icon);
            eMenuItem.setMnemonic(KeyEvent.VK_L);
            eMenuItem.setToolTipText("Load new puzzle");
            eMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    loadNewBoard(fileChooser());
                    System.out.println(board.getDisplay());
                }
            });
            file.add(eMenuItem);

            eMenuItem = new JMenuItem("Save", icon);
            eMenuItem.setMnemonic(KeyEvent.VK_S);
            eMenuItem.setToolTipText("Save current puzzle");
            eMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    try {
                        board.saveToFile(fileChooser());
                    } catch (IOException ex) {
                        Logger.getLogger(SwingHidato.class.getName()).log(Level.SEVERE, null, ex);
                    }
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
                        
                        try {
                            //NEEEDS PRE NOT COMPLETE
                            BoardSolver.Move m = bsolver.getHint();
                            board.setCell(m.row, m.col, m.value);
                            botoes[m.col][m.row].setText(m.value+"");
                            
                            next_value = calculate_nextValueToInsert();
                            
                        } catch (Z3Exception ex) {
                            Logger.getLogger(SwingHidato.class.getName()).log(Level.SEVERE, null, ex);
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

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

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

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setPreferredSize(new java.awt.Dimension(100, 150));

        jTextField1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextField1FocusGained(evt);
            }
        });

        jLabel2.setText("Next Value :");

        jButton1.setText("Play for Me");
        jButton1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addGap(12, 12, 12)
                            .addComponent(jTextField1))
                        .addComponent(jLabel2))
                    .addComponent(jButton1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1)
                .addContainerGap(59, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        System.exit(0);
    }//GEN-LAST:event_formWindowClosed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if(PUZZLE_COMPLETE)
            return;

        try {
            //NEEEDS PRE NOT COMPLETE
            BoardSolver.Move m = bsolver.getHint();
            board.setCell(m.row, m.col, m.value);
            botoes[m.col][m.row].setText(m.value+"");

            next_value = calculate_nextValueToInsert();

        } catch (Z3Exception ex) {
            Logger.getLogger(SwingHidato.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTextField1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField1FocusGained
        
    }//GEN-LAST:event_jTextField1FocusGained

    
   
    
    private void buttonActionClick(java.awt.event.ActionEvent evt , int x , int y) {   
        
        if(! botoes[x][y].getText().equals(" ")){
            
            try {
                board.setCell(x, y, 0);
                bsolver.undoMove(Integer.parseInt(botoes[x][y].getText()));
                
                
            } catch (Z3Exception ex) {
                Logger.getLogger(SwingHidato.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            next_value = calculate_nextValueToInsert();
            
            
            botoes[x][y].setText(" ");
            
            return;
        }
       
        
        int next_to_place = 0;
        
        
        try{
            next_to_place = Integer.parseInt( this.jTextField1.getText() );
        } catch (Exception e){
            return;
        }
         
        
        this.pushValue(x, y, next_to_place);
      

        System.out.printf("Click [%2d,%2d]\n",x,y);
        
        //System.out.println(bsolver.getSol().toString());
        
        this.next_value = calculate_nextValueToInsert();
        
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
            bsolver.makeMove(i,j, value);
            
            
            if( bsolver.checkSolution()){
                botoes[i][j].setText("" + value);
                board.setCell(i, j, value);
                
                if(check_complete_puzzle())
                    JOptionPane.showMessageDialog(null, printFicheiro("win.txt"));
                
                return true;
            }
            
            bsolver.undoMove(value);
            
            
        } catch (Z3Exception ex) {
            System.out.println(ex);
        }
        return false;
    }
    
    public boolean check_complete_puzzle(){
        for(int i = 0 ; i < botoes.length ; i++){
            for(int j = 0 ; j < botoes[0].length; j++){
                if(botoes[i][j].getText().matches(" ")){
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
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
