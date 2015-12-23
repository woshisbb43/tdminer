/*
 * ThreadedProgressManager.java
 *
 * Created on November 20, 2006, 3:26 PM
 *
 */

package edu.iisc.tdminer.model;

import edu.iisc.tdminer.gui.ITaskPanel;
import edu.iisc.tdminer.gui.ProgressMonitorUI;
import edu.iisc.tdminercore.util.IObserver;
import edu.iisc.tdminercore.util.AbstractObserver;
import edu.iisc.tdminercore.miner.SessionInfo;
import edu.iisc.tdminercore.data.IEvent;
import edu.iisc.tdminercore.data.IEventDataStream.CONSTRAINT_MODE;

import java.awt.Frame;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author hzg3nc
 */
public class ThreadedProgressManager extends AbstractObserver
{
    private ProgressMonitorUI progressUI;
    private volatile boolean cancelled;
    private volatile int range;
    private volatile int value;
    private volatile String title;
    private ITaskPanel taskpanel;
    private Frame frame;
    private SessionInfo session;
    private CONSTRAINT_MODE constraintMode;
    private int taskIndex = 0;
    
    /** Creates a new instance of ThreadedProgressManager */
    public ThreadedProgressManager(Frame frame, ITaskPanel taskpanel, int taskIndex)
    {
        this(frame, taskpanel);
        this.taskIndex = taskIndex;
    }
    
    public ThreadedProgressManager(Frame frame, ITaskPanel taskpanel)
    {
        this.frame = frame;
        this.taskpanel = taskpanel;
        progressUI = new ProgressMonitorUI(frame, this);
        cancelled = false;
        this.constraintMode = null;
    }

    @Override
    public void setTitle(String title)
    {
        this.title = title;
    }

    @Override
    public void setExtent(int range)
    {
        this.range = range;
    }

    @Override
    public boolean update(int value)
    {
        this.value = value;
        return cancelled;
    }
    
    public void setSession( SessionInfo session )
    {
        this.session = session;
    }
       
    public int getProgress()
    {
        if (range == 0) { return 0; }
        return (value * 100)/range;
    }
    
    public double getNetProgress()
    {
        if (session == null) return 0.0;
        if (session.getCandidateGeneratorProgress() == null) return 0.0D;
        
        return session.getCandidateGeneratorProgress().getProgress();
    }

    @Override
    public boolean interrupted()
    {
        return cancelled;
    }

    public void interrupt()
    {
        System.out.println("USER INTERRUPT at " + new Date());
        cancelled = true;
    }

    @Override
    public void taskStarted()
    {
        // Deals with the Macro task
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                progressUI.setVisible(true);
            }
        });        
        progressUI.startMonitorThread();
    }

    @Override
    public void taskComplete()
    {
        // Deals with the Macro task
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                progressUI.setVisible(false);
                progressUI.dispose();
            }
        });
        if (taskpanel != null)
        {
            taskpanel.handleTaskCompletion(taskIndex);
        }
        progressUI.stopMonitorThread();
    }

    @Override
    public void exceptionOccured(Exception e)
    {
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                progressUI.setVisible(false);
            }
        });
        if (taskpanel != null)
        {
            taskpanel.handleTaskCompletion(0);
            JOptionPane.showMessageDialog(frame, e.getClass().toString() + " : " + 
                    e.getMessage(), "Error occured while processing", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void startup()
    {
        // Deals with subtask
        progressUI.setText(title);
    }

    @Override
    public void shutdown()
    {
        // Deals with subtask
    }

    public void handleEpisodeCompletion(int episodeIndex, int[] eventTypes, List<IEvent> t)
     throws IObserver.NotImplementedException
    {
        this.markEvents(t);
    }
    
    
    @Override
    public CONSTRAINT_MODE getConstraintMode() { return this.constraintMode; }
    @Override
    public void setConstraintMode(CONSTRAINT_MODE mode) { this.constraintMode = mode; }
}
