package utils.manual;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import lombok.SneakyThrows;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Manual_Scheduler implements ManualSchedulerInterface {
   private JTextField textField;
   private JButton addAllReleasesInFolder;
   private JPanel panel;
   private JLabel enterFolderPathLabel;
   private JButton addOne;
   private JFrame frame;
   
   @SneakyThrows
   Manual_Scheduler() {
      init();
      addAllReleasesInFolder.addActionListener(e -> {
         String path = textField.getText();
         File file = new File(path);
         File[] mainFolder = file.listFiles();
         new Thread(() -> {
            for (File releaseFolder : mainFolder) {
               checkRelease(releaseFolder);
            }
            enterFolderPathLabel.setText("DONE");
            textField.setText("");
         }).start();
         
      });
   
      addOne.addActionListener(e -> {
         String path = textField.getText();
         File release = new File(path);
         new Thread(() -> {
            String name = release.getName();
            enterFolderPathLabel.setText("Adding: " + name);
            checkRelease(release);
            enterFolderPathLabel.setText("DONE");
            textField.setText("");
         }).start();
         
      });
      
   }
   
   public static void main(String[] args) {
      SwingUtilities.invokeLater(() -> {
         Manual_Scheduler display = new Manual_Scheduler();
         display.frame.setVisible(true);
      });
   }
   
   @SneakyThrows
   private void init() {
      frame = new JFrame("Manual Scheduler");
      frame.add(panel);
      frame.setAlwaysOnTop(true);
      UIManager.setLookAndFeel(new WindowsLookAndFeel());
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      // frame.setUndecorated(true);
      frame.pack();
   }
   
   {
      // GUI initializer generated by IntelliJ IDEA GUI Designer
      // >>> IMPORTANT!! <<<
      // DO NOT EDIT OR ADD ANY CODE HERE!
      $$$setupUI$$$();
   }
   
   /**
    * Method generated by IntelliJ IDEA GUI Designer
    * >>> IMPORTANT!! <<<
    * DO NOT edit this method OR call it in your code!
    *
    * @noinspection ALL
    */
   private void $$$setupUI$$$() {
      panel = new JPanel();
      panel.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
      panel.setMinimumSize(new Dimension(400, 150));
      panel.setPreferredSize(new Dimension(400, 150));
      textField = new JTextField();
      Font textFieldFont = this.$$$getFont$$$("Droid Sans Mono", Font.BOLD, 18, textField.getFont());
      if (textFieldFont != null)
         textField.setFont(textFieldFont);
      panel.add(textField, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, 30), null, 0, false));
      enterFolderPathLabel = new JLabel();
      Font enterFolderPathLabelFont = this.$$$getFont$$$("Droid Sans Mono", Font.BOLD, 18, enterFolderPathLabel.getFont());
      if (enterFolderPathLabelFont != null)
         enterFolderPathLabel.setFont(enterFolderPathLabelFont);
      enterFolderPathLabel.setText("Enter Folder Path Below");
      panel.add(enterFolderPathLabel, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
      addOne = new JButton();
      Font addOneFont = this.$$$getFont$$$("Droid Sans Mono", Font.BOLD, 18, addOne.getFont());
      if (addOneFont != null)
         addOne.setFont(addOneFont);
      addOne.setText("Add One");
      panel.add(addOne, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
      addAllReleasesInFolder = new JButton();
      Font addAllReleasesInFolderFont = this.$$$getFont$$$("Droid Sans Mono", Font.BOLD, 18, addAllReleasesInFolder.getFont());
      if (addAllReleasesInFolderFont != null)
         addAllReleasesInFolder.setFont(addAllReleasesInFolderFont);
      addAllReleasesInFolder.setText("Add All Releases In Folder");
      panel.add(addAllReleasesInFolder, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
   }
   
   /**
    * @noinspection ALL
    */
   private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
      if (currentFont == null)
         return null;
      String resultName;
      if (fontName == null) {
         resultName = currentFont.getName();
      } else {
         Font testFont = new Font(fontName, Font.PLAIN, 10);
         if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
            resultName = fontName;
         } else {
            resultName = currentFont.getName();
         }
      }
      return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
   }
   
   /**
    * @noinspection ALL
    */
   public JComponent $$$getRootComponent$$$() {
      return panel;
   }
   
}
