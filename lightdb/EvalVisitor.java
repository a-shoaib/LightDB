package ed.inf.adbs.lightdb;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.schema.Column;

import java.util.Stack;

public class EvalVisitor extends ExpressionDeParser {
    private Tuple currentTuple;
    private Schema schema;
    private Stack<Boolean> booleanEvaluationStack = new Stack<>();
    private Stack<Integer> intEvaluationStack = new Stack<>();


    public EvalVisitor(Schema schema) {
        this.schema = schema;
    }

    public boolean evaluate(Tuple tuple, Expression expression) {
        this.currentTuple = tuple;
        this.booleanEvaluationStack.clear();
        expression.accept(this);
        return booleanEvaluationStack.isEmpty() ? false : booleanEvaluationStack.pop();

    }

    @Override
    public void visit(LongValue longValue) {
        // when encountering a long value, push its value into the int stack
        intEvaluationStack.push((int) longValue.getValue());
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        // visit the left expression, pushing its value into the integer stack
        equalsTo.getLeftExpression().accept(this);
        // pop the left expression
        int leftvalue = intEvaluationStack.pop();


        // visit the right expression, pushing its value into the stack
        equalsTo.getRightExpression().accept(this);
        // pop the right expression
        int rightValue = intEvaluationStack.pop();

        // push comparison result between left and right expressions into the boolean stack
        booleanEvaluationStack.push(leftvalue == rightValue);
    }

//    public void visitBinaryExpression(BinaryExpression binaryExpression) {
//
//        if (binaryExpression instanceof EqualsTo) {
//            visit((EqualsTo) binaryExpression);
//        } else if (binaryExpression instanceof GreaterThan) {
//            visit((GreaterThan) binaryExpression);
//
//        } else if (binaryExpression instanceof MinorThan) {
//            visit((MinorThan) binaryExpression);
//
//        } else if (binaryExpression instanceof GreaterThanEquals) {
//            visit((GreaterThanEquals) binaryExpression);
//
//        } else if (binaryExpression instanceof MinorThanEquals) {
//            visit((MinorThanEquals) binaryExpression);
//
//        }
//
//    }

    @Override
    public void visit(GreaterThan greaterThan) {

        greaterThan.getLeftExpression().accept(this);
        greaterThan.getRightExpression().accept(this);

        // pop the right expression
        int rightValue = intEvaluationStack.pop();
        // pop the left expression
        int leftValue = intEvaluationStack.pop();


        // push comparison result between left and right expressions into the boolean stack
        booleanEvaluationStack.push(leftValue > rightValue);
    }

    @Override
    public void visit(MinorThan minorThan) {

        minorThan.getLeftExpression().accept(this);
        minorThan.getRightExpression().accept(this);

        // pop the right expression
        int rightValue = intEvaluationStack.pop();
        // pop the left expression
        int leftValue = intEvaluationStack.pop();

        // push comparison result between left and right expressions into the boolean stack
        booleanEvaluationStack.push(leftValue < rightValue);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {

        greaterThanEquals.getLeftExpression().accept(this);
        greaterThanEquals.getRightExpression().accept(this);

        /// pop the right expression
        int rightValue = intEvaluationStack.pop();
        // pop the left expression
        int leftValue = intEvaluationStack.pop();

        // push comparison result between left and right expressions into the boolean stack
        booleanEvaluationStack.push(leftValue >= rightValue);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {

        minorThanEquals.getLeftExpression().accept(this);
        minorThanEquals.getRightExpression().accept(this);

        // pop the right expression
        int rightValue = intEvaluationStack.pop();
        // pop the left expression
        int leftValue = intEvaluationStack.pop();

        // push comparison result between left and right expressions into the boolean stack
        booleanEvaluationStack.push(leftValue <= rightValue);
    }





}
