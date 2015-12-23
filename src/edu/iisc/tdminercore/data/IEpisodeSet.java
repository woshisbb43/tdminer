/*
 * IEpisodeSet.java
 *
 * Created on March 26, 2007, 3:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.iisc.tdminercore.data;

import edu.iisc.tdminercore.writer.IWriter;
import java.util.List;
import java.util.Set;

/**
 *
 * @author phreed@gmail.com
 */
public interface IEpisodeSet 
        extends Set<IEpisode>
{
   public void export(IWriter writer); 
   EventFactor getEventFactor();
   public Iterable<List<IEpisode>> levels();
}
