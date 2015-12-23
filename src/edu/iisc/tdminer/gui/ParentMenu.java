/*
 * ParentMenu.java
 *
 * Created on July 30, 2007, 9:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.iisc.tdminer.gui;

import javax.swing.JMenu;

/**
 *
 * @author Deb
 */
public interface ParentMenu
{
    public JMenu getExportFileMenu();
    public JMenu getVisualizationSettingsMenu();
    public JMenu getSimulatorSettingsMenu();
    public JMenu getProspectorSettingsMenu();
}
