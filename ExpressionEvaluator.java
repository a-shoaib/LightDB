package ed.inf.adbs.lightdb;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.schema.Column;

public class ExpressionEvaluator {
    private Schema schema;

    public ExpressionEvaluator(Schema schema) {
        this.schema = schema;
    }

    public int evaluate(Expression expression, Tuple tuple) {
        if (expression instanceof LongValue) {
            return (int) ((LongValue) expression).getValue();
        } else if (expression instanceof Column) {
            String columnName = ((Column) expression).getColumnName();
            return tuple.getFieldByName(columnName, schema);
        } else if (expression instanceof Multiplication) {
            Multiplication multiplication = (Multiplication) expression;
            int left = evaluate(multiplication.getLeftExpression(), tuple);
            int right = evaluate(multiplication.getRightExpression(), tuple);
            return left * right;
        } else if (expression instanceof Addition) {
            Addition addition = (Addition) expression;
            int left = evaluate(addition.getLeftExpression(), tuple);
            int right = evaluate(addition.getRightExpression(), tuple);
            return left + right;
        } else if (expression instanceof Subtraction) {
            Subtraction subtraction = (Subtraction) expression;
            int left = evaluate(subtraction.getLeftExpression(), tuple);
            int right = evaluate(subtraction.getRightExpression(), tuple);
            return left - right;
        } else if (expression instanceof Division) {
            Division division = (Division) expression;
            int left = evaluate(division.getLeftExpression(), tuple);
            int right = evaluate(division.getRightExpression(), tuple);
            return left / right; // watch out for division by zero
        } else {
            throw new UnsupportedOperationException("Expression type not supported: " + expression.getClass().getSimpleName());
        }
    }
}
