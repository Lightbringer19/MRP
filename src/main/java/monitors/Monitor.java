package monitors;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import lombok.SneakyThrows;
import monitors.components.ComponentResizer;
import monitors.components.MessageConsole;
import monitors.components.TextBubbleBorder;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;

public class Monitor {
   private static String TITLE = "monitor";
   private static SwingWorker<Void, Void> worker;
   private final Thread colorChange;
   private JPanel panel;
   private JTextArea jTextArea;
   private JScrollPane scroll;
   private JLabel mainLabel;
   private JPanel label;
   private JFrame frame;
   private Point initialClick;
   private MessageConsole mc;
   
   Monitor() {
      init();
      addListeners();
      colorChange = new Thread(() -> {
         while (true) {
            for (int i = 100; i > 0; i--) {
               changeTitleProps(i);
            }
            for (int i = 0; i < 100; i++) {
               changeTitleProps(i);
            }
         }
      });
      colorChange.start();
   }
   
   static void setTITLE(String TITLE) {
      Monitor.TITLE = TITLE;
   }
   
   private static void timer() {
      Timer timer = new Timer(1000, e ->
        System.out.println(new Date().toString()));
      timer.start();
   }
   
   @SneakyThrows
   static void doAll(Thread test) {
      invokeWindow();
      Thread.sleep(1500);
      addWorker(test);
   }
   
   private static void addWorker(Thread test) {
      worker = new SwingWorker<Void, Void>() {
         @Override
         protected Void doInBackground() {
            test.run();
            return null;
         }
      };
      worker.execute();
   }
   
   private static void invokeWindow() {
      SwingUtilities.invokeLater(() -> {
         Monitor display = new Monitor();
         display.frame.setVisible(true);
      });
   }
   
   private void changeTitleProps(float i) {
      float hue = 0.1f; //hue
      float saturation = 1.0f; //saturation
      float brightness = 1.0f; //brightness
      float b = (float) (1 - Math.pow(1 - (i / 100), 5)); // ease out
      
      Color myRGBColor = Color.getHSBColor(0.11f, saturation, b);
      mainLabel.setForeground(myRGBColor);
      try {
         Thread.sleep(14);
      } catch (InterruptedException ex) {
         ex.printStackTrace();
      }
   }
   
   private void addListeners() {
      MouseAdapter close = new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
            if (e.getButton() == 2) {
               colorChange.stop();
               frame.dispose();
               worker.cancel(true);
            }
            initialClick = e.getPoint();
            frame.getComponentAt(initialClick);
         }
      };
      MouseAdapter move = new MouseAdapter() {
         @Override
         public void mouseDragged(MouseEvent e) {
            super.mouseDragged(e);
            // get location of Window
            int thisX = frame.getLocation().x;
            int thisY = frame.getLocation().y;
            
            // Determine how much the mouse moved since the initial click
            int xMoved = e.getX() - initialClick.x;
            int yMoved = e.getY() - initialClick.y;
            
            // Move window to this position
            int X = thisX + xMoved;
            int Y = thisY + yMoved;
            frame.setLocation(X, Y);
         }
      };
   
      jTextArea.addMouseListener(close);
      jTextArea.addMouseMotionListener(move);
      label.addMouseListener(close);
      label.addMouseMotionListener(move);
   }
   
   @SneakyThrows
   private void init() {
   
      frame = new JFrame(TITLE.toUpperCase());
      frame.add(panel);
      // frame.setIconImage(Toolkit.getDefaultToolkit().getImage("Files\\pickmyrec.jpg")); // Icons
      mainLabel.setText(TITLE.toUpperCase());
      
      Color color = Color.getHSBColor(0.11f, 1, 1);
      AbstractBorder brdrLeft = new TextBubbleBorder(color, 2, 4, 0);
      label.setBorder(brdrLeft);
      scroll.setBorder(brdrLeft);
      
      ComponentResizer cr = new ComponentResizer();
      cr.setSnapSize(new Dimension(1, 1));
      cr.setMinimumSize(new Dimension(150, 150));
      cr.registerComponent(frame);
   
      mc = new MessageConsole(jTextArea);
      mc.redirectOut();
      mc.redirectErr(Color.RED, null);
      mc.setMessageLines(50);
      
      UIManager.setLookAndFeel(new WindowsLookAndFeel());
      Color red = new Color(91, 0, 0);
      Color brighterRed = new Color(147, 0, 0);
      UIManager.put("ScrollBar.track", new ColorUIResource(red));
      UIManager.put("ScrollBar.thumbHighlight", new ColorUIResource(brighterRed));
      UIManager.put("ScrollBar.thumb", new ColorUIResource(brighterRed));
      UIManager.put("ScrollBar.thumbShadow", new ColorUIResource(Color.BLACK));
      UIManager.put("ScrollBar.thumbDarkShadow", new ColorUIResource(Color.BLACK));
      scroll.getVerticalScrollBar().setUI(new BasicScrollBarUI());
      scroll.getHorizontalScrollBar().setUI(new BasicScrollBarUI());
   
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setUndecorated(true);
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
      panel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
      panel.setBackground(new Color(-16777216));
      panel.setDoubleBuffered(true);
      panel.setForeground(new Color(-16777216));
      panel.setMinimumSize(new Dimension(300, 300));
      panel.setOpaque(true);
      panel.setPreferredSize(new Dimension(480, 300));
      panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), null));
      scroll = new JScrollPane();
      scroll.setAutoscrolls(true);
      scroll.setBackground(new Color(-16777216));
      scroll.setForeground(new Color(-16777216));
      scroll.setName("scroll");
      scroll.setOpaque(false);
      scroll.setVerticalScrollBarPolicy(21);
      scroll.setVisible(true);
      panel.add(scroll, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
      scroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), null));
      jTextArea = new JTextArea();
      jTextArea.setAutoscrolls(false);
      jTextArea.setBackground(new Color(-15989242));
      jTextArea.setCaretColor(new Color(-27392));
      jTextArea.setEditable(false);
      jTextArea.setEnabled(true);
      jTextArea.setFocusable(false);
      Font jTextAreaFont = this.$$$getFont$$$("Helvetica", Font.BOLD, 18, jTextArea.getFont());
      if (jTextAreaFont != null)
         jTextArea.setFont(jTextAreaFont);
      jTextArea.setForeground(new Color(-27136));
      jTextArea.setInheritsPopupMenu(false);
      jTextArea.setLineWrap(true);
      jTextArea.setOpaque(true);
      jTextArea.setRequestFocusEnabled(true);
      jTextArea.setSelectedTextColor(new Color(-15989242));
      jTextArea.setVerifyInputWhenFocusTarget(true);
      jTextArea.setWrapStyleWord(true);
      scroll.setViewportView(jTextArea);
      label = new JPanel();
      label.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
      label.setEnabled(false);
      label.setForeground(new Color(-16777216));
      label.setOpaque(false);
      panel.add(label, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
      mainLabel = new JLabel();
      mainLabel.setAlignmentX(0.5f);
      mainLabel.setAutoscrolls(false);
      mainLabel.setEnabled(true);
      Font mainLabelFont = this.$$$getFont$$$("Helvetica", Font.BOLD, 18, mainLabel.getFont());
      if (mainLabelFont != null)
         mainLabel.setFont(mainLabelFont);
      mainLabel.setForeground(new Color(-6813696));
      mainLabel.setHorizontalAlignment(0);
      mainLabel.setOpaque(false);
      mainLabel.setText("");
      mainLabel.setVisible(true);
      label.add(mainLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(0, 13), null, 0, false));
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
