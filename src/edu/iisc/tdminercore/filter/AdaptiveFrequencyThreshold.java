/*
 * AdaptiveFrequencyThreshold.java
 *
 * Created on November 17, 2006, 5:00 PM
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
 * @author cznwdr
 */
public class AdaptiveFrequencyThreshold extends AbstractFrequencyThreshold 
{
    @Override
    public List<IEpisode> prune(List<IEpisode> candidates, IObserver observer, 
            SessionInfo session)
    {
        int level = session.getCurrentLevel();
        if (level == 1)
        {
            session.setCurrentThreshold(session.getFrequencyThreshold(level));
        }
        else
        {
            double t = session.getFrequencyThreshold(level) * 2.0 / (double)level;
            session.setCurrentThreshold(t);
        }
        return super.prune(candidates, observer, session);
    }    
}
