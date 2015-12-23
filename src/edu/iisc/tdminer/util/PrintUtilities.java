/*
 * PrintUtilities.java
 *
 * Created on July 12, 2007, 3:47 PM
 *
 */

package edu.iisc.tdminer.util;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.RepaintManager;

/** A simple utility class that lets you very simply print
 *  an arbitrary component. Just pass the component to the
 *  PrintUtilities.printComponent. The component you want to
 *  print doesn't need a print method and doesn't have to
 *  implement any interface or do anything special at all.
 *  <P>
 *  If you are going to be printing many times, it is marginally more 
 *  efficient to first do the following:
 *  <PRE>
 *    PrintUtilities printHelper = new PrintUtilities(theComponent);
 *  </PRE>
 *  then later do printHelper.print(). But this is a very tiny
 *  difference, so in most cases just do the simpler
 *  PrintUtilities.printComponent(componentToBePrinted).
 *
 *  7/99 Marty Hall, http://www.apl.jhu.edu/~hall/java/
 *  May be freely used or adapted.
 */

public class PrintUtilities implements Printable {
  private Component componentToBePrinted;

  public static void printComponent(Component c) {
    new PrintUtilities(c).print();
  }
  
  public PrintUtilities(Component componentToBePrinted) {
    this.componentToBePrinted = componentToBePrinted;
  }
  
  public void print() {
    PrinterJob printJob = PrinterJob.getPrinterJob();
    printJob.setPrintable(this);
    if (printJob.printDialog())
      try {
        printJob.print();
      } catch(PrinterException pe) {
        System.out.println("Error printing: " + pe);
      }
  }

  public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
    if (pageIndex > 0) {
      return(NO_SUCH_PAGE);
    } else {
      Graphics2D g2d = (Graphics2D)g;
      g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
      disableDoubleBuffering(componentToBePrinted);
      componentToBePrinted.paint(g2d);
      enableDoubleBuffering(componentToBePrinted);
      return(PAGE_EXISTS);
    }
  }

  /** The speed and quality of printing suffers dramatically if
   *  any of the containers have double buffering turned on.
   *  So this turns if off globally.
   *  @see enableDoubleBuffering
   */
  public static void disableDoubleBuffering(Component c) {
    RepaintManager currentManager = RepaintManager.currentManager(c);
    currentManager.setDoubleBufferingEnabled(false);
  }

  /** Re-enables double buffering globally. */
  
  public static void enableDoubleBuffering(Component c) {
    RepaintManager currentManager = RepaintManager.currentManager(c);
    currentManager.setDoubleBufferingEnabled(true);
  }
  
  public static void printToFile(JPanel panel) {
      printToFile(panel,panel.getWidth(),panel.getHeight());
  }
  public static void printToFile(JPanel panel, int width, int height) 
  {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileFilter()
        {
            public boolean accept(File f)
            {
                return (f.getName().toLowerCase().endsWith("png") || f.isDirectory());
            }
            public String getDescription()
            {
                return "Portable Network Graphics format (*.png)";
            }
        });
        if (Constants.CURRENT_DIR != null) fc.setCurrentDirectory(Constants.CURRENT_DIR);
        int ret = fc.showSaveDialog(null);
        Constants.CURRENT_DIR = fc.getCurrentDirectory();
        
        if (ret == JFileChooser.APPROVE_OPTION)
        {
            File f = fc.getSelectedFile();
            BufferedImage image = 
                new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            panel.paint(g2);
            g2.dispose();
            
            try
            {
                if (!f.getName().toLowerCase().endsWith(".png"))
                {
                    f = new File(f.getPath() + ".png");
                }
                ImageIO.write(image,"PNG",f);
            }
            catch(IOException ioe)
            {
                System.out.println("Error while saving event sequence plot");
                // JOptionPane.showMessageDialog(this, ioe.getMessage(), "Error while saving event sequence plot", JOptionPane.ERROR_MESSAGE);
            }
        }
  }
}
