//package ed.inf.adbs.lightdb;
//
//import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
//import net.sf.jsqlparser.schema.Column;
//
//import net.sf.jsqlparser.expression.Expression;
//import java.util.ArrayList;
//import java.util.List;
//import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
//
//import java.util.*;
//
//public class QueryPlannerVol2 {
//    private DatabaseCatalog catalog;
//    private QueryParserVol2 queryParser;
//
//    /**
//     * Constructs an instance of QueryPlanner given the database catalog.
//     *
//     * @param catalog the database catalog
//     */
//    public QueryPlannerVol2(DatabaseCatalog catalog) {
//        this.catalog = catalog;
//        this.queryParser = new QueryParserVol2();
//    }
//
//    /**
//     * Creates an execution plan for a given SQL query.
//     *
//     * @param queryFilePath the file path to the SQL query
//     * @return the root operator of the constructed query plan
//     * @throws Exception if there is an error in parsing or planning
//     */
//    public Operator createQueryPlan(String queryFilePath) throws Exception {
//        queryParser.parseQuery(queryFilePath);
//
//        Operator rootOperator = null;
//        Operator prevOperator = null;
//        Schema combinedSchema = null;
//
//        for (String tableName : queryParser.getTables()) {
//            String filePath = catalog.getTablePath(tableName);
//            Schema tableSchema = catalog.getTableSchema(tableName);
//            Operator scanOperator = new ScanOperator(filePath, tableSchema);
//
//            Expression selectCondition = findSelectConditionForTable(tableName, queryParser.getSelectionConditions());
//            if (selectCondition != null) {
//                scanOperator = new SelectOperator(scanOperator, selectCondition, tableSchema);
//            }
//
//
//            // If this is the first table, it becomes the root
//            if (rootOperator == null) {
//                rootOperator = scanOperator;
//                combinedSchema = tableSchema;
//            } else {
//                // if it is not the first table, then we should join the current table with the previous table(s),
//                // the schema of which is in the combinedSchema variable
//                combinedSchema = combineSchemas(combinedSchema, tableSchema);
//                Expression joinCondition = findJoinCondition(prevOperator, scanOperator, queryParser.getJoinConditions());
//                rootOperator = new JoinOperator(rootOperator, scanOperator, joinCondition, combinedSchema);
//            }
//            prevOperator = scanOperator; // Keep the reference to the previous operator for the next iteration
//        }
//
//        // Apply projections if needed
//        if (!queryParser.getProjectionColumns().isEmpty()) {
//            rootOperator = new ProjectOperator(rootOperator, queryParser.getProjectionColumns(), combinedSchema);
//        }
//
//        return rootOperator;
//    }
//
//    /**
//     * Finds a selection condition for a given table.
//     *
//     * @param tableName the name of the table
//     * @param selectionConditions the map of selection conditions
//     * @return the selection condition for the table, if any
//     */
//    private Expression findSelectConditionForTable(String tableName, Map<String, Expression> selectionConditions) {
//        return selectionConditions.get(tableName);
//    }
//
//    /**
//     * Finds a join condition between two tables.
//     *
//     * @param leftOperator  the operator for the left table
//     * @param rightOperator the operator for the right table
//     * @param joinConditions the list of join conditions
//     * @return the join condition between the two tables, if any
//     */
//    private Expression findJoinCondition(Operator leftOperator, Operator rightOperator, List<Expression> joinConditions) {
//        for (Expression expression : joinConditions) {
//            if (isJoinConditionForTables(expression, leftOperator.getSchema(), rightOperator.getSchema())) {
//                return expression;
//            }
//        }
//        return null; // Return null if no join condition found
//    }
//
//    /**
//     * Combines the schemas of two tables for a join operation.
//     *
//     * @param leftSchema  the schema of the left table
//     * @param rightSchema the schema of the right table
//     * @return the combined schema
//     */
//    private Schema combineSchemas(Schema leftSchema, Schema rightSchema) {
//        List<String> combinedColumnNames = new ArrayList<>(leftSchema.getColumnNames());
//        combinedColumnNames.addAll(rightSchema.getColumnNames());
//        return new Schema(combinedColumnNames);
//    }
//
//    /**
//     * Checks if an expression is a join condition for the specified tables.
//     *
//     * @param expression  the expression to check
//     * @param leftSchema  the schema of the left table
//     * @param rightSchema the schema of the right table
//     * @return true if the expression is a join condition, false otherwise
//     */
//    private boolean isJoinConditionForTables(Expression expression, Schema leftSchema, Schema rightSchema) {
//        if (!(expression instanceof EqualsTo)) {
//            return false;
//        }
//        EqualsTo equals = (EqualsTo) expression;
//        String leftColumn = ((Column) equals.getLeftExpression()).getColumnName();
//        String rightColumn = ((Column) equals.getRightExpression()).getColumnName();
//        return leftSchema.getColumnIndex(leftColumn) != -1 && rightSchema.getColumnIndex(rightColumn) != -1;
//    }
//}
