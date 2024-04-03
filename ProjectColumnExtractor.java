package ed.inf.adbs.lightdb;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class utlizies  SelecItem to extract projection columns
 */
public class ProjectColumnExtractor extends SelectItemVisitorAdapter{
    private List<String> projectionColumns = new ArrayList<>();

    private int sumCount = 0;


    //@Override
    public void visit(SelectItem selectItem) {
        Expression expression = selectItem.getExpression();
        if (expression instanceof Column) {
            Column column = (Column) expression;
            projectionColumns.add(column.getColumnName());
        }
        if (expression instanceof Function) {
            Function func = (Function) expression;
            if (Objects.equals(func.getName(), "SUM")) {
                projectionColumns.add("SUM_"+(++sumCount));
            }
            //System.out.println(" Function Expression "+func.getName());
        }
        //System.out.println("Expression"+expression);
    }
    //@Override
    public void visit(AllColumns allColumns) {
        // This method handles SELECT *
        projectionColumns.add("*");
    }

    //@Override
    public void visit(AllTableColumns allTableColumns) {
        // This method handles table.*
        projectionColumns.add(allTableColumns.getTable().getFullyQualifiedName() + ".*");
    }

    public List<String> getProjectionColumns() {
        System.out.println("Projection Columns: " + projectionColumns);
        return projectionColumns;
    }


}
