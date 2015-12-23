/*
 * ITaskPanel.java
 *
 * Created on March 21, 2006, 12:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.iisc.tdminer.gui;

/**
 *
 * @author Deb
 */
public interface ITaskPanel
{
    public void refreshState();
    
    public void handleTaskCompletion(int taskIndex);
    
}
