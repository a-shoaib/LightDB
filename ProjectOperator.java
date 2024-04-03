package ed.inf.adbs.lightdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProjectOperator extends Operator {
    private Operator childOperator;
    private List<String> projectionColumns;
    private Schema schema;
    private boolean alteredSchema = false;

    public ProjectOperator(Operator childOperator, List<String> projectionColumns, Schema schema) {
        this.childOperator = childOperator;
        this.projectionColumns = projectionColumns;
        this.schema = schema;
    }


    @Override
    public Tuple getNextTuple() {
        Tuple childTuple = childOperator.getNextTuple();
        System.out.println("Child Tuple: " + childTuple);
        if (childTuple == null) return null; // No more tuples




        String[] projectedValues = projectionColumns.stream()
                .mapToInt(column -> {

                    int columnIndex = schema.getColumnIndex(column);
                    System.out.println("Col Index: " + columnIndex + column);
                    if (columnIndex == -1) {
                        throw new IllegalArgumentException("Column not found in schema: " + column);
                    }
                    return childTuple.getField(columnIndex);
                })
                .mapToObj(String::valueOf) // Convert integers to strings
                .toArray(String[]::new); // Collect into a String array

        return new Tuple(projectedValues);
    }


    @Override
    public void reset() {
        childOperator.reset();
    }

    /**
     * Returns the schema of the operator's output.
     * @return Schema object representing the output schema of this operator.
     */
    @Override
    public Schema getSchema() {
        return this.schema;
    }


}
