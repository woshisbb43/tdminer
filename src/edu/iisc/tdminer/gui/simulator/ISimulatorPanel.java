/*
 * ISimulatorPanel.java
 *
 * Created on May 1, 2006, 12:01 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.iisc.tdminer.gui.simulator;

import edu.iisc.tdminer.data.StateInfo;
import edu.iisc.tdminercore.simulation.ISimulator;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author Deb
 */
public interface ISimulatorPanel
{
    public void setStateInfo(StateInfo stateInfo);
    public void setJLabelStatus(JLabel jLabelStatus);
    public void setFrame(JFrame frame);
    public ISimulator getSimulator();
}
