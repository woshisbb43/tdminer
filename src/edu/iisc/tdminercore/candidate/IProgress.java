/*
 * IProgress.java
 *
 * Created on May 18, 2007, 3:33 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.iisc.tdminercore.candidate;

import edu.iisc.tdminercore.data.IEpisode;
import java.util.List;

/**
 * This interface provides a method monitor the progress.
 *
 * @author phreed@gmail.com
 */
public interface IProgress {
    
    public void record(List<IEpisode> episodes);
    public boolean atBeginning();
    
    /**
     * indicates how far the episode nominator has progressed in
     * producing all of its candidates.
     * @return the progress as a value between 0.0 and 1.0
     */
    public double getProgress();
    public void reset();
    
}
