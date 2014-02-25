/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdario.hlea4tc;

import jade.core.AID;
import java.awt.Dimension;
import java.awt.Toolkit;

/**
 *
 * @author cesar
 */
public class IntersectionGUI extends javax.swing.JFrame {

    private Intersection myIntersection;

    /**
     * Creates new form IntersectionGUI
     */
    public IntersectionGUI() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelIntersection = new javax.swing.JLabel();
        labelGreen = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        labelIntersection.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        labelIntersection.setText("00");

        labelGreen.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        labelGreen.setForeground(new java.awt.Color(0, 153, 102));
        labelGreen.setText("00");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addComponent(labelIntersection))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(77, 77, 77)
                        .addComponent(labelGreen)))
                .addContainerGap(76, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(labelIntersection)
                .addGap(33, 33, 33)
                .addComponent(labelGreen)
                .addContainerGap(52, Short.MAX_VALUE))
        );

        labelIntersection.getAccessibleContext().setAccessibleName("lableName");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    IntersectionGUI(Intersection i) {
        super(i.getLocalName());
        myIntersection = i;
         initComponents();
        labelIntersection.setText(i.getLocalName());
        labelGreen.setText("" + myIntersection.remainingGreen);
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
            java.util.logging.Logger.getLogger(IntersectionGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(IntersectionGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(IntersectionGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(IntersectionGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new IntersectionGUI().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel labelGreen;
    private javax.swing.JLabel labelIntersection;
    // End of variables declaration//GEN-END:variables

    public void showGui() {
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (int) screenSize.getWidth() / 2;
        int centerY = (int) screenSize.getHeight() / 2;
        setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
        super.setVisible(true);
    }

    @Override
    public void repaint() {
        labelGreen.setText("" + myIntersection.remainingGreen);
        super.repaint(); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
