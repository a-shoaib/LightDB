package ed.inf.adbs.lightdb;

import java.util.ArrayList;
import java.util.List;

public class Schema {
    private List<String> columnNames;

    /**
     * Constructs an instance of schema given a list of column names
     * @param columnNames   Columns in the schema
     */
    public Schema(List<String> columnNames) {
        this.columnNames = new ArrayList<>(columnNames);
    }

    /**
     * Constructs an instance of schema given strings corresponding to column names
     * @param columnNames
     */
    public Schema(String... columnNames) {
        this.columnNames = new ArrayList<>();
        for (String columnName : columnNames){
            this.columnNames.add(columnName);
        }
    }


    public List<String> getColumnNames() {
        return columnNames;
    }

    /**
     *
     * @param columnName
     * @return index of a given column
     */
    public int getColumnIndex(String columnName) {
        return columnNames.indexOf(columnName);
    }

    public void addColumnName(String columnName) {
        columnNames.add(columnName);
    }

}
