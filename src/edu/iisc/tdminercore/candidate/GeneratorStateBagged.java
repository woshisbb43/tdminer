/*
 * GeneratorStateBagged.java
 *
 * Created on May 9, 2007, 3:30 PM
 *
 */

package edu.iisc.tdminercore.candidate;

import edu.iisc.tdminercore.data.IEpisode;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author phreed@gmail.com
 */
public class GeneratorStateBagged 
        extends GeneratorState
        implements IProgress
{ 
    private LinkedList<IEpisode> bag = new LinkedList<IEpisode>();
    
    /** Creates a new instance of GeneratorStateBagged */
    public GeneratorStateBagged(int size) {
        super(size);
    }
   
    public boolean isBagEmpty() { 
        return this.bag.isEmpty();
    }
    
    public IEpisode popBag() { 
        return this.bag.remove();
    }
    
    public boolean pushBag( IEpisode episode) { 
        return this.bag.add(episode);
    }
    
    public Iterable<IEpisode> getBag() {
        return new Iterable<IEpisode>() {
            public Iterator<IEpisode> iterator() 
            {
                return bag.iterator();
            }
        };
    }
    
}
