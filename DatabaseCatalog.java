package ed.inf.adbs.lightdb;

import net.sf.jsqlparser.schema.Database;

import javax.xml.crypto.Data;
import java.io.*;
import java.util.*;

public class DatabaseCatalog {

    // Maps to store file paths and schemas for each table
    private Map<String, String> tablePaths;
    private Map<String, Schema> tableSchemas;

    // This Map is to handle schemas with aliases
    private Map<String, Schema> aliasSchemas;

    // To implement the singleton pattern
    private static DatabaseCatalog instance = null;

    // private constructor
    private DatabaseCatalog(){
        tablePaths = new HashMap<>();
        tableSchemas = new HashMap<>();
        aliasSchemas = new HashMap<>();
    }

    /**
     * Returns the singleton instance of the DatabaseCatalog
     * @return The singleton instance
     */
    public static DatabaseCatalog getInstance(){
        if (instance == null){
            instance = new DatabaseCatalog();
//            System.out.println("New Catalog");
        }
        else{
//            System.out.println("Old Catalog");
        }
        return instance;
    }

    /**
     * To add new tables to the catalog
     * @param filePath  File path to table's data
     * @param schema    The schema of the table
     * @param tableName Name of the table
     */
    public void addTable(String tableName, String filePath, Schema schema){
//        System.out.println("Table name: " + tableName );
//        System.out.println("Table path: " + filePath );
//        System.out.println("Table Schema: " + schema );

        tablePaths.put(tableName, filePath);
        tableSchemas.put(tableName, schema);
    }

    /**
     * Returns file path for a specified table
     * @param tableName Name of the table to get the path of
     * @return file path of the table's data
     */
    public String getTablePath(String tableName){
        return tablePaths.get(tableName);
    }

    /**
     * Returns file path for a specified table
     * @param tableName Name of the table to get the path of
     * @return the schema of the table
     */
    public Schema getTableSchema(String tableName){
        return tableSchemas.get(tableName);
    }

    public void loadSchemas(String baseDirPath) throws IOException {

        // storing paths schema file and csv files
        String schemaFilePath = baseDirPath  + File.separator + "schema.txt";
        String dataDirPath = baseDirPath + File.separator + "data" + File.separator;

        try (BufferedReader br = new BufferedReader((new FileReader(schemaFilePath)))){
            String line;
            while ((line = br.readLine()) != null){
                String [] parts = line.split("\\s+");   // splits each line into an array

                String tableName = parts[0];
                String [] columnNames = Arrays.copyOfRange(parts,1, parts.length);

                Schema schema = new Schema(columnNames);


                // To construct the file path for the table's data
                String tableDataFilePath = dataDirPath + tableName + ".csv";

                // Adds the table schema and file path to the catalog
                addTable(tableName, tableDataFilePath, schema);
            }
        }
    }

    /**
     *
     * @param alias
     * @param actualTableName
     * This method adds to the catalog an alias of the actual table and assigns to it the same tablePath and column
     * names. That way, we can deal with aliases directly
     */
    public void addAlias(String alias, String actualTableName) {
        if (tablePaths.containsKey(actualTableName) && tableSchemas.containsKey(actualTableName)) {
            tablePaths.put(alias, tablePaths.get(actualTableName));
            tableSchemas.put(alias, tableSchemas.get(actualTableName));
        } else {
            throw new IllegalArgumentException("No such table to alias: " + actualTableName);
        }
    }

    /**
     *
     * @param alias
     * Removes the alias from table paths and from schema.This is vital to avoid overpopulating the catalog with
     * unnecessary information.
     */
    public void removeAlias(String alias) {
        if (tablePaths.containsKey(alias)) {
            tablePaths.remove(alias);
        }
        if (tableSchemas.containsKey(alias)) {
            tableSchemas.remove(alias);
        }
    }

    public void addAliasSchema(String aliasName, Schema schema) {
        aliasSchemas.put(aliasName, schema);
    }

    // Method to get a schema by table name or alias
    public Schema getSchema(String tableNameOrAlias) {
        // Check if it's an alias first
        if (aliasSchemas.containsKey(tableNameOrAlias)) {
            return aliasSchemas.get(tableNameOrAlias);
        }
        // Otherwise, return the normal table schema
        return tableSchemas.get(tableNameOrAlias);
    }





}

