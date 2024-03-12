package ed.inf.adbs.lightdb;

import java.io.PrintStream; // to implement the dump() method
import java.util.NoSuchElementException;

public abstract class Operator {

    /**
     *
     * @return next tuple, or null if no more tuples
     */
    public abstract Tuple getNextTuple();

    /**
     * resets the operator to allow the iterator to start reading from the beginning at the next scan
     */
    public abstract void reset();


    /**
     * Dumps tuples from the operator to a PrintStream
     * @param out   PrintStream to dump tuples
     */
    public void dump(PrintStream out){
        try{
            Tuple tuple;
            while ((tuple = getNextTuple()) != null){
                out.println(tuple);
            }
        } catch(NoSuchElementException e){
            // no more tuples to output
        }
    }
}
