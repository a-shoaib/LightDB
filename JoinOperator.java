package ed.inf.adbs.lightdb;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import java.util.ArrayList;
import java.util.List;

/**
 * JoinOperator joins two sets of tuples based on a join condition.
 */
public class JoinOperator extends Operator {
    private Operator leftChild;
    private Operator rightChild;
    private Expression joinCondition;
    private EvalVisitor evalVisitor;
    private Tuple currentLeftTuple;
    private boolean needNewLeftTuple = true;
    private Schema schema;

    /**
     * Constructs a JoinOperator with left and right child operators and a join condition.
     *
     * @param leftChild     the left child Operator
     * @param rightChild    the right child Operator
     * @param joinCondition the join condition
     * @param schema        the schema for the join operator
     */
    public JoinOperator(Operator leftChild, Operator rightChild, Expression joinCondition, Schema schema) {
        this.leftChild = leftChild;
        this.rightChild = rightChild;
        this.joinCondition = joinCondition;
        this.schema = schema;
        this.evalVisitor = new EvalVisitor(schema);
        System.out.println("Join Condition in JoinOperator: " +this.joinCondition);

    }

    /**
     * Gets the next tuple from the join operation.
     *
     * @return the next tuple or null if no more tuples
     */
    @Override
    public Tuple getNextTuple() {
        System.out.println("JOIN HAPPENING");

        int counterLeft = 0;
        int counterRight = 0;
        System.out.println("Entered get next tuple");
        System.out.println(this.schema.getColumnNames());

        while(true) {
            if (needNewLeftTuple) {
                currentLeftTuple = leftChild.getNextTuple();
                System.out.println("Current left tuple: " + (++counterLeft) + currentLeftTuple);
                if (currentLeftTuple == null) {
                    return null; // No more tuples in the leftChild
                }
                needNewLeftTuple = false;
                rightChild.reset(); // Reset right child for new left tuple iteration
            }

            Tuple rightTuple = rightChild.getNextTuple();
            System.out.println("Current right tuple: " + (++counterRight) + currentLeftTuple);
            if (rightTuple == null) {
                needNewLeftTuple = true;
                continue;
            }

            if(joinCondition == null) {
                System.out.println("Join Condition == NULL");
            }
            if (joinCondition == null || evalVisitor.evaluate(new Tuple(currentLeftTuple, rightTuple), joinCondition)) {
                System.out.println("join condddddd" + joinCondition);
                return new Tuple(currentLeftTuple, rightTuple);
            }
        }

    }

    /**
     * Resets the join operation to the start.
     */
    @Override
    public void reset() {
        leftChild.reset();
        rightChild.reset();
        needNewLeftTuple = true;
    }

    /**
     * Returns the schema of the join operator.
     *
     * @return the schema object
     */
    @Override
    public Schema getSchema() {
        return this.schema;
    }
}
