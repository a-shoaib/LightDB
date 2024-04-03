package ed.inf.adbs.lightdb;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import java.util.*;

public class SortOperator extends Operator {
    private Operator childOperator;
    private List<Tuple> buffer;
    private List<Column> orderByColumns;
    private Comparator<Tuple> tupleComparator;
    private Iterator<Tuple> iterator;

    public SortOperator(Operator childOperator, List<Column> orderByColumns, Schema schema) {
        this.childOperator = childOperator;
        this.orderByColumns = orderByColumns;
        this.buffer = new ArrayList<>();
        this.tupleComparator = createTupleComparator(orderByColumns, schema);

        // Read and buffer all tuples from the child operator
        Tuple tuple;
        while ((tuple = childOperator.getNextTuple()) != null) {
            buffer.add(tuple);
        }

        // Sort the buffer
        Collections.sort(buffer, tupleComparator);

        // Create an iterator for sorted results
        this.iterator = buffer.iterator();
    }


    private Comparator<Tuple> createTupleComparator(List<Column> orderByColumns, Schema schema) {
        return (Tuple t1, Tuple t2) -> {
            for (Column col : orderByColumns) {
                String colName = col.getColumnName();
                if (colName.contains(".")) {
                    if (!schema.isSelfJoin) {
                        colName = colName.split("\\.")[1];
                    }
                }
                int index = schema.getColumnIndex(colName);
                System.out.println("COLUMN NAME: " + colName);
                if (index == -1) {
                    throw new IllegalStateException("Column not found in schema: " + col.getColumnName());
                }
                System.out.println("Schema: " + schema.getColumnNames());
                System.out.println("T1: " + t1);
                System.out.println("T2: " + t2);
                Comparable value1 = (Comparable) t1.getFieldByName(colName, schema);
                Comparable value2 = (Comparable) t2.getFieldByName(colName, schema);
                System.out.println("T1: " + t1);
                int comparison = value1.compareTo(value2);
                if (comparison != 0) {
                    return comparison; // Ascending order
                }

            }
            return 0; // All compared columns are equal
        };
    }






    @Override
    public Tuple getNextTuple() {

        return iterator.hasNext() ? iterator.next() : null;
    }

    @Override
    public void reset() {
        this.iterator = buffer.iterator();
    }

    @Override
    public Schema getSchema() {
        return childOperator.getSchema();
    }
}

