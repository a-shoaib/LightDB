package ed.inf.adbs.lightdb;

import net.sf.jsqlparser.expression.Expression;
public class SelectOperator extends Operator {
    private Operator childOperator; // Scan Operator
    private Experssion whereCondition;

    @Override
    public Tuple getNextTuple() {

    }

    @Override
    public void reset(){

    }
}
