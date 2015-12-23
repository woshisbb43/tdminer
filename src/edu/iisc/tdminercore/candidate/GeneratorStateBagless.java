/*
 * GeneratorStateBagless.java
 *
 * Created on May 9, 2007, 3:30 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.iisc.tdminercore.candidate;

/**
 *
 * @author phreed@gmail.com
 */
public class GeneratorStateBagless
        extends GeneratorState
        implements IProgress
{ 
    /**
     * Creates a new instance of GeneratorStateBagless
     */
    public GeneratorStateBagless(int size) {
        super(size);
    }
}
