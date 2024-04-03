//package ed.inf.adbs.lightdb;
//
//import net.sf.jsqlparser.expression.BinaryExpression;
//import net.sf.jsqlparser.expression.operators.relational.*;
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
//        Schema combinedSchema = null;
//        List<Operator> operators = new ArrayList<>(); // Store operators for all tables
//
//        // Process where clause to separate selection from join conditions
//        WhereClauseProcessor whereClauseProcessor = new WhereClauseProcessor(queryParser.getTables());
//        Expression whereExpression = queryParser.getWhereExpression();
//        whereClauseProcessor.processWhereClause(whereExpression);
//        Map<String, List<Expression>> selectionConditions = whereClauseProcessor.getSelectionConditions();
//        List<Expression> joinConditions = whereClauseProcessor.getJoinConditions();
//
//        for (String tableName : queryParser.getTables()) {
//            String filePath = catalog.getTablePath(tableName);
//            Schema tableSchema = catalog.getTableSchema(tableName);
//            Operator scanOperator = new ScanOperator(filePath, tableSchema);
//
//            // Apply selection conditions to scan operator, if any
//            List<Expression> tableSelectionConditions = selectionConditions.getOrDefault(tableName, new ArrayList<>());
//            for (Expression condition : tableSelectionConditions) {
//                scanOperator = new SelectOperator(scanOperator, condition, tableSchema);
//            }
//
//            // Keep track of operators for all tables
//            operators.add(scanOperator);
//
//            // Combine schemas progressively
//            if (combinedSchema == null) {
//                combinedSchema = tableSchema; // The first table schema
//            } else {
//                combinedSchema = combineSchemas(combinedSchema, tableSchema);
//            }
//
//            // Set the first table as the root operator
//            if (rootOperator == null) {
//                rootOperator = scanOperator;
//            }
//        }
//
//        // Apply join conditions if there are more than one table
//        if (operators.size() > 1) {
//            for (int i = 1; i < operators.size(); i++) {
//                Operator leftOperator = (i == 1) ? operators.get(0) : rootOperator;
//                Operator rightOperator = operators.get(i);
//
//                // You may need to adjust how the joinCondition is found
//                Expression joinCondition = findApplicableJoinCondition(leftOperator, rightOperator, joinConditions);
//                rootOperator = new JoinOperator(leftOperator, rightOperator, joinCondition, combinedSchema);
//            }
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
//
//    private Expression findApplicableJoinCondition(Operator leftOperator, Operator rightOperator, List<Expression> joinConditions) {
//        Schema leftSchema = leftOperator.getSchema();
//        Schema rightSchema = rightOperator.getSchema();
//
//        for (Expression condition : joinConditions) {
//            if (condition instanceof EqualsTo) {
//                EqualsTo equalsCondition = (EqualsTo) condition;
//                Column leftColumn = (Column) equalsCondition.getLeftExpression();
//                Column rightColumn = (Column) equalsCondition.getRightExpression();
//
//                boolean leftColumnBelongs = leftSchema.getColumnNames().contains(leftColumn.getColumnName());
//                boolean rightColumnBelongs = rightSchema.getColumnNames().contains(rightColumn.getColumnName());
//
//                // For join condition, each column must belong to different tables
//                if (leftColumnBelongs != rightColumnBelongs) {
//                    return equalsCondition;
//                }
//            }
//            // Add other conditions for GreaterThan, GreaterThanEquals, etc. if needed
//        }
//
//        return null; // No applicable join condition found
//    }
//
//
//    private Column getBinaryExpressionColumn(Expression expression) {
//        if (expression instanceof Column) {
//            return (Column) expression;
//        }
//        // Add other conditions if there are other ways columns can be represented in your binary expressions
//        return null;
//    }
//
//
//
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
package ed.inf.adbs.lightdb;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.schema.Column;

import net.sf.jsqlparser.expression.Expression;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Table;

import java.util.*;
import java.util.stream.Collectors;

public class QueryPlannerVol2 {
    private DatabaseCatalog catalog;
    private QueryParserVol2 queryParser;

    private List<String> projectionColumns;

    /**
     * Constructs an instance of QueryPlanner given the database catalog.
     *
     * @param catalog the database catalog
     */
    public QueryPlannerVol2(DatabaseCatalog catalog) {
        this.catalog = catalog;
        this.queryParser = new QueryParserVol2();
    }

    /**
     * Creates an execution plan for a given SQL query.
     *
     * @param queryFilePath the file path to the SQL query
     * @return the root operator of the constructed query plan
     * @throws Exception if there is an error in parsing or planning
     */
    public Operator createQueryPlan(String queryFilePath) throws Exception {
//        try {
        queryParser.parseQuery(queryFilePath);
        this.catalog = DatabaseCatalog.getInstance();


        Operator rootOperator = null;
        Schema combinedSchema = null;
        Expression whereExpression = queryParser.getWhereExpression();
        System.out.println("Where  = " + whereExpression);

        WhereClauseProcessor whereClauseProcessor = null;
        Map<String, List<Expression>> selectionConditions = null;
        List<Expression> joinConditions = null;

        if (whereExpression != null) {
            // Initialize the WhereClauseProcessor and process the WHERE clause
            whereClauseProcessor = new WhereClauseProcessor(queryParser.getTables());
            whereClauseProcessor.processWhereClause(queryParser.getWhereExpression());
            selectionConditions = whereClauseProcessor.getSelectionConditions();
            joinConditions = whereClauseProcessor.getJoinConditions();
            System.out.println("Select Conditions = " + selectionConditions);
            System.out.println("Join Conditions = " + joinConditions);
        }
        // For each table, we will create a scan operator as it is always necessary
        System.out.println("All tables: " + queryParser.getTables());

        for (String tableName : queryParser.getTables()) {
            System.out.println("Table: " + tableName);
            String filePath = catalog.getTablePath(tableName);
            Schema tableSchema = catalog.getTableSchema(tableName);
            System.out.println("Current Schema: "+tableSchema.getColumnNames());
            Operator scanOperator = new ScanOperator(filePath, tableSchema);
            System.out.println("SCAN OPERATOR INITIALIZED");
            // If there are selection conditions, we will loop through them and apply all selection.
            // We complete all selection before moving on to joins as to minimize the size of the joined relations
            System.out.println("Selection Conditions: " + selectionConditions);
            if (selectionConditions != null && selectionConditions.containsKey(tableName)) {
                for (Expression selectCondition : selectionConditions.get(tableName)) {
                    scanOperator = new SelectOperator(scanOperator, selectCondition, tableSchema);
                    System.out.println("SELECT OPERATOR INITIALIZED");
                }
            }

            // If this is the first table, it becomes the root operator.
            if (rootOperator == null) {
                rootOperator = scanOperator;
                combinedSchema = tableSchema;
                System.out.println("FIRST TABLE");
            } else {
                // For subsequent tables, we will join with the root operator using the appropriate join condition
                System.out.println("PLANNING JOIN");
                combinedSchema = combineSchemas(combinedSchema, tableSchema);
                if (joinConditions != null) {
                    Expression joinCondition = findApplicableJoinCondition(rootOperator, scanOperator, joinConditions, combinedSchema);
                    rootOperator = new JoinOperator(rootOperator, scanOperator, joinCondition, combinedSchema);
                    System.out.println("CONDITION-BASED JOIN");
                }
                // if there are no join conditions, then this is a cartesian product join
                else {
                    rootOperator = new JoinOperator(rootOperator, scanOperator, null, combinedSchema);
                    System.out.println("CROSS JOIN");
                }
                //combinedSchema = rootOperator.getSchema();


            }

        }

        if (queryParser.getSumColumn() != null || queryParser.getGroupByColumns().size() > 0) {
            // If GROUP BY is present, we need to pass groupByColumns; otherwise, pass an empty list or null
            String sumColumn;
            String fullColumn = queryParser.getSumColumn();
            if (fullColumn == null) fullColumn = "0";
            if (fullColumn.contains(".")) {
                sumColumn = fullColumn.split("\\.")[1];
            }
            else {
                sumColumn = fullColumn;
            }
            List<String> groupByColumns = queryParser.getGroupByColumns() != null ? queryParser.getGroupByColumns() : Collections.emptyList();
            Expression sumExpression = queryParser.getSumExpression();

            System.out.println("full column:" +fullColumn + groupByColumns);


                    // Wrap the current rootOperator in a SumOperator
            rootOperator = new SumOperator(rootOperator, groupByColumns, sumExpression, combinedSchema);

            System.out.println("SUM (GROUP BY) OPERATOR WITH TABLES: "+combinedSchema.getColumnNames());

            // GROUP BY might change the schema, so ensure you update the combined schema
            combinedSchema = rootOperator.getSchema();
            System.out.println("Comb schema after grp by: "+combinedSchema.getColumnNames());

        }



        // Finally, if there are projection columns specified, we wrap the operator in a ProjectOperator
        if (!queryParser.isSelectAll() && !queryParser.getProjectionColumns().isEmpty()) {
            rootOperator = new ProjectOperator(rootOperator, queryParser.getProjectionColumns(), combinedSchema);
            System.out.println("PROJECTION OPERATOR WITH TABLES: " + combinedSchema.getColumnNames());
            combinedSchema = rootOperator.getSchema();
            this.projectionColumns = queryParser.getProjectionColumns();
            System.out.println("Projection columns: " + projectionColumns);
        }
        else {
            this.projectionColumns = combinedSchema.getColumnNames();
        }


        if (queryParser.getQueryRequiresDistinct()) {
            rootOperator = new DuplicateEliminationOperator(rootOperator, this.projectionColumns ,combinedSchema);
            System.out.println("DISTINCT OPERATOR");
        }

        if (queryParser.getOrderByElements() != null && !queryParser.getOrderByElements().isEmpty()) {
            List<Column> orderByColumns = queryParser.getOrderByElements().stream()
                    .map(orderBy -> new Column(new Table(null, null), orderBy.toString()))
                    .collect(Collectors.toList());
            rootOperator = new SortOperator(rootOperator, orderByColumns, combinedSchema);
            System.out.println("ORDERY BY OPERATOR");
        }



        System.out.println("Operator: "+rootOperator);
        return rootOperator;

    }

    private Operator handleSumAggregation(Operator currentOperator, Schema schema) throws IOException {
        // Extract the SUM expression and group by columns from the QueryParser
        Expression sumExpression = this.queryParser.getSumExpression(); // You should define a method to get this from the parser
        List<String> groupByColumns = queryParser.getGroupByColumns();

        // If no SUM in the query, just return the current operator
        if (sumExpression == null) {
            return currentOperator;
        }

        // Instantiate your SumOperator with the expression evaluator
        ExpressionEvaluator evaluator = new ExpressionEvaluator(schema);
        SumOperator sumOperator = new SumOperator(currentOperator, groupByColumns, sumExpression, schema);

        // Return the new SumOperator
        return sumOperator;
    }


    private boolean isJoinConditionApplicable(Expression joinCondition, Schema leftSchema, Schema rightSchema, Schema combinedSchema) {
        combinedSchema.isSelfJoin = false;
        if (joinCondition instanceof BinaryExpression) {
            System.out.println( "Join Condition check: " + joinCondition);
            String leftColumnName;
            String rightColumnName;
            BinaryExpression binaryCondition = (BinaryExpression) joinCondition;
            Expression leftExpression = binaryCondition.getLeftExpression();
            Expression rightExpression = binaryCondition.getRightExpression();
            System.out.println("Left Expression: " + leftExpression);
            System.out.println("Right Expression: " + rightExpression);

            if (queryParser.isSelfJoin) {
                combinedSchema.isSelfJoin = true;
                leftColumnName =  leftExpression.toString();
                rightColumnName = rightExpression.toString();
                boolean leftColumnExists = combinedSchema.getColumnNames().contains(leftColumnName);
                boolean rightColumnExists = combinedSchema.getColumnNames().contains(rightColumnName);
                System.out.println("Left Col Exists: " + leftColumnExists);
                System.out.println("Right Col Exists: " + rightColumnExists);


                boolean differentTables = !((Column) leftExpression).getTable().getName().equals(((Column) rightExpression).getTable().getName());

                return leftColumnExists && rightColumnExists && differentTables;

            } else if (leftExpression instanceof Column && rightExpression instanceof Column) {
                leftColumnName = ((Column) leftExpression).getColumnName();
                rightColumnName = ((Column) rightExpression).getColumnName();
                System.out.println("Left ColName: " + leftColumnName);
                System.out.println("Right ColName: " + rightColumnName);


                boolean leftColumnExists = combinedSchema.getColumnNames().contains(leftColumnName);
                boolean rightColumnExists = combinedSchema.getColumnNames().contains(rightColumnName);
                System.out.println("Left Col Exists: " + leftColumnExists);
                System.out.println("Right Col Exists: " + rightColumnExists);


                boolean differentTables = !((Column) leftExpression).getTable().getName().equals(((Column) rightExpression).getTable().getName());

                return leftColumnExists && rightColumnExists && differentTables;

            }
//            if (leftExpression instanceof Column && rightExpression instanceof Column) {
//                String leftColumnName = ((Column) leftExpression).getColumnName();
//                String rightColumnName = ((Column) rightExpression).getColumnName();
//                System.out.println("Left ColName: " + leftColumnName);
//                System.out.println("Right ColName: " + rightColumnName);
//
//
//                boolean leftColumnExists = combinedSchema.getColumnNames().contains(leftColumnName);
//                boolean rightColumnExists = combinedSchema.getColumnNames().contains(rightColumnName);
//                System.out.println("Left Col Exists: " + leftColumnExists);
//                System.out.println("Right Col Exists: " + rightColumnExists);
//
//
//                boolean differentTables = !((Column) leftExpression).getTable().getName().equals(((Column) rightExpression).getTable().getName());
//
//                return leftColumnExists && rightColumnExists && differentTables;
//            }
        }
        return false; // If not a binary expression with columns, or if columns are from the same table, return false
    }




    /**
     * Finds a selection condition for a given table.
     *
     * @param tableName the name of the table
     * @param selectionConditions the map of selection conditions
     * @return the selection condition for the table, if any
     */
    private Expression findSelectConditionForTable(String tableName, Map<String, Expression> selectionConditions) {
        return selectionConditions.get(tableName);
    }

    /**
     * Finds a join condition between two tables.
     *
     * @param leftOperator  the operator for the left table
     * @param rightOperator the operator for the right table
     * @param joinConditions the list of join conditions
     * @return the join condition between the two tables, if any
     */
    private Expression findJoinCondition(Operator leftOperator, Operator rightOperator, List<Expression> joinConditions) {
        for (Expression expression : joinConditions) {
            if (isJoinConditionForTables(expression, leftOperator.getSchema(), rightOperator.getSchema())) {
                return expression;
            }
        }
        return null; // Return null if no join condition found
    }

    /**
     * Combines the schemas of two tables for a join operation.
     *
     * @param leftSchema  the schema of the left table
     * @param rightSchema the schema of the right table
     * @return the combined schema
     */
    private Schema combineSchemas(Schema leftSchema, Schema rightSchema) {
        List<String> combinedColumnNames = new ArrayList<>(leftSchema.getColumnNames());
        combinedColumnNames.addAll(rightSchema.getColumnNames());
        return new Schema(combinedColumnNames);
    }

    /**
     * Checks if an expression is a join condition for the specified tables.
     *
     * @param expression  the expression to check
     * @param leftSchema  the schema of the left table
     * @param rightSchema the schema of the right table
     * @return true if the expression is a join condition, false otherwise
     */
    private boolean isJoinConditionForTables(Expression expression, Schema leftSchema, Schema rightSchema) {
        if ((expression instanceof EqualsTo)) {
            EqualsTo equals = (EqualsTo) expression;
            String leftColumn = ((Column) equals.getLeftExpression()).getColumnName();
            String rightColumn = ((Column) equals.getRightExpression()).getColumnName();
            return leftSchema.getColumnIndex(leftColumn) != -1 && rightSchema.getColumnIndex(rightColumn) != -1;
        } else if (expression instanceof MinorThan) {

            MinorThan minorThan = (MinorThan) expression;
            String leftColumn = ((Column) minorThan.getLeftExpression()).getColumnName();
            String rightColumn = ((Column) minorThan.getRightExpression()).getColumnName();
            return leftSchema.getColumnIndex(leftColumn) != -1 && rightSchema.getColumnIndex(rightColumn) != -1;

        }

        return false;

    }

    private Expression findApplicableJoinCondition(Operator leftOperator, Operator rightOperator, List<Expression> joinConditions, Schema combinedSchema) {
        for (Expression condition : joinConditions) {
            if (isJoinConditionApplicable(condition, leftOperator.getSchema(), rightOperator.getSchema(), combinedSchema)) {
                return condition;
            }
        }
        // No explicit join condition; possibly a cross product is intended
        return null;
    }

}
