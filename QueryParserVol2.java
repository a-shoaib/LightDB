//package ed.inf.adbs.lightdb;
//
//import net.sf.jsqlparser.expression.Expression;
//import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
//import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
//import net.sf.jsqlparser.parser.CCJSqlParserUtil;
//import net.sf.jsqlparser.schema.Column;
//import net.sf.jsqlparser.statement.Statement;
//import net.sf.jsqlparser.statement.select.*;
//import net.sf.jsqlparser.statement.select.SelectItem;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.*;
//import java.util.stream.Collectors;
//import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
//
//public class QueryParserVol2 {
//
//    private List<String> tables = new ArrayList<>();
//    private Expression whereExpression;
//    private List<String> projectionColumns = new ArrayList<>();
//    private Map<String, Expression> selectionConditions = new HashMap<>();
//    private List<Expression> joinConditions = new ArrayList<>();
//    private PlainSelect plainSelect;
//    private Map<String, Expression> processedSelectionConditions = new HashMap<>();
//    private List<Expression> processedJoinConditions = new ArrayList<>();
//
//    public void parseQuery(String queryFilePath) throws Exception {
//        String sqlQuery = new String(Files.readAllBytes(Paths.get(queryFilePath)));
//        Statement statement = CCJSqlParserUtil.parse(sqlQuery);
//
//        if (statement instanceof Select) {
//            Select selectStatement = (Select) statement;
//            this.plainSelect = (PlainSelect) selectStatement;
//
//            // Handle tables in the FROM clause
//            tables.add(plainSelect.getFromItem().toString());
//            if (plainSelect.getJoins() != null) {
//                for (Join join : plainSelect.getJoins()) {
//                    tables.add(join.getRightItem().toString());
//                }
//            }
//
//            // Handles WHERE expression
//            this.whereExpression = plainSelect.getWhere();
//            parseWhereExpression();
//
//            // Handles SELECT items for projection
//            ProjectColumnExtractor extractor = new ProjectColumnExtractor();
//            plainSelect.getSelectItems().forEach(item -> item.accept(extractor));
//            this.projectionColumns = extractor.getProjectionColumns();
//        } else {
//            throw new IllegalArgumentException("The query is not a SELECT statement.");
//        }
//    }
//
//    private void parseWhereExpression() {
//        if (whereExpression != null) {
//            // Initialize the WhereClauseProcessor with the list of tables
//            WhereClauseProcessor whereClauseProcessor = new WhereClauseProcessor(tables);
//            // Process the where clause expression
//            whereClauseProcessor.processWhereClause(whereExpression);
//
//            // Retrieve and store the processed conditions
//            processedSelectionConditions = whereClauseProcessor.getSelectionConditions()
//                    .entrySet().stream()
//                    .collect(Collectors.toMap(
//                            Map.Entry::getKey,
//                            e -> e.getValue().stream()
//                                    .reduce(AndExpression::new)
//                                    .orElse(null)  // Use null as the default for empty lists, which should not happen here
//                    ));
//
//            processedJoinConditions = whereClauseProcessor.getJoinConditions();
//        }
//    }
//
//
//    public List<String> getTables() {
//        return tables;
//    }
//    public Map<String, Expression> getProcessedSelectionConditions() {
//        return processedSelectionConditions;
//    }
//
//    public List<Expression> getProcessedJoinConditions() {
//        return processedJoinConditions;
//    }
//
//    public Expression getWhereExpression() {
//        return whereExpression;
//    }
//
//    public List<String> getProjectionColumns() {
//        return projectionColumns;
//    }
//
//    public Map<String, Expression> getSelectionConditions() {
//        return selectionConditions;
//    }
//
//    public List<Expression> getJoinConditions() {
//        return joinConditions;
//    }
//}


package ed.inf.adbs.lightdb;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class QueryParserVol2 {

    private List<String> tables = new ArrayList<>();
    private Expression whereExpression;
    private List<String> projectionColumns = new ArrayList<>();
    private Map<String, List<Expression>> selectionConditions = new HashMap<>();
    private List<Expression> joinConditions = new ArrayList<>();
    private PlainSelect plainSelect;

    private Set<String> aliases = new HashSet<>();
    private DatabaseCatalog catalog = DatabaseCatalog.getInstance();
    private List<OrderByElement> orderByElements;

    private boolean queryRequiresDistinct = false;
    private boolean isSelectAll = false;


    private String sumColumn; // The column to apply SUM on
    private List<String> groupByColumns; // The columns used in GROUP BY
    private Expression sumExpression;

    public boolean hasAlias = false;
    public boolean isSelfJoin = false;

    private Set<String> joinTables = new HashSet<String>();


    public void parseQuery(String queryFilePath) throws Exception {
        String sqlQuery = new String(Files.readAllBytes(Paths.get(queryFilePath)));
        Statement statement = CCJSqlParserUtil.parse(sqlQuery);


        // This map will be used to determine if there is a self join
        // If there is, we will approach the aliases differently.
        Map<String, Integer> TableCount = new HashMap<>();



        if (statement instanceof Select) {
            Select selectStatement = (Select) statement;
            this.plainSelect = (PlainSelect) selectStatement;
            if (plainSelect.getDistinct() != null) {
                queryRequiresDistinct = true;
            }
            isSelectAll = plainSelect.getSelectItems().stream()
                    .anyMatch(item -> item.toString().equals("*"));
            FromItem fromItem = plainSelect.getFromItem();
            if (fromItem instanceof Table) {
                Table table = (Table) fromItem;
                String tableName = table.getName();
                TableCount.put(tableName,1);
            }
            if (plainSelect.getJoins() != null) {
                for (Join join : plainSelect.getJoins()) {
                    Table table = (Table) join.getFromItem();
                    String tableName = table.getName();
                    if (TableCount.containsKey(tableName)) {
                        TableCount.put(tableName, TableCount.get(tableName) + 1);
                    }
                    else {
                        TableCount.put(tableName, 1);
                    }
                }
            }

            this.whereExpression = plainSelect.getWhere();
            parseWhereExpression();

            // Handles SELECT items for projection
            ProjectColumnExtractor extractor = new ProjectColumnExtractor();
            plainSelect.getSelectItems().forEach(item -> item.accept(extractor));
            this.projectionColumns = extractor.getProjectionColumns();


            fromItem = plainSelect.getFromItem();
            if (fromItem instanceof Table) {
                Table table = (Table) fromItem;
                if (table.getAlias() != null) {
                    hasAlias = true;
                    String aliasName = table.getAlias().getName();
                    String tableName = table.getName();
                    joinTables.add(tableName);
                    Schema tableSchema = catalog.getTableSchema(tableName);
                    aliases.add(aliasName);
//                    System.out.println("Alias: " + aliasName);
                    // Add alias to the catalog with the same schema and file path as the original table
                    DatabaseCatalog catalog = DatabaseCatalog.getInstance();
                    Schema aliasSchema = new Schema();
                    if (TableCount.get(tableName) > 1){
                        List<String> columns = tableSchema.getColumnNames();
                        for (String column : columns) {
                            String columnName = aliasName + "." + column;
                            aliasSchema.addColumnName(columnName);
                        }
                        catalog.addTable(aliasName, catalog.getTablePath(tableName), aliasSchema);
                    }
                    else {
                        catalog.addTable(aliasName, catalog.getTablePath(tableName), catalog.getTableSchema(tableName));
                    }
                    if (!tables.contains(aliasName)) {
                        tables.add(aliasName);
                    }

                }
                else {
                    tables.add(table.getName());
                }
            }
            if (plainSelect.getJoins() != null) {
//                System.out.println("PARSED JOIN");
                for (Join join : plainSelect.getJoins()) {
                    Table table = (Table) join.getFromItem();
                    if (table.getAlias() != null) {
                        String aliasName = table.getAlias().getName();
                        String tableName = table.getName();
                        if(joinTables.contains(tableName)) {
                            isSelfJoin = true;
                        }
                        joinTables.add(tableName);
                        Schema tableSchema = catalog.getTableSchema(tableName);
                        aliases.add(aliasName);
                        //System.out.println("Alias: " + aliasName);
                        // Add alias to the catalog with the same schema and file path as the original table
                        Schema aliasSchema = new Schema();
                        if (TableCount.get(tableName) > 1) {
                            List<String> columns = tableSchema.getColumnNames();
                            for (String column : columns) {
                                String columnName = aliasName + "." + column;
                                aliasSchema.addColumnName(columnName);
                            }
                            catalog.addTable(aliasName, catalog.getTablePath(tableName), aliasSchema);

                        } else {
                            catalog.addTable(aliasName, catalog.getTablePath(tableName), catalog.getTableSchema(tableName));
                        }
                        if (!tables.contains(aliasName)) {
                            tables.add(aliasName);
                        }
                    }
                    else {
                        tables.add(table.getName());
                    }
                }
            }

            for (SelectItem item : plainSelect.getSelectItems()) {
                if (item instanceof SelectItem) {
                    Expression expression = ((SelectItem) item).getExpression();
                    if (expression instanceof Function) {
                        Function function = (Function) expression;
                        if ("SUM".equalsIgnoreCase(function.getName())) {
                            this.sumExpression = (Expression) function.getParameters().getExpressions().get(0);
                            break; // Assuming only one SUM function in the SELECT
                        }
                        else {

                        }
                    }
                }
            }






        } else {
            throw new IllegalArgumentException("The query is not a SELECT statement.");
        }

        this.orderByElements = plainSelect.getOrderByElements();

        this.sumColumn = extractSumColumn(plainSelect.getSelectItems());
        if (plainSelect.getGroupBy() != null) {
            this.groupByColumns = (List<String>) plainSelect.getGroupBy().getGroupByExpressions().stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        } else {
            this.groupByColumns = new ArrayList<>();
        }


    }

    private void parseWhereExpression() {
        if (whereExpression != null) {
            //System.out.println(whereExpression);
            // Initialize the WhereClauseProcessor with the list of tables
            WhereClauseProcessor whereClauseProcessor = new WhereClauseProcessor(tables);
            // Process the where clause expression
            whereClauseProcessor.processWhereClause(whereExpression);

            // Retrieve and store the processed conditions
            selectionConditions = whereClauseProcessor.getSelectionConditions();
            joinConditions = whereClauseProcessor.getJoinConditions();
        }
    }

    private String extractSumColumn(List<SelectItem<?>> selectItems) {
        for (SelectItem item : selectItems) {
            if (item instanceof SelectItem) {
                Expression expression = ((SelectItem) item).getExpression();
                if (expression instanceof Function) {
                    Function function = (Function) expression;
                    if ("SUM".equalsIgnoreCase(function.getName())) {
                        Expression sumExpression = (Expression) function.getParameters().getExpressions().get(0);
                        return parseSumExpression(sumExpression);
                    }
                }
            }
        }
        return null;
    }

    private String parseSumExpression(Expression sumExpression) {
        // If the SUM expression is just a simple column, return its string representation
        if (sumExpression instanceof Column) {
            return sumExpression.toString();
        }

        // If the SUM expression involves a multiplication or addition, handle accordingly
        // You will need to implement logic here based on how complex your SUM expressions can be

        // Example for multiplication:
        if (sumExpression instanceof Multiplication) {
            Multiplication multiplication = (Multiplication) sumExpression;
            // For simplicity, concatenate operands with *
            return multiplication.getLeftExpression().toString() + " * " + multiplication.getRightExpression().toString();
        }

        // Add logic for addition or other arithmetic expressions if needed

        // Default to calling toString on the expression
        return sumExpression.toString();
    }

    private List<String> extractGroupByColumns(List<Expression> groupByExpressions) {
        List<String> columns = new ArrayList<>();
        if (groupByExpressions != null) {
            for (Expression expr : groupByExpressions) {
                columns.add(expr.toString());
            }
        }
        return columns;
    }




    public List<String> getTables() {
        return tables;
    }

    public Expression getWhereExpression() {
        return whereExpression;
    }

    public List<String> getProjectionColumns() {
        return projectionColumns;
    }

    public Map<String, List<Expression>> getSelectionConditions() {
        return selectionConditions;
    }

    public List<Expression> getJoinConditions() {
        return joinConditions;
    }

    public Set<String> getAliases(){
        return aliases;
    }

    public List<OrderByElement> getOrderByElements() {
        return orderByElements;
    }

    public boolean getQueryRequiresDistinct() {
        return queryRequiresDistinct;
    }

    public String getSumColumn() {
        return sumColumn;
    }

    // Getter for GROUP BY columns
    public List<String> getGroupByColumns() {
        return groupByColumns;
    }

    public boolean hasSumFunction() {
        return sumExpression != null;
    }

    // Define a method to get the SUM expression
    public Expression getSumExpression() {
        return sumExpression;
    }

    public boolean isSelectAll() {
        return isSelectAll;
    }


}

