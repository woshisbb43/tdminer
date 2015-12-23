/*
 * SplashWindow3.java
 *
 * Created on March 19, 2006, 8:56 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.iisc.tdminer.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.swing.JDialog;

/**
 *
 * @author Deb
 */
public class AboutWindow extends JDialog
{
    /** Creates a new instance of SplashWindow3 */
    public AboutWindow(String filename, Frame f)
    {
	super(f, "GMiner About", true);
	AboutPanel about = new AboutPanel();
	about.getJButtonOk().addActionListener(new ActionListener()
	{
	    public void actionPerformed(ActionEvent e)
	    {
		setVisible(false);
		dispose();
	    }
	});
	try
	{
	    StringBuffer buf = new StringBuffer("SVN Revision History \n");
	    InputStream in = TDMinerInterface.class.getResourceAsStream("/status-file.txt");
	    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
	    String line = null;
	    while((line = reader.readLine()) != null)
	    {
		buf.append(line);
		buf.append("\n");
	    }
	    reader.close();

	    about.getJTextAreaRevision().setText(buf.toString());
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	    about.getJTextAreaRevision().setText("No Revision history available");
	}

	getContentPane().add(about, BorderLayout.CENTER);
	pack();
	Dimension frameSize = f.getSize();
	Dimension labelSize = about.getPreferredSize();
	Point location = f.getLocationOnScreen();
	
	setLocation((int)location.getX() + frameSize.width/2 - (labelSize.width/2),
		(int)location.getY() + frameSize.height/2 - (labelSize.height/2));
	setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	setVisible(true);
    }
}
