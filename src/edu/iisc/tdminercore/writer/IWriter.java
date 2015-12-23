/*
 * IWriter.java
 *
 * Created on March 9, 2007, 11:39 AM
 *
 */

package edu.iisc.tdminercore.writer;

import edu.iisc.tdminercore.data.IEpisodeSet;
import edu.iisc.tdminercore.data.EpisodeSet;
import edu.iisc.tdminercore.data.EpisodeInstanceSet;

/**
 *
 * @author phreed@gmail.com
 */
public interface IWriter {
     public boolean write(EpisodeSet prospect);
     public boolean write(EpisodeInstanceSet prospect);
     public boolean write(IEpisodeSet prospect);
}
