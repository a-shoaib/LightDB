package ed.inf.adbs.lightdb;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;

import java.awt.*;

public class SelectOperator extends Operator {
    private Operator childOperator; // Scan Operator
    private Expression whereCondition;  // Parsed SQL condition
    private Schema schema;  // Schema of the table
    private boolean alteredSchema = false;

    public SelectOperator(Operator childOperator, Expression whereCondition, Schema schema) {
        this.childOperator = childOperator;
        this.whereCondition = whereCondition;
        this.schema = schema;
    }

    @Override
    public Tuple getNextTuple() {
        Tuple tuple;
        System.out.println("ENTERED SELECT");
        while ((tuple = childOperator.getNextTuple()) != null) {
            System.out.println("Current Select tuple: "  + tuple);
            EvalVisitor evalVisitor = new EvalVisitor(schema);
            if (evalVisitor.evaluate(tuple, whereCondition)) {
                // if the statement is true, then the current tuple passed the where condition
                System.out.println("Current Select tuple PASSED: "  + tuple);
                return tuple;
            }
        }
        return null;    // no more tuples satisfy this condition
    }

    @Override
    public void reset(){
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
