package monitors.disk_monitor;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.awt.*;

public class DM {
   private JPanel Now;
   private JLabel label;
   private JLabel label2;
   private JFrame frame;
   
   {
      // GUI initializer generated by IntelliJ IDEA GUI Designer
      // >>> IMPORTANT!! <<<
      // DO NOT EDIT OR ADD ANY CODE HERE!
      $$$setupUI$$$();
   }
   
   public DM() {
      init();
      addListeners();
      addTimer();
   }
   
   public static void main(String[] args) {
      SwingUtilities.invokeLater(() -> {
         DM display = new DM();
         display.frame.setVisible(true);
      });
   }
   
   private void addListeners() {
   }
   
   private void init() {
      frame = new JFrame("SPACE MONITOR");
      try {
         UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
         e.printStackTrace();
      }
      frame.add(Now);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.pack();
   }
   
   private void addTimer() {
      int delay = 1000 * 1;
      Timer timer = new Timer(delay, evt -> new Thread(() -> {
         Disk_Monitor.Space space = new Disk_Monitor.Space().invoke();
         int intPercent = space.getIntPercent();
         String humanReadableByteCount = space.getHumanReadableByteCount();
         label.setText("Free Space Left: " + humanReadableByteCount);
         label2.setText(intPercent + " %");
      }).start());
      timer.setInitialDelay(0);
      timer.start();
      
   }
   
   /**
    * Method generated by IntelliJ IDEA GUI Designer
    * >>> IMPORTANT!! <<<
    * DO NOT edit this method OR call it in your code!
    *
    * @noinspection ALL
    */
   private void $$$setupUI$$$() {
      Now = new JPanel();
      Now.setLayout(new FormLayout("center:p:grow", "center:d:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
      Now.setOpaque(true);
      Now.setPreferredSize(new Dimension(400, 80));
      label = new JLabel();
      Font labelFont = this.$$$getFont$$$("Unispace", Font.BOLD, 24, label.getFont());
      if (labelFont != null)
         label.setFont(labelFont);
      label.setHorizontalAlignment(0);
      label.setText("Label");
      CellConstraints cc = new CellConstraints();
      Now.add(label, cc.xy(1, 1, CellConstraints.CENTER, CellConstraints.DEFAULT));
      label2 = new JLabel();
      Font label2Font = this.$$$getFont$$$("Unispace", Font.BOLD, 24, label2.getFont());
      if (label2Font != null)
         label2.setFont(label2Font);
      label2.setHorizontalAlignment(0);
      label2.setText("Label");
      Now.add(label2, cc.xy(1, 3));
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
      return Now;
   }
   
}
