/*
 * ProgressManager.java
 *
 * Created on March 21, 2006, 11:57 AM
 *
 */

package edu.iisc.tdminer.model;

import edu.iisc.tdminercore.data.IEvent;
import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminer.gui.ITaskPanel;
import edu.iisc.tdminercore.util.IObserver;
import edu.iisc.tdminercore.util.AbstractObserver;
import edu.iisc.tdminercore.data.IEventDataStream.CONSTRAINT_MODE;

import java.awt.Component;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.util.List;

/**
 *
 * @author Deb
 */
public class ProgressManager extends AbstractObserver {
    static final boolean DEBUG = true; // false to remove debugging
    
    private int range;
    private String message;
    private Component parent;
    private ProgressMonitor pm;
    private boolean cancelled = false;
    private int percent = 0;
    private CONSTRAINT_MODE constraintMode;
    
    public  ProgressManager(Component parent) {
        if (DEBUG) System.out.println("ProgressManager: <constructor>");
        this.parent = parent;
        this.constraintMode = null;
    }
    
    // The init and dispose routines need to be coordinated.
    // A reference to a new progress monitor cannot be created
    // so long as the old reference is in use.
    
    public synchronized void setTitle(String title) {
        if (DEBUG) System.out.println("ProgressManager: init");
        this.message = title;
    }
    
    public synchronized void setExtent(int range) {
        if (DEBUG) System.out.println("ProgressManager: set extent");
        this.range = range;
    }
    
    public synchronized void dispose() {
        if (DEBUG) System.out.println("ProgressManager: dispose");
        
        SwingUtilities.invokeLater(new Runnable() {
            private ProgressMonitor oldpm = pm;
            public synchronized void run() {
                if (oldpm != null) {
                    oldpm.setProgress(100);
                    oldpm.close();
                    oldpm = null;
                }
            }
        }
        );
        pm = null;
        Thread.yield();
    }
    
    public synchronized boolean update(final int value) {//if (DEBUG) System.out.println("ProgressManager: update");
        this.cancelled = false;
        if (pm == null) {
            System.out.println("No progress manager defined");
        } else {
            this.cancelled = pm.isCanceled();
            if (!this.cancelled) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        percent = (int)(value * 100.0 / range);
                        synchronized (pm) {
                            if (pm != null) {
                                pm.setProgress(percent);
                                pm.setNote("Processing " + percent + " % complete ");
                            }
                        }
                    }
                });
            }
        }
        Thread.yield();
        return this.cancelled;
    }
    
    public int progress() {
        return this.percent;
    }
    
    
    public synchronized boolean interrupted() {
        if (DEBUG) System.out.println("ProgressManager: interrupted");
        if (pm != null) this.cancelled = pm.isCanceled();
        return this.cancelled;
    }
    
    public void taskStarted() {
        if (true) System.out.println("ProgressManager: task started");
    }
    
    public synchronized void taskComplete() {
        if (DEBUG) System.out.println("ProgressManager: task complete");
        if (parent instanceof ITaskPanel) {
            ((ITaskPanel)parent).handleTaskCompletion(0);
        }
    }
    
    public synchronized void exceptionOccured(Exception e) {
        if (DEBUG) System.out.println("ProgressManager: exceptionOccured");
        //dispose();
        if (parent instanceof ITaskPanel) {
            ((ITaskPanel)parent).handleTaskCompletion(0);
            JOptionPane.showMessageDialog(parent, e.getClass().toString() + " : " + e.getMessage(), "Error occured while processing", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Placeholder methods these may be extended by subclass
    public synchronized void startup() {
        if (DEBUG) System.out.println("ProgressManager: episode handling startup");
        
        this.cancelled = false;
        
        UIManager.put("ProgressMonitor.progressText", "TDMiner progress monitor");
        int min = 0;
        int max = 100;
        
        String note = "";
        
        if (pm != null) {
            System.out.println("ProgressManager: has non-null process monitor");
            // Continue using the old one
            return;
        }
        pm = new ProgressMonitor(parent, this.message, note, min, max);
        pm.setMillisToDecideToPopup(100);
    }
    
    public synchronized void shutdown() {
        if (DEBUG) System.out.println("ProgressManager: episode handling shutdown");
    }
    
    public synchronized void handleEpisodeCompletion(int episodeIndex, int[] et, List<IEvent> events)
    throws IObserver.NotImplementedException {
        if (DEBUG) System.out.println("ProgressManager: handle episode completion");
    }
    
    
    public CONSTRAINT_MODE getConstraintMode() { return this.constraintMode; }
    public void setConstraintMode(CONSTRAINT_MODE mode) { this.constraintMode = mode; }
}
