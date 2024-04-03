package ed.inf.adbs.lightdb;

import java.io.FileReader;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;

import org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;


/**
 * Lightweight in-memory database system
 *
 */
public class LightDB {

	public static void main(String[] args) {

		if (args.length != 3) {
			System.err.println("Usage: LightDB database_dir input_file output_file");
			return;
		}

		String databaseDir = args[0];
		String inputFile = args[1];
		String outputFile = args[2];

		try {
			runQuery(databaseDir, inputFile, outputFile);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Just for demonstration, replace this function call with your logic
//		parsingExample(inputFile);
	}

	/**
	 * Example method for getting started with JSQLParser. Reads SQL statement from
	 * a file and prints it to screen; then extracts SelectBody from the query and
	 * prints it to screen.
	 */

	public static void parsingExample(String filename) {
		try {
			Statement statement = CCJSqlParserUtil.parse(new FileReader(filename));
//            Statement statement = CCJSqlParserUtil.parse("SELECT * FROM Boats");
			if (statement != null) {
//				System.out.println("Read statement: " + statement);
//				Select select = (Select) statement;
//				System.out.println("Select body is " + select.getSelectBody());
			}
		} catch (Exception e) {
			System.err.println("Exception occurred during parsing");
			e.printStackTrace();
		}
	}

	public static void runQuery(String database_dir, String input_file, String outputFile) throws Exception {
		DatabaseCatalog catalog;
		catalog = DatabaseCatalog.getInstance();
		catalog.loadSchemas(database_dir);

		QueryPlannerVol2 planner = new QueryPlannerVol2(catalog);

		// The SQL query should include a join condition
		String queryFilePath = "C:/Users/shoai/OneDrive/Desktop/Edinburgh Spring 2024/Advanced DB Systems/LightDB/samples/input/query3.sql";

		// Use the QueryPlanner to create a query plan
		Operator operator = planner.createQueryPlan(queryFilePath);

//		QueryParser query = new QueryParser();
//		query.parseQuery(input_file);
//
//		QueryPlanner planner = new QueryPlanner(catalog);
//		Operator operator = planner.createQueryPlan(input_file);
//
//
//
//		String tableName = query.getTable();
//		Expression whereCondition = query.getWhereExpression();
//
//		Schema schema = catalog.getTableSchema(tableName);
//		String filePath = catalog.getTablePath(tableName);


		// Writing the tuple to the output file using the dump() method
		String outputFilePath = outputFile;
		try (PrintStream out = new PrintStream(new File(outputFilePath))) {
			operator.dump(out);
		}

		operator.reset();
	}








}


