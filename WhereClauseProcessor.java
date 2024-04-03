package ed.inf.adbs.lightdb;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Processes WHERE clause conditions and separates them into selection and join conditions.
 */
public class WhereClauseProcessor extends ExpressionVisitorAdapter {
    private Map<String, List<Expression>> selectionConditions;
    private List<Expression> joinConditions;
    private List<String> tables;
    private Map<String, String> aliases; // Assume this is initialized elsewhere


    public WhereClauseProcessor(List<String> tables) {
        this.selectionConditions = new HashMap<>();
        this.joinConditions = new ArrayList<>();
        this.tables = tables;
    }

    // Entry point to start processing the WHERE clause.
    public void processWhereClause(Expression whereExpression) {
        whereExpression.accept(this);
    }

    @Override
    public void visit(AndExpression andExpression) {
        andExpression.getLeftExpression().accept(this);
        andExpression.getRightExpression().accept(this);
    }

    @Override
    public void visit(EqualsTo equalsTo) { processBinaryCondition(equalsTo); }

    @Override
    public void visit(GreaterThan greaterThan) {
        processBinaryCondition(greaterThan);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        processBinaryCondition(greaterThanEquals);
    }

    @Override
    public void visit(MinorThan minorThan) {
        processBinaryCondition(minorThan);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        processBinaryCondition(minorThanEquals);
    }


    @Override
    public void visit(Column column) {
        // Skip single columns; focus on binary expressions for conditions.
    }

    private void processBinaryCondition(BinaryExpression binaryExpression) {
        // Attempt to cast both sides of the binary expression to columns
        Expression leftExpression = binaryExpression.getLeftExpression();
        Expression rightExpression = binaryExpression.getRightExpression();

        Column leftColumn = null, rightColumn = null;
        if (leftExpression instanceof Column) {
            leftColumn = (Column) leftExpression;
            if (leftColumn.getTable() != null && leftColumn.getTable().getName() != null) {
                leftColumn = (Column) leftExpression;
            }
        }

        if (rightExpression instanceof Column) {
            rightColumn = (Column) rightExpression;
            if (rightColumn.getTable() != null && rightColumn.getTable().getName() != null) {
                rightColumn = (Column) rightExpression;
            }
        }

        // Determine if the binary expression is a selection or join condition
        if (leftColumn != null && rightColumn != null) {
            // Check if the columns belong to different tables
            if (!leftColumn.getTable().getName().equals(rightColumn.getTable().getName())) {
                joinConditions.add(binaryExpression);
            } else {
                // Same table, so it's a selection condition
                addSelectionCondition(leftColumn.getTable().getName(), binaryExpression);
            }
        } else if (leftColumn != null) {
            // Only one column is present, so it's a selection condition
            addSelectionCondition(leftColumn.getTable().getName(), binaryExpression);
        } else if (rightExpression instanceof LongValue || rightExpression instanceof DoubleValue) {
            // Right expression is a literal value, so we treat it as a selection condition on the left column's table
            if (leftColumn != null) {
                addSelectionCondition(leftColumn.getTable().getName(), binaryExpression);
            }
        }
    }



    private void addSelectionCondition(String tableName, Expression condition) {
        // If the table already has conditions, add this one to the list.
        selectionConditions.computeIfAbsent(tableName, k -> new ArrayList<>()).add(condition);
    }

    public Map<String, List<Expression>> getSelectionConditions() {
        return selectionConditions;
    }

    public List<Expression> getJoinConditions() {
        return joinConditions;
    }
}
