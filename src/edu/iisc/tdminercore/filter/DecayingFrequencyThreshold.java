/*
 * DecayingFrequencyThreshold.java
 *
 * Created on November 17, 2006, 5:18 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.iisc.tdminercore.filter;

import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.miner.SessionInfo;
import edu.iisc.tdminercore.util.IObserver;
import java.util.List;

/**
 *
 * @author phreed@gmail.com
 */
public class DecayingFrequencyThreshold extends AbstractFrequencyThreshold  
{
    @Override
    public List<IEpisode> prune(List<IEpisode> candidates, IObserver observer, 
            SessionInfo session)
    {
        int level = session.getCurrentLevel();
        if (level == 1)
        {
            session.setCurrentThreshold(session.getFrequencyThreshold(1));
            
        }
        else
        {
            double t = session.getFrequencyThreshold(2);
	    System.out.println("level = " + level);
	    System.out.println("session.getFreqDecay() = " + session.getFreqDecay());
            for (int i = 0; i < (level - 2); i++) t *= session.getFreqDecay();
	    System.out.println("t = " + t);
            session.setCurrentThreshold(t);
        }
        return super.prune(candidates, observer, session);
    }    
}
