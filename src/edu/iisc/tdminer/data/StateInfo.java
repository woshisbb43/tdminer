/*
 * StateInfo.java
 *
 * Created on March 16, 2006, 12:22 AM
 */

package edu.iisc.tdminer.data;


import edu.iisc.tdminer.gui.TDMinerInterface;
import edu.iisc.tdminercore.miner.SessionInfo;
import edu.iisc.tdminercore.data.EpisodeInstanceSet;
import java.util.prefs.Preferences;

/**
 * @author Deb
 * @author phreed@gmail.com
 */
public class StateInfo
{
    private SessionInfo session;
    private EpisodeInstanceSet harvest;
    
    
    private int counterIndex;
    private int candidateGenIndex;
    
    
    /** Creates a new instance of StateInfo */
    public StateInfo(){
    }
    
    
    public synchronized EpisodeInstanceSet getHarvest() { return harvest; }
    public synchronized void setHarvest(EpisodeInstanceSet eis){ this.harvest = eis; }
    
    public int getCounterIndex(){return counterIndex;}
    public int getCandidateGenIndex(){return this.candidateGenIndex;}
    public void setCounterIndex(int ci){this.counterIndex = ci;}
    public void setCandidateGenIndex(int ci){this.candidateGenIndex = ci;}
    
    public SessionInfo getSession()
    {
        if (session != null) return session;
        
        session = new SessionInfo();
        
        Preferences prefs = Preferences.userNodeForPackage( TDMinerInterface.class );
        String intervals = prefs.get("intervals", "");
	if (!intervals.equals("")) session.setIntervalsList(intervals);
        String durations = prefs.get("durations", "");
	if (!durations.equals("")) session.setDurationsList(durations);
        return session;
    }

    public void setSession(SessionInfo session)
    {
        this.session = session;
    }

}
