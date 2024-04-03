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
//
//public class QueryParserVol2 {
//
//    private List<String> tables = new ArrayList<>();
//    private Expression whereExpression;
//    private List<String> projectionColumns = new ArrayList<>();
//    private Map<String, Expression> selectionConditions = new HashMap<>();
//    private List<Expression> joinConditions = new ArrayList<>();
//    private PlainSelect plainSelect;
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
//            whereExpression.accept(new ExpressionVisitorAdapter() {
//                @Override
//                public void visit(Column column) {
//                    // If the column's table name is in our list of tables, it's a selection condition
//                    if (tables.contains(column.getTable().getName())) {
//                        selectionConditions.put(column.getTable().getName(), whereExpression);
//                    } else {
//                        // Otherwise, it's part of a join condition
//                        joinConditions.add(whereExpression);
//                    }
//                }
//
//                @Override
//                public void visit(EqualsTo equalsTo) {
//                    Column leftColumn = (Column) equalsTo.getLeftExpression();
//                    Column rightColumn = (Column) equalsTo.getRightExpression();
//
//                    if (tables.contains(leftColumn.getTable().getName()) && tables.contains(rightColumn.getTable().getName())) {
//                        // This is a join condition
//                        joinConditions.add(equalsTo);
//                    } else {
//                        // This is a selection condition
//                        if (tables.contains(leftColumn.getTable().getName())) {
//                            selectionConditions.put(leftColumn.getTable().getName(), equalsTo);
//                        } else if (tables.contains(rightColumn.getTable().getName())) {
//                            selectionConditions.put(rightColumn.getTable().getName(), equalsTo);
//                        }
//                    }
//                }
//            });
//        }
//    }
//
//    public List<String> getTables() {
//        return tables;
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
