//package ed.inf.adbs.lightdb;
//
//import net.sf.jsqlparser.expression.Expression;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//public class SumOperator extends Operator {
//    private Operator childOperator;
//    private Map<List<Object>, Integer> groupBySumMap;
//    private Iterator<Map.Entry<List<Object>, Integer>> iterator;
//    private List<String> groupByColumnNames;
//    private Expression sumExpression;
//    private Schema schema;
//    private ExpressionEvaluator evaluator;
//
//    public SumOperator(Operator childOperator, List<String> groupByColumns, Expression sumExpression, Schema schema) {
//        this.childOperator = childOperator;
//        this.groupByColumnNames = groupByColumns;
//        this.sumExpression = sumExpression;
//        this.schema = schema;
//        this.groupBySumMap = new HashMap<>();
//        this.evaluator = new ExpressionEvaluator(schema);
//        aggregate();
//    }
//
//    private void aggregate() {
//        Tuple tuple;
//        while ((tuple = childOperator.getNextTuple()) != null) {
//            List<Object> key = new ArrayList<>();
//            for (String fullColumn : groupByColumnNames) {
//                String column = fullColumn;
//                if (fullColumn.contains(".")) {
//                    column = fullColumn.split("\\.")[1];
//                }
//                key.add(tuple.getFieldByName(column, schema));
//            }
//            Integer sum = groupBySumMap.getOrDefault(key, 0);
//            sum += evaluator.evaluate(sumExpression, tuple); // Evaluator returns the result of the SUM expression
//            groupBySumMap.put(key, sum);
//        }
//        iterator = groupBySumMap.entrySet().iterator();
//    }
//
//    @Override
//    public Tuple getNextTuple() {
//        if (!iterator.hasNext()) {
//            return null;
//        }
//        Map.Entry<List<Object>, Integer> entry = iterator.next();
//        List<Object> fields = new ArrayList<>(entry.getKey());
//        fields.add(entry.getValue()); // Add sum value
//        // Convert List<Object> to String[] for the Tuple constructor
//        String[] fieldsArray = fields.stream()
//                .map(Object::toString)
//                .toArray(String[]::new);
//        return new Tuple(fieldsArray);
//    }
//
//    @Override
//    public void reset() {
//        iterator = groupBySumMap.entrySet().iterator();
//    }
//
//    @Override
//    public Schema getSchema() {
//        // You need to build a new schema that includes the group by columns and the sum column
//        List<String> newSchemaColumns = new ArrayList<>(groupByColumnNames);
//        newSchemaColumns.add("SUM"); // or however you want to name the sum column
//        return new Schema(newSchemaColumns);
//    }
//}

package ed.inf.adbs.lightdb;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SumOperator extends Operator {
    private Operator childOperator; // Child operator to get input tuples from
    private List<Tuple> outputAfterSum; // List to store output tuples after sum aggregation
    private int outputIndex; // Index for iterating through the output tuples
    private ExpressionEvaluator evaluator; // An evaluator to compute the SUM expression
    private List<String> groupByColumns; // Columns used for grouping
    private Expression sumExpression; // The expression used in the SUM function
    private Schema schema; // Schema of the input tuples

    // Constructor for SumOperator
    public SumOperator(Operator childOperator, List<String> groupByColumns, Expression sumExpression, Schema schema) throws IOException {
        this.childOperator = childOperator;
        this.groupByColumns = groupByColumns;
        this.sumExpression = sumExpression;
        this.schema = schema;
        this.outputAfterSum = new ArrayList<>();
        this.evaluator = new ExpressionEvaluator(schema);
        this.outputIndex = 0;
        aggregate(); // Perform the aggregation when the operator is created
    }

     //Method to perform the SUM aggregation with possible GROUP BY
//    private void aggregate() throws IOException {
//        // Use a map to aggregate sums for each group
//        HashMap<List<Object>, Object> groupBySumMap = new HashMap<>();
//
//        Tuple tuple;
//        // Iterate over all input tuples
//        while ((tuple = childOperator.getNextTuple()) != null) {
//            // Extract the grouping key based on groupByColumns
//            List<Object> key = extractKey(tuple);
//
//            // Get the current sum for the group, or initialize it if the group is new
//            Object sumValue = groupBySumMap.getOrDefault(key, 0);
//
//            // Evaluate the SUM expression for the current tuple
//            Object tupleSum = evaluator.evaluate(sumExpression, tuple);
//
//            // Aggregate the sum for the current group
//            // Here we handle the Integer case; other data types should be handled accordingly
//            if (sumValue instanceof Integer && tupleSum instanceof Integer) {
//                sumValue = (Integer) sumValue + (Integer) tupleSum;
//            }
//
//            // Put the new sum back into the map
//            groupBySumMap.put(key, sumValue);
//        }
//
//        // After processing all tuples, create new tuples from the aggregated sums
//        for (Map.Entry<List<Object>, Object> entry : groupBySumMap.entrySet()) {
//            // The new tuple should include the group by columns and the aggregated sum
//            List<Object> newTupleFields = new ArrayList<>(entry.getKey());
//            newTupleFields.add(entry.getValue()); // Assuming this is the sum value you want to add
//
//            // Convert List<Object> to String[] for the Tuple constructor
//            String[] fieldsArray = newTupleFields.stream()
//                    .map(Object::toString) // Convert each Object to a String
//                    .toArray(String[]::new); // Collect as String[]
//
//            Tuple newTuple = new Tuple(fieldsArray); // Now it's a String[], no type mismatch
//            outputAfterSum.add(newTuple);
//        }
//    }

//    private void aggregate() {
//        // Map to store the aggregated sum for each group defined by groupByColumns
//        Map<List<Integer>, Integer> groupBySumMap = new HashMap<>();
//
//        Tuple tuple;
//        // Iterate over all the tuples from the child operator
//        while ((tuple = childOperator.getNextTuple()) != null) {
//
//            // Create a key for the groupBy map based on the values of the groupByColumns
//            Tuple finalTuple = tuple;
//
//            List<Integer> key = groupByColumns.stream()
//                    .map(columnName -> finalTuple.getFieldByName(columnName, schema))
//                    .collect(Collectors.toList());
//
//
//            // Evaluate the sumExpression for the current tuple
//            Integer sumValue = evaluator.evaluate(sumExpression, tuple);
//
//            // Update the sum for the current group
//            groupBySumMap.merge(key, sumValue, Integer::sum);
//        }
//
//        // Convert the grouped sums into a list of Tuples
//        for (Map.Entry<List<Integer>, Integer> entry : groupBySumMap.entrySet()) {
//            List<Integer> groupValues = entry.getKey();
//            Integer sumResult = entry.getValue();
//
//            // Create a string representation for the group values
//            String[] groupValueStrings = groupValues.stream().map(Object::toString).toArray(String[]::new);
//
//            // Create a new tuple with both the group values and the sum
//            String[] tupleData = new String[groupValueStrings.length + 1];
//            System.arraycopy(groupValueStrings, 0, tupleData, 0, groupValueStrings.length);
//            tupleData[tupleData.length - 1] = sumResult.toString(); // Add the sum at the end
//
//            // Add the new Tuple to the list of results
//            Tuple aggregatedTuple = new Tuple(tupleData);
//            outputAfterSum.add(aggregatedTuple);
//        }
//    }


    // Helper method to extract grouping key from a tuple
    // Helper method to extract grouping key from a tuple
    // Method to perform the SUM aggregation with possible GROUP BY
    private void aggregate() throws IOException {
        // Use a map to aggregate sums for each group key, where a key is a list of objects
        HashMap<List<Object>, Object> groupBySumMap = new HashMap<>();
        System.out.println("Entered AGG");


        Tuple tuple;
        // Iterate over all input tuples
        while ((tuple = childOperator.getNextTuple()) != null) {
            // Extract the grouping key based on groupByColumns
            List<Object> key = extractKey(tuple);

            // Get the current sum for the group, or initialize it if the group is new
            Object sumValue = groupBySumMap.getOrDefault(key, 0);

            if (sumExpression != null) {
                // Evaluate the SUM expression for the current tuple
                Object tupleSum = evaluator.evaluate(sumExpression, tuple);

                // Handle the sum aggregation here, considering the data type of sumValue and tupleSum
                // Assume Integer for now, but you may need to handle other types
                if (sumValue instanceof Integer && tupleSum instanceof Integer) {
                    sumValue = (Integer) sumValue + (Integer) tupleSum;
                }
            }




            // Update the sum back into the map for the group key
            groupBySumMap.put(key, sumValue);
        }

        // After processing all tuples, create new tuples from the aggregated sums
        outputAfterSum = convertMapEntriesToTuples(groupBySumMap);
        this.adjustSchema();
    }

    private void adjustSchema() {
        System.out.println("Adjusting Schema");
        schema.clearSchema();
        List<String> modifiedColumns = new ArrayList<>();
        for (String col : groupByColumns) {
            if (col.contains(".")) {
                col = col.split("\\.")[1];
            }
            modifiedColumns.add(col);
        }
        schema.initColumns(modifiedColumns);
        schema.addColumnName("SUM_1");
        System.out.println("FINAL COL NAMES: "+schema.getColumnNames());
    }

    // Helper method to extract grouping key from a tuple
    private List<Object> extractKey(Tuple tuple) {
        List<Object> key = new ArrayList<>();
        for (String colName : groupByColumns) {
            // If colName includes a table alias (like "Sailors.B"), extract only the column name
            if (colName.contains(".")) {
                colName = colName.split("\\.")[1];
            }

            key.add(tuple.getFieldByName(colName, schema));
        }
        return key;
    }

    // Convert the map entries into output tuples
    private List<Tuple> convertMapEntriesToTuples(HashMap<List<Object>, Object> groupBySumMap) {
        List<Tuple> tuples = new ArrayList<>();
        System.out.println("Map: " + groupBySumMap);
        for (Map.Entry<List<Object>, Object> entry : groupBySumMap.entrySet()) {
            // The new tuple should include the group by columns and the aggregated sum
            List<Object> newTupleFields = new ArrayList<>(entry.getKey());
            newTupleFields.add(entry.getValue()); // Add sum value to the end of the list
            System.out.println("Tuple fields: " + newTupleFields);

            // Convert List<Object> to String[] for the Tuple constructor
            String[] fieldsArray = newTupleFields.stream()
                    .map(Object::toString)
                    .toArray(String[]::new);

            tuples.add(new Tuple(fieldsArray));
        }
        System.out.println("Tuples = " + tuples);
        return tuples;
    }


    // Override of the getNextTuple method to return tuples from the aggregated result
    @Override
    public Tuple getNextTuple() {
        System.out.println("OutputAfterSum: "+outputAfterSum);
        if (outputIndex < outputAfterSum.size()) {
            // Return the next tuple in the list and increment the index
            return outputAfterSum.get(outputIndex++);
        }
        return null; // Return null when all tuples have been returned
    }

    // Override of the reset method to reset the index for iterating through the tuples
    @Override
    public void reset() {
        outputIndex = 0;
    }

    // Override of the getSchema method to return the schema of the operator
    @Override
    public Schema getSchema() {
        return schema;
    }
}
