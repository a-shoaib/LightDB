package ed.inf.adbs.lightdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Schema {
    private List<String> columnNames;
    private Map<String, String> tableAliases;

    private Map<String, String> columnTableMap;

    public boolean isSelfJoin = false;

    // Constructor
    public Schema(List<String> columnNames) {
        this.columnNames = columnNames;
        this.columnTableMap = new HashMap<>();
    }

    /**
     * Constructs an instance of schema given strings corresponding to column names
     * @param columnNames
     */
    public Schema(String... columnNames) {
        this.clearSchema();
        this.initColumns(columnNames);
    }

    public void clearSchema() {
        this.columnNames = new ArrayList<>();
        this.columnTableMap = new HashMap<>();
    }

    public void initColumns(String... columnNames) {
        for (String columnName : columnNames){
            this.columnNames.add(columnName);
        }
    }

    public void initColumns(List<String> columnNames) {
        for (String columnName : columnNames){
            this.columnNames.add(columnName);
        }
    }


    // Method to add a fully qualified column name
    public void addFullyQualifiedColumnName(String columnName, String tableName) {
        columnTableMap.put(columnName, tableName);
    }

    // Getter for the new map
    public String getTableNameForColumn(String columnName) {
        return columnTableMap.get(columnName);
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

    // Method to resolve column name with alias
    public String resolveColumnName(String columnName) {
        String[] parts = columnName.split("\\.");
        if (parts.length > 1 && tableAliases.containsKey(parts[0])) {
            // Replace the alias with the actual table name
            return tableAliases.get(parts[0]) + "." + parts[1];
        }
        return columnName; // If no alias, return the original column name
    }

    // Method to check if a column exists
    public boolean hasColumn(String columnName) {
        columnName = resolveColumnName(columnName); // Resolve aliases
        return columnNames.contains(columnName);
    }

    public void addColumnName(String columnName) {
        columnNames.add(columnName);
    }

}
