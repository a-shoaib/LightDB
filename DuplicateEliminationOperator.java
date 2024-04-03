//package ed.inf.adbs.lightdb;
//
//import java.util.LinkedHashSet;
//import java.util.Set;
//
///**
// * Operator to eliminate duplicate tuples.
// */
//public class DuplicateEliminationOperator extends Operator {
//    private Operator child;
//    private Set<Tuple> seenTuples;
//    private Tuple nextTuple;
//
//    public DuplicateEliminationOperator(Operator child) {
//        this.child = child;
//        this.seenTuples = new LinkedHashSet<>();
//        this.nextTuple = null;
//    }
//
//    @Override
//    public Tuple getNextTuple() {
//        while ((nextTuple = child.getNextTuple()) != null) {
//            if (!seenTuples.contains(nextTuple)) {
//                // The tuple has not been seen before and will be added to the set, return it.
//                return nextTuple;
//            }
//        }
//        // No more unique tuples, return null.
//        return null;
//    }
//
//    @Override
//    public void reset() {
//        child.reset();
//        seenTuples.clear();
//        nextTuple = null;
//    }
//
//    @Override
//    public Schema getSchema() {
//        return child.getSchema();
//    }
//}
package ed.inf.adbs.lightdb;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;


import java.util.*;

public class DuplicateEliminationOperator extends Operator {
    private Operator childOperator;
    private Iterator<Tuple> uniqueIterator;

    public DuplicateEliminationOperator(Operator childOperator, List<String> columnNames, Schema schema) {
        System.out.println("ENTERED DISTINCT");
        List<Column> columns = columnNames.stream()
                .map(colName -> new Column(new Table(null, null), colName))
                .collect(Collectors.toList());
        this.childOperator = new SortOperator(childOperator, columns, schema);
        Set<Tuple> uniqueTuples = new LinkedHashSet<>();

        // Read all tuples from the child operator, now sorted, and add them to the set
        Tuple tuple;
        while ((tuple = this.childOperator.getNextTuple()) != null) {
            System.out.println("Tuple: " + tuple);
            uniqueTuples.add(tuple);
        }

        // Create an iterator for the unique, sorted tuples
        this.uniqueIterator = uniqueTuples.iterator();
//        while ((tuple = this.getNextTuple()) != null) {
//            System.out.println("Unique tuple " + tuple);
//        }
    }

    @Override
    public Tuple getNextTuple() {
        return uniqueIterator.hasNext() ? uniqueIterator.next() : null;
    }

    @Override
    public void reset() {
        this.childOperator.reset();
        Set<Tuple> uniqueTuples = new LinkedHashSet<>();
        Tuple tuple;
        while ((tuple = this.childOperator.getNextTuple()) != null) {
            uniqueTuples.add(tuple);
        }
        this.uniqueIterator = uniqueTuples.iterator();
    }


    @Override
    public Schema getSchema() {
        return this.childOperator.getSchema();
    }
}
