package com.describer;

/**
*
* @author Mike V
*/
@SuppressWarnings("serial")
public class AboutDlg extends javax.swing.JDialog {

   /** Creates new form AboutDlg */
   public AboutDlg(java.awt.Frame parent, String title, boolean modal) {
       super(parent, title, modal);
       initComponents();
   }

   /** This method is called from within the constructor to
    * initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is
    * always regenerated by the Form Editor.
    */
   private void initComponents() {

       jLabel1 = new javax.swing.JLabel();
       jLabel2 = new javax.swing.JLabel();
       jLabel3 = new javax.swing.JLabel();
       jLabel4 = new javax.swing.JLabel();
       jLabel5 = new javax.swing.JLabel();

       setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

       jLabel1.setText("Entity Trees Editor, 1.0");

       jLabel2.setText("Author: Mike V. Vostrikov");

       jLabel3.setText("BMSTU, IU5, 2010");

       jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/describer/resources/images/logo.png"))); // NOI18N

       jLabel5.setText("Advisor: Vinogradova M.V.");

       javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
       getContentPane().setLayout(layout);
       layout.setHorizontalGroup(
           layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
           .addGroup(layout.createSequentialGroup()
               .addGap(6, 6, 6)
               .addComponent(jLabel4)
               .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
               .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                   .addComponent(jLabel3)
                   .addComponent(jLabel2)
                   .addComponent(jLabel1)
                   .addComponent(jLabel5))
               .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
       );
       layout.setVerticalGroup(
           layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
           .addGroup(layout.createSequentialGroup()
               .addContainerGap()
               .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                   .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                   .addGroup(layout.createSequentialGroup()
                       .addComponent(jLabel1)
                       .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                       .addComponent(jLabel2)
                       .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                       .addComponent(jLabel5)))
               .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
               .addComponent(jLabel3)
               .addContainerGap())
       );

       pack();
   }// </editor-fold>

   // Variables declaration - do not modify
   private javax.swing.JLabel jLabel1;
   private javax.swing.JLabel jLabel2;
   private javax.swing.JLabel jLabel3;
   private javax.swing.JLabel jLabel4;
   private javax.swing.JLabel jLabel5;
   // End of variables declaration

}
