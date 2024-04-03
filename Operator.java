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
     * Returns the schema of the operator's output.
     * @return Schema object representing the output schema of this operator.
     */
    public abstract Schema getSchema();


    /**
     * Dumps tuples from the operator to a PrintStream
     * @param out   PrintStream to dump tuples
     */
    public void dump(PrintStream out){
        try{
            System.out.println("DUMP CALLED");
            Tuple tuple;
            while ((tuple = getNextTuple()) != null){
                System.out.println("Tuple: "+tuple);
                out.println(tuple);
            }
        } catch(NoSuchElementException e){
            // no more tuples to output
        }
    }
}
