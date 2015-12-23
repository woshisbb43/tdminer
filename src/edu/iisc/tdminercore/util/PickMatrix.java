/*
 * PickMatrix.java
 *
 * Created on January 5, 2007, 12:06 PM
 *
 */

package edu.iisc.tdminercore.util;

import java.util.List;
import java.util.Iterator;

/**
 * A pick matrix is a rectangular array
 * whose rows and columns are represented by two lists.
 * The cells of the matrix are boolean values indicating
 * the use of the row by the column.
 * The columns are associated with sets of rows.
 * In particular this class was developed to provide an association
 * between event types (of a particular factor) and a subset of 
 * a list of durations for events of that type.
 *
 * @author phreed@gmail.com
 */

public class PickMatrix<F>
{
    public static final int PROPER_SUBSET = 1;
    public static final int STRICT_SUBSET = 2;
    
    private int[] types;
    private List<F> shingles;
    private PickMap[] maps;
    
    /**
     * Creates a new instance of PickMap
     */
    public PickMatrix(int size, List<F> shingles) {
        this.types = new int[size];
        this.shingles = shingles;
        this.maps = new PickMap[size];
        for(int i = 0; i < this.maps.length; i++)
            this.maps[i] = new PickMap(this.shingles.size());
    }
       
    public PickMatrix(int[] types, List<F> shingles) {
        this.types = types;
        this.shingles = shingles;
        this.maps = new PickMap[types.length];
        for(int i = 0; i < this.maps.length; i++)
            this.maps[i] = new PickMap(this.shingles.size());
    }
    
    public PickMatrix(int[] types, List<F> shingles, int[] maps) {
        this.types = types;
        this.shingles = shingles;
        if (types.length != maps.length) {
            throw new RuntimeException("types.length != maps.length in PickMatrix()");
        }
        this.maps = new PickMap[maps.length];
        for(int ix=0; ix < maps.length; ix++) 
        {
            this.maps[ix] = new PickMap(shingles.size());
            this.maps[ix].set(maps[ix]);
        }
    }
    
    public List<F> getShingles() {
        return this.shingles;
    }
    
    public int sizeShingles() {
        return this.shingles.size();
    }
   
    public void setMap(int index, PickMap map) {
        this.maps[index] = map;
    }
    public void setMap(int index, int map) {
        this.maps[index].set(map);
    }
  
    
    public int getMap(int index) {
        return this.maps[index].get();
    }
    
    public int getLastMap() {
        return this.maps[this.maps.length - 1].get();
    }
    
    public int[] getMaps() {
        int[] maps = new int[this.maps.length];
        for(int ix=0; ix<this.maps.length; ix++) maps[ix] = this.maps[ix].get();
        return maps;
    }
    
    public String toString(int eventIndex)
    {
        StringBuffer buff = new StringBuffer();
        buff.append("(");
        buff.append(this.maps[eventIndex].toString(this.shingles));
        buff.append(")");
        return buff.toString();
    }
    
    /** 
     * Do the two sets have the same members?
     */
    public boolean matchPrefix(PickMatrix<F> that) {
        
        boolean ret = true;
        for (int i = 0; i < this.maps.length - 1; i++)
        {
            if (this.types[i] != that.types[i] || !this.maps[i].equals(that.maps[i]))
            {
                ret = false;
                break;
            }
        }
        return ret;
        
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final PickMatrix<F> that = (PickMatrix<F>) obj;
        if (this.types != that.types) {
            if (this.types.length != that.types.length) return false;
            for (int ix= 0; ix < this.types.length; ix++) {
                if (this.types[ix] != that.types[ix]) return false;
            }
        }

        if (this.maps.length != that.maps.length) return false;
        for(int ix=0; ix<this.maps.length; ix++) {
            if (! this.maps[ix].equals(that.maps[ix])) return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 71 * hash + (this.types != null ? this.types.hashCode() : 0);
        hash = 71 * hash + (this.maps != null ? this.maps.hashCode() : 0);
        return hash;
    }

    

    /**
     * A pick set is similar to an Enum Set.
     * Except in lieu of an Enum it uses a List.
     * The fundamental component is the bit vector.
     * In this implementation the bit vector is an 'int'
     * but a more general implementation could include a 'BitSet'.
     */
    public static class PickMap<F> {
        // private BitSet[] map;
        private int map;
        public PickMap(int size) {
            if (size > 30) {
                // too big for an integer 
                // throw Exception;
            }
        }
        
        public void set(int map) { this.map = map; }
        public int get() { return this.map; }
        /**
         * is the bit set for the specified index?
         */
        public boolean isSet(int index) { 
            int pos = 1; 
            pos = pos << index;
            return (this.map & pos) > 0; 
        }
           
        public void shiftUp() { map = map << 1; }
        // BitSet: map.clear(); map.set(kx,true);
        public void shiftDown() { map = map >> 1; }

        public void increment() { map++; }
        // BitSet: kx=map.nextClearBit(0);
        //         map.set(kx,true); if (kx > 0) map.clear(0,kx-1); 
        
        public int nextClearBit() {
            int jx = 0;
            for (int ix=map; ix > 0; jx++) {
                ix = ix >> 1;
            }
            return jx;
        }
        // BitSet: kx=map.nextClearBit(0);
        
        /** 
         * Do the two sets have the same members?
         */
        public boolean isEquivalent(PickMap that) {
            return (this.map & that.map) > 0 ? false : true;
        }
        
        /**
         * Is this set is a subset of another set?
         * @param that - another set
         * @param subsetType - check if a proper or strict subset
         * @return boolean true - it is; false - it is not.
         */
       
        public boolean isSubSet(PickMap that, int subsetType) {
            int equivalent = this.map & that.map;
            int properSubset = ~(equivalent ^ this.map);
            switch (subsetType) {
                case PROPER_SUBSET: 
                    return (properSubset > 0) ? false : true;
                case STRICT_SUBSET:
                    return (properSubset > 0 && equivalent > 0) ? false : true;
            }
           return false;
        }
        
        public String toString(List<F> shingles)
        {
            StringBuffer buff = new StringBuffer();
            boolean firstflag = true;
            buff.append("[");
            for (int pos=1, ix=0; pos <= this.map; pos=pos<<1, ix++) {
                if (!firstflag) buff.append(",");
                if ((pos & this.map) > 0) {
                    firstflag = false;
                    buff.append(shingles.get(ix).toString());  
                }
            }
            buff.append("]");
            return buff.toString();
        }
        
        @Override
        public String toString()
        {
            StringBuffer buff = new StringBuffer();
            buff.append("[" + this.map + "]");
            return buff.toString();
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final PickMap<F> other = (PickMap<F>) obj;
            if (this.map != other.map)
            {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 97 * hash + this.map;
            return hash;
        }
        
    }
    
    /**
     * The Permutors produce all the permutations of the shingles.
     * The PermutorSingle produces one map for each shingle.
     * The PermutorFull produces maps for all possible combinations of shingles.
     */
    private static abstract class AbstractPermuter implements Iterator<Integer> {
        protected int limit;
        protected int map;
        public AbstractPermuter(int size) { this.limit = 1 << size; }
        public boolean hasNext() { return (this.map < this.limit)? true : false; }
        /**
         * @return the number of permutations that will be returned
         */
        public abstract int size();
        public Integer next() {
            this.map = this.map << 1;
            return new Integer(this.map);
        } 
        public void remove() { }
    }
    public static class PermutorSingle extends AbstractPermuter {
        private int size;
        public PermutorSingle(int size) { super(size); this.size = size; }
        public int size() { return size; }
        @Override
        public Integer next() {
            this.map = this.map << 1;
            return new Integer(this.map);
        } 
    }
    public static class PermutorFull extends AbstractPermuter {
        public PermutorFull(int size) { super(size); }
        public int size() { return limit; }
        @Override
        public Integer next() {
            this.map++;
            return new Integer(map);
        } 
    }
    
}